#!/usr/bin/env node
// Compact seed emitter for CBT-1 full mocks with multiple sections.
// Usage: node src/emit-full-mock-seed.mjs <candidates/ntpc-cbt1-full-mock-XX.json>

import { readFile, writeFile, mkdir } from "node:fs/promises";
import { resolve, dirname, basename } from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = dirname(fileURLToPath(import.meta.url));
const path = process.argv[2];
if (!path) {
  console.error("usage: emit-full-mock-seed.mjs <candidates.json>");
  process.exit(2);
}

const bundle = JSON.parse(await readFile(resolve(path), "utf8"));
const q = (s) => s == null ? "null" : `'${String(s).replace(/'/g, "''")}'`;
const arr = (a) => `array[${(a ?? []).map((t) => q(t)).join(",")}]::text[]`;

const sections = bundle.sections ?? [];
if (sections.length === 0) throw new Error(`${bundle.slug}: missing sections[]`);
const totalQ = sections.reduce((sum, s) => sum + (s.questions?.length ?? 0), 0);
if (totalQ !== bundle.questions.length) throw new Error(`${bundle.slug}: flat question count mismatch`);

let sql = `do $$ declare v_t uuid; v_s uuid; v_q uuid; begin\n`;
sql += `insert into public.tests (slug, title_en, title_hi, kind, exam_target, total_questions, total_minutes, negative_marking_fraction, status) values (${q(bundle.slug)}, ${q(bundle.title_en)}, ${q(bundle.title_hi)}, 'CBT1_FULL', 'NTPC_CBT1', ${totalQ}, ${bundle.total_minutes ?? 90}, 0.3333, 'active') on conflict (slug) do update set title_en = excluded.title_en, title_hi = excluded.title_hi, total_questions = excluded.total_questions, total_minutes = excluded.total_minutes, updated_at = now() returning id into v_t;\n`;
sql += `delete from public.test_sections where test_id = v_t;\n`;

sections.forEach((section, sectionIndex) => {
  sql += `insert into public.test_sections (test_id, title_en, title_hi, question_count, display_order, subject_hint) values (v_t, ${q(section.title_en)}, ${q(section.title_hi)}, ${section.questions.length}, ${sectionIndex + 1}, ${q(section.subject_hint)}) returning id into v_s;\n`;
  section.questions.forEach((qst, i) => {
    sql += `insert into public.questions (section_id, display_order, stem_en, stem_hi, explanation_method_en, explanation_concept_en, explanation_method_hi, explanation_concept_hi, difficulty, tags, source, license, status) values (v_s, ${i}, ${q(qst.stem_en)}, ${q(qst.stem_hi)}, ${q(qst.explanation_method_en)}, ${q(qst.explanation_concept_en)}, ${q(qst.explanation_method_hi)}, ${q(qst.explanation_concept_hi)}, ${q(qst.difficulty)}, ${arr(qst.tags)}, ${q(qst.source)}, ${q(qst.license)}, 'active') returning id into v_q;\n`;
    for (const opt of qst.options) {
      sql += `insert into public.options (question_id, label, text_en, text_hi, is_correct, trap_reason_en, trap_reason_hi) values (v_q, ${q(opt.label)}, ${q(opt.text_en)}, ${q(opt.text_hi)}, ${opt.is_correct ? "true" : "false"}, ${q(opt.trap_reason_en)}, ${q(opt.trap_reason_hi)});\n`;
    }
  });
});

sql += `end $$;\n`;

const outDir = resolve(__dirname, "..", "sql", "compact");
await mkdir(outDir, { recursive: true });
const outPath = resolve(outDir, `${basename(path).replace(".json", "")}.sql`);
await writeFile(outPath, sql, "utf8");
console.log(`${basename(outPath)}: ${sql.length} bytes`);
