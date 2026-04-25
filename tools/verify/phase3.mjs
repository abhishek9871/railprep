#!/usr/bin/env node
// phase3.mjs — Gate 2 scripted verification.
// Exercises start_attempt / upsertAnswer / submit_attempt through the real
// Supabase JS SDK against the live project, as an authenticated test user.
//
// Produces docs/phase3-evidence/script-results.txt with PASS/FAIL per step.

import { createClient } from '@supabase/supabase-js';
import { config as dotenv } from 'dotenv';
import { mkdir, writeFile } from 'node:fs/promises';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

dotenv();

const __dirname = dirname(fileURLToPath(import.meta.url));
const ROOT = resolve(__dirname, '..', '..');
const EVIDENCE = resolve(ROOT, 'docs', 'phase3-evidence');
const SAMPLE_SLUG = 'ntpc-cbt1-sample-01';

const out = [];
function log(line) { console.log(line); out.push(line); }
function assert(id, ok, detail = '') {
  const tag = ok ? 'PASS' : 'FAIL';
  log(`[${tag}] ${id}  ${detail}`);
  if (!ok) process.exitCode = 1;
  return ok;
}

async function main() {
  log(`# Phase 3 Gate-2 verification — ${new Date().toISOString()}`);
  for (const k of ['SUPABASE_URL', 'SUPABASE_ANON_KEY', 'VERIFY_EMAIL', 'VERIFY_PASSWORD']) {
    if (!process.env[k]) throw new Error(`missing env ${k}; see tools/verify/README.md`);
  }

  const supabase = createClient(process.env.SUPABASE_URL, process.env.SUPABASE_ANON_KEY, {
    auth: { persistSession: false, autoRefreshToken: false },
  });

  const { error: authError } = await supabase.auth.signInWithPassword({
    email: process.env.VERIFY_EMAIL,
    password: process.env.VERIFY_PASSWORD,
  });
  if (authError) throw new Error(`sign-in failed: ${authError.message}`);
  const { data: { user } } = await supabase.auth.getUser();
  log(`signed in as ${user.email} (id=${user.id.slice(-8)})`);

  // Resolve the sample test.
  const { data: tests, error: tErr } = await supabase
    .from('tests').select('*').eq('slug', SAMPLE_SLUG).limit(1);
  if (tErr) throw tErr;
  if (!tests || tests.length === 0) throw new Error(`sample test ${SAMPLE_SLUG} not found`);
  const test = tests[0];
  log(`test: ${test.title_en} (${test.total_questions}Q / ${test.total_minutes}min / neg=${test.negative_marking_fraction})`);

  // ---- S1: start_attempt creates or resumes ----
  const { data: att1, error: e1 } = await supabase.rpc('start_attempt', { p_test_id: test.id });
  assert('S1', !e1 && att1 && att1.status === 'IN_PROGRESS', e1 ? `err=${e1.message}` : `aid=${att1?.id?.slice(-8)}`);
  const attemptId = att1.id;

  // ---- S2: idempotency ----
  const { data: att2, error: e2 } = await supabase.rpc('start_attempt', { p_test_id: test.id });
  assert('S2', !e2 && att2 && att2.id === attemptId, e2 ? `err=${e2.message}` : `reused aid=${att2?.id?.slice(-8)}`);

  // ---- Pick 5 questions + option ids ----
  const { data: sections } = await supabase
    .from('test_sections').select('*').eq('test_id', test.id).order('display_order');

  const questionsBySection = {};
  for (const s of sections) {
    const { data: qs } = await supabase
      .from('questions').select('*, options(*)')
      .eq('section_id', s.id).order('display_order');
    questionsBySection[s.subject_hint] = qs;
  }
  const pick = {
    q_math1: questionsBySection.math[0],           // correct
    q_reas1: questionsBySection.reason[0],         // correct
    q_ga1:   questionsBySection.ga[0],             // correct
    q_math2: questionsBySection.math[1],           // wrong
    q_reas2: questionsBySection.reason[1],         // skipped (selected=null)
  };
  const correctOf = q => q.options.find(o => o.is_correct).id;
  const wrongOf   = q => q.options.find(o => !o.is_correct).id;

  // ---- S3: upsert 5 answers ----
  const upserts = [
    { attempt_id: attemptId, question_id: pick.q_math1.id, selected_option_id: correctOf(pick.q_math1), flagged: false },
    { attempt_id: attemptId, question_id: pick.q_reas1.id, selected_option_id: correctOf(pick.q_reas1), flagged: false },
    { attempt_id: attemptId, question_id: pick.q_ga1.id,   selected_option_id: correctOf(pick.q_ga1),   flagged: false },
    { attempt_id: attemptId, question_id: pick.q_math2.id, selected_option_id: wrongOf(pick.q_math2),   flagged: false },
    { attempt_id: attemptId, question_id: pick.q_reas2.id, selected_option_id: null,                    flagged: true  },
  ];
  const { error: upErr } = await supabase
    .from('attempt_answers').upsert(upserts, { onConflict: 'attempt_id,question_id' });
  assert('S3', !upErr, upErr ? `err=${upErr.message}` : `5 rows upserted`);

  // ---- S4/S5/S6/S7: submit_attempt ----
  const { data: subm, error: sErr } = await supabase.rpc('submit_attempt', { p_attempt_id: attemptId });
  assert('S4', !sErr && subm?.status === 'SUBMITTED', sErr ? `err=${sErr.message}` : `status=${subm?.status}`);
  assert('S5', subm.correct_count === 3 && subm.wrong_count === 1 && subm.skipped_count === 1,
         `correct=${subm.correct_count} wrong=${subm.wrong_count} skipped=${subm.skipped_count}`);
  assert('S6', Math.abs(subm.score - 2.6667) < 1e-3, `score=${subm.score}`);

  const shapeOK = Array.isArray(subm.section_breakdown?.sections) &&
    subm.section_breakdown.sections.every(s => (
      typeof s.section_id === 'string' &&
      typeof s.subject_hint === 'string' &&
      Number.isFinite(s.attempted) && Number.isFinite(s.correct) &&
      Number.isFinite(s.wrong) && Number.isFinite(s.skipped) &&
      Number.isFinite(s.score) && Number.isFinite(s.max_score)
    ));
  assert('S7', shapeOK, `sections=${subm.section_breakdown?.sections?.length}`);

  // ---- S8: post-submit INSERT on attempt_answers rejected with 55000 ----
  const q_ga2 = questionsBySection.ga[1];
  const { error: postErr } = await supabase.from('attempt_answers').insert({
    attempt_id: attemptId, question_id: q_ga2.id, selected_option_id: null, flagged: false,
  });
  // PostgREST surfaces plpgsql error codes in error.code ("55000") or error.message.
  const triggered = !!postErr && (postErr.code === '55000' ||
    /submitted; answers are immutable/.test(postErr.message || ''));
  assert('S8', triggered,
         postErr ? `code=${postErr.code || '—'} msg=${(postErr.message || '').slice(0, 80)}` :
                   'no error — trigger did NOT fire!');

  await supabase.auth.signOut();

  const summary = `\n${process.exitCode ? 'FAIL' : 'PASS'}: ${out.filter(l => l.startsWith('[PASS]')).length}/${out.filter(l => /^\[PASS\]|^\[FAIL\]/.test(l)).length} assertions passed`;
  log(summary);

  await mkdir(EVIDENCE, { recursive: true });
  await writeFile(resolve(EVIDENCE, 'script-results.txt'), out.join('\n') + '\n');
  console.log(`\nwritten to ${resolve(EVIDENCE, 'script-results.txt')}`);
}

main().catch((e) => { console.error(e); process.exit(1); });
