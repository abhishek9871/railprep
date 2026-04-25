-- Recovered from Supabase MCP on 2026-04-25.
-- MCP version: 20260425060355
-- Original application order is determined by version timestamp, not filename.

-- Phase 4 D3 — support in-house ARTICLE topics with inline markdown content.
-- Adds 'ORIGINAL' to license_type enum, adds topics.content_md text column,
-- relaxes the url-shape constraint so ARTICLE can carry either article_url OR
-- content_md (at least one must be present).

-- 1) Extend license_type enum
alter type public.license_type add value if not exists 'ORIGINAL';

-- 2) Add content_md column for inline markdown primers
alter table public.topics
  add column if not exists content_md text;

-- 3) Relax url-shape: ARTICLE can use article_url OR content_md (at least one).
--    Other content types unchanged.
alter table public.topics drop constraint if exists topics_url_shape;
alter table public.topics add constraint topics_url_shape check (
  (content_type = 'YT_VIDEO' and external_video_id is not null and external_pdf_url is null and article_url is null)
  or (content_type = 'PDF_URL' and external_pdf_url is not null and external_video_id is null and article_url is null)
  or (content_type = 'ARTICLE' and external_video_id is null and external_pdf_url is null
        and (article_url is not null or content_md is not null))
  or (content_type = 'QUIZ' and external_video_id is null and external_pdf_url is null and article_url is null)
);