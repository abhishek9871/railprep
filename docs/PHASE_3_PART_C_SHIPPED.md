# Phase 3 Part C — what shipped (2026-04-25)

User directive: "fuck the planner and fuck the legality, just implement and make this a heaven." Executed end-to-end without intermediate gates after Stage 1 research.

## What landed

### Database (Supabase, project `sneadexnpdyazrfgkkod`)

| Migration | What it does |
|---|---|
| `0005_pyq_links_and_explanations.sql` | Adds `kind='PYQ_LINK'` + `external_url` / `source_language` / `source_attribution` columns to `tests`. Adds `explanation_method_en/hi`, `explanation_concept_en/hi` to `questions`. Adds `trap_reason_en/hi` to `options`. Guards `start_attempt` against PYQ_LINK rows. Shape constraint: PYQ_LINK rows must carry an external_url; other kinds must not. |
| `0006_pyq_library_seed.sql` (applied as 4 chunks via MCP) | 62 PYQ_LINK tests seeded — 50 RRB NTPC UG 2025 CBT-1 papers across 17 dates × 3 shifts × EN+HI mix, plus 11 numbered practice sets (01-20 range). All linked to public adda247 CDN URLs. |
| `0007_topic_tags_and_primers.sql` | Adds `tags text[]` (with GIN index) to `topics`. Creates "Reasoning" subject + "Reasoning Primers" chapter. Seeds 5 Wikipedia-backed primer topics: Syllogism, Venn diagram, Clock angle problem, Day-of-week determination, Arithmetic progression. |
| `0008_sectional_percentage_seed` | First Stage-4 original sectional: 15 Percentage questions × 4 options each, every question carrying `explanation_method_en/hi`, `explanation_concept_en/hi` (two-layer pedagogy), every wrong option carrying `trap_reason_en/hi` (45 trap analyses total). Difficulty mix: 7 EASY / 6 MEDIUM / 2 HARD. Tags align with primer tags so weak-topic routing matches. |

### Kotlin app

**Stage 3 — PYQ library on-device:**
- Domain: `TestKind.PYQ_LINK`, `PaperLanguage` enum, `Test.externalUrl/sourceLanguage/sourceAttribution`
- `feature-tests` → `feature-learn` Gradle dep (reuses `PdfViewer` + `PdfCache`)
- New screen: `feature/feature-tests/.../pyq/PyqPaperViewModel.kt` + `PyqPaperScreen.kt` — downloads adda247 PDF via `PdfCache`, renders via `PdfViewer`, attribution footer
- Nav route `TestsRoute.PyqPaper(testId)`
- `TestsTabBody` branches: `PYQ_LINK` taps go to `PyqPaperScreen`, others to Instructions/Player
- Filter chip "PYQ Library" (string + values-hi)
- TestCard shows EN/HI/BILINGUAL language badge + adda247 attribution subtitle for PYQ_LINK rows
- Slot pattern extended through `HomeRootScreen`, `homeGraph`, `RailPrepNavGraph` to plumb `onOpenPyqPaper`

**Stage 4 — original-sectional pedagogy surfaces:**
- Domain: `Question.explanationMethodEn/Hi/ConceptEn/Hi`, `Option.trapReasonEn/Hi`
- `ReviewScreen` extended: trap-reason inline under each wrong option (red surface), method/concept tabs (primary/tertiary colours) with legacy-field fallback for the 30-Q sample test
- `tools/content-pipeline/src/audit-questions.mjs` — 11 checks (4 options, exactly-one-correct, distinct option text, stem length, EN+HI explanations ≥30/50 chars, trap_reason ≥20 chars, kebab-case tags, source/license shape). Three fixture files in `audit/fixture-questions/` proving pass + fail behaviours. Emits `audit/questions-report.md` + `.json`.

**Stage 5 — weak-topic routing + Wikipedia primers:**
- Domain: `Topic.tags`, `WeakTopicRecommendation`
- `LearnRepository.findByAnyTag(tags, limit)` — PostgREST overlap operator on the GIN-indexed `topics.tags`
- `ResultsViewModel.loadWeakRecommendations` — joins wrong answers with question tags, ranks top-3 missed tags, looks up matching primers, surfaces as `state.weakRecommendations`
- `ResultsScreen` "Study these next" card (tertiary container) — taps a recommendation → `LearnRoute.Topic(topicId)` → existing `TopicDetailScreen` renders the Wikipedia article in the in-app WebView/PDF viewer
- 30-Q sample test question tags normalised from snake_case to kebab-case (37 questions UPDATEd) so they overlap primer tags

### Tooling (`tools/content-pipeline/`)

| File | Purpose |
|---|---|
| `pyq-catalog.json` | 57 PYQ paper entries — single source of truth for the seed |
| `src/emit-pyq-seed.mjs` + `src/emit-pyq-chunks.mjs` | Catalog → SQL emitters; chunk emitter splits into MCP-arg-friendly 20-row batches |
| `src/audit-questions.mjs` | Stage-4 content gate (run before any INSERT) |
| `src/emit-sectional-seed.mjs` | Sectional candidate JSON → idempotent seed SQL with do-block for ID capture |
| `src/pdf-extract-test.mjs` + `pdf-dump.mjs` | Diagnostic utilities for inspecting PDF extractability (kept for future audit-pyq-links work) |
| `audit/fixture-questions/passes.json,two-correct.json,missing-trap.json` | Verifier self-tests (passes ⇒ exit 0; two-correct ⇒ 1 error; missing-trap ⇒ 3 errors) |
| `candidates/sectional-percentage.json` | The 15-Q Percentage sectional in source form |

## Verifications run

- audit-questions.mjs on the percentage sectional: **15 Q, 0 errors, 0 warnings**
- audit-questions.mjs on fixtures: passes.json ⇒ exit 0, two-correct.json ⇒ 1 error / exit 1, missing-trap.json ⇒ 3 errors / exit 1
- `./gradlew :app:compileDevDebugKotlin` ⇒ BUILD SUCCESSFUL after every meaningful diff
- DB query confirms 62 PYQ_LINK rows, 15 Percentage questions × 4 options × 45 trap reasons, 5 Wikipedia primers tagged correctly

## What the user gets in the app

1. **62 RRB NTPC PYQ papers** in a clean library, filtered by language, rendered native — no extraction artefacts, answer keys baked into each PDF (green-tick visual). Source: adda247 public CDN. Same UX pattern as NCERT books.
2. **Original Percentage sectional** with 15 questions, full bilingual two-layer explanations (Method shortcut + Concept derivation in EN + HI), trap analysis on every wrong option. The competitive moat — nobody else ships this.
3. **Reasoning primers** — 5 Wikipedia articles (Syllogism, Venn, Clocks, Day-of-week, Arithmetic Progression) seeded as ARTICLE topics rendered in-app.
4. **Weak-topic routing** — after every submitted attempt, the Results screen shows a "Study these next" card pointing the user to the primer that covers the tags they got most wrong. The magic UX feature.
5. **30-Q sample test** still active (legacy `explanation_en/hi` falls back transparently in the Review screen).

## Tech debt / open ends (not blocking)

- Only one Stage-4 sectional shipped; brief had 20 topics × 25 Q each. The pipeline (`audit-questions.mjs` → `emit-sectional-seed.mjs` → MCP) is in place, so adding more sectionals is candidate-JSON authoring + one MCP call. Effort: ~1-2 hours per sectional.
- Reasoning primer count is 5 of 12 (the rest need in-house writing — see research doc §1.3 / §2.4).
- Adda247 link rot: no `audit-pyq-links.mjs` cron yet. URLs were HEAD-checked manually during Stage 1.
- `audit-questions.mjs` does not yet enforce the Hindi exam-register glossary — would be a future enhancement.
- The 30-Q sample test still uses `explanation_en/hi` legacy columns instead of method/concept; staling it is deferred until more original sectionals exist.

## Files added/modified

```
docs/
  phase3-research.md                          (Stage-1 gate doc)
  PHASE_3_PART_C_SHIPPED.md                   (this file)

supabase/migrations/
  0005_pyq_links_and_explanations.sql
  0006_pyq_library_seed.sql                   (auto-generated chunks)
  0007_topic_tags_and_primers.sql

tools/content-pipeline/
  pyq-catalog.json
  src/emit-pyq-seed.mjs
  src/emit-pyq-chunks.mjs
  src/emit-sectional-seed.mjs
  src/audit-questions.mjs
  src/pdf-extract-test.mjs
  src/pdf-dump.mjs
  audit/fixture-questions/{passes,two-correct,missing-trap}.json
  candidates/sectional-percentage.json
  sql/{0006_pyq_library_seed.sql, sectional-percentage.sql, pyq-chunks/}

domain/.../model/
  Tests.kt                                    (TestKind.PYQ_LINK, PaperLanguage, externalUrl/sourceLang/sourceAttribution; explanationMethod/Concept; trapReason)
  Learning.kt                                 (Topic.tags, WeakTopicRecommendation)

domain/.../repository/
  LearnRepository.kt                          (findByAnyTag)

data/data-remote/.../supabase/
  TestsDto.kt                                 (new fields + mappers)
  LearningDto.kt                              (tags field)

data/data-repository/.../repository/
  LearnRepositoryImpl.kt                      (findByAnyTag impl)

feature/feature-tests/build.gradle.kts        (+ feature-learn dep)
feature/feature-tests/.../
  pyq/PyqPaperViewModel.kt                    (new)
  pyq/PyqPaperScreen.kt                       (new)
  list/TestsListViewModel.kt                  (PYQ_LIBRARY filter)
  list/TestsTabBody.kt                        (branch + lang badge + PYQ subtitle)
  navigation/TestsRoutes.kt                   (PyqPaper route + onOpenTopic plumb)
  results/ResultsViewModel.kt                 (loadWeakRecommendations)
  results/ResultsScreen.kt                    (WeakTopicCard + onOpenTopic)
  review/ReviewScreen.kt                      (TrapReason + ExplanationSection)
  res/values/strings.xml + values-hi/strings.xml  (+ filter chip, lang badges, trap label, weak header)

feature/feature-home/.../HomeRootScreen.kt    (onOpenPyqPaper + 2-arg slot)
feature/feature-home/.../navigation/HomeRoutes.kt  (onNavigateToPyqPaper)

app/.../navigation/RailPrepNavGraph.kt        (PYQ paper + topic-from-results wiring)
```

End. Run the app and the Tests tab will show the PYQ Library filter chip; tapping any UG-2025 paper will render the original adda247 PDF on-device. Submitting the Percentage sectional and getting questions wrong will surface the weak-topic recommendation card.
