# RailPrep — Developer Handoff

This folder is a complete brief for building the RailPrep Android app. Hand it to Claude Code (or any Android team).

## What's inside

| File | Purpose |
|---|---|
| `CLAUDE.md` | **Start here.** Master instructions for Claude Code — stack, architecture, module layout, screen map, quality bar. |
| `design-tokens.json` | Design system: colors, typography, spacing, radii, elevation, motion. Map to Compose theme. |
| `data-models.md` | All Kotlin data classes and Firestore collection layout. |
| `api-spec.md` | Every REST endpoint with request/response shapes. |
| `backend-setup.md` | Firebase project + Cloud Functions setup steps. |
| `admin-panel.md` | Separate admin web app for content ops. |
| `content-ops.md` | How the self-updating content pipeline works end-to-end. |
| `play-store-listing.md` | Store copy (EN + HI), ASO keywords, screenshot captions. |
| `compliance.md` | India DPDP Act, Play policy, in-app purchase rules. |
| `roadmap.md` | 12-week build plan, phased. |
| `../RailPrep Prototype.html` | Visual spec — open in a browser to see all 23 screens. |

## How to use with Claude Code on Windows 11

1. **Install prerequisites** on your laptop:
   - Android Studio Hedgehog or newer
   - JDK 17
   - Node.js 20+ (for Cloud Functions and admin panel)
   - Firebase CLI (`npm i -g firebase-tools`)
   - Git

2. **Create a Firebase project**
   - Enable: Auth (Phone), Firestore, Functions, Storage, FCM, Remote Config, Crashlytics, Analytics
   - Enable **Phone Auth** and add test numbers for development
   - Download `google-services.json` (added later when the Android package name is set)

3. **Open Claude Code in a new folder** on your PC:
   ```
   cd C:\Projects\railprep
   ```

4. **Copy this entire handoff folder** into `C:\Projects\railprep\docs\` and the prototype HTML into `C:\Projects\railprep\prototype\`. Structure should look like:
   ```
   C:\Projects\railprep\
     docs\CLAUDE.md
     docs\design-tokens.json
     docs\data-models.md
     docs\api-spec.md
     ...
     prototype\RailPrep Prototype.html
   ```

5. **Bootstrap the project.** In Claude Code, paste:
   > Read `docs/CLAUDE.md` in full. Then read the other files it references. Then scaffold the Android project per the specified module structure. Set up the Compose theme by reading `docs/design-tokens.json`. Do NOT start building features yet — show me the scaffold for review.

6. **Iterate feature by feature.** After review, tell Claude Code:
   > Build `feature-auth` end-to-end: all 5 screens (splash, language, onboarding, OTP, goal). Match the prototype pixel-for-pixel at 360x780. Include EN + HI strings. Include unit tests and Paparazzi screenshot tests.

   Review, refine, then move to the next feature.

7. **Deploy backend alongside.** Claude Code can also scaffold the Firebase Functions project under `functions/` — use TypeScript.

## Recommended build order (see `roadmap.md` for details)

- **Week 1-2:** Project scaffold, theme, core components library, auth flow
- **Week 3-4:** Home, Subjects, Lesson detail, Video player (read-only reads from Firestore)
- **Week 5-6:** Mock tests flow end-to-end (the money-maker)
- **Week 7:** Current affairs feed + Notifications + Remote Config
- **Week 8:** Analytics + Leaderboard + Profile
- **Week 9:** Doubts community
- **Week 10:** Paywall + Razorpay integration
- **Week 11:** Offline mode polish, performance pass, accessibility audit
- **Week 12:** Play Store submission, pre-launch report fixes

## What you (the founder) need in parallel

- Register a business entity (Pvt Ltd or LLP) — required for Razorpay.
- Get a Play Console account ($25 one-time).
- Host a privacy policy & terms. Use a generator, then have a lawyer review.
- Domain: `railprep.app` (or whatever brand you finalize — the prototype uses "RailPrep" as a placeholder; you own the brand decision).
- Hire or become a **content editor.** Self-updating content only works if someone publishes 5-10 current-affairs items per day. This is the single biggest operational task.
- Small seed content: 2 full mocks, 6 subject-wise mini mocks, 30 current-affairs articles, 10 video lessons (can be screencasts + voiceover initially). You can produce these before launch.

## Getting help

- **Claude Code stuck on a decision?** Tell it to re-read `CLAUDE.md` and ask you directly rather than guess.
- **Design question not covered?** Open the prototype HTML, screenshot the relevant screen, and share it back.
- **Backend question?** The API spec is the contract. If a field is missing, add it explicitly and document it.

## Licensing

All code generated under this spec is your property. The design system is original; fonts used (Plus Jakarta Sans, Inter, Noto Sans Devanagari) are all OFL / free to embed. No Google or competitor IP is referenced.
