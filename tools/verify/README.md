# tools/verify — scripted end-to-end verification

## Setup (one-time)

The script signs in as a provisioned test user via email+password, exercises the
`start_attempt` → `upsertAnswer` → `submit_attempt` flow through the real
Supabase JS SDK, and asserts scoring + immutability.

Create `tools/verify/.env` (gitignored):

```
SUPABASE_URL=https://sneadexnpdyazrfgkkod.supabase.co
SUPABASE_ANON_KEY=<anon key from Supabase dashboard → Project Settings → API>
VERIFY_EMAIL=<existing test-user email from auth.users>
VERIFY_PASSWORD=<that user's password>
```

**Never commit the password.** The script refuses to run with a committed
`.env` because it checks that this README's notice is still present.

## Run

```
cd tools/verify
node phase3.mjs
```

Outputs `docs/phase3-evidence/script-results.txt`.

## Assertions

Phase 3 Gate 2 assertions (all must PASS):

| ID | Case |
|---|---|
| S1 | start_attempt creates/resumes an IN_PROGRESS attempt |
| S2 | Idempotency — calling start_attempt again returns the same attempt |
| S3 | upsertAnswer writes 5 answers (3 correct + 1 wrong + 1 skip) |
| S4 | submit_attempt returns status=SUBMITTED |
| S5 | correct_count=3, wrong_count=1, skipped_count=1 |
| S6 | score ≈ 2.6667 (1e-4 tolerance) |
| S7 | section_breakdown has the locked 8-key shape |
| S8 | POST-submit INSERT on attempt_answers returns the 55000 trigger error |
