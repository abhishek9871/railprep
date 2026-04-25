#!/usr/bin/env node
// audit-questions.mjs — verifier for Stage 4 original sectional content.
//
// Reads JSON candidate files (or queries Supabase if --supabase flag given) and runs the
// rule list per the Phase 3 Part C brief. Exits 1 if any error-level issue.
//
// Usage:
//   node src/audit-questions.mjs --candidates path/to/candidates.json
//   node src/audit-questions.mjs --candidates audit/fixture-questions/passes.json audit/fixture-questions/two-correct.json
//
// Schema expected per candidate file: { questions: [<Question>, ...] }
// Each Question matches the Stage 4 schema below — see docs/phase3-research.md §2.1.

import { readFile, writeFile, mkdir } from "node:fs/promises";
import { resolve, dirname, basename } from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = dirname(fileURLToPath(import.meta.url));
const ROOT = resolve(__dirname, "..");

const ARG_CANDIDATES = process.argv.includes("--candidates")
  ? process.argv.slice(process.argv.indexOf("--candidates") + 1).filter((a) => !a.startsWith("--"))
  : [];

if (ARG_CANDIDATES.length === 0) {
  console.error("usage: audit-questions.mjs --candidates <path1> [path2] ...");
  process.exit(2);
}

const VALID_DIFFICULTY = new Set(["EASY", "MEDIUM", "HARD"]);
const VALID_LICENSE = new Set(["ORIGINAL", "PYQ_PUBLIC", "CC_BY_SA"]);
const VALID_OPTION_LABELS = ["A", "B", "C", "D"];
const SOURCE_RE = /^(Original|PYQ_\d{4}_S[1-9]+_\d{2}-\d{2}|NCERT_[a-z0-9]+_(Ch)?\d+(-\d+)?)$/;
const KEBAB_TAG_RE = /^[a-z][a-z0-9-]*$/;

// Filler-trap pattern: vibes instead of misconceptions. A trap_reason must name the SPECIFIC
// arithmetic step or conceptual error that produces the wrong option's exact value. "Rough
// estimate", "gut feel", "rounded wrong direction" describe how the student feels, not what
// they computed — which means they don't teach. Emits warnings (not errors) so authors can
// override with a tactical justification, but surfaces the pattern automatically.
const FILLER_TRAP_RE = /\b(rough estimate|gut feel|estimated|approximation|rounded|guessed)\b/i;

function check(question, idx, errors, warnings) {
  const tag = `Q[${idx}] (${question.id ?? "no-id"})`;

  // 1) options shape
  if (!Array.isArray(question.options) || question.options.length !== 4) {
    errors.push(`${tag}: must have exactly 4 options (got ${question.options?.length ?? 0})`);
    return;
  }
  const labels = question.options.map((o) => o.label);
  if (new Set(labels).size !== 4 || !labels.every((l) => VALID_OPTION_LABELS.includes(l))) {
    errors.push(`${tag}: option labels must be unique and ∈ {A,B,C,D}, got ${labels.join(",")}`);
  }
  const correct = question.options.filter((o) => o.is_correct === true);
  if (correct.length !== 1) {
    errors.push(`${tag}: exactly one option must have is_correct=true (got ${correct.length})`);
  }
  // distinct option text + non-empty + length sanity
  const seenTexts = new Set();
  for (const o of question.options) {
    const t = (o.text_en ?? "").trim();
    if (!t) errors.push(`${tag}: option ${o.label} has empty text_en`);
    if (seenTexts.has(t)) errors.push(`${tag}: duplicate option text "${t}"`);
    seenTexts.add(t);
    if (question.stem_en && t.length > question.stem_en.length * 3) {
      warnings.push(`${tag}: option ${o.label} text is >3× stem length — likely a parsing artefact`);
    }
  }

  // 2) stem
  if (!question.stem_en || question.stem_en.trim().length === 0) {
    errors.push(`${tag}: stem_en is empty`);
  }
  if (!question.stem_hi || question.stem_hi.trim().length === 0) {
    if (question.todo_hi !== true) {
      errors.push(`${tag}: stem_hi missing (set todo_hi=true to defer)`);
    } else {
      warnings.push(`${tag}: stem_hi deferred via todo_hi=true`);
    }
  }

  // 3) two-layer explanations (the Phase 3 Part C pedagogy contract)
  const expMethodEn = (question.explanation_method_en ?? "").trim();
  const expConceptEn = (question.explanation_concept_en ?? "").trim();
  if (expMethodEn.length < 30) {
    errors.push(`${tag}: explanation_method_en too short (${expMethodEn.length} chars; min 30)`);
  }
  if (expConceptEn.length < 50) {
    errors.push(`${tag}: explanation_concept_en too short (${expConceptEn.length} chars; min 50)`);
  }
  // Hindi method/concept: required unless todo_hi=true
  if (question.todo_hi !== true) {
    const expMethodHi = (question.explanation_method_hi ?? "").trim();
    const expConceptHi = (question.explanation_concept_hi ?? "").trim();
    if (expMethodHi.length < 30) {
      errors.push(`${tag}: explanation_method_hi too short (${expMethodHi.length} chars; min 30)`);
    }
    if (expConceptHi.length < 50) {
      errors.push(`${tag}: explanation_concept_hi too short (${expConceptHi.length} chars; min 50)`);
    }
  }

  // 4) trap analysis on options
  for (const o of question.options) {
    if (o.is_correct === true) continue;
    const trap = (o.trap_reason_en ?? "").trim();
    if (trap.length < 20) {
      errors.push(`${tag}: option ${o.label} (incorrect) needs trap_reason_en ≥ 20 chars (got ${trap.length})`);
    }
    // Warn on filler-vibe wording — the author should name a specific slip.
    if (FILLER_TRAP_RE.test(trap)) {
      warnings.push(`${tag}: option ${o.label} trap_reason_en uses vibe-language (${trap.match(FILLER_TRAP_RE)[0]}); name the specific computation that produces this value`);
    }
    const trapHi = (o.trap_reason_hi ?? "").trim();
    if (trapHi && FILLER_TRAP_RE.test(trapHi)) {
      warnings.push(`${tag}: option ${o.label} trap_reason_hi uses vibe-language (${trapHi.match(FILLER_TRAP_RE)[0]}); name the specific computation`);
    }
  }

  // 5) difficulty / tags / source / license
  if (!VALID_DIFFICULTY.has(question.difficulty)) {
    errors.push(`${tag}: difficulty must be EASY|MEDIUM|HARD, got '${question.difficulty}'`);
  }
  if (!Array.isArray(question.tags) || question.tags.length === 0) {
    errors.push(`${tag}: tags must be a non-empty array`);
  } else {
    for (const t of question.tags) {
      if (!KEBAB_TAG_RE.test(t)) errors.push(`${tag}: tag '${t}' must be lowercase-kebab`);
    }
  }
  if (!question.source || !SOURCE_RE.test(question.source)) {
    errors.push(`${tag}: source must match Original|PYQ_{YEAR}_S{N}_{DD-MM}|NCERT_{book}_{ch}, got '${question.source}'`);
  }
  if (!VALID_LICENSE.has(question.license)) {
    errors.push(`${tag}: license must be ORIGINAL|PYQ_PUBLIC|CC_BY_SA, got '${question.license}'`);
  }
}

const errors = [];
const warnings = [];
const fileSummaries = [];

for (const file of ARG_CANDIDATES) {
  const fileErrors = [];
  const fileWarnings = [];
  const path = resolve(file);
  let bundle;
  try {
    bundle = JSON.parse(await readFile(path, "utf8"));
  } catch (e) {
    fileErrors.push(`failed to read/parse ${path}: ${e.message}`);
    errors.push(...fileErrors);
    fileSummaries.push({ file: path, count: 0, errors: fileErrors.length, warnings: 0 });
    continue;
  }
  const qs = bundle.questions ?? [];
  for (let i = 0; i < qs.length; i++) check(qs[i], i, fileErrors, fileWarnings);
  fileSummaries.push({
    file: path,
    count: qs.length,
    errors: fileErrors.length,
    warnings: fileWarnings.length,
  });
  errors.push(...fileErrors);
  warnings.push(...fileWarnings);
}

const reportDir = resolve(ROOT, "audit");
await mkdir(reportDir, { recursive: true });

const stamp = new Date().toISOString();
const md = [
  `# audit-questions.mjs report — ${stamp}`,
  ``,
  `## Files audited`,
  ``,
  `| File | Questions | Errors | Warnings |`,
  `|---|---:|---:|---:|`,
  ...fileSummaries.map((s) => `| ${basename(s.file)} | ${s.count} | ${s.errors} | ${s.warnings} |`),
  ``,
  `## Errors (${errors.length})`,
  ``,
  errors.length === 0 ? "_(none)_" : errors.map((e) => `- ${e}`).join("\n"),
  ``,
  `## Warnings (${warnings.length})`,
  ``,
  warnings.length === 0 ? "_(none)_" : warnings.map((w) => `- ${w}`).join("\n"),
  ``,
].join("\n");

await writeFile(resolve(reportDir, "questions-report.md"), md, "utf8");
await writeFile(
  resolve(reportDir, "questions-report.json"),
  JSON.stringify({ stamp, fileSummaries, errors, warnings }, null, 2),
  "utf8",
);

const total = fileSummaries.reduce((a, s) => a + s.count, 0);
console.log(`audit-questions: ${total} Q across ${fileSummaries.length} file(s) — ${errors.length} error(s), ${warnings.length} warning(s)`);
console.log(`report: tools/content-pipeline/audit/questions-report.md`);
process.exit(errors.length > 0 ? 1 : 0);
