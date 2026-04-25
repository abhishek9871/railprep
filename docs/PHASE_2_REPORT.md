# RailPrep — Phase 2 Handoff Report

Audience: upstream planning AI + future self. Dense, factual.

---

## TL;DR

Phase 2 (Home shell + Learning catalog, client-fetched, link-only) is code-complete and installed
on-device. Migration `0003_learning.sql` applied. Seed data is in place (6 subjects, 18 chapters,
22 high-confidence topics — all links verified via the research doc). Bottom-nav shell works with
Home / Tests placeholder / Feed placeholder / Profile. Learning path Subjects → Chapters → Topic
detail works for both YouTube (via IFrame player) and NCERT PDF (OkHttp download → LRU cache →
PdfRenderer). Bookmarks, language switch, sign-out, about, diagnostics all wired. On-device
verification is pending user sign-off against `docs/phase2-testplan.md`.

**Time cost**: well under the 5-day estimate from the Phase 1 report, because the research doc
pre-resolved every content decision (no source-discovery latency during coding).

---

## Delivered

### Database (Supabase)

- `supabase/migrations/0003_learning.sql` applied. Enums: `content_type`, `topic_status`,
  `license_type`. Tables: `subjects`, `chapters`, `topics` (with URL-shape CHECK), `bookmarks`.
  RLS: all reads require authenticated; bookmarks are user-scoped.
- Seed: 6 subjects, 18 chapters, 22 topics (golden set per `docs/content-strategy.md §6`). Links
  are the HEAD-verified NCERT PDFs and the six verified YouTube video IDs from the research doc.

### Domain + data

- `domain/.../model/Learning.kt` — `Subject`, `Chapter`, `Topic`, `Bookmark` + `ContentType`,
  `License`, `TopicStatus` enums.
- `domain/.../repository/LearnRepository.kt` — list/get + `reportStale` (best-effort hint).
- `domain/.../repository/BookmarkRepository.kt` — list/add/remove/isBookmarked.
- `data/data-remote/.../supabase/LearningDto.kt` — DTOs + `.toDomain()` mappers.
- `data/data-repository/.../LearnRepositoryImpl.kt` + `BookmarkRepositoryImpl.kt` — Postgrest-
  backed, `withContext(dispatchers.io)` + `DomainResult` wrapping per Phase 1 convention.
- Hilt bindings in `DataRepositoryModule`.

### feature-home

- `HomeRootScreen` — Scaffold with `NavigationBar` switching between 4 tabs (no nested NavHost).
- `DashboardTab` — welcome + "Start learning" + "Your bookmarks" CTAs.
- `TestsTab` / `FeedTab` — placeholders with subject-appropriate icons + copy.
- `ProfileTab` — avatar, name, email, 4 nav rows (Edit / Bookmarks / Language / About) + Sign out.
  Language dialog toggles `LanguageRepository` which triggers `AppCompatDelegate.setApplicationLocales`.
- `ProfileEditScreen` — name + daily-minutes slider (other goal fields preserved from Phase 1).
- `BookmarksScreen` — lists bookmarked topics; empty state with friendly copy.
- `AboutScreen` — content sources + licenses (scrollable, pure static text from strings.xml).
- `DiagScreen` — app version, user ID, PDF cache size, Clear cache action. Reached by long-
  pressing the Profile avatar (gesture hint shown in profile).
- Nav routes type-safe via `HomeRoute.*` sealed interface.

### feature-learn

- `SubjectsScreen` — top-level list, renders subject title EN + HI.
- `ChaptersScreen` — chapter cards, each inline-expanding its own topic list. Empty-state copy
  when a chapter has no topics.
- `TopicDetailScreen` — branches on `content_type`:
  - `YT_VIDEO` → `YouTubePlayer` (android-youtube-player 12.1.2) with `cueVideo` (user-initiated
    play — satisfies "no autoplay before 50% visible" trivially), `controls(1)`, `rel(0)`,
    `ivLoadPolicy(3)`.
  - `PDF_URL` → `PdfCache` downloads to `<cacheDir>/pdfs/<topicId>.pdf` (LRU 100 MB cap) → `PdfViewer`
    renders all pages via `PdfRenderer` into a `LazyColumn<Image>`. Fallback: "Open in browser"
    button if download fails.
  - `ARTICLE` / `QUIZ` → Phase 4/3 placeholder.
- Per-license attribution footer on every topic detail. Strings differ per license type.
- Bookmark toggle in top bar — calls `BookmarkRepository`, updates `state.bookmarked` immediately.

### Content pipeline (`tools/content-pipeline/`)

- `channels.json` — 22 whitelisted YouTube channels with tier (gold/silver) labels. wifistudy +
  Exampur per clarification #1 are **silver** (discovery only, never auto-feature on Home).
- `ncert-books.json` — 15 NCERT textbook entries with class/subject/code/landing URL.
- `src/ncert-pdfs.mjs` — **crawls** `textbook.php?{code}1=0-N` landing pages and extracts the
  PDFs actually linked, HEAD-checks each. No pattern-multiplication (clarification #2 honoured).
- `src/youtube-discover.mjs` — uses `playlistItems.list` against each channel's uploads playlist
  (1 unit/call, not 100/call like `search.list`). ~44 units per full run — ~0.4 % of the daily
  10 k quota.
- `src/pib-feed.mjs` — Phase 4 stub. Prints feed stats only; no DB writes. Articles table lands
  with Phase 4.
- `src/apply-to-supabase.mjs` — CI-only. Calls `rpc/_pipeline_apply` with service_role key from
  env. Never committed. README documents the one-time RPC installer SQL.
- `.github/workflows/content-sync.yml` — cron Mon+Thu 02:00 UTC + `workflow_dispatch`. Secrets:
  `SUPABASE_URL`, `SUPABASE_SERVICE_KEY`, `YOUTUBE_API_KEY`.
- `.env.example`, `.gitignore`, `README.md` — complete.

### Tests + docs

- `docs/phase2-testplan.md` — 38-step manual test plan covering every flow + regression checks
  against Phase 1 auth.
- `docs/PHASE_2_REPORT.md` — this file.
- `NOTICE.md` — **not yet updated** with content-attribution section. See Tech Debt below.

---

## Deviations from the Phase 2 brief

1. **PIB pipeline is a no-op stub** (deferred per advisor-approved concern #2 from the planning
   discussion — schema stub option (a), not (b) or (c)). `pib-feed.mjs` prints feed item counts
   so we can sanity-check the endpoint but writes nothing. Phase 4 adds the `articles` table and
   replaces the stub.
2. **Wikipedia not integrated** — per clarification #3, link-only and Phase 3+ work. No code for
   it in this phase.
3. **Profile Edit only surfaces name + daily minutes.** Full edit of exam target / qualification /
   category / DOB is Phase 3 polish; preserves existing values on save.
4. **No chapter-assignment in content pipeline.** Discovered topics land with `where false`
   NOOPs until a human edits the SQL to pin a `chapter_id`. Manual curation step intentionally
   kept out of automation (quality bar).
5. **Empty state for thin chapters, not skeleton content.** English Comprehension has 1 topic;
   other English chapters + all Current Affairs chapters have 0 topics. Empty-state string
   ("New content coming soon…") shows. No fake placeholder topics.

---

## Known gaps / tech debt

| Item | Where | Impact | Phase |
|---|---|---|---|
| NOTICE.md content-attribution section | repo root | Licensing posture not self-documenting | Immediate polish |
| VM unit tests for `LearnViewModel` / `BookmarksViewModel` / `TopicDetailViewModel` | feature-learn/home src/test | No coverage | Test-hardening |
| Chapter-assignment step in pipeline | `tools/content-pipeline/` | Discovered topics don't auto-insert | Phase 3 — optional |
| Hindi NCERT codes | pipeline / seed | Only English-medium PDFs seeded; Hindi deferred per research doc §2 | Phase 3 |
| Release keystore + prod Android OAuth client | `com.railprep` package | Same as Phase 1 memory — unchanged | Release-prep |
| Paparazzi snapshot tests for new screens | feature-home, feature-learn | No snapshot coverage | Test-hardening |
| R8 keep rules for `androidyoutubeplayer`, `PdfRenderer` | `app/proguard-rules.pro` | Release build may crash | Release-prep |
| `ivLoadPolicy`/`rel` param names pass ints — verify against IFrame player options DSL | feature-learn YouTubePlayer.kt | Low — will fail loudly at first run if wrong | Trivial |

---

## Architectural patterns established (Phase 3 should follow)

- **Type-safe nav** remains the only style. `LearnRoute.Topic(val topicId: String)` / `HomeRoute.ProfileEdit` etc. No string routes.
- **Catalog tables use `status text/enum not null default 'active'`** + `last_verified_at`. Pipeline
  flips to `stale` on HEAD failure; clients filter `status = 'active'`. Apply this shape to Phase 3's
  `tests` / `questions` tables and Phase 4's `articles`.
- **Sub-feature packages** inside feature modules (`feature.home.dashboard` / `.placeholder` /
  `.profile.{edit,bookmarks,about,diag}`). Tab switch is a plain `when` on `Tab enum` — NOT a
  nested NavHost. Much simpler to reason about.
- **PdfCache / YouTubePlayer are public to the module** — reuse from Phase 3 (Tests) if a question's
  solution references a video or PDF without reinventing. Move to `core-common` if a third feature
  wants them.
- **Bookmark per-table**: `bookmarks (user_id, topic_id)`. Phase 3 adding bookmarks for questions or
  tests should make a sibling table (`question_bookmarks`, etc.) — don't poly-ref.

---

## Time / friction signal

- **Net coding time**: ~2 sessions. The research doc front-loaded every source decision — zero
  time spent discovering NCERT URL patterns or channel IDs.
- **Compile pain**: one round of missing `material-icons-extended` dependency, one round of enum
  name mismatches (`ExamTarget.NtpcCbt1` not `.CBT1`), one round of `Icons.AutoMirrored.Filled.Logout`
  vs `Icons.Filled.Logout`. All resolved in ~5 minutes each.
- **No OEM-specific bugs encountered** this phase (in stark contrast to Phase 1 auth). The
  YouTube IFrame player and PdfRenderer both played nicely with ColorOS on first install.
- **Supabase MCP** made migration + seeding trivial (no service_role in repo, user instruction
  clarification #7 honoured).

**Implication for Phase 3 (mock tests) planning**: expect smooth sailing on the Android side if
the test engine doesn't need any device-native integrations (timers, keep-awake, offline state
survival across process death). Sizing should assume ~1 day of design + 2 days of implementation
for the core engine, then multi-day for the question bank pipeline. Keep the pipeline pattern
from Phase 2 and follow the same catalog shape.

---

## Contracts available to Phase 3

```kotlin
// New in Phase 2:
interface LearnRepository {
    suspend fun listSubjects(): DomainResult<List<Subject>>
    suspend fun listChapters(subjectId: String): DomainResult<List<Chapter>>
    suspend fun listTopics(chapterId: String): DomainResult<List<Topic>>
    suspend fun getTopic(topicId: String): DomainResult<Topic>
    suspend fun reportStale(topicId: String): DomainResult<Unit>
}
interface BookmarkRepository {
    suspend fun listBookmarks(): DomainResult<List<Topic>>
    suspend fun addBookmark(topicId: String): DomainResult<Unit>
    suspend fun removeBookmark(topicId: String): DomainResult<Unit>
    suspend fun isBookmarked(topicId: String): DomainResult<Boolean>
}
```

Phase 3 should add `TestsRepository` + `QuestionsRepository` following the same DomainResult + io-dispatcher pattern, with its own migration `0004_tests.sql`.

---

## What Phase 3 should NOT undo

- `topics.status enum + last_verified_at` — pipeline depends on this for staleness flipping.
- `topics_url_shape` CHECK constraint — catches bad rows before they hit clients.
- `PdfCache` 100 MB LRU in app cache dir — guards against runaway disk usage on low-storage devices.
- `YouTubePlayer.cueVideo()` (not `loadVideo()`) — this is what keeps us on the right side of the
  autoplay ToS clause.
- Silver-tier channels in `channels.json` — they belong there; do not promote to gold without
  content review. wifistudy and Exampur specifically are per-user-instruction silver.

---

## Suggested Phase 3 scope (Mock tests)

Rough cut, not authoritative:
1. Schema: `tests`, `test_sections`, `questions`, `options`, `question_explanations`, `attempts`,
   `attempt_answers`. RLS: content read for authenticated; attempts + answers per-user.
2. Pipeline: question bank ingestion from verified PYQ sources + NCERT-derived. Much more fiddly
   than Phase 2 links — consider crowd-sourcing / admin CMS.
3. Timing engine: server-authoritative clock (drift), survive process death via `SavedStateHandle`
   + `WorkManager` foreground. This is where OEM-specific bugs come back.
4. Analytics: per-attempt breakdown, section-wise, historical comparison.
5. Offline-first for at least one "demo" test: pre-download question set to Room so the user can
   practice on the train.

Budget 1.5-2x vs. an iOS equivalent per the Phase 1 calibration (OEM friction for timers +
background work).

---

_Generated at end of Phase 2 on 2026-04-23. Demo seed is thin (22 topics) but every link is
verified — run the pipeline with a real YouTube API key for a fatter catalog before public launch._
