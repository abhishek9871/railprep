#!/usr/bin/env node
// Compact one-line-per-statement seed emitter for sectionals.
// Goal: fit a full 25Q sectional in <50KB so it's MCP-friendly in a single call.
// Usage: node src/emit-compact-seed.mjs <candidates/sectional-X.json>

import { readFile, writeFile, mkdir } from "node:fs/promises";
import { resolve, dirname, basename } from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = dirname(fileURLToPath(import.meta.url));
const path = process.argv[2];
if (!path) { console.error("usage: emit-compact-seed.mjs <candidates.json>"); process.exit(2); }

const bundle = JSON.parse(await readFile(resolve(path), "utf8"));
const q = (s) => s == null ? "null" : `'${String(s).replace(/'/g, "''")}'`;
const arr = (a) => `array[${(a ?? []).map((t) => q(t)).join(",")}]::text[]`;

const totalQ = bundle.questions.length;
const totalMinutes = bundle.total_minutes ?? 25;
const slug = bundle.slug;

let sql = `do $$ declare v_t uuid; v_s uuid; v_q uuid; begin\n`;
sql += `insert into public.tests (slug, title_en, title_hi, kind, exam_target, total_questions, total_minutes, negative_marking_fraction, status) values (${q(slug)}, ${q(bundle.title_en)}, ${q(bundle.title_hi)}, 'SECTIONAL', 'NTPC_CBT1', ${totalQ}, ${totalMinutes}, 0.3333, 'active') on conflict (slug) do update set title_en = excluded.title_en, title_hi = excluded.title_hi, total_questions = excluded.total_questions, updated_at = now() returning id into v_t;\n`;
sql += `delete from public.test_sections where test_id = v_t;\n`;
sql += `insert into public.test_sections (test_id, title_en, title_hi, question_count, display_order, subject_hint) values (v_t, ${q(bundle.section.title_en)}, ${q(bundle.section.title_hi)}, ${totalQ}, 0, ${q(bundle.section.subject_hint)}) returning id into v_s;\n`;

bundle.questions.forEach((qst, i) => {
  sql += `insert into public.questions (section_id, display_order, stem_en, stem_hi, explanation_method_en, explanation_concept_en, explanation_method_hi, explanation_concept_hi, difficulty, tags, source, license, status) values (v_s, ${i}, ${q(qst.stem_en)}, ${q(qst.stem_hi)}, ${q(qst.explanation_method_en)}, ${q(qst.explanation_concept_en)}, ${q(qst.explanation_method_hi)}, ${q(qst.explanation_concept_hi)}, ${q(qst.difficulty)}, ${arr(qst.tags)}, ${q(qst.source)}, ${q(qst.license)}, 'active') returning id into v_q;\n`;
  for (const opt of qst.options) {
    sql += `insert into public.options (question_id, label, text_en, text_hi, is_correct, trap_reason_en, trap_reason_hi) values (v_q, ${q(opt.label)}, ${q(opt.text_en)}, ${q(opt.text_hi)}, ${opt.is_correct ? "true" : "false"}, ${q(opt.trap_reason_en)}, ${q(opt.trap_reason_hi)});\n`;
  }
});

sql += `end $$;\n`;

const outDir = resolve(__dirname, "..", "sql", "compact");
await mkdir(outDir, { recursive: true });
const outPath = resolve(outDir, `${basename(path).replace(".json", "")}.sql`);
await writeFile(outPath, sql, "utf8");
console.log(`${basename(outPath)}: ${sql.length} bytes`);
