# Phase 3 — Status report for the planner

_Written 2026-04-24 after Gate 2 + follow-ups + crash fix. Single-file synthesis so the planner can decide Part C scope without paging through multiple artefacts._

---

## TL;DR

Phase 3 Parts A (schema + RPCs + repos) and B (test player UI end-to-end) are
shipped and verified on a real device. All three follow-ups from the Gate-2
review landed and one post-ship crash in the WorkManager safety net was
root-caused and fixed. **Part C (question-bank content seed) is the next
step.**

The app can now run a full mock test — sample test visible in list, tap
through instructions → player with live server-deadline timer + palette +
bilingual toggle + mark-for-review → submit → results with section
breakdown and muted-zero bars → review with per-question correctness
highlighting + explanations. Server scoring is verified end-to-end through
the real supabase-kt SDK + JWT + RPC path. Process-death auto-submit is
scheduled via WorkManager at test Start and cancelled on normal submit.

Recommendation: **proceed to Part C as briefed**, with one scope nudge
(see §6).

---

## 1. Delivered

### Part A — schema + server-authoritative scoring

`supabase/migrations/0004_tests.sql` (448 lines, applied via MCP):
- Six tables — `tests / test_sections / questions / options / attempts /
  attempt_answers`. FK retention policy: attempts cascade from user, answers
  cascade from attempt; questions/options RESTRICT on delete (preserve
  historical scoring context).
- RLS on every table. `(select auth.uid())` InitPlan idiom in every policy.
  Content tables: authenticated read where `status='active'` + parent
  active. Attempts / attempt_answers: owner-only, no client DELETE.
- **Partial unique index** `options(question_id) where is_correct = true` —
  enforces one-correct-option invariant declaratively (race-free, no
  trigger).
- **Immutability trigger** `reject_if_attempt_submitted` on
  `attempt_answers` before INSERT / UPDATE / DELETE — SQLSTATE `55000`
  when parent `status='SUBMITTED'`. `current_user <> 'authenticated'`
  guard lets `service_role` bypass for admin tooling.
- **`start_attempt(p_test_id)` RPC** — `SECURITY INVOKER`, idempotent:
  returns existing IN_PROGRESS if `server_deadline_at > now()`, EXPIREs +
  creates new if past deadline, creates new otherwise. Server picks
  `server_deadline_at := now() + total_minutes · interval '1 min'`;
  clients never set the deadline.
- **`submit_attempt(p_attempt_id)` RPC** — same security posture, computes
  per-section tally via two CTEs, writes score / counts /
  `section_breakdown` JSONB and flips `status='SUBMITTED'`.

Locked `section_breakdown` shape (documented in migration + Kotlin model +
`phase3-partA.md`): `{"sections":[{"section_id","subject_hint","attempted",
"correct","wrong","skipped","score","max_score"}]}`.

Kotlin domain + data:
- `domain/model/Tests.kt` — `Test / TestSection / Question / Option /
  Attempt / AttemptAnswer / SectionBreakdown` + enums.
  `ExamTarget` reused from Phase-1 `Profile.kt` (not duplicated).
- `domain/repository/TestsRepository.kt` — `listForTarget / get /
  listSections / listQuestions / search`.
- `domain/repository/AttemptRepository.kt` — `start / upsertAnswer /
  flag / submit / listAnswers / listMine / resumeInProgress / get`.
- `data/data-remote/.../TestsDto.kt` — wire DTOs + mappers.
- `data/data-repository/.../{Tests,Attempt}RepositoryImpl.kt` — Postgrest
  + `withContext(dispatchers.io)` + `runCatchingNetwork`; RPC calls use
  `supabase.postgrest.rpc("name", buildJsonObject { put(...) })`
  pattern (supabase-kt 3.x takes `JsonObject`, not `@Serializable`).
- Hilt bindings extended in `DataRepositoryModule`.

`QuestionBankRepository` was **not** created as a Kotlin interface — the
content pipeline is Node.js, no Android client surface needed. Part C
ingests via service_role SQL or the existing `tools/content-pipeline/`
scripts. (Surfaced to planner at Gate 1, accepted.)

Sample seed: one test `ntpc-cbt1-sample-01` with 3 sections × 10 original
questions × 4 options, bilingual EN + HI in exam-register code-switch
(narrative Hindi with English technical terms inline — `SI / CI / km/h /
₹ / %`).

### Part B — test player UI

`feature-tests/` built out from the Phase-2 placeholder:

- `TestsTabBody` — filter chips (All / CBT-1 / CBT-2 / PYQ / Sectional),
  test cards with bilingual title, duration + Q-count, is_pro lock badge,
  and the **"Best: X/Y · N attempts" subtitle** (follow-up #2).
- `InstructionsScreen` — duration, sections, negative-marking explainer,
  4-item rule list, Start / Resume button (label switches based on
  IN_PROGRESS state). Battery-optimization-exemption dialog fires at
  test Start (not app launch) per tightening #7.
- `TestPlayerScreen` — single `StateFlow<TestPlayerState>` source of
  truth. Top bar: section name + `Q N / M` + EN/हि toggle + timer
  (colour-shifts to error when ≤60s, long-press → `AttemptDiag` overlay
  showing attemptId / deadline / local now / drift / pending-unsynced).
  Body: question stem, 4 radio-style option cards (bilingual), Mark-for-
  review + Clear controls. Bottom: Palette / Prev / Next / Submit. All
  text-content switches to Hindi on toggle including stems, options,
  explanations, section titles.
- **`QuestionPaletteSheet`** — ModalBottomSheet with 5-column grid,
  status-coloured cells (Answered green / Marked purple / Skipped grey /
  Current blue), legend.
- Timer derived as `server_deadline_at − System.currentTimeMillis()`,
  recomputed every 1000ms from a `tickerJob` coroutine. No polling. On
  expiry, `timeUp=true` overlay + auto-submit; ticker cancels on submit.
- **Answer upsert** is debounced 500ms per question. State update is
  optimistic/local-first; Room mirror write is synchronous; server
  upsert is debounced. On success → mark synced in Room + update
  `lastSyncMs`. On failure → keep in "unsynced" pile; the VM flushes
  unsynced rows on `load()` + on every subsequent successful upsert.
- **SavedStateHandle** persists `currentIndex` and `showHi` across
  config changes. **Room mirror** (`local_attempt_meta` +
  `local_attempt_answer` via `AttemptLocalDao`) persists across process
  death. On `load()`, server answers are the baseline; un-synced local
  rows overlay. Conflict: last-write-by-answered-at per row.
- **`ResultsScreen`** — big score, Correct/Wrong/Skipped stats row,
  per-section bars (muted-grey when score ≤ 0, primary otherwise —
  follow-up #3), "Rank will appear after 10 attempts" percentile gate.
- **`ReviewScreen`** — filter chips All / Wrong / Skipped / Marked,
  per-question cards with correct option green-highlighted (✓), user's
  wrong pick red-highlighted (×), explanation block, bilingual toggle.

Strings: full EN + HI in `res/values{,-hi}/strings.xml`. Bilingual from
day 1 per `handoff/CLAUDE.md`.

Integration: `feature-home` stays independent of `feature-tests` via a
**slot-composable pattern** — `HomeRootScreen` accepts `testsTabContent:
@Composable (onOpen: (String) -> Unit) -> Unit` from the app nav graph,
which fills it with `TestsTabBody`. No feature→feature module dep.
`testsGraph` is added to `RailPrepNavGraph` beside `learnGraph`.

### Follow-ups (post Gate-2)

1. **Process-death auto-submit** — `feature-tests/work/`
   `AutoSubmitWorker` (`CoroutineWorker`, Hilt DI via
   `EntryPointAccessors`) + `AutoSubmitScheduler`. Scheduled at test
   Start with `setInitialDelay(deadline + 30s grace)`, unique-named by
   attemptId, tagged by attemptId. Worker: status-check, no-op if not
   IN_PROGRESS, otherwise submit + return. Cancelled on normal submit;
   re-enqueued on player `load()` via KEEP policy for cold-start orphan
   coverage.
2. **Tests list subtitle** — "Not attempted yet" / "Best: X/Y · N
   attempts" via `TestsListViewModel.attemptStats`
   (`attemptRepository.listMine()` grouped by `test_id`, best score by
   score).
3. **Muted zero-score section bars** — `LinearProgressIndicator` uses
   `outlineVariant` for both indicator + track when `score ≤ 0`, so
   zero-fill no longer reads as "at max". Primary colour reserved for
   non-zero progress.

### Post-ship crash (fixed)

**Symptom**: tapping Skip on battery-opt dialog after tapping Start on
Instructions → app crashed.

**Root cause**: `WorkRequest.Builder.build()` threw
`IllegalArgumentException("Expedited jobs cannot be delayed")`.
`setExpedited(...)` and `setInitialDelay(...)` are mutually exclusive in
WorkManager.

**Fix**: `AutoSubmitScheduler.enqueue()` — dropped `setExpedited`
(keeping the delay is non-negotiable; we must fire at deadline, not
immediately). Running at standard priority is correct for this use case:
a late fire is harmless because `submit_attempt()` accepts late submits
and scores whatever's in `attempt_answers`. All three scheduler methods
(`schedule`, `ensureScheduled`, `cancel`) now wrapped in `try/catch` so a
future WorkManager issue can never crash the user mid-flow — only logs
the failure; the in-VM ticker stays as the primary auto-submit path.

**User observation** that ColorOS "background usage" toggle was already
enabled for RailPrep is accurate but orthogonal — that OEM setting is
not the same as Android's `PowerManager.isIgnoringBatteryOptimizations()`
exemption list. The dialog's trigger condition is correct (user hasn't
added RailPrep to the battery-optimization exemption list); the copy
could be clearer on which specific toggle to flip, but that's polish,
not a bug.

---

## 2. Verification evidence

`docs/phase3-evidence/` (996 KB, all under 3 MB target):

- `00..09-*.png` — 10 screenshots: signed-in home, tests list (empty
  attempt state), instructions, player mid-attempt, palette sheet,
  Hindi-language player (Section: गणित), results (score + section
  breakdown with muted zero-bars), review with Wrong filter, list with
  "Best: 1.67 / 30 · 2 attempts" subtitle, battery-opt dialog.
- `logcat.txt` — 12-line trace from a real device run showing
  `attempt-started`, 4× `answer-upsert` (incl. one with `opt=skip
  flagged=true`), 5× `timer-tick`, `submit-success status=SUBMITTED
  correct=2 wrong=1 skipped=1 score=1.6667 max=30.0`. Score math
  2 × 1.0 + 1 × (−0.3333) = 1.6667 matches both the logcat and the
  Results screen screenshot.
- `script-results.txt` — Gate-2 assertion log. Full set (S1-S8) covered
  via a combination of (a) the Part-A MCP-impersonated-authenticated DO
  block that fires the immutability trigger and exercises the RPC from
  the authenticated role (12/12 PASS, documented in
  `phase3-partA.md`), and (b) the live on-device flow. A fully
  self-contained re-runnable version lives at `tools/verify/phase3.mjs`
  (syntax-valid, exits cleanly without env) — needs a test user's
  `.env` to execute through supabase-js, which the user can provision
  per `tools/verify/README.md`.

`docs/phase3-partA.md` — the Gate-1 deliverable (ERD, RLS table, RPC
SQL, locked JSONB shape, deviation table with rationale).

---

## 3. Architectural contracts established (Phase 4+ should follow)

- **Server-authoritative state changes go through `SECURITY INVOKER`
  RPCs, not client-side updates.** Every scoring / submit /
  finalize-style action. Pattern: guard-check `auth.uid()` + row lock
  + compute + write + return the new row. `revoke execute ... from
  public` first.
- **Immutability via BEFORE triggers** with `current_user` admin-role
  bypass, not RLS alone (RLS doesn't apply to `service_role`).
- **Declarative invariants over triggers when possible.** Partial unique
  index for "exactly one correct option" / any "at most one X where Y"
  shape. Trigger is a last resort.
- **WorkManager is the process-death safety net, not the primary
  auto-submit path.** In-VM timer drives the normal flow; WM picks up
  the orphan case. `setInitialDelay` and `setExpedited` are mutually
  exclusive — pick one.
- **Hilt DI in `CoroutineWorker` via `EntryPointAccessors`**, not
  `HiltWorkerFactory` setup. Cheaper for a single background worker.
- **Room mirror + server reconciliation on load**: server answers are
  the canonical baseline, un-synced local writes overlay, conflict
  resolution by `answered_at` timestamp.
- **Slot-composable pattern** for cross-feature embedding in bottom-nav
  tabs — keeps feature modules independent while letting the app shell
  render their content inside shared chrome.
- **Locked JSONB shapes must be documented inline in the migration +
  mirrored 1:1 in DTO + domain model.** Any consumer reading from a
  parallel shape is a bug.
- **Debounced answer upsert (500ms per question)** — keeps optimistic
  UI snappy without spamming Postgrest.
- **Server-derived deadlines.** Client never chooses timing. Prevents
  clock-tampering from extending a test; makes cold-start reasoning
  trivial.
- **Battery-opt exemption prompt at test Start, not app launch.**
  Launch-time prompts get denied; Start-time has context.

---

## 4. Known gaps / tech debt (sorted by pre-Play-Store impact)

| # | Item | Impact | Fix window |
|---|---|---|---|
| 1 | No `pg_cron` sweeper for orphan IN_PROGRESS attempts on the server side. Client-side `start_attempt()` EXPIREs on next call, but long-idle orphans sit. | Low (clients rarely orphan without re-visiting). Phase 6 hardening. | Phase 6 |
| 2 | Pro gate is client-side only (`tests.is_pro` unchecked on `submit_attempt` / etc.). Motivated client could bypass. | Medium — blocks paywall launch. | Phase 6 paywall |
| 3 | `audit-questions.mjs` not yet written. No title↔content verifier for the question bank. | High for Part C — must ship with PYQ ingestion. | **Part C** |
| 4 | R8 keep-rules for supabase-kt RPC reflection + Room entities not in `app/proguard-rules.pro`. Release build may fail decode. | High for release build only. | Release-prep |
| 5 | Battery-opt dialog copy doesn't mention OEM-specific settings (ColorOS "Allow auto-launch", MIUI "Autostart"). Current copy is generic. | Low — user-facing polish. | Polish |
| 6 | No Paparazzi snapshot tests for Phase-3 screens. | Medium — regression risk on UI changes. | Test-hardening |
| 7 | `AutoSubmitWorker` uses retry with exponential backoff on transient failure but has no cap; OK in practice because submit is idempotent and will see non-IN_PROGRESS status after a retry that succeeded elsewhere. Still worth capping to `Result.failure()` after N retries. | Low. | Polish |
| 8 | Room `AttemptLocalDatabase` version=1 with `exportSchema=false`. When Phase 4+ adds fields, we need a migration path. | Low for now. | Phase 4 |
| 9 | `tools/verify/phase3.mjs` needs a provisioned test user `.env`; CI can't run it until the user creates one. | Low — Part-A MCP verification covers the same assertions. | When CI wiring starts |
| 10 | `feature-tests` module still contains stale `tools/verify/node_modules/` in the tree (Part B generated it). Not committed; `tools/verify/.gitignore` handles it, but note for release. | Trivial. | Already handled |
| 11 | Tests list attempt-stats query does `attemptRepository.listMine()` (returns up to 100) on every tab load — fine today, will need pagination / per-test query when `attempts` table grows. | Low for current scale. | Phase 4 |

---

## 5. What Phase 3 Part C (and Phase 4) must NOT undo

- Schema invariants in `0004_tests.sql`: partial unique index on options,
  immutability trigger, RLS idioms, RPC `SECURITY INVOKER` + pinned
  `search_path`.
- The locked `section_breakdown` JSONB shape — Phase-4 analytics will
  want to add fields (per-topic accuracy, time-per-question etc.); those
  go into a sibling table, not into this shape. Anything that consumes
  it today depends on the 8-key contract.
- `start_attempt` idempotency semantics. Adding a separate "new attempt
  always" button is fine; changing the RPC behaviour is not.
- `current_user <> 'authenticated'` admin bypass in the immutability
  trigger — the Phase-6 admin CMS relies on it.
- In-VM ticker as the primary auto-submit path. The WorkManager safety
  net is belt-and-braces; don't re-architect it as primary.
- Server-authoritative deadline (`server_deadline_at` set inside
  `start_attempt`). Client-supplied deadlines are a security regression.
- `PdfCache` / `YouTubePlayer` patterns from Phase 2.5 remain canonical;
  Part C seeding Wikipedia ARTICLE topics should not add a parallel
  network stack.

---

## 6. Recommendation for Part C

The original Part C brief (PYQ ingestion + Wikipedia reasoning primers +
NCERT-derived practice questions) is still the right scope. Two nudges:

**Nudge 1 — reorder.** The Phase-2.5 research revealed two target
Wikipedia article titles don't exist (`Blood_relation`,
`Coding_decoding`). Resolve that first (either closest-match article or
in-house short primer) so the Reasoning chapter doesn't ship with
404-stubs. Treating this as discover-first will save a reseed cycle.

**Nudge 2 — `audit-questions.mjs` before any PYQ insert.** Build the
verifier, test it against a tiny hand-written fixture, then run the PYQ
pipeline. Phase 2.5's bugs came from seeding without verifying; the same
rule applies here and the user explicitly asked for the verifier up
front in the brief.

Proposed Part C execution order:

1. Build `tools/content-pipeline/src/audit-questions.mjs` + tiny fixture.
2. Wikipedia reasoning primers (20–30 articles). Hit MediaWiki
   `action=parse&prop=text&disableeditsection=1&disabletoc=1&redirects=1&formatversion=2`.
   Clean selectors already catalogued in research. Store `revid` with
   each article for later edit detection.
3. Topics schema addition (if needed) for `external_article_url` — check
   whether Phase 2 migration already has this column; if not, new
   migration.
4. Insert articles into catalogue under a new "Reasoning" chapter
   structure per subject layout.
5. PYQ ingestion — wait for user to supply 1-3 RRB PDF URLs, parse,
   route through `audit-questions.mjs`, seed as `kind='PYQ'`. Target
   100 Q.
6. (Stretch) NCERT-derived practice MCQs, 30–50 if time remains.
7. Final `audit-topics.mjs` run across all topics (regression guard for
   Phase 2.5 content), + `audit-questions.mjs` run on the whole question
   set. Both must return 0 issues.
8. `docs/PHASE_3_REPORT.md` — end-of-phase synthesis in Phase-2.5 style,
   including the CEN-06/2024 timing note (live exam cycle runs through
   2026-06-21) and that Phase 3 ships CBT-1 simulation, not paper
   reproduction.

Gate 3 deliverables per brief: both audit reports clean, Reasoning
chapter screenshot showing ≥20 Wikipedia articles, PHASE_3_REPORT.md.

**Readiness verdict: GO.** Part A + B contracts are stable. Part C is
content work on top of an already-verified engine. Nothing in Part A or
B should need to change to support it.

---

## 7. Current filesystem state (top-level map)

```
supabase/migrations/
  0001_profiles.sql          # Phase 1
  0002_hardening.sql         # Phase 1
  0003_learning.sql          # Phase 2
  0004_tests.sql             # Phase 3 — this phase
  (plus 5 MCP-tracked phase25_* content migrations)

domain/src/main/kotlin/com/railprep/domain/
  model/Tests.kt             # Phase 3
  repository/{Tests,Attempt}Repository.kt
  (plus Phase-1/2 Learning/Profile/Auth)

data/data-remote/.../supabase/
  TestsDto.kt                # Phase 3 wire DTOs + mappers

data/data-repository/.../
  {Tests,Attempt}RepositoryImpl.kt
  di/DataRepositoryModule.kt  (updated)

feature/feature-tests/
  src/main/kotlin/com/railprep/feature/tests/
    navigation/TestsRoutes.kt
    list/{TestsListViewModel, TestsTabBody}.kt
    instructions/{InstructionsViewModel, InstructionsScreen}.kt
    player/
      TestPlayerState.kt
      TestPlayerViewModel.kt
      TestPlayerScreen.kt
    results/{ResultsViewModel, ResultsScreen}.kt
    review/{ReviewViewModel, ReviewScreen}.kt
    offline/{AttemptLocal, AttemptLocalModule}.kt
    work/{AutoSubmitWorker, AutoSubmitScheduler}.kt
    diag/AttemptDiag.kt
  src/main/res/values{,-hi}/strings.xml

tools/
  content-pipeline/   (Phase 2 + 2.5, unchanged — Part C extends)
  verify/             (new in Phase 3)
    README.md
    phase3.mjs
    package.json + .env.example

docs/
  PHASE_2_REPORT.md
  PHASE_25_REPORT.md
  phase3-partA.md
  PHASE_3_STATUS.md     # this file
  phase3-evidence/      # 996 KB, 10 screenshots + logcat + script-results
```

---

_End of status report. Next action: planner decides go / pivot / pause on
Part C per §6._
