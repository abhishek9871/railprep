#!/usr/bin/env node
// audit-topics.mjs — verify every catalog topic's title actually describes its PDF.
//
// Strategy (three independent signals, take the best):
//   (1) The chapter title usually appears as a running header on pages 2+. Lines that
//       show up 2+ times and look like titles (not page numbers, not the book name) are
//       strong candidates for the real chapter title.
//   (2) The opening paragraph of page 1 often contains identifying keywords — a
//       chapter called "Agriculture" usually says "farmer", "crops", "soil" in the
//       first 2000 chars.
//   (3) Token overlap: after dropping noise words (NCERT/Class/Chapter/numbers/subject
//       names), how many title words appear in the PDF's first 2500 chars and in the
//       running header? Ratio gives the verdict.
//
// Verdicts:
//   MATCH     — running header or content has >=50% of DB-title tokens
//   PARTIAL   — 20-50% overlap; worth a human look
//   MISMATCH  — <20% overlap; DB title does not describe the file
//   GENERIC   — DB title has no signal tokens (e.g. "Class N Mathematics — Chapter N")
//   ERROR     — download or parse failed
//
// Usage:
//   1. Dump active PDF topics to audit/topics.json (shape: [{id,title_en,external_pdf_url,subject,chapter}])
//   2. node src/audit-topics.mjs
//   3. Read audit/report.md

import { readFile, writeFile, mkdir, stat } from 'node:fs/promises';
import { dirname, resolve, basename } from 'node:path';
import { fileURLToPath } from 'node:url';
import { request } from 'undici';
import { PDFParse } from 'pdf-parse';

const __dirname = dirname(fileURLToPath(import.meta.url));
const ROOT = resolve(__dirname, '..');
const AUDIT = resolve(ROOT, 'audit');
const PDFS = resolve(AUDIT, 'pdfs');
await mkdir(PDFS, { recursive: true });

const UA =
  'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) ' +
  'Chrome/147.0.0.0 Safari/537.36 RailPrep-Audit/1.0';

async function fileExists(p) {
  try { await stat(p); return true; } catch { return false; }
}

async function downloadIfMissing(url) {
  const file = resolve(PDFS, basename(url));
  if (await fileExists(file)) return file;
  let lastErr;
  for (let i = 1; i <= 3; i++) {
    try {
      const r = await request(url, {
        headers: {
          'User-Agent': UA,
          'Accept': 'application/pdf,*/*;q=0.8',
          'Accept-Language': 'en-IN,en;q=0.9',
          'Referer': 'https://ncert.nic.in/textbook.php',
        },
        maxRedirections: 5,
      });
      if (r.statusCode >= 400) throw new Error(`http ${r.statusCode}`);
      const buf = Buffer.from(await r.body.arrayBuffer());
      if (buf.length < 50_000 || buf.slice(0, 4).toString('latin1') !== '%PDF') {
        throw new Error(`not a pdf (size=${buf.length}, magic=${buf.slice(0, 4).toString('latin1')})`);
      }
      await writeFile(file, buf);
      return file;
    } catch (e) {
      lastErr = e;
      await new Promise((r) => setTimeout(r, 600 * i));
    }
  }
  throw lastErr;
}

async function extractText(file) {
  const buf = await readFile(file);
  const parser = new PDFParse({ data: buf });
  const r = await parser.getText();
  return r.text;
}

// Noise tokens — carry no identity. Lowercase.
const STOP = new Set([
  'ncert', 'class', 'chapter', 'the', 'and', 'of', 'in', 'a', 'to', 'for', 'an',
  'on', 'is', 'as', 'by', 'with', 'from', 'that', 'this', 'it', 'be', 'are', 'or',
  'mathematics', 'english', 'geography', 'history', 'polity', 'economics', 'science',
  'biology', 'physics', 'chemistry', 'world', 'indian', 'india',
  'spl', 'iii', 'ii', 'iv', 'book', 'part', 'edition', 'reprint',
  'kaveri', 'beehive', 'moments', 'ganita', 'manjari', 'prakash',
  'one', 'two', 'three', 'four', 'five', 'six', 'seven', 'eight', 'nine', 'ten',
]);

function tokens(s) {
  return s
    .toLowerCase()
    .replace(/[^\p{L}\p{N}\s]/gu, ' ')
    .split(/\s+/)
    .filter((w) => w.length >= 3 && !STOP.has(w) && !/^\d+$/.test(w));
}

// Identify the running chapter-title header. Strategy: count short lines (4-70 chars)
// that appear 2+ times and aren't obvious page furniture. The winner is the most common.
function findRunningHeader(text) {
  const lines = text.split(/\n/).map((l) => l.trim());
  const counts = new Map();
  for (const l of lines) {
    if (l.length < 4 || l.length > 70) continue;
    if (/^\d+(\s*of\s*\d+)?$/.test(l)) continue;
    if (/^(Reprint\s*20|NCERT|Kaveri|Ganita\s*(Prakash|Manjari)|Beehive|Moments|Textbook|Part\s+\w+|Unit\s+\w+|Chapter\s+\w+)\b/i.test(l)) continue;
    if (/^(I+|IV|V|VI|VII|VIII|IX)\.?$/.test(l)) continue; // roman numeral markers
    counts.set(l, (counts.get(l) || 0) + 1);
  }
  const ranked = [...counts.entries()].filter(([, c]) => c >= 2).sort((a, b) => b[1] - a[1]);
  // Pick the most-common short line whose token set has at least one meaningful word.
  for (const [line] of ranked) {
    if (tokens(line).length > 0) return line;
  }
  return null;
}

function classify(dbTitle, text) {
  const header = findRunningHeader(text) || '';
  const opening = text.slice(0, 2500);
  const dbTok = new Set(tokens(dbTitle));
  if (dbTok.size === 0) {
    return { verdict: 'GENERIC', overlap: 0, total: 0, header, hitTokens: [] };
  }
  // Combine header + opening for matching so short chapter names still match when
  // the running header is absent.
  const pdfTok = new Set([...tokens(header), ...tokens(opening)]);
  const hits = [...dbTok].filter((w) => pdfTok.has(w));
  const ratio = hits.length / dbTok.size;
  let verdict;
  if (ratio >= 0.5) verdict = 'MATCH';
  else if (ratio >= 0.2) verdict = 'PARTIAL';
  else verdict = 'MISMATCH';
  return { verdict, overlap: hits.length, total: dbTok.size, header, hitTokens: hits };
}

async function main() {
  const topicsPath = resolve(AUDIT, 'topics.json');
  if (!(await fileExists(topicsPath))) {
    console.error(`Missing ${topicsPath}. Dump active PDF topics from Supabase first.`);
    process.exit(2);
  }
  const topics = JSON.parse(await readFile(topicsPath, 'utf-8'));
  const rows = [];
  let i = 0;
  for (const t of topics) {
    i++;
    const tag = `[${i}/${topics.length}]`;
    try {
      const file = await downloadIfMissing(t.external_pdf_url);
      const text = await extractText(file);
      const c = classify(t.title_en, text);
      rows.push({
        subject: t.subject,
        chapter: t.chapter,
        url: t.external_pdf_url,
        title: t.title_en,
        verdict: c.verdict,
        overlap: c.overlap,
        total: c.total,
        pdf_header: c.header,
        hits: c.hitTokens,
      });
      console.log(`${tag} ${c.verdict.padEnd(8)} ${basename(t.external_pdf_url).padEnd(16)} ${t.title_en.slice(0, 60)}`);
    } catch (e) {
      rows.push({
        subject: t.subject,
        chapter: t.chapter,
        url: t.external_pdf_url,
        title: t.title_en,
        verdict: 'ERROR',
        error: e.message,
      });
      console.log(`${tag} ERROR    ${basename(t.external_pdf_url).padEnd(16)} ${e.message}`);
    }
  }

  await writeFile(resolve(AUDIT, 'report.json'), JSON.stringify(rows, null, 2));

  const by = (v) => rows.filter((r) => r.verdict === v);
  let md = `# Content audit — ${new Date().toISOString()}\n\n`;
  md += `Total active PDF topics: **${rows.length}**\n\n`;
  md += `| Verdict | Count |\n|---|---|\n`;
  for (const v of ['MATCH', 'PARTIAL', 'MISMATCH', 'GENERIC', 'ERROR']) {
    md += `| ${v} | ${by(v).length} |\n`;
  }

  const show = (title, list) => {
    if (!list.length) return;
    md += `\n## ${title} (${list.length})\n\n`;
    md += `| File | Subject → Chapter | DB title | PDF header line | Overlap | Hits |\n`;
    md += `|---|---|---|---|---|---|\n`;
    for (const r of list) {
      const hits = (r.hits || []).join(', ');
      md += `| \`${basename(r.url)}\` | ${r.subject} → ${r.chapter} | ${r.title} | ${r.pdf_header || ''} | ${r.overlap}/${r.total} | ${hits} |\n`;
    }
  };

  show('MISMATCH — DB title does not describe the PDF content', by('MISMATCH'));
  show('PARTIAL — weak overlap, worth a manual look', by('PARTIAL'));
  show('GENERIC — title has no signal tokens (skipped by matcher)', by('GENERIC'));
  show('ERROR', by('ERROR'));

  await writeFile(resolve(AUDIT, 'report.md'), md);

  console.log(`\nWritten audit/report.md and audit/report.json`);
  console.log(`Summary: ${by('MATCH').length} match, ${by('PARTIAL').length} partial, ${by('MISMATCH').length} mismatch, ${by('GENERIC').length} generic, ${by('ERROR').length} errors`);
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
