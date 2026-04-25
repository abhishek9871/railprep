# RailPrep

Android app for RRB NTPC (Railway Recruitment Board — Non-Technical Popular Categories) exam prep.
See `handoff/CLAUDE.md` for the full product brief; the stack overrides below supersede it where they conflict.

> **Phase 1 status:** auth + onboarding live. Splash, Language picker, Onboarding carousel, Auth (Google + Email), Goal setup, and a placeholder Home all wire to a real Supabase backend. Feature work beyond this is deferred to later phases.

## Stack

- **Android native** — Kotlin 2.1, Jetpack Compose (Material 3), Hilt, KSP.
- **Minimum Android 7.0** (API 24) / **compile + target API 35**.
- **Backend:** Supabase (Postgres + Auth + Storage + Edge Functions + Realtime).
- **Auth providers at launch:** Google Sign-In via Credential Manager **and** Supabase email + password. **No phone OTP** at launch.
- **Payments:** Google Play Billing Library.
- **Content sources:** public-domain Indian government sources only (PIB, MyGov, data.gov.in, Wikipedia current events, NCERT, RRB official).

## Prerequisites

- **Android Studio** — any 2024.x or later (bundles the Android SDK + AVD tools).
- **JDK 17** (or 21 — both work; the Gradle toolchain coerces to 17). Set `JAVA_HOME` to match.
- **Android SDK** with platform **35** and build-tools **35.0.0** installed (Android Studio → SDK Manager).
- Optional: physical device with USB debugging, or an emulator running Android 10+.

## One-time setup

1. Clone the repo and `cd` into its root.
2. Create a `local.properties` file at the repo root (see template below). The build reads three BuildConfig fields from it — missing values just emit a warning and assemble with empty strings, so you can build with empty secrets. **But auth won't work until all three are populated.**
3. Point `sdk.dir` at your Android SDK (Android Studio auto-fills this when you open the project; otherwise set it by hand).
4. For Phase 1 features to actually run, follow the two OAuth setup walkthroughs below (**Google Cloud Console** + **Supabase Dashboard**). Both sides must be configured; a reader who only sets the Google side will have broken sign-in.

### `local.properties` template

Copy this verbatim into `local.properties` at the repo root and fill in each value when the corresponding phase lands.

```properties
# --- Android SDK (auto-filled by Android Studio on first open) ---
sdk.dir=C\:\\Users\\<you>\\AppData\\Local\\Android\\Sdk

# --- Supabase (Phase 1 — create the project first, then fill these in) ---
# From Supabase Dashboard → Project Settings → API → Project URL
SUPABASE_URL=https://xxxxxx.supabase.co
# From Supabase Dashboard → Project Settings → API → anon / public key
SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

# --- Google Sign-In (Phase 1) ---
# WEB client ID from Google Cloud Console (NOT the Android client ID).
# See the "Google Cloud Console walkthrough" section below.
GOOGLE_WEB_CLIENT_ID=123456789012-abcdefghijk.apps.googleusercontent.com

# --- Release signing (Phase 9 — Play Store submission only) ---
# Path is relative to the repo root. Keystore itself lives OUTSIDE the repo.
RP_KEYSTORE_PATH=
RP_KEYSTORE_PASSWORD=
RP_KEY_ALIAS=
RP_KEY_PASSWORD=
```

## Backend setup (Supabase + Google OAuth) — Phase 1

You need two things, wired to each other:
1. A Supabase project with the Phase 1 schema.
2. A Google OAuth **Web** Client ID registered in both Google Cloud Console **and** the Supabase Dashboard.

### A. Create and provision the Supabase project

1. Go to **https://supabase.com/dashboard** → **New project**.
2. Pick a name, a strong DB password (save it — you'll rarely need it but losing it is painful), and region **ap-south-1 (Mumbai)** for Indian users.
3. Wait ~1 minute for the project to spin up.
4. Open **Project Settings → API**:
   - Copy **Project URL** → paste into `local.properties` as `SUPABASE_URL`.
   - Copy **Project API keys → anon / public** → paste as `SUPABASE_ANON_KEY`. **Don't use the `service_role` key** — it's a full-access key that must never ship in the app.
5. Apply the Phase 1 migration:
   - **SQL Editor** → **New query** → paste the contents of `supabase/migrations/0001_profiles.sql` → **Run**.
   - (Or via CLI: `supabase login`, `supabase link --project-ref <ref>`, `supabase db push`.)
6. Verify: **Table Editor** → you should see `profiles` with a lock icon (RLS enabled).

### B. Create the Google OAuth Web Client ID

Google doesn't let you share a single client across native Android and web — but our flow is "Android Credential Manager fetches an ID token, then Supabase verifies it server-side as if it were a web flow." That means we only need **one** Web Client ID, shared between the Android app and Supabase.

1. Go to **https://console.cloud.google.com/**. Create a new project (or pick an existing one) — name it `railprep-prod`.
2. **APIs & Services → OAuth consent screen**:
   - User type: **External**.
   - App name: `RailPrep`. User support email: yours. Developer contact: yours. Save.
   - Scopes: default (`openid`, `email`, `profile`). Save.
   - Test users: add any Google accounts you'll use to test sign-in during development. Save.
3. **APIs & Services → Credentials → + Create Credentials → OAuth client ID**:
   - Application type: **Web application**.
   - Name: `RailPrep Web (used for ID tokens)`.
   - **Authorized JavaScript origins:** leave blank. (We're not hosting a web frontend.)
   - **Authorized redirect URIs:** add **`https://<your-supabase-project-ref>.supabase.co/auth/v1/callback`** (replace the ref with the one from step A.4 above).
   - Click **Create**. Copy the **Client ID** and **Client Secret** that appear.
4. Paste the Client ID into `local.properties` as `GOOGLE_WEB_CLIENT_ID`.

### C. Wire Google into Supabase

1. In Supabase Dashboard → **Authentication → Providers → Google**.
2. Toggle **Enable**.
3. **Client ID:** paste the Web Client ID from step B.3.
4. **Client Secret:** paste the secret from step B.3.
5. **Skip nonce check:** leave **OFF**. Our app passes a nonce through Credential Manager → Supabase verifies it.
6. Click **Save**.
7. Optional: under **Authentication → URL Configuration**, set **Site URL** to your eventual production URL or leave the default — not critical for Phase 1.

### D. Enable email auth (default, but confirm)

1. Supabase Dashboard → **Authentication → Providers → Email**. Should already be **Enabled**.
2. **Confirm email:** recommended **ON** for production. For faster dev iteration, turn **OFF** until you're ready to test the confirmation flow.
3. Password policy: leave defaults (min 6 chars; the app enforces 8).

### E. Verify end-to-end

Run the Phase 1 manual test plan in `docs/phase1-testplan.md`. The cURL snippets at the end confirm that RLS is working and that `profiles` rows get created on signup.

## Building

From the repo root (with the Gradle wrapper bootstrapped):

```powershell
# Dev debug (the default during development):
.\gradlew.bat :app:assembleDevDebug

# Install on a connected device / running emulator:
.\gradlew.bat :app:installDevDebug

# Prod release (needs signing keys — see Phase 9 in local.properties):
.\gradlew.bat :app:assembleProdRelease

# Module-scoped sanity check on the design system:
.\gradlew.bat :core:core-design:assembleDebug
```

### Flavors

Two product flavors under the `environment` dimension:

| Flavor | applicationId    | Notes                              |
|--------|------------------|------------------------------------|
| `dev`  | `com.railprep.dev` | Installs side-by-side with `prod`. |
| `prod` | `com.railprep`     | Play Store build.                  |

## Module tree

```
app/                          — single application module
core/
  core-design/                — RailPrepTheme, tokens, stub components
  core-network/               — Retrofit + Supabase wiring (deferred)
  core-database/              — Room DB + DAOs (deferred)
  core-common/                — utils + extensions
  core-analytics/             — empty shell (PostHog lands Phase 3)
  core-i18n/                  — empty shell (LocalizedString lives in domain/)
feature/
  feature-auth/    feature-home/    feature-learn/    feature-tests/
  feature-feed/    feature-notifications/  feature-doubts/   feature-profile/
  feature-paywall/ feature-offline/
data/
  data-repository/
  data-remote/
domain/                       — pure Kotlin/JVM; domain models + use cases
build-logic/                  — convention plugins
```

### Convention plugins

All applied from `build-logic/`:

| Plugin id                             | Applied to                                           |
|---------------------------------------|------------------------------------------------------|
| `railprep.android.application`        | `:app`                                               |
| `railprep.android.library`            | non-Compose core + data modules                      |
| `railprep.android.compose.library`    | `:core:core-design`                                  |
| `railprep.android.feature`            | every `:feature:feature-*` (lib + Compose + Hilt)    |
| `railprep.jvm.library`                | `:domain` (pure Kotlin, no Android)                  |

## What's shipped after Phase 1

- Splash / Language / Onboarding / Auth (Google + Email) / Goal / placeholder Home — all wired through Navigation Compose with type-safe routes.
- Real Supabase auth (`signInWith(IDToken)` for Google, `signInWith(Email)` for email/password).
- Supabase schema: `profiles` table with RLS, auth-insert trigger, `updated_at` trigger.
- Android-side per-app locale switching via `AppCompatDelegate.setApplicationLocales`, persisted in DataStore.
- 23 unit tests + 12 Paparazzi snapshots (6 screens × en/hi).
- Bundled Inter + Plus Jakarta Sans TTFs (SIL OFL — see `NOTICE.md`). Google Fonts provider kept as a fallback.

## What's deferred to later phases

- Any feature beyond auth/onboarding — every non-auth, non-home `feature-*` module is still a placeholder.
- Room schema, DAOs, migrations.
- WorkManager sync chains, offline queuing, real `NetworkMonitor`.
- Google Play Billing integration.
- Media3 player, Coil image loading, Paging 3 feeds.
- PostHog analytics, Sentry crash reporting.
- Dark theme (tokens don't define dark values yet).
- CI pipeline, Play Console wiring.
- Privacy policy + Terms hosting on Cloudflare Pages.
- In-app deep link for password reset (currently uses Supabase's hosted reset page).
- Noto Sans Devanagari bundled fallback (Downloadable-only in Phase 1).

## References

- Product brief & screen map: `handoff/CLAUDE.md`
- Visual design: `handoff/prototype/RailPrep Prototype.html`
- Tokens: `handoff/design-tokens.json`
- Data shapes: `handoff/data-models.md`
- API contracts: `handoff/api-spec.md`
