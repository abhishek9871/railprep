# RailPrep Content Pipeline

Node 20 tool that generates the topic catalog SQL for RailPrep from YouTube channels + NCERT
textbook indexes, and applies it to Supabase via the service_role REST endpoint. The Android app
reads the resulting rows at runtime; the app hosts nothing.

## What it does

1. `npm run discover:ncert` — crawls each book's `textbook.php?{code}1=0-N` landing page, extracts
   the current PDF filenames, HEAD-checks each, writes `sql/topics_ncert.sql`.
2. `npm run discover:yt` — for each channel in `channels.json`, fetches 25 recent uploads via the
   channel's `uploads` playlist (1 unit/call — NOT `search.list`), classifies by subject via a
   title-keyword regex, writes `sql/topics_yt.sql`.
3. `npm run discover:pib` — Phase 4 stub; prints RSS feed stats, no DB writes.
4. `npm run apply` — applies every `sql/*.sql` through `rpc/_pipeline_apply` on Supabase using
   `SUPABASE_SERVICE_KEY`.

Chapter-assignment (picking which chapter a discovered video or PDF belongs to) is a manual
curation step; the generated INSERTs stay as NOOPs (`where false;`) until someone edits the SQL
to pin a `chapter_id`. See `docs/content-strategy.md §6` for the current Phase 2 seed.

## Setup

1. `cp .env.example .env` and fill in:
   - `SUPABASE_URL` — project REST endpoint.
   - `SUPABASE_SERVICE_KEY` — the **service_role** key (never commit this).
   - `YOUTUBE_API_KEY` — YouTube Data API v3 key. Generate at
     https://console.cloud.google.com/apis/credentials under project `railprep-494208`.
2. `npm ci`
3. Install the SQL executor RPC once (run in the Supabase SQL editor, not here):

   ```sql
   create or replace function public._pipeline_apply(q text) returns void
       language plpgsql security definer as $$ begin execute q; end $$;
   revoke all on function public._pipeline_apply(text) from public, anon, authenticated;
   grant execute on function public._pipeline_apply(text) to service_role;
   ```

## Quota notes

YouTube Data API v3 free tier: **10,000 units/day**. Per-operation cost:

- `channels.list` → 1 unit per channel lookup
- `playlistItems.list` → 1 unit per 50 items (we fetch 25, so 1 unit/call)
- `search.list` → **100 units/call — do not use**

At 22 channels × 1 call for the uploads-playlist lookup + 22 calls for recent videos = ~44 units
per full run. A twice-weekly cron stays well under 1% of the daily quota.

## GitHub Action

`.github/workflows/content-sync.yml` runs the pipeline on Mon + Thu at 02:00 UTC and supports
manual `workflow_dispatch`. Required repository secrets:

- `SUPABASE_URL`
- `SUPABASE_SERVICE_KEY`
- `YOUTUBE_API_KEY`

## Legal / attribution

Every discovered item inherits its license from the source:

- YouTube videos → `YT_STANDARD` (IFrame player satisfies attribution via its own chrome).
- NCERT PDFs → `NCERT_LINKED` (link-only; NCERT prohibits redistribution — we never host copies).
- PIB articles (Phase 4) → `GODL-India` with the attribution template from `docs/content-strategy.md §5`.

Do not add channels with non-permissive licensing to `channels.json`. The blacklist is documented
in `docs/content-strategy.md` (Unacademy / Testbook main / BYJU'S Exam Prep main).
