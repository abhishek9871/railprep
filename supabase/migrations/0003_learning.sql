-- Phase 2 — Learning catalog (client-fetched, link-only)
-- We store NO media. Only a catalog of where content lives + how to display it.

-- ============================================================================
-- Enums
-- ============================================================================
create type public.content_type as enum ('YT_VIDEO', 'PDF_URL', 'ARTICLE', 'QUIZ');
create type public.topic_status as enum ('active', 'stale', 'removed');
create type public.license_type as enum (
    'CC-BY-SA',
    'GODL-India',
    'PUBLIC_DOMAIN',
    'YT_STANDARD',
    'NCERT_LINKED'
);

-- ============================================================================
-- Subjects
-- ============================================================================
create table public.subjects (
    id              uuid primary key default gen_random_uuid(),
    slug            text unique not null,
    title_en        text not null,
    title_hi        text not null,
    icon            text,
    display_order   smallint not null default 0,
    created_at      timestamptz not null default now()
);

-- ============================================================================
-- Chapters
-- ============================================================================
create table public.chapters (
    id              uuid primary key default gen_random_uuid(),
    subject_id      uuid not null references public.subjects(id) on delete cascade,
    slug            text not null,
    title_en        text not null,
    title_hi        text not null,
    display_order   smallint not null default 0,
    created_at      timestamptz not null default now(),
    unique (subject_id, slug)
);

create index chapters_subject_order_idx on public.chapters (subject_id, display_order);

-- ============================================================================
-- Topics — the actual content pointers. We host nothing; we link to YouTube
-- and NCERT (and PIB articles in Phase 4). status='stale' marks broken links
-- discovered by the pipeline or reported by the player.
-- ============================================================================
create table public.topics (
    id                  uuid primary key default gen_random_uuid(),
    chapter_id          uuid not null references public.chapters(id) on delete cascade,
    title_en            text not null,
    title_hi            text,
    content_type        public.content_type not null,
    external_video_id   text,
    external_pdf_url    text,
    article_url         text,
    source              text not null,
    license             public.license_type not null,
    duration_seconds    int,
    status              public.topic_status not null default 'active',
    last_verified_at    timestamptz,
    display_order       smallint not null default 0,
    created_at          timestamptz not null default now(),
    updated_at          timestamptz not null default now(),
    -- Exactly one of the three url fields must be populated per content_type.
    constraint topics_url_shape check (
        (content_type = 'YT_VIDEO' and external_video_id is not null and external_pdf_url is null and article_url is null)
        or (content_type = 'PDF_URL' and external_pdf_url is not null and external_video_id is null and article_url is null)
        or (content_type = 'ARTICLE' and article_url is not null and external_video_id is null and external_pdf_url is null)
        or (content_type = 'QUIZ' and external_video_id is null and external_pdf_url is null and article_url is null)
    )
);

create index topics_chapter_order_idx on public.topics (chapter_id, status, display_order);

create trigger topics_updated_at
    before update on public.topics
    for each row execute function public.set_updated_at();

-- ============================================================================
-- Bookmarks — one row per (user, topic). Per-user RLS.
-- ============================================================================
create table public.bookmarks (
    user_id     uuid not null references auth.users(id) on delete cascade,
    topic_id    uuid not null references public.topics(id) on delete cascade,
    created_at  timestamptz not null default now(),
    primary key (user_id, topic_id)
);

-- ============================================================================
-- RLS
-- ============================================================================
alter table public.subjects  enable row level security;
alter table public.chapters  enable row level security;
alter table public.topics    enable row level security;
alter table public.bookmarks enable row level security;

-- Catalog is readable by any authenticated user.
create policy subjects_read on public.subjects
    for select to authenticated using (true);

create policy chapters_read on public.chapters
    for select to authenticated using (true);

-- Non-active topics are hidden from clients; pipeline uses service_role which bypasses RLS.
create policy topics_read on public.topics
    for select to authenticated using (status = 'active');

-- Bookmarks: user manages their own only.
create policy bookmarks_select on public.bookmarks
    for select to authenticated using (auth.uid() = user_id);

create policy bookmarks_insert on public.bookmarks
    for insert to authenticated with check (auth.uid() = user_id);

create policy bookmarks_delete on public.bookmarks
    for delete to authenticated using (auth.uid() = user_id);
