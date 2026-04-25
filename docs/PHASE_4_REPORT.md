# RailPrep — Phase 4 final report

Written 2026-04-25 after D4 implementation, content cleanup, and regression gates.

---

## TL;DR

Phase 4 shipped the daily habit loop and question-bank scale-up: Daily Digest with streaks, opt-in local notifications, 5 new 25-Q sectionals, 4 reasoning primers, and D4 question bookmarks surfaced from Review and Profile. The content pipeline is stricter than at the start of the phase: `audit-questions.mjs` now blocks/warns on trap-reason filler language and warns when numeric explanations do not end near the marked answer. The legacy percentage trap-reason debt that produced the 7 FILLER warnings is fixed in DB and source candidates; DB cleanup is tracked as migration `0014_percentage_trap_reason_cleanup`.

D4 code compiles and the question/content audit gates are clean. A targeted post-fix device reinstall for the Saved Question note-save patch could not run because Gradle reported no connected devices. Earlier on-device D4 flow verified Profile row ordering, saved-question list labels, detail remove, filters, pull-to-refresh, and bookmark add/remove logging; the note-save interaction bug found during that pass was patched after the fact.

---

## Delivered

### D1 — Daily Digest + streaks

- Lazy `ensure_today_digest()` SECURITY DEFINER path creates a 10-Q daily mix on demand.
- `submit_digest` server-scores the digest and writes attempt outcome.
- Streak state lives on `profiles.streak_current`, `profiles.streak_best`, `profiles.last_study_date`.
- `streak_events` audit table records streak mutation history.
- Digest UX is opt-in by use: notification prompt only appears after first digest submit.

### D2 — local notifications

- No FCM. Notifications use `AlarmManager` plus WorkManager `PeriodicWorkRequest`.
- `profiles.notifications_enabled` defaults false.
- Runtime notification permission and battery/OEM constraints are handled in-app.
- WorkManager remains background work only, not foreground-service based.

### D3 — content scale

- Added 5 sectionals: Profit & Loss, Time/Speed/Distance, Ratio & Proportion, Averages, Syllogism.
- Added 125 original sectional questions and 500 options.
- Added 4 in-house reasoning primers: Blood Relations, Coding-Decoding, Seating Arrangement, Direction Sense.
- Migration `0011_inhouse_primers` added `ORIGINAL` license enum value and `topics.content_md`.
- `audit-questions.mjs` gained `FILLER_TRAP_RE` warning for vague trap reasons.
- `emit-compact-seed.mjs` added for MCP-size-safe compact data emits.

### D4 — question bookmarks

- Migrations:
  - `0012_question_bookmarks`: `question_bookmarks` table, `(user_id, question_id)` PK, recent-user index, RLS with `(select auth.uid())`, UPDATE policy includes `WITH CHECK`.
  - `0013_question_bookmarks_question_idx`: FK-support index on `question_id`.
  - `0014_percentage_trap_reason_cleanup`: content cleanup for legacy percentage trap reasons.
- Domain/data:
  - `QuestionBookmark` domain model.
  - `QuestionBookmarkRepository` with `list`, `listStates`, `add`, `remove`, `updateNote`, `isBookmarked`, all returning `DomainResult`.
  - Supabase DTOs + repository implementation using PostgREST, `runCatchingNetwork`, `withContext(dispatchers.io)`.
- Review:
  - Bookmark icon per question card.
  - Single bookmark-state load via `listStates(questionIds)`.
  - Optimistic add/remove with revert-on-failure.
  - Long-press sheet with Save toggle, note, Save/Remove.
- Profile:
  - Existing Learn-topic Bookmarks row kept first.
  - New Saved Questions row second, with count badge.
- Saved Questions:
  - New list screen under Profile with newest-first ordering, source labels, dates, note previews, pull-to-refresh.
  - Filter chips: All / With note / Sectionals / PYQ / Daily Digest.
  - Detail screen shows full question, options, correct highlight, trap reasons, method/concept tabs, editable note, remove.
  - Note-save interaction patched with IME Done action, `imePadding`, and focus clearing before Save/Remove.

---

## Patterns Established

- **Question quality gate:** 4 options, exactly 1 correct, bilingual stem/options, bilingual trap reasons, method + concept explanations, difficulty/tags/source/license validated before seed.
- **Trap reasons are forensic:** wrong options must name the exact arithmetic or conceptual slip. Vibe words like rough estimate, gut feel, estimated, approximation, rounded, guessed are audit warnings.
- **Explanation-answer consistency:** numeric correct options are checked against the final 200 chars of method/concept explanations. This caught the Q6/Q12 avg-20-class bugs before D4.
- **Candidate emit pipeline:** author candidate JSON, audit locally, emit compact SQL for MCP, then verify counts/audits.
- **Weak-topic routing:** section/test results carry enough topic/tag data to route weak areas back into Learn/Practice surfaces.
- **Bookmark state loading:** Review screens batch-fetch bookmark state once per screen, never per card.

---

## Locked Contracts

- `attempts.section_breakdown` remains the fixed JSONB schema from migration `0004`.
- `server_deadline_at` remains server-authoritative from `start_attempt()`.
- `options(question_id) WHERE is_correct = true` remains the one-correct-option invariant.
- `reject_if_attempt_submitted` immutability trigger remains intact with admin bypass.
- RLS policies continue using `(select auth.uid())`, not bare `auth.uid()`.
- YouTube WebView and PDF cache/viewer stacks were not refactored.
- `AutoSubmitWorker` keeps delayed WorkManager semantics; no expedited delayed work.
- Repositories return `DomainResult<T>` across the domain boundary.
- EN + HI strings are required for new user-facing text.
- Content goes stale or is updated in place; no destructive content deletes.
- Service role key was not written to disk; DB writes went through Supabase MCP.

---

## Verification

- `node src/audit-questions.mjs --candidates ...sectional*.json`: 140 Q, 0 errors, 0 warnings.
- DB FILLER regex check over `public.options`: 0 remaining matches.
- `node src/audit-topics.mjs`: 118/118 MATCH, 0 errors.
- `.\gradlew.bat :app:compileDevDebugKotlin`: BUILD SUCCESSFUL.
- `.\gradlew.bat :app:installDevDebug`: failed after compile/package because no device was connected (`No connected devices!`).

Evidence files:
- `docs/phase4-evidence/d4-logcat.txt` contains saved-detail edit/remove log lines from the on-device D4 pass.

---

## Tech Debt

- D4 targeted post-patch device verification is still pending because the OPPO device disconnected before install. The note-save bug is patched and compile-verified, but should be rechecked once the device is visible to Gradle/adb.
- `docs/phase4-evidence/d4-logcat.txt` currently has edit/remove evidence only; the earlier add lines were visible in logcat during the session but were lost before the final evidence file was assembled.
- TSD sectional keeps the accepted 4E/16M/5H skew instead of the 8E/12M/5H target.
- Legacy percentage trap debt is fixed now; future cleanup should scan for filler-adjacent wording outside the current regex, not only exact regex hits.

---

## Phase 5 Scope Proposal

1. Search across question bank: stem/options/explanations/tags, with filters for source, section, difficulty, bookmarked, and wrong-history.
2. Per-topic accuracy heatmap: aggregate attempts by tag/topic, expose weak areas in Profile and Results.
3. Remaining sectionals to round out syllabus: Number System, Simplification, SI/CI, Algebra, Geometry/Mensuration, DI, Calendar/Clock, Series, Analogy, Classification, Venn, Non-verbal basics.
4. Adda247 link-rot watchdog: scheduled checker for PYQ PDF URLs, status report, stale marking path.
5. Bookmark polish: batch note edit reliability recheck on device, search/filter inside Saved Questions, and optional export/share later.
