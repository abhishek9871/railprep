#!/usr/bin/env node
// Emit per-sectional SQL split into MCP-friendly chunks (~20KB each).
// Chunk 1: tests row + section row + first 5 questions/options.
// Chunks 2..N: additional question batches that look up the section_id by slug.
// Usage: node src/emit-sectional-chunks.mjs <candidates/sectional-X.json>

import { readFile, writeFile, mkdir } from "node:fs/promises";
import { resolve, dirname, basename } from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = dirname(fileURLToPath(import.meta.url));
const path = process.argv[2];
if (!path) { console.error("usage: emit-sectional-chunks.mjs <candidates.json>"); process.exit(2); }

const bundle = JSON.parse(await readFile(resolve(path), "utf8"));
const q = (s) => s == null ? "null" : `'${String(s).replace(/'/g, "''")}'`;
const arr = (a) => `array[${(a ?? []).map((t) => q(t)).join(",")}]::text[]`;

const totalQ = bundle.questions.length;
const totalMinutes = bundle.total_minutes ?? 25;
const slug = bundle.slug;
const CHUNK_SIZE = 5;

function emitQuestion(q_, i) {
  let sql = `  insert into public.questions (\n    section_id, display_order, stem_en, stem_hi,\n    explanation_method_en, explanation_concept_en, explanation_method_hi, explanation_concept_hi,\n    difficulty, tags, source, license, status\n  ) values (\n    v_section_id, ${i}, ${q(q_.stem_en)}, ${q(q_.stem_hi)},\n    ${q(q_.explanation_method_en)}, ${q(q_.explanation_concept_en)}, ${q(q_.explanation_method_hi)}, ${q(q_.explanation_concept_hi)},\n    ${q(q_.difficulty)}, ${arr(q_.tags)}, ${q(q_.source)}, ${q(q_.license)}, 'active'\n  ) returning id into v_q_id;\n`;
  for (const opt of q_.options) {
    sql += `  insert into public.options (question_id, label, text_en, text_hi, is_correct, trap_reason_en, trap_reason_hi)\n  values (v_q_id, ${q(opt.label)}, ${q(opt.text_en)}, ${q(opt.text_hi)}, ${opt.is_correct ? "true" : "false"}, ${q(opt.trap_reason_en)}, ${q(opt.trap_reason_hi)});\n`;
  }
  return sql + "\n";
}

const outDir = resolve(__dirname, "..", "sql", "sectional-chunks", basename(path).replace(".json", ""));
await mkdir(outDir, { recursive: true });

// Chunk 1: tests + section + Q[0..CHUNK_SIZE-1]
let chunk1 = `do $$\ndeclare\n  v_test_id    uuid;\n  v_section_id uuid;\n  v_q_id       uuid;\nbegin\n  insert into public.tests (slug, title_en, title_hi, kind, exam_target, total_questions, total_minutes, negative_marking_fraction, status)\n  values (${q(slug)}, ${q(bundle.title_en)}, ${q(bundle.title_hi)}, 'SECTIONAL', 'NTPC_CBT1', ${totalQ}, ${totalMinutes}, 0.3333, 'active')\n  on conflict (slug) do update set title_en = excluded.title_en, title_hi = excluded.title_hi, total_questions = excluded.total_questions, total_minutes = excluded.total_minutes, updated_at = now()\n  returning id into v_test_id;\n\n  delete from public.test_sections where test_id = v_test_id;\n\n  insert into public.test_sections (test_id, title_en, title_hi, question_count, display_order, subject_hint)\n  values (v_test_id, ${q(bundle.section.title_en)}, ${q(bundle.section.title_hi)}, ${totalQ}, 0, ${q(bundle.section.subject_hint)})\n  returning id into v_section_id;\n\n`;
for (let i = 0; i < Math.min(CHUNK_SIZE, totalQ); i++) {
  chunk1 += emitQuestion(bundle.questions[i], i);
}
chunk1 += `end $$;\n`;
await writeFile(resolve(outDir, `chunk-01.sql`), chunk1, "utf8");
console.log(`chunk-01.sql: tests + section + Q1..${Math.min(CHUNK_SIZE, totalQ)} (${chunk1.length} bytes)`);

// Chunks 2..N: additional questions, looking up v_section_id by slug
let chunkIdx = 2;
for (let start = CHUNK_SIZE; start < totalQ; start += CHUNK_SIZE) {
  const end = Math.min(start + CHUNK_SIZE, totalQ);
  let chunk = `do $$\ndeclare\n  v_test_id    uuid;\n  v_section_id uuid;\n  v_q_id       uuid;\nbegin\n  select id into v_test_id from public.tests where slug = ${q(slug)};\n  select id into v_section_id from public.test_sections where test_id = v_test_id order by display_order limit 1;\n\n`;
  for (let i = start; i < end; i++) {
    chunk += emitQuestion(bundle.questions[i], i);
  }
  chunk += `end $$;\n`;
  const fname = `chunk-${String(chunkIdx).padStart(2, "0")}.sql`;
  await writeFile(resolve(outDir, fname), chunk, "utf8");
  console.log(`${fname}: Q${start + 1}..${end} (${chunk.length} bytes)`);
  chunkIdx++;
}
