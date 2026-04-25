# Supabase — database + auth setup

## Apply migrations

**Option A — Supabase Dashboard SQL editor (simplest):**

1. Open your project in https://supabase.com/dashboard.
2. Go to **SQL Editor** → **New query**.
3. Paste the contents of `migrations/0001_profiles.sql`.
4. Click **Run**.

**Option B — Supabase CLI:**

```bash
npm i -g supabase
supabase login
supabase link --project-ref <your-project-ref>
supabase db push
```

The CLI reads every file in `migrations/` in filename order (hence the `0001_` prefix).

## Verify

In the Dashboard → **Table Editor**, you should see a `profiles` table with RLS enabled (lock icon on the table name).

Sign up a throwaway user via **Authentication** → **Users** → **Invite user**. A row should auto-appear in `profiles` with only the `id` filled in — that's the trigger firing.

## Phase 1 auth providers

1. **Email + password** is enabled by default. Check under **Authentication** → **Providers** → **Email**. Recommended: set **Confirm email** ON for production, OFF while developing to skip the confirmation-email round trip.

2. **Google** — see the step-by-step in the root `README.md`. You need a **Web** OAuth 2.0 Client ID from Google Cloud Console pasted into:
   - Supabase Dashboard → Authentication → Providers → Google → **Client ID** + **Client Secret**.
   - The Android app's `local.properties` as `GOOGLE_WEB_CLIENT_ID` (Web client ID — **not** the Android client ID).
