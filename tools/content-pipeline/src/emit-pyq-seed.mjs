#!/usr/bin/env node
// Read pyq-catalog.json, emit an idempotent upsert SQL block for the PYQ_LINK tests.
// Usage: node src/emit-pyq-seed.mjs > sql/0006_pyq_library_seed.sql

import { readFile } from "node:fs/promises";
import { resolve, dirname } from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = dirname(fileURLToPath(import.meta.url));
const catalog = JSON.parse(
  await readFile(resolve(__dirname, "..", "pyq-catalog.json"), "utf8"),
);

const q = (s) => s == null ? "null" : `'${String(s).replace(/'/g, "''")}'`;

const header = `-- AUTO-GENERATED via tools/content-pipeline/src/emit-pyq-seed.mjs — do not hand-edit.
-- Re-run the emitter after changing pyq-catalog.json, then apply via Supabase MCP.
-- Each row is idempotent on tests.slug.

begin;
`;

const rows = catalog.papers.map((p) => {
  const totalQ = p.exam_cbt === 2 ? 120 : 100;
  const minutes = 90;
  const examTarget = p.exam_cbt === 2 ? "NTPC_CBT2" : "NTPC_CBT1";
  return `insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  ${q(p.slug)}, ${q(p.title_en)}, ${q(p.title_hi)}, 'PYQ_LINK', ${q(examTarget)},
  ${totalQ}, ${minutes}, 0.3333,
  ${q(p.url)}, ${q(p.language)}, ${q("adda247.com")},
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();`;
}).join("\n\n");

console.log(header);
console.log(rows);
console.log("\ncommit;");
console.log(`\n-- ${catalog.papers.length} PYQ_LINK rows upserted.`);
