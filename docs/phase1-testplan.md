# Phase 1 — Manual test plan

Run each section in order against a real device or emulator with `gradlew.bat :app:installDevDebug`. The test expects a live Supabase project with the migrations in `supabase/migrations/` applied and Google Sign-In configured (see root `README.md`).

## Prerequisites

- [ ] `local.properties` has all three of `SUPABASE_URL`, `SUPABASE_ANON_KEY`, `GOOGLE_WEB_CLIENT_ID` populated.
- [ ] Supabase project has migration `0001_profiles.sql` applied; the `profiles` table is visible in the Table Editor with RLS enabled.
- [ ] Google OAuth Web Client ID is wired into Supabase → Authentication → Providers → Google.
- [ ] A throwaway Google account available for sign-in testing.
- [ ] ADB reachable; install: `./gradlew.bat :app:installDevDebug`.

## Checklist

### 1. First launch — Language picker
1. [ ] Uninstall any prior build, then install fresh.
2. [ ] Launch. Splash appears on indigo background with "RailPrep" wordmark for ~600 ms, then routes to **Language**.
3. [ ] Tap **Bengali** (bn). Snackbar: "Coming soon — English for now." English stays selected.
4. [ ] Tap **Hindi** (hi). Card highlights.
5. [ ] Tap **Continue**. App recreates activity; next screen's strings are in Hindi.

### 2. Onboarding
6. [ ] Verify 3 slides are visible via pager dots. Each slide shows an illustration, title, body.
7. [ ] Swipe through all three and tap **Get started**.
8. [ ] Next screen is the two-button **Auth** screen.

### 3. Email sign-up + confirmation
9. [ ] On Auth screen, tap **Sign in with email**.
10. [ ] Switch to the **Create account** tab.
11. [ ] Enter an invalid email (e.g. `foo`). Inline error: "That doesn't look like a valid email." Submit disabled.
12. [ ] Enter a valid email + a 5-char password. Password error: "Use at least 8 characters."
13. [ ] Fix to a valid email + 8-char password. Tap **Create account**. Snackbar: "Account created. Check your email to confirm." (If the Supabase project has email-confirmation off, you'll be routed straight to Goal instead — that's also valid.)

### 4. Email sign-in
14. [ ] Back on the Email screen, **Sign in** tab. Enter your just-created credentials (confirm via Supabase Dashboard if email-confirmation is on). Tap **Sign in**.
15. [ ] App routes to the **Goal** screen.

### 5. Goal form validation
16. [ ] Without filling anything, tap **Finish setup**. Button stays disabled; errors shown under empty required fields.
17. [ ] Pick a target date 10 days from today. Inline error: "Pick a date between 30 days and 2 years from today."
18. [ ] Pick a DOB of today minus 17 years. Inline error: "You must be at least 18 years old."
19. [ ] Fill every required field with valid values. Leave display name blank. Tap **Finish setup**.

### 6. Home + sign out
20. [ ] App routes to Home. Greeting uses your email's prefix (since display name was blank).
21. [ ] Tap **Sign out**. App routes back to the Auth screen (via Splash).

### 7. Google Sign-In (fresh install)
22. [ ] Uninstall and reinstall to reset local state. (Or: clear app data.)
23. [ ] Step through Language → Onboarding → Auth again.
24. [ ] Tap **Continue with Google**. Native Credential Manager sheet appears listing Google accounts.
25. [ ] Pick a Google account. App routes to Goal (display name should be pre-filled from the Google profile).
26. [ ] Complete Goal. Home shows the Google profile name.

### 8. Password reset
27. [ ] Sign out. Tap **Sign in with email** → **Forgot password?**.
28. [ ] Enter the email you used. Tap. Snackbar: "Password reset email sent."
29. [ ] Check inbox. The email contains a Supabase-hosted reset link — clicking opens the Supabase-branded page in a browser where you can set a new password. (Deep link into the app is a Phase 8 polish.)

### 9. Database verification (cURL)

After the Google sign-in in step 24, verify the `profiles` row exists.

**Find the user's access token** in Supabase Dashboard → Authentication → Users → click your user → copy `access_token` from the JSON panel. (Alternatively: if the app is running, enable verbose logcat and `adb logcat | grep Bearer`.)

Replace `<PROJECT-REF>`, `<ANON-KEY>` and `<ACCESS-TOKEN>` below.

```bash
# 1. Does the profile row exist? (owner-only, via RLS)
curl -s "https://<PROJECT-REF>.supabase.co/rest/v1/profiles?select=*" \
  -H "apikey: <ANON-KEY>" \
  -H "Authorization: Bearer <ACCESS-TOKEN>" | jq

# 2. After completing Goal — is onboarding_complete = true with your answers?
curl -s "https://<PROJECT-REF>.supabase.co/rest/v1/profiles?select=display_name,exam_target,daily_minutes,onboarding_complete" \
  -H "apikey: <ANON-KEY>" \
  -H "Authorization: Bearer <ACCESS-TOKEN>" | jq

# 3. Attempt to read another user's row (should return empty array, not 403 — RLS silently filters)
curl -s "https://<PROJECT-REF>.supabase.co/rest/v1/profiles?id=eq.some-other-uuid&select=*" \
  -H "apikey: <ANON-KEY>" \
  -H "Authorization: Bearer <ACCESS-TOKEN>" | jq
```

Expected:
- Step 1 returns exactly one row (your user).
- Step 2 shows your entered goal values and `onboarding_complete: true`.
- Step 3 returns `[]` — RLS working.

### 10. Offline / no-network behavior
30. [ ] Put device in airplane mode. Kill and relaunch app. Splash → routes to Home (cached onboarding flag). Sign-out button fails silently (expected for Phase 1 — no offline queue yet). Turn network back on.

### 11. TalkBack smoke test
31. [ ] Enable TalkBack. Swipe through Auth screen. Every interactive element announces its label. Minimum touch target is respected.

### 12. Text scaling
32. [ ] Settings → Accessibility → Font size: Largest. Relaunch. All screens handle the scale without clipping (some truncation/wrapping is OK; no content disappears).

---

If any step fails, capture a screen recording and file a bug with the step number.
