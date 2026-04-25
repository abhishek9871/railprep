# 12-Week Build Roadmap

Assumes 1 full-stack Android developer working with Claude Code, 1 backend developer (or same person for functions), 1 designer on-call for questions. Add 50% if solo.

## Phase 1 — Foundations (Weeks 1-2)
- [ ] Project scaffold, module structure, build config
- [ ] Firebase projects (dev + prod), Auth, Firestore, Functions scaffolds
- [ ] Compose theme from `design-tokens.json`
- [ ] Core component library: Button, Card, Chip, Badge, Avatar, ProgressRing, Bar, TopBar, BottomNav, Skeleton
- [ ] Localization setup (EN + HI), DataStore for user prefs
- [ ] Navigation graph stubbed
- **Ship:** Empty app running on device, bottom nav + theme + splash visible.

## Phase 2 — Auth & Onboarding (Week 3)
- [ ] Splash
- [ ] Language picker
- [ ] Onboarding carousel
- [ ] OTP login (Firebase Phone Auth, test numbers in dev)
- [ ] Goal setup
- [ ] User seeding via `onUserCreate` function
- **Ship:** A user can sign up and reach an empty Home.

## Phase 3 — Learning content (Weeks 4-5)
- [ ] Subjects list
- [ ] Chapter/lesson detail with tabs
- [ ] Video player (ExoPlayer + HLS)
- [ ] PDF viewer
- [ ] Bookmarks + "continue where you left off"
- [ ] Admin can publish via Firestore console (admin panel comes later)
- **Ship:** Users can watch videos and read PDFs with progress tracking.

## Phase 4 — Tests (Weeks 6-7) ⭐ critical feature
- [ ] Tests list with filters (PYQ, sectional, full)
- [ ] Instructions screen
- [ ] Quiz player — bilingual toggle, palette, timer, flag-for-review, auto-save
- [ ] Submit + server scoring (`scoreAttempt`)
- [ ] Results screen with section breakdown
- [ ] Review screen (question-by-question with solutions)
- **Ship:** End-to-end mock test flow with real scoring.

## Phase 5 — Dynamic content (Week 8)
- [ ] Current affairs feed with pull-to-refresh + Paging 3
- [ ] Article detail + audio player (TTS)
- [ ] Daily digest
- [ ] Notifications inbox + FCM integration
- [ ] Exam notifications
- [ ] Remote Config wiring for feature flags
- **Ship:** Editor can publish content via Firestore console, users see it live.

## Phase 6 — Engagement & analytics (Week 9)
- [ ] Home dashboard final polish (countdown ticket, streaks, AI insights placeholder)
- [ ] Profile + stats
- [ ] Leaderboard (weekly, state, friends)
- [ ] Analytics screen (charts via Vico compose charts)
- [ ] Streak logic + reminder notifications
- **Ship:** Full profile + gamification flywheel.

## Phase 7 — Community & Pro (Week 10)
- [ ] Doubts list, detail, post, answer, like
- [ ] Image upload to Storage
- [ ] Moderator flags
- [ ] Paywall screen
- [ ] Razorpay integration + `verifyPayment` function
- [ ] Subscription management (view, cancel auto-renew)
- **Ship:** First real revenue possible.

## Phase 8 — Offline, polish, perf (Week 11)
- [ ] Offline hub screen
- [ ] WorkManager sync jobs
- [ ] Attempt-while-offline queue
- [ ] Download manager for videos/PDFs
- [ ] Accessibility audit (TalkBack, contrast, scaling)
- [ ] Performance pass (startup time, scroll jank, image size)
- [ ] Crash-free rate > 99% in QA
- **Ship:** App works on the train.

## Phase 9 — Launch (Week 12)
- [ ] Play Store listing (EN + HI)
- [ ] Screenshots, feature graphic, icon
- [ ] Privacy policy & terms hosted
- [ ] Data Safety form
- [ ] Pre-launch report review
- [ ] Soft launch in 2 states (Bihar, UP — largest NTPC aspirant base)
- [ ] Collect feedback, patch, roll out wide
- **Ship:** Public on Play Store.

## Post-launch (ongoing)

- Month 2: Admin panel shipped; editorial team onboarded
- Month 3: Add Bengali + Tamil
- Month 4: Live tests with real-time leaderboard
- Month 5: Video doubt-solving (student submits photo of problem, mentor records video response)
- Month 6: Expand to RRB Group D, SSC CHSL — same platform, new exam track

## Risk register

| Risk | Mitigation |
|---|---|
| Low-end device crashes in quiz player | Test on Android Go devices in QA; cap concurrent images |
| Razorpay rejection (new entity) | Start KYC in week 1; use sandbox until approved |
| Hindi translation quality complaints | Human editor reviews every article; never auto-publish |
| Content staleness during team illness | Keep a 3-day buffer of drafted articles |
| Play Store rejection (data safety) | Fill form carefully; test all disclosed permissions |
| Cost overrun at scale | BigQuery for analytics (not live Firestore reads); aggressive client caching |
