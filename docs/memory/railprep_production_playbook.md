---
name: RailPrep production release playbook
description: What to do before shipping RailPrep to Play Store — keystore, SHA-1s, Google OAuth additions, Supabase email, Proguard. Consult when user asks about release, production, Play Store, keystore, or "shipping".
type: project
originSessionId: 14001d42-4e8c-4462-9af2-e53ccd128965
---
# RailPrep — Production Release Playbook

All code/config is Phase-1-dev-ready. Before a real Play Store upload, these items must be handled in this order. Do NOT treat as done until each is verified.

## Key facts (as of 2026-04-23)

- **Supabase project**: `sneadexnpdyazrfgkkod` (name: railprep-dev, URL: `https://sneadexnpdyazrfgkkod.supabase.co`)
- **Google Cloud project**: `railprep-494208` (display: RailPrep)
- **Web Client ID** (used by app code via `GOOGLE_WEB_CLIENT_ID` in local.properties): `936171577314-gjojf071950qplrlrhn9nkr8o3a4st63.apps.googleusercontent.com`
- **Android OAuth client registered (debug only)**: package `com.railprep.dev`, debug SHA-1 `1F:8A:F4:71:5F:63:F0:AF:1C:8F:47:1E:43:35:CC:75:26:2D:D4:02`, client ID `936171577314-3nhercs700t7al07eqf8r5l1chnqkcvb.apps.googleusercontent.com`
- **Prod applicationId** (NOT YET registered with Google): `com.railprep`
- **Flavors**: `dev` → `com.railprep.dev` | `prod` → `com.railprep` (see `build-logic/convention/.../AndroidApplicationConventionPlugin.kt`)
- **Test device**: ZX9HQCAYO785NVBI (OPPO CPH2491, ColorOS) — known quirks below

## Release checklist (do in order)

### 1. Generate release keystore
- **When**: Before first signed release build.
- **How**: `keytool -genkey -v -keystore /secure/location/railprep-release.keystore -alias railprep -keyalg RSA -keysize 2048 -validity 10000` — keytool lives at `/c/Program Files/Java/jdk-21/bin/keytool.exe`.
- **Store passwords** in `~/.gradle/gradle.properties` (NOT in repo) as `RAILPREP_RELEASE_STORE_PASSWORD` / `RAILPREP_RELEASE_KEY_PASSWORD`. Reference from `app/build.gradle.kts` signingConfigs.
- **Extract fingerprints**: `keytool -list -v -alias railprep -keystore /path/to/railprep-release.keystore` → copy SHA-1 and SHA-256.
- NEVER commit the keystore or passwords. Verify with `git check-ignore`.

### 2. Register prod Android OAuth client with Google
- **Why**: Currently only `com.railprep.dev` + debug SHA-1 is registered. Without the prod variant, Google sign-in fails in release with `[16] Account reauth failed` — this was exactly the bug we hit in dev.
- **How**: Chrome MCP (user already logged in) → navigate `https://console.cloud.google.com/auth/clients?project=railprep-494208` → Create client → Android.
- **Fields**:
  - Name: `RailPrep Android Prod`
  - Package: `com.railprep`
  - SHA-1: the release keystore SHA-1 from step 1
- **Also add the Play App Signing SHA-1** to this same client after first upload: Play Console → App integrity → "App signing key certificate" → copy SHA-1. Play re-signs your bundle with its own key, so THAT SHA-1 is what runs on user devices, not your upload SHA-1.

### 3. Configure release signing in Gradle
- Add release signingConfig in `app/build.gradle.kts` referencing gradle.properties vars.
- Do NOT enable minify+R8 until step 5.

### 4. Supabase — email deliverability
- Current state: "Confirm email" is OFF (we disabled it to unblock dev). For launch:
  - Either keep it OFF and accept anyone can sign up with any email (OK for an exam prep app)
  - Or turn it back ON + configure custom SMTP (Resend / Postmark / SendGrid) via Auth → Emails → SMTP Settings. Default Supabase sender (`noreply@mail.app.supabase.io`) gets aggressive spam-filtering on Outlook.
- Password reset email template uses `{{ .Token }}` — 8-digit OTP. No change needed for launch.
- Rate limits (Auth → Rate Limits): defaults are tight (3/hour). Bump before launch if you expect many simultaneous sign-ups.

### 5. Enable R8 / Proguard for release
- Supabase-kt (Ktor + kotlinx.serialization) and androidx.credentials need keep rules.
- Start with the defaults in each library's docs. Add rules to `app/proguard-rules.pro`.
- **Always** run a signed release APK through full sign-in flows (email + Google + password reset) on a real device before shipping — R8 strips reflection-dependent code silently.

### 6. OAuth consent screen — keep minimal
- Currently: Published to Production, scopes = `openid email profile`. No Google verification required.
- Do NOT add sensitive scopes (Drive, Gmail, Contacts) casually — they trigger a weeks-long verification.

### 7. Version + upload
- Bump `versionCode` + `versionName` in `app/build.gradle.kts`.
- Build: `./gradlew :app:bundleProdRelease` (not bundle — ambiguous due to flavors).
- Upload AAB to Play Console internal track first.

## Tools available (confirmed working)

- **Chrome MCP** (claude-in-chrome) — user pre-authenticated on Supabase + Google Cloud. Call `tabs_context_mcp` first each session (tabs from previous sessions are stale).
- **Supabase MCP** — `execute_sql`, `get_logs` (service: auth/api/postgres/edge-function), `list_tables`, `apply_migration`. Use `get_logs service=auth` to see live auth events when debugging.
- **adb**: `/c/Users/VASU/AppData/Local/Android/Sdk/platform-tools/adb.exe` (not on PATH). Device ID: `ZX9HQCAYO785NVBI`.
- **keytool**: `/c/Program Files/Java/jdk-21/bin/keytool.exe`.
- **Gradle install task**: `:app:installDevDebug` (NOT `:app:installDebug` — ambiguous due to flavors). For release: `:app:installProdRelease` or `:app:bundleProdRelease`.

## Known OEM gotchas (learned the hard way on ZX9HQCAYO785NVBI)

- **ColorOS filters app logs from logcat** even at Log.e. Use `Toast.makeText(...)` for on-device diagnostic signals during debugging. Don't waste time chasing missing Log.i/Log.w output.
- **Credential Manager disposes composables during its system sheet.** Any auth flow using `getCredential` MUST run in `viewModelScope` or similar lifecycle-surviving scope — NOT `rememberCoroutineScope`. Orphaned continuations appear as "nothing happens after account selection."
- **`[16] Account reauth failed` after selecting an account** = Android OAuth client with matching package+SHA-1 is not registered. Fix: register it, wait 1–5 min for propagation.
- **Supabase default `noreply@mail.app.supabase.io`** gets spam-filtered hard by Outlook/Hotmail. Use OTP codes (we did) OR custom SMTP.

## Do NOT regress these fixes

- Google flow lives in `AuthViewModel.startGoogleSignIn` under `viewModelScope` with 25s `withTimeout` — do not move back into the Composable scope.
- Password reset uses OTP (`{{ .Token }}`), not magic links. Template is in Supabase Dashboard → Auth → Emails → Reset password. OTP length is 8 digits for this project; client accepts 6–10.
- `INTERNET` + `ACCESS_NETWORK_STATE` in `app/src/main/AndroidManifest.xml` — without these, Supabase-kt (OkHttp) throws on its dispatcher thread and kills the process before our catch blocks see anything.
