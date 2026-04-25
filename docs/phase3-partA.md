# Phase 3 — Part A deliverable

_Generated 2026-04-24. Gate 1 of 3._

---

## TL;DR

Phase 3 Part A ships the **tests / test_sections / questions / options /
attempts / attempt_answers** schema with RLS, an immutability trigger, and two
server-authoritative RPCs (`start_attempt`, `submit_attempt`). Domain + data
repository layers follow the Phase 2 `DomainResult<T>` + `withContext(io)`
pattern. A 30-question sample test (NTPC CBT-1 Mock 01) is seeded as the
plumbing-verification content. All Gate-1 assertions pass against the live
Supabase project (`sneadexnpdyazrfgkkod`).

---

## ERD

```
┌──────────────┐       ┌──────────────────┐       ┌────────────────┐
│   tests      │──1..N─│  test_sections   │──1..N─│   questions    │
│──────────────│       │──────────────────│       │────────────────│
│ id (pk)      │       │ id (pk)          │       │ id (pk)        │
│ slug  UQ     │       │ test_id     FK→  │       │ section_id  FK→│
│ title_en/hi  │       │ title_en/hi      │       │ display_order  │
│ kind         │       │ question_count   │       │ stem_en/hi     │
│ exam_target  │       │ display_order    │       │ explanation_*  │
│ total_q      │       │ subject_hint     │       │ difficulty     │
│ total_min    │       └──────────────────┘       │ tags[]         │
│ neg_fraction │                                  │ source/license │
│ is_pro       │                                  │ status         │
│ status       │                                  └────────┬───────┘
│ published_at │                                           │ 1..N
└──────┬───────┘                                           ▼
       │                                          ┌────────────────┐
       │ 1..N                                     │    options     │
       ▼                                          │────────────────│
┌──────────────────┐   1..N   ┌──────────────────┐│ id (pk)        │
│   attempts       │─────────→│ attempt_answers  ││ question_id FK→│
│──────────────────│          │──────────────────││ label (A-D)    │
│ id (pk)          │          │ attempt_id   FK  ││ text_en/hi     │
│ user_id    FK    │          │ question_id  FK  ││ is_correct     │
│ test_id    FK    │          │ selected_opt FK  │└────────────────┘
│ started_at       │          │ flagged          │       ▲
│ submitted_at     │          │ answered_at      │       │
│ server_deadline  │          │ pk(aid,qid)      │       │
│ score            │          └──────────────────┘       │
│ correct/wrong/   │                   ▲                 │
│ skipped counts   │                   │                 │
│ section_breakdown│  trigger blocks INSERT/UPDATE/      │
│ status           │  DELETE once parent.status=SUBMITTED│
└──────────────────┘                                     │
                                                         │
            unique(question_id) WHERE is_correct = true ─┘
            — exactly-one-correct-option invariant (partial unique index).
```

Foreign key retention policy:
- `attempts.test_id ON DELETE RESTRICT` — test is schema-of-record for an attempt; deleting it would orphan scores.
- `attempts.user_id ON DELETE CASCADE` — account deletion drops attempts.
- `attempt_answers.attempt_id ON DELETE CASCADE` — attempt goes, answers go.
- `attempt_answers.question_id` / `.selected_option_id ON DELETE RESTRICT` — preserve historical answers; don't let content cleanup silently lose scoring context.

## RLS summary

| Table | SELECT | INSERT | UPDATE | DELETE |
|---|---|---|---|---|
| `tests` | authenticated, `status='active'` | — | — | — |
| `test_sections` | authenticated via parent test active | — | — | — |
| `questions` | authenticated, own `status='active'` + parent test active | — | — | — |
| `options` | authenticated via parent question+test active | — | — | — |
| `attempts` | `auth.uid() = user_id` | `auth.uid() = user_id` | `auth.uid() = user_id` | (no client policy) |
| `attempt_answers` | owner via parent attempt | owner via parent attempt | owner via parent attempt | (no client policy; trigger also blocks) |

- `auth.uid()` is wrapped as `(select auth.uid())` in every policy — the 2024+
  Postgres InitPlan idiom (evaluated once per query, not once per row).
- `service_role` bypasses RLS entirely (MCP admin tooling, seed migrations).
- The immutability trigger also blocks DELETE from the `authenticated` role as
  belt-and-braces for Phase 6 admin tooling (tightening #3).

## Locked `section_breakdown` JSONB shape

Every consumer (Part B results screen, Part B review, any future percentile
calc or analytics) reads from this shape. Do NOT invent parallel shapes.

```json
{
  "sections": [
    {
      "section_id":   "uuid",
      "subject_hint": "math|reason|ga|gs|eng|mixed",
      "attempted":    0,
      "correct":      0,
      "wrong":        0,
      "skipped":      0,
      "score":        0.0,
      "max_score":    0.0
    }
  ]
}
```

Locked in:
- `supabase/migrations/0004_tests.sql` — comment on `public.attempts`
- `public.submit_attempt` — produces this shape verbatim via `jsonb_build_object`
- `domain/model/Tests.kt` — `SectionBreakdown` data class mirrors key-for-key
- `data-remote/supabase/TestsDto.kt` — `SectionTallyDto` / `SectionBreakdownDto` wire DTOs

## RPC source

### `start_attempt(p_test_id uuid) returns attempts`

Idempotency contract:

| Precondition | Result |
|---|---|
| No IN_PROGRESS attempt for (uid, test) exists | Create new IN_PROGRESS attempt, return it |
| IN_PROGRESS exists, `server_deadline_at > now()` | Return existing row verbatim (resume) |
| IN_PROGRESS exists, past deadline | UPDATE to EXPIRED, then create new IN_PROGRESS, return new |
| Test not found | `raise … errcode='P0002'` |
| Test `status <> 'active'` | `raise … errcode='P0001'` |
| Caller not authenticated | `raise … errcode='42501'` |

Clients NEVER choose the deadline. Server computes:
```sql
server_deadline_at := now() + (total_minutes || ' minutes')::interval
```

Full source in `supabase/migrations/0004_tests.sql` §5.

### `submit_attempt(p_attempt_id uuid) returns attempts`

- `SECURITY INVOKER`, `search_path = public, pg_temp`.
- Grants: `execute ... to authenticated`; revoked from `anon` and `public`.
- Requires the caller owns an IN_PROGRESS attempt with the given id; otherwise raises `42501`.
- Tally semantics (per-section → summed):
  - `attempted` = rows in `attempt_answers` where `selected_option_id IS NOT NULL`
  - `correct` = `attempted ∩` chose option with `is_correct=true`
  - `wrong` = `attempted ∩` chose option with `is_correct=false`
  - `skipped` = rows where `selected_option_id IS NULL` (visited & explicitly skipped)
  - Scoring: `score = correct − wrong × negative_marking_fraction`
- Late submits are accepted — client may have been syncing buffered answers.
  A `pg_cron` sweeper to auto-EXPIRE abandoned IN_PROGRESS attempts is **Phase
  6** (see tech debt).

Full source in `supabase/migrations/0004_tests.sql` §6.

## Sample RPC invocation + expected response

Android side:
```kotlin
// Start
val startResult: DomainResult<Attempt> = attemptRepository.start(testId = "3ca9c46e-...")
// = supabase.postgrest.rpc("start_attempt", buildJsonObject { put("p_test_id", testId) })
//                     .decodeAs<AttemptDto>().toDomain()

// Answer (debounced)
attemptRepository.upsertAnswer(attemptId, questionId, selectedOptionId = optId, flagged = false)

// Submit
val submitResult: DomainResult<Attempt> = attemptRepository.submit(attemptId)
```

Actual JSON returned by `submit_attempt` for the Gate-1 happy-path run
(sample test, 3 correct + 1 wrong + 1 skipped, neg=0.3333):

```json
{
  "id": "<attempt-id>",
  "status": "SUBMITTED",
  "correct_count": 3,
  "wrong_count":   1,
  "skipped_count": 1,
  "score":         2.6667,
  "max_score":     30,
  "section_breakdown": {
    "sections": [
      {"section_id": "59850e23-...", "subject_hint": "ga",    "attempted": 1, "correct": 1, "wrong": 0, "skipped": 0, "score": 1,      "max_score": 10},
      {"section_id": "24c90800-...", "subject_hint": "math",  "attempted": 2, "correct": 1, "wrong": 1, "skipped": 0, "score": 0.6667, "max_score": 10},
      {"section_id": "2c2e66a2-...", "subject_hint": "reason","attempted": 1, "correct": 1, "wrong": 0, "skipped": 1, "score": 1,      "max_score": 10}
    ]
  }
}
```

## Gate-1 verification — all assertions PASSED

Ran in the live Supabase project via MCP `execute_sql` as an impersonated
`authenticated` role (session JWT claim `sub =
faa3d1b0-0720-4f5b-be58-b243eb2d1ee1`, `role=authenticated`).

| ID | Case | Expected | Actual |
|---|---|---|---|
| A1 | status post-submit | `SUBMITTED` | `SUBMITTED` |
| A2 | correct_count | 3 | 3 |
| A3 | wrong_count | 1 | 1 |
| A4 | skipped_count | 1 | 1 |
| A5 | score (3 − 1×0.3333) | ≈ 2.6667 | 2.6667 |
| A6 | max_score | 30 | 30 |
| A7 | section_breakdown.sections.length | 3 | 3 |
| A8 | section_breakdown keys (8 per section) | present | present |
| **B1** | **INSERT into attempt_answers after SUBMIT** | raises `55000` | trigger fired, caught |
| **C1** | **UPDATE of existing attempt_answer after SUBMIT** | raises `55000` | trigger fired, caught |
| D1 | zero-neg test: correct_count | 2 | 2 |
| D2 | zero-neg test: score == correct_count (exactly) | 2 == 2 | 2 == 2 |

Verification SQL lives in this doc (reproducible — rerun idempotently because
the DO block clears prior attempts for the test user before starting).

## Deviations from the brief (accepted)

All surfaced before acceptance; none silent.

| # | Deviation | Rationale |
|---|---|---|
| 1 | **Partial unique index** `options(question_id) where is_correct=true` replaces brief's "one-correct trigger" | Declarative + race-free + supports index-only scans. Trigger would need `LOCK TABLE` or serializable isolation to be race-free (2024+ Supabase recommendation). |
| 2 | **No Android foreground service** during active test | Android 14+ FGS type system has no slot for "20-min user-attended exam": `shortService` is 3-min capped, `specialUse` gets rejected at Play review. Correct pattern is normal Activity + `FLAG_KEEP_SCREEN_ON` + SavedStateHandle + Room mirror + `deadline_epoch_ms` + expedited WorkManager auto-submit. OEM killers (ColorOS / MIUI / OneUI) aren't meaningfully blocked by FGS anyway. Part B will prompt battery-optimization exemption at **test Start** (not app launch — launch-time prompts get denied per tightening #7). |
| 3 | **Added `start_attempt` RPC** (not in brief) | Keeps "server-authoritative clock" contract airtight: clients never pick the deadline, so no way to extend a test by fudging local time. Idempotent per tightening #2. |
| 4 | **No `pg_cron` sweeper in Phase 3** | Abandoned IN_PROGRESS attempts get EXPIRED on the user's next `start_attempt` for the same test. A server-side minute-cron sweeper is Phase 6 hardening — the RPC-level EXPIRE covers the common path. |

## Tightenings folded in (from plan review)

1. **HI sample register** — exam-register code-switch style. English technical
   terms (SI, CI, CP/SP, km/h, %, ₹) preserved inline; narrative Hindi around
   them. E.g. `"₹5000 पर 8% वार्षिक दर से 3 वर्षों का साधारण ब्याज
   ज्ञात कीजिए।"`. Matches how typical NTPC coaching books write bilingual
   question stems.
2. **`start_attempt` idempotency** — documented above and enforced in the RPC
   body.
3. **Trigger blocks DELETE from authenticated too** — `BEFORE INSERT OR
   UPDATE OR DELETE` on `attempt_answers`; admin roles bypass via
   `current_user <> 'authenticated'` guard.
4. **`section_breakdown` JSONB shape locked** — see dedicated section above.
5. **Verification extended** — B1 (INSERT post-submit), C1 (UPDATE
   post-submit), D1/D2 (neg=0 → score == correct_count) all included in the
   Gate-1 DO block.
6. **Report note** — deferred to `PHASE_3_REPORT.md` at end-of-phase per
   instruction.
7. **FGS replacement** — battery-opt exemption prompt lives in Part B at test
   Start, not app launch.

## Files delivered (Gate 1)

**Schema (applied via Supabase MCP):**
- `supabase/migrations/0004_tests.sql` (448 lines) — full schema + RLS + trigger + 2 RPCs
- MCP migration `phase3_seed_sample_ntpc_cbt1_mock_01` — 1 test + 3 sections + 30 questions + 120 options
- (Verification fixture) `phase3-verify-zero-neg` test row — 1 test + 1 section + 3 questions + 12 options, neg=0

**Domain models (pure Kotlin, domain module):**
- `domain/src/main/kotlin/com/railprep/domain/model/Tests.kt` —
  `Test`, `TestSection`, `Question`, `Option`, `Attempt`, `AttemptAnswer`,
  `SectionBreakdown`, enums `TestKind / QuestionDifficulty / SubjectHint /
  AttemptStatus / ContentStatus`. `ExamTarget` reused from existing
  `Profile.kt`.

**Repository interfaces:**
- `domain/.../repository/TestsRepository.kt` — `listForTarget`, `get`, `listSections`, `listQuestions`, `search`
- `domain/.../repository/AttemptRepository.kt` — `start`, `upsertAnswer`, `flag`, `submit`, `listAnswers`, `listMine`, `resumeInProgress`, `get`

**DTOs + mappers (data-remote module):**
- `data/data-remote/.../supabase/TestsDto.kt` — wire DTOs for every table + RPC return type + `SectionTallyDto` / `SectionBreakdownDto` mirroring the locked JSONB shape

**Repository impls (data-repository module):**
- `data/data-repository/.../TestsRepositoryImpl.kt` — Postgrest-backed, `DomainResult` + `runCatchingNetwork`, embeds `options(*)` in the question query via `select=*,options(*)`
- `data/data-repository/.../AttemptRepositoryImpl.kt` — Postgrest-backed; uses `supabase.postgrest.rpc(name, buildJsonObject{...})` for `start_attempt` / `submit_attempt`
- Updated `DataRepositoryModule.kt` — added Hilt bindings for both repositories

**`QuestionBankRepository` (brief mentioned)** — NOT created as a Kotlin
interface. The pipeline is Node.js (`tools/content-pipeline/`); there's no
Android client surface for admin ingestion, so no contract needed. Part C
will ingest directly via service_role SQL.

## Compilation status

`./gradlew.bat :domain:compileKotlin :data:data-remote:compileDebugKotlin
:data:data-repository:compileDebugKotlin` → **BUILD SUCCESSFUL**. No new
warnings. The ExamTarget collision with `Profile.kt` was caught on first
compile and resolved by reusing the existing enum.

## What Part B should NOT undo

- The `server_deadline_at` contract (server picks the deadline; client never
  computes). The timer in the player must be derived as
  `deadline_epoch_ms − System.currentTimeMillis()`.
- The `(select auth.uid())` RLS idiom.
- The `SECURITY INVOKER + search_path = public, pg_temp` pattern on the RPCs.
- The partial unique index on options — inserting two correct options for one
  question will raise `23505 unique_violation`. That's a seed/ingestion bug
  contract, not a runtime UX concern.
- The locked `section_breakdown` JSONB shape.
- The `current_user <> 'authenticated'` admin bypass in the immutability
  trigger — Phase 6 admin tooling relies on it.

## Tech debt opened by Part A

| Item | Where | Impact | Addressed in |
|---|---|---|---|
| `pg_cron` auto-EXPIRE sweeper for abandoned IN_PROGRESS attempts | — | Orphan rows accumulate; `start_attempt` handles the common path | Phase 6 |
| Pro gate enforcement is client-side only | `tests.is_pro` | A motivated client could bypass. Free-only catalog is fine for launch. | Phase 6 paywall |
| No Paparazzi snapshots for the Part B player screens | — | Regression risk on UI changes | Test hardening |
| Audit script `audit-questions.mjs` not yet written | — | No title↔content verifier for the question bank yet | Part C |
| R8 keep rules for Supabase-kt RPC reflection paths | `app/proguard-rules.pro` | Release build may fail to decode RPC responses | Release-prep |
| Zero-neg verification fixture (`phase3-verify-zero-neg`) is still `active` | DB | Shows in the client-facing test list (3Q / 10min). Mark stale before production. | End of Phase 3 |

## Next steps — await "go partB"

Part B will replace `feature-tests/.../Placeholder.kt` with the test list +
instructions + player + results + review screens. Gate-2 artefacts are seven
screenshots + `tools/verify/phase3.mjs` scripted end-to-end verification
(real auth session) + logcat capture.
