-- =====================================================================
-- 0007_topic_tags_and_primers.sql — Phase 3 Stage 5.
--
-- Extends topics with a tags array so the weak-topic routing query can
-- match question.tags ↔ topic.tags. Then seeds 5 reasoning primers
-- backed by Wikipedia under a new "RRB NTPC Reasoning Primers" chapter.
-- =====================================================================

alter table public.topics
  add column if not exists tags text[] not null default '{}'::text[];

create index if not exists topics_tags_gin_idx
  on public.topics using gin (tags);

-- Idempotent: insert subject "Reasoning" if missing, chapter "Reasoning Primers" if missing,
-- then upsert the 5 primer topics by their slug-shaped article_url.
do $$
declare
  v_subject_id uuid;
  v_chapter_id uuid;
begin
  select id into v_subject_id from public.subjects where slug = 'reasoning';
  if v_subject_id is null then
    insert into public.subjects (slug, title_en, title_hi, display_order)
    values ('reasoning', 'Reasoning', 'रीज़निंग', 60)
    returning id into v_subject_id;
  end if;

  select id into v_chapter_id
    from public.chapters
   where subject_id = v_subject_id and slug = 'reasoning-primers';
  if v_chapter_id is null then
    insert into public.chapters (subject_id, slug, title_en, title_hi, display_order)
    values (v_subject_id, 'reasoning-primers', 'Reasoning Primers', 'रीज़निंग के मूल पाठ', 1)
    returning id into v_chapter_id;
  end if;

  -- Upsert primers. We use article_url as the natural key here; an explicit unique
  -- constraint isn't on the table, so we use a "delete then insert" pattern within
  -- the chapter to keep the script idempotent.
  delete from public.topics
   where chapter_id = v_chapter_id
     and article_url like 'https://en.wikipedia.org/%';

  insert into public.topics (
    chapter_id, title_en, title_hi, content_type, article_url,
    source, license, status, display_order, tags
  ) values
    (v_chapter_id, 'Syllogism', 'न्यायवाक्य (Syllogism)', 'ARTICLE',
     'https://en.wikipedia.org/wiki/Syllogism',
     'Wikipedia', 'CC_BY_SA', 'active', 10,
     array['syllogism','reasoning','statement-conclusion']),
    (v_chapter_id, 'Venn Diagrams', 'वेन आरेख (Venn Diagrams)', 'ARTICLE',
     'https://en.wikipedia.org/wiki/Venn_diagram',
     'Wikipedia', 'CC_BY_SA', 'active', 20,
     array['venn-diagrams','reasoning','set-theory']),
    (v_chapter_id, 'Clock Problems', 'घड़ी (Clocks)', 'ARTICLE',
     'https://en.wikipedia.org/wiki/Clock_angle_problem',
     'Wikipedia', 'CC_BY_SA', 'active', 30,
     array['clocks','reasoning','clock-angle']),
    (v_chapter_id, 'Calendar Problems — Day of Week', 'कैलेंडर — दिन निकालना', 'ARTICLE',
     'https://en.wikipedia.org/wiki/Determination_of_the_day_of_the_week',
     'Wikipedia', 'CC_BY_SA', 'active', 40,
     array['calendars','reasoning','day-of-week','zellers-congruence']),
    (v_chapter_id, 'Number Series — Arithmetic Progression', 'संख्या श्रेणी — Arithmetic Progression', 'ARTICLE',
     'https://en.wikipedia.org/wiki/Arithmetic_progression',
     'Wikipedia', 'CC_BY_SA', 'active', 50,
     array['number-series','arithmetic-progression','math','algebra']);
end $$;
