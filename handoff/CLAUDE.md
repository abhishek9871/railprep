# CLAUDE.md — Instructions for Claude Code

You are building **RailPrep**, a production-grade Android app for Indian students preparing for the RRB NTPC (Railway Recruitment Board Non-Technical Popular Categories) examination. This file is your single source of truth.

## Product summary

- **Audience:** Indian students aged 18-36 preparing for RRB NTPC CBT-1 & CBT-2. Mix of urban, tier-2/3, and rural users. Low-end Android phones are common.
- **Core promise:** Self-updating content (current affairs, notifications, mock tests) + full CBT-pattern practice + bilingual (Hindi + English minimum).
- **Monetization:** Freemium. Free tier shows basic mocks + limited content. Pro (₹199/mo, ₹799/6mo) unlocks all mocks, PYQs, analytics, ad-free, priority doubts.

## Design reference

The approved visual design is in `prototype/RailPrep Prototype.html` (open it in a browser — all 23 screens are shown on a design canvas).

**Every pixel decision should match this prototype.** If something isn't covered by the prototype, consult `handoff/design-tokens.json` and follow the established patterns.

### Key principles
- Deep indigo primary `#2B3EA8`, warm amber accent `#F59A2E` for streaks/CTAs.
- Plus Jakarta Sans for headings/display, Inter for body, Noto Sans Devanagari for Hindi.
- Subject colors are consistent across every screen (see tokens).
- Bilingual support must be real from day 1: every user-facing string goes through the i18n system with EN + HI at minimum.
- Touch targets ≥ 44dp. Respect Material 3 motion curves.

## Tech stack (non-negotiable unless you have a strong reason)

- **Android native** — Kotlin 1.9+, Jetpack Compose (Material 3), min SDK 24 (Android 7.0), target SDK latest.
- **Architecture:** MVVM + Clean Architecture. `domain` / `data` / `feature-*` modules. Use Hilt for DI, Coroutines + Flow for async.
- **Navigation:** Jetpack Navigation Compose. Type-safe destinations.
- **Networking:** Retrofit + OkHttp + Moshi. ApolloGraphQL NOT used — REST per `handoff/api-spec.md`.
- **Local DB:** Room. Paging 3 for feeds.
- **Offline:** WorkManager for background sync, Room as source of truth, attempts queued locally and flushed on reconnect.
- **Video:** Media3 ExoPlayer with HLS.
- **Image:** Coil.
- **Auth:** Firebase Auth (Phone / Google / Facebook).
- **Backend:** Firebase — Firestore, Functions (TypeScript), FCM, Remote Config, Crashlytics, Analytics. Cloud Storage for user uploads in doubts.
- **Payments:** Razorpay Android SDK. Standard checkout. Server-side signature verification.
- **Localization:** Standard Android `strings.xml` per locale + dynamic content's `LocalizedString` shape. Bundled: en, hi. Phase 2: bn, ta, te, mr, gu, pa.
- **Testing:** JUnit5, MockK, Turbine for Flow, Compose UI tests with Paparazzi for screenshot tests.
- **CI:** GitHub Actions — build, lint, test, assemble release AAB on tag.
- **Analytics:** Firebase Analytics + Crashlytics. Key funnel events: signup, onboarding_complete, test_start, test_submit, paywall_view, purchase.

## Module structure

```
app/                          # single app module, entry point
core/
  core-design/                # theme, tokens mapped from design-tokens.json, components
  core-network/               # Retrofit, interceptors, auth
  core-database/              # Room DB, DAOs
  core-common/                # utilities, extensions
  core-analytics/
  core-i18n/
feature/
  feature-auth/               # splash, language, onboarding, OTP, goal setup
  feature-home/
  feature-learn/              # subjects, chapters, topics, video player, PDF viewer
  feature-tests/              # list, instructions, quiz player, results
  feature-feed/               # current affairs, daily digest
  feature-notifications/
  feature-doubts/
  feature-profile/            # profile, leaderboard, analytics
  feature-paywall/
  feature-offline/
data/
  data-repository/            # repos, mapping between API/DB/UI
  data-remote/                # API service, DTOs
domain/                       # use cases, pure Kotlin models
build-logic/                  # convention plugins, shared build config
```

## Screen map (from prototype)

| # | Screen | Route | Notes |
|---|---|---|---|
| 01 | Splash | `/splash` | Check auth, fetch Remote Config, route onward |
| 02 | Language | `/language` | First launch only; persisted in DataStore |
| 03 | Onboarding | `/onboarding` | 3-slide intro |
| 04 | OTP login | `/auth/login` | Firebase Phone Auth |
| 05 | Goal setup | `/onboarding/goal` | Exam timeline |
| 06 | Home ⭐ | `/home` | Bottom-nav root; exam countdown ticket |
| 07 | Subjects | `/learn` | Bottom-nav root |
| 08 | Lesson detail | `/learn/chapter/{id}` | Tabs: Overview/Topics/Practice/Notes |
| 09 | Video player | `/learn/topic/{id}/play` | Fullscreen, CC, speed |
| 10 | PDF library | `/learn/pdfs` | Downloadable notes |
| 11 | Tests list | `/tests` | Bottom-nav root |
| 12 | Test instructions | `/tests/{id}/instructions` | |
| 13 | Quiz player ⭐ | `/tests/{id}/attempt` | Full CBT replica, palette, timer, lang toggle |
| 14 | Results | `/tests/{id}/results` | Score, section breakdown, weak topics |
| 15 | Analytics | `/me/analytics` | Charts, AI insights, cut-off predictor |
| 16 | Feed | `/feed` | Bottom-nav root; current affairs |
| 17 | Notifications | `/notifications` | |
| 18 | Exam notification | `/exam-notifications/{id}` | Official RRB notification detail |
| 19 | Offline | `/offline` | Downloaded content hub |
| 20 | Profile | `/me` | Bottom-nav root |
| 21 | Leaderboard | `/leaderboard` | |
| 22 | Doubts | `/doubts` | Community |
| 23 | Pro paywall ⭐ | `/paywall` | Appears from locked Pro content |

## How to work

1. **Read these handoff files first:**
   - `handoff/design-tokens.json` — colors, type, spacing, radii
   - `handoff/data-models.md` — Kotlin data classes and Firestore layout
   - `handoff/api-spec.md` — every REST endpoint
   - `prototype/RailPrep Prototype.html` — visual truth

2. **Start order:**
   - Scaffold project + modules
   - Set up Compose theme from `design-tokens.json`
   - Build `core-design` component library (Button, Card, Chip, SubjectBadge, ProgressRing, Bar, Avatar, Placeholder, TopBar, BottomNav) — match prototype exactly
   - Build `feature-auth` end-to-end (splash → language → onboarding → OTP → goal) with real Firebase auth
   - Wire Home with mocked data, then replace with real Firestore reads
   - Feature by feature, always end-to-end (UI + repo + API + tests) before moving on

3. **Definition of done for each screen:**
   - Matches prototype visually at 360x780 dp
   - All strings in `strings.xml` with EN + HI
   - Loading, empty, error states implemented
   - Content-description on every icon/image
   - Compose preview screenshot checked in
   - Unit tests for VM + repo; Paparazzi screenshot test for the screen
   - Works offline (cached data) where the feature applies
   - Accessibility: TalkBack audit passes, text scales up to 200%

4. **Self-updating content is the product's lifeblood.** Never bake content into APK. Everything dynamic (articles, tests, notifications, syllabus, cut-offs, UI copy for promos) comes from Firestore + Remote Config. An admin web panel is a follow-up project.

5. **Offline mode must be first-class.** A user on a train should be able to: open app, resume last lesson, attempt a downloaded mock, read saved articles, add doubts (queued). All writes queue through WorkManager and flush on connectivity.

## Quality bar

- Cold start < 2s on a mid-range phone
- APK size < 30 MB base (split APKs by ABI)
- Crash-free rate target: 99.5%
- Respect data: compress images server-side, lazy-load video, no autoplay on cellular
- Privacy: DPDP Act 2023 compliant. Data deletion on request within 30 days. Privacy policy URL in Play listing.

## Security

- Never log tokens, phone numbers, payment signatures.
- Certificate pinning for api.railprep.app.
- ProGuard/R8 enabled for release; keep Razorpay + Firebase rules.
- Root detection? No — too user-hostile for this audience.
- Razorpay signature verification MUST happen server-side only.

## Play Store readiness checklist

- [ ] App signing via Play App Signing
- [ ] Target SDK ≥ latest required
- [ ] 64-bit support
- [ ] Data Safety form filled (collected: phone, name, progress; shared: none)
- [ ] Content rating: Everyone
- [ ] Privacy policy hosted, linked in listing & app
- [ ] In-app subscriptions declared
- [ ] Feature graphic 1024x500, 8 screenshots 1080x1920 portrait, app icon 512x512
- [ ] Localized listing: en-IN, hi-IN
- [ ] Pre-launch report clean (no crashes, no accessibility blockers)

## Questions? Escalate, don't guess.

If a spec is ambiguous, halt and ask the product owner. Do not silently deviate from the prototype.
