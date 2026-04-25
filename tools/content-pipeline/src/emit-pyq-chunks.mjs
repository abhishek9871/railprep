#!/usr/bin/env node
// Emit chunk files for applying via Supabase MCP (single-chunk arg-size friendly).
// Usage: node src/emit-pyq-chunks.mjs <chunkSize>

import { readFile, writeFile, mkdir } from "node:fs/promises";
import { resolve, dirname } from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = dirname(fileURLToPath(import.meta.url));
const catalog = JSON.parse(
  await readFile(resolve(__dirname, "..", "pyq-catalog.json"), "utf8"),
);

const CHUNK = Number(process.argv[2] ?? 20);
const q = (s) => s == null ? "null" : `'${String(s).replace(/'/g, "''")}'`;

const outDir = resolve(__dirname, "..", "sql", "pyq-chunks");
await mkdir(outDir, { recursive: true });

for (let i = 0; i < catalog.papers.length; i += CHUNK) {
  const batch = catalog.papers.slice(i, i + CHUNK);
  const sql = batch.map((p) => {
    const totalQ = p.exam_cbt === 2 ? 120 : 100;
    const minutes = 90;
    const examTarget = p.exam_cbt === 2 ? "NTPC_CBT2" : "NTPC_CBT1";
    return `insert into public.tests (slug, title_en, title_hi, kind, exam_target, total_questions, total_minutes, negative_marking_fraction, external_url, source_language, source_attribution, status)
values (${q(p.slug)}, ${q(p.title_en)}, ${q(p.title_hi)}, 'PYQ_LINK', ${q(examTarget)}, ${totalQ}, ${minutes}, 0.3333, ${q(p.url)}, ${q(p.language)}, 'adda247.com', 'active')
on conflict (slug) do update set title_en = excluded.title_en, title_hi = excluded.title_hi, external_url = excluded.external_url, source_language = excluded.source_language, updated_at = now();`;
  }).join("\n");

  const idx = String(Math.floor(i / CHUNK) + 1).padStart(2, "0");
  await writeFile(resolve(outDir, `chunk-${idx}.sql`), sql, "utf8");
  console.log(`chunk-${idx}.sql: ${batch.length} rows, ${sql.length} bytes`);
}
