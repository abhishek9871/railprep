#!/usr/bin/env node
// Read a sectional candidate JSON and emit upsert SQL for a single SECTIONAL test
// + its single section + N questions + 4N options. All idempotent on tests.slug.
//
// Usage: node src/emit-sectional-seed.mjs <candidates/sectional-X.json>

import { readFile, writeFile, mkdir } from "node:fs/promises";
import { resolve, dirname, basename } from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = dirname(fileURLToPath(import.meta.url));
const path = process.argv[2];
if (!path) { console.error("usage: emit-sectional-seed.mjs <candidates.json>"); process.exit(2); }

const bundle = JSON.parse(await readFile(resolve(path), "utf8"));
const q = (s) => s == null ? "null" : `'${String(s).replace(/'/g, "''")}'`;
const arr = (a) => `array[${(a ?? []).map((t) => q(t)).join(",")}]::text[]`;

const totalQuestions = bundle.questions.length;
const totalMinutes = bundle.total_minutes ?? 25;

let sql = `-- SECTIONAL seed for ${bundle.slug}\nbegin;\n\n`;

// Test row
sql += `with up_test as (\n`;
sql += `  insert into public.tests (slug, title_en, title_hi, kind, exam_target, total_questions, total_minutes, negative_marking_fraction, status)\n`;
sql += `  values (${q(bundle.slug)}, ${q(bundle.title_en)}, ${q(bundle.title_hi)}, 'SECTIONAL', 'NTPC_CBT1', ${totalQuestions}, ${totalMinutes}, 0.3333, 'active')\n`;
sql += `  on conflict (slug) do update set title_en = excluded.title_en, title_hi = excluded.title_hi, total_questions = excluded.total_questions, total_minutes = excluded.total_minutes, updated_at = now()\n`;
sql += `  returning id\n`;
sql += `)\n`;
sql += `select id from up_test \\gset test_\n`;

// Simpler approach: use a do block that captures ids in declarations.
sql = `-- SECTIONAL seed for ${bundle.slug}\nbegin;\n\n`;
sql += `do $$\n`;
sql += `declare\n`;
sql += `  v_test_id    uuid;\n`;
sql += `  v_section_id uuid;\n`;
sql += `  v_q_id       uuid;\n`;
sql += `begin\n`;
sql += `  -- 1) Test\n`;
sql += `  insert into public.tests (slug, title_en, title_hi, kind, exam_target, total_questions, total_minutes, negative_marking_fraction, status)\n`;
sql += `  values (${q(bundle.slug)}, ${q(bundle.title_en)}, ${q(bundle.title_hi)}, 'SECTIONAL', 'NTPC_CBT1', ${totalQuestions}, ${totalMinutes}, 0.3333, 'active')\n`;
sql += `  on conflict (slug) do update set title_en = excluded.title_en, title_hi = excluded.title_hi, total_questions = excluded.total_questions, total_minutes = excluded.total_minutes, updated_at = now()\n`;
sql += `  returning id into v_test_id;\n\n`;

// Wipe sections + questions for this test (idempotent reseed). Cascade handles options + questions.
sql += `  -- 2) Wipe-and-reseed sections (cascade clears questions + options).\n`;
sql += `  delete from public.test_sections where test_id = v_test_id;\n\n`;

// Section
sql += `  insert into public.test_sections (test_id, title_en, title_hi, question_count, display_order, subject_hint)\n`;
sql += `  values (v_test_id, ${q(bundle.section.title_en)}, ${q(bundle.section.title_hi)}, ${totalQuestions}, 0, ${q(bundle.section.subject_hint)})\n`;
sql += `  returning id into v_section_id;\n\n`;

// Questions + options
bundle.questions.forEach((qst, i) => {
  sql += `  -- Q${i + 1}: ${qst.id}\n`;
  sql += `  insert into public.questions (\n`;
  sql += `    section_id, display_order, stem_en, stem_hi,\n`;
  sql += `    explanation_method_en, explanation_concept_en, explanation_method_hi, explanation_concept_hi,\n`;
  sql += `    difficulty, tags, source, license, status\n`;
  sql += `  ) values (\n`;
  sql += `    v_section_id, ${i}, ${q(qst.stem_en)}, ${q(qst.stem_hi)},\n`;
  sql += `    ${q(qst.explanation_method_en)}, ${q(qst.explanation_concept_en)}, ${q(qst.explanation_method_hi)}, ${q(qst.explanation_concept_hi)},\n`;
  sql += `    ${q(qst.difficulty)}, ${arr(qst.tags)}, ${q(qst.source)}, ${q(qst.license)}, 'active'\n`;
  sql += `  ) returning id into v_q_id;\n`;

  qst.options.forEach((opt) => {
    sql += `  insert into public.options (question_id, label, text_en, text_hi, is_correct, trap_reason_en, trap_reason_hi)\n`;
    sql += `  values (v_q_id, ${q(opt.label)}, ${q(opt.text_en)}, ${q(opt.text_hi)}, ${opt.is_correct ? "true" : "false"}, ${q(opt.trap_reason_en)}, ${q(opt.trap_reason_hi)});\n`;
  });
  sql += `\n`;
});

sql += `end $$;\n\ncommit;\n`;

const outDir = resolve(__dirname, "..", "sql");
await mkdir(outDir, { recursive: true });
const outPath = resolve(outDir, `${basename(path).replace(".json", "")}.sql`);
await writeFile(outPath, sql, "utf8");
console.log(`wrote ${outPath} (${sql.length} bytes, ${totalQuestions} Q × 4 opts)`);
