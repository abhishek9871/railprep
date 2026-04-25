#!/usr/bin/env node
// NCERT PDF discovery — Phase 2.5 rewrite.
//
// The NCERT landing page (textbook.php?{code}1=0-N) does NOT contain chapter PDF URLs inline —
// they are constructed at click-time by JavaScript. So the earlier crawl-and-regex approach
// captured only the `ps`/`an` prelim/answer-key files (which are small PDFs of 2-15 pages).
//
// This rewrite pattern-enumerates `{code}01..{code}NN` with HEAD+range checks. Each candidate
// must:
//   - respond 200
//   - Content-Type match pdf
//   - first 4 bytes = %PDF
//   - be >= 250 KB (full chapters on NCERT are always at least this size)
// Post-2023 rationalisation removed chapters — 404s just get skipped.

import { readFile, writeFile, mkdir } from 'node:fs/promises';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import { request } from 'undici';

const __dirname = dirname(fileURLToPath(import.meta.url));
const ROOT = resolve(__dirname, '..');
const UA =
  'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) ' +
  'Chrome/125.0.0.0 Safari/537.36 RailPrep-ContentPipeline/2.5';
const MIN_CHAPTER_BYTES = 250_000;
const MAX_CHAPTER_INDEX = 20; // NCERT textbooks never exceed ~20 chapters
const PDF_BASE = 'https://ncert.nic.in/textbook/pdf';

async function verify(url) {
  try {
    const r = await request(url, {
      method: 'GET',
      headers: { 'User-Agent': UA, Range: 'bytes=0-4095' },
      maxRedirections: 5,
    });
    if (r.statusCode < 200 || r.statusCode >= 400) return { ok: false, reason: `http ${r.statusCode}` };
    const ct = (r.headers['content-type'] || '').toString();
    // Range request: content-range tells us the full size; fall back to content-length.
    const rangeHeader = (r.headers['content-range'] || '').toString();
    const fullSize = rangeHeader ? Number(rangeHeader.split('/').pop()) : Number(r.headers['content-length'] || 0);
    const buf = Buffer.from(await r.body.arrayBuffer());
    const magic = buf.slice(0, 4).toString('latin1');
    if (magic !== '%PDF') return { ok: false, reason: `magic=${magic}` };
    if (!/pdf/i.test(ct)) return { ok: false, reason: `ct=${ct}` };
    if (fullSize && fullSize < MIN_CHAPTER_BYTES) return { ok: false, reason: `size=${fullSize}` };
    return { ok: true, size: fullSize };
  } catch (e) {
    return { ok: false, reason: e.code || e.message };
  }
}

function partFromLanding(book) {
  // Landings look like .../textbook.php?{code}{part}=0-N. Extract the single digit after code.
  const m = new RegExp(`\\?${book.code}(\\d)=`).exec(book.landing);
  return m ? m[1] : '1';
}

async function enumerateBook(book) {
  const part = partFromLanding(book);
  const out = [];
  for (let i = 1; i <= MAX_CHAPTER_INDEX; i++) {
    const nn = String(i).padStart(2, '0');
    const fname = `${book.code}${part}${nn}.pdf`;
    const url = `${PDF_BASE}/${fname}`;
    const v = await verify(url);
    if (v.ok) {
      out.push({
        class: book.class,
        subject: book.subject,
        title: `${book.title} — Chapter ${i}`,
        fname,
        url,
        size: v.size,
      });
    } else if (/^http 4\d\d/.test(v.reason) || v.reason === 'http 404') {
      // Continue — a missing chapter in the middle is legitimate (rationalised content).
    } else if (v.reason.startsWith('http 5')) {
      // Server hiccup — stop this book to avoid hammering.
      console.warn(`  ${fname}: ${v.reason} (bailing out of book)`);
      break;
    }
  }
  return out;
}

function toSql(rows) {
  const esc = (s) => `'${String(s).replaceAll("'", "''")}'`;
  const lines = [];
  for (const r of rows) {
    lines.push(
      `-- ${r.title} (${r.size} bytes)`,
      `insert into public.topics (title_en, content_type, external_pdf_url, source, license, display_order, status, last_verified_at)`,
      `  select ${esc(r.title)}, 'PDF_URL', ${esc(r.url)}, 'NCERT', 'NCERT_LINKED', 99, 'active', now()`,
      `  where false;  -- NOOP until chapter_id is curated`,
      '',
    );
  }
  return lines.join('\n');
}

async function main() {
  const { books } = JSON.parse(await readFile(resolve(ROOT, 'ncert-books.json'), 'utf-8'));
  const found = [];
  for (const b of books) {
    try {
      const chapters = await enumerateBook(b);
      console.log(`${b.code} (Class ${b.class} ${b.subject}): ${chapters.length} chapters verified`);
      found.push(...chapters);
    } catch (e) {
      console.error(`error for ${b.code}:`, e.message);
    }
  }
  const outDir = resolve(ROOT, 'sql');
  await mkdir(outDir, { recursive: true });
  await writeFile(resolve(outDir, 'topics_ncert.sql'), toSql(found));
  await writeFile(
    resolve(outDir, 'topics_ncert.json'),
    JSON.stringify(found, null, 2),
  );
  console.log(`wrote sql/topics_ncert.sql and .json (${found.length} verified PDFs)`);
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
