-- Phase F — question search and per-tag accuracy RPCs.
-- Search stays server-side and paginated. Accuracy is SECURITY INVOKER and
-- aggregates only the calling user's submitted attempts through RLS-safe joins.

alter table public.questions
  add column if not exists search_vector tsvector;

create or replace function public.refresh_question_search_vector()
  returns trigger
  language plpgsql
  set search_path = public, pg_temp
as $$
begin
  new.search_vector :=
    to_tsvector(
      'simple',
      coalesce(new.stem_en, '') || ' ' ||
      coalesce(new.stem_hi, '') || ' ' ||
      coalesce(new.explanation_en, '') || ' ' ||
      coalesce(new.explanation_hi, '') || ' ' ||
      coalesce(new.explanation_method_en, '') || ' ' ||
      coalesce(new.explanation_concept_en, '') || ' ' ||
      coalesce(new.explanation_method_hi, '') || ' ' ||
      coalesce(new.explanation_concept_hi, '') || ' ' ||
      array_to_string(new.tags, ' ')
    );
  return new;
end;
$$;

drop trigger if exists questions_refresh_search_vector on public.questions;
create trigger questions_refresh_search_vector
  before insert or update of stem_en, stem_hi, explanation_en, explanation_hi,
    explanation_method_en, explanation_concept_en, explanation_method_hi,
    explanation_concept_hi, tags
  on public.questions
  for each row execute function public.refresh_question_search_vector();

update public.questions q
   set stem_en = q.stem_en
 where q.search_vector is null;

create index if not exists questions_search_vector_idx
  on public.questions using gin (search_vector);

create or replace function public.search_questions(
  p_query text,
  p_filters jsonb default '{}'::jsonb,
  p_limit int default 50,
  p_offset int default 0
)
  returns table (
    question_id uuid,
    test_id uuid,
    test_slug text,
    test_title_en text,
    test_kind text,
    exam_target text,
    section_title_en text,
    subject_hint text,
    display_order int,
    stem_en text,
    stem_hi text,
    difficulty text,
    tags text[],
    source text,
    is_bookmarked boolean,
    was_wrong boolean
  )
  language sql
  security invoker
  set search_path = public, pg_temp
as $$
  with query_input as (
    select
      nullif(trim(coalesce(p_query, '')), '') as q,
      least(greatest(coalesce(p_limit, 50), 1), 100) as lim,
      greatest(coalesce(p_offset, 0), 0) as off
  ),
  fulltext as (
    select
      qi.q,
      case when qi.q is null then null else websearch_to_tsquery('simple', qi.q) end as tsq,
      qi.lim,
      qi.off
    from query_input qi
  )
  select
    q.id as question_id,
    t.id as test_id,
    t.slug as test_slug,
    t.title_en as test_title_en,
    t.kind as test_kind,
    t.exam_target,
    s.title_en as section_title_en,
    s.subject_hint,
    q.display_order::int,
    q.stem_en,
    q.stem_hi,
    q.difficulty,
    q.tags,
    q.source,
    qb.question_id is not null as is_bookmarked,
    exists (
      select 1
        from public.attempt_answers aa
        join public.attempts a on a.id = aa.attempt_id
        join public.options o on o.id = aa.selected_option_id
       where a.user_id = (select auth.uid())
         and a.status = 'SUBMITTED'
         and aa.question_id = q.id
         and o.is_correct = false
    ) as was_wrong
  from fulltext f
  join public.questions q on q.status = 'active'
  join public.test_sections s on s.id = q.section_id
  join public.tests t on t.id = s.test_id and t.status = 'active'
  left join public.question_bookmarks qb
    on qb.question_id = q.id and qb.user_id = (select auth.uid())
  where
    (
      f.q is null
      or q.search_vector @@ f.tsq
      or exists (
        select 1 from public.options o
         where o.question_id = q.id
           and to_tsvector('simple', coalesce(o.text_en, '') || ' ' || coalesce(o.text_hi, '')) @@ f.tsq
      )
      or q.stem_en ilike '%' || f.q || '%'
      or coalesce(q.stem_hi, '') ilike '%' || f.q || '%'
    )
    and (
      coalesce(p_filters->>'source', '') = ''
      or q.source = p_filters->>'source'
      or t.kind = p_filters->>'source'
    )
    and (
      coalesce(p_filters->>'subject_hint', '') = ''
      or s.subject_hint = p_filters->>'subject_hint'
    )
    and (
      coalesce(p_filters->>'difficulty', '') = ''
      or q.difficulty = p_filters->>'difficulty'
    )
    and (
      coalesce((p_filters->>'bookmarked')::boolean, false) = false
      or qb.question_id is not null
    )
    and (
      coalesce((p_filters->>'wrong_history')::boolean, false) = false
      or exists (
        select 1
          from public.attempt_answers aa
          join public.attempts a on a.id = aa.attempt_id
          join public.options o on o.id = aa.selected_option_id
         where a.user_id = (select auth.uid())
           and a.status = 'SUBMITTED'
           and aa.question_id = q.id
           and o.is_correct = false
      )
    )
  order by
    case when f.tsq is null then 0 else ts_rank(q.search_vector, f.tsq) end desc,
    t.published_at desc,
    s.display_order asc,
    q.display_order asc
  limit (select lim from fulltext)
  offset (select off from fulltext);
$$;

grant execute on function public.search_questions(text, jsonb, int, int) to authenticated;

create or replace function public.get_topic_accuracy()
  returns table (
    tag text,
    subject_hint text,
    attempted int,
    correct int,
    accuracy_pct numeric
  )
  language sql
  security invoker
  set search_path = public, pg_temp
as $$
  select
    tag,
    s.subject_hint,
    count(*)::int as attempted,
    count(*) filter (where o.is_correct)::int as correct,
    round((100.0 * count(*) filter (where o.is_correct) / nullif(count(*), 0))::numeric, 1) as accuracy_pct
  from public.attempt_answers aa
  join public.attempts a on a.id = aa.attempt_id
  join public.questions q on q.id = aa.question_id
  join public.test_sections s on s.id = q.section_id
  join public.options o on o.id = aa.selected_option_id
  cross join lateral unnest(q.tags) as tag
  where a.user_id = (select auth.uid())
    and a.status = 'SUBMITTED'
    and aa.selected_option_id is not null
    and q.status = 'active'
  group by tag, s.subject_hint
  order by accuracy_pct asc nulls last, attempted desc, tag asc;
$$;

grant execute on function public.get_topic_accuracy() to authenticated;
