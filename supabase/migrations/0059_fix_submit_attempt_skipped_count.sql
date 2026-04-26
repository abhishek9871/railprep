-- Count unanswered questions as skipped even when the client never wrote an
-- attempt_answers row. This preserves the locked section_breakdown JSON shape.
create or replace function public.submit_attempt(p_attempt_id uuid)
  returns public.attempts
  language plpgsql
  security invoker
  set search_path = public, pg_temp
as $$
declare
  v_uid      uuid := auth.uid();
  v_att      public.attempts;
  v_test     public.tests;
  v_neg      real;
  v_correct  int  := 0;
  v_wrong    int  := 0;
  v_skipped  int  := 0;
  v_score    real := 0;
  v_max      real := 0;
  v_sections jsonb;
begin
  if v_uid is null then
    raise exception 'not authenticated' using errcode = '42501';
  end if;

  select * into v_att
    from public.attempts
   where id = p_attempt_id
     and user_id = v_uid
     and status = 'IN_PROGRESS'
   for update;

  if not found then
    raise exception 'attempt not found, not owned, or already submitted'
      using errcode = '42501';
  end if;

  select * into v_test from public.tests where id = v_att.test_id;
  v_neg := v_test.negative_marking_fraction;

  with sec as (
    select s.id as section_id, s.subject_hint, s.question_count as max_q
      from public.test_sections s
     where s.test_id = v_att.test_id
  ),
  sec_q as (
    select s.section_id, s.subject_hint, s.max_q, q.id as question_id
      from sec s
      left join public.questions q
        on q.section_id = s.section_id and q.status = 'active'
  ),
  tally as (
    select
      sq.section_id,
      sq.subject_hint,
      sq.max_q,
      count(aa.question_id) filter (where aa.selected_option_id is not null) as attempted,
      count(aa.question_id) filter (
        where aa.selected_option_id is not null
          and exists (
            select 1 from public.options o
             where o.id = aa.selected_option_id and o.is_correct = true
          )
      ) as correct,
      count(aa.question_id) filter (
        where aa.selected_option_id is not null
          and exists (
            select 1 from public.options o
             where o.id = aa.selected_option_id and o.is_correct = false
          )
      ) as wrong,
      greatest(
        count(sq.question_id)
          - count(aa.question_id) filter (where aa.selected_option_id is not null),
        0
      ) as skipped
    from sec_q sq
    left join public.attempt_answers aa
      on aa.attempt_id = v_att.id and aa.question_id = sq.question_id
    group by sq.section_id, sq.subject_hint, sq.max_q
  ),
  rolled as (
    select
      section_id, subject_hint, max_q, attempted, correct, wrong, skipped,
      (correct::real - wrong::real * v_neg) as score,
      max_q::real                           as max_score
    from tally
  )
  select
    coalesce(sum(correct), 0)::int,
    coalesce(sum(wrong),   0)::int,
    coalesce(sum(skipped), 0)::int,
    coalesce(sum(score),   0)::real,
    coalesce(sum(max_score), 0)::real,
    jsonb_build_object(
      'sections',
      coalesce(
        jsonb_agg(
          jsonb_build_object(
            'section_id',   section_id,
            'subject_hint', subject_hint,
            'attempted',    attempted,
            'correct',      correct,
            'wrong',        wrong,
            'skipped',      skipped,
            'score',        score,
            'max_score',    max_score
          )
          order by subject_hint
        ),
        '[]'::jsonb
      )
    )
  into v_correct, v_wrong, v_skipped, v_score, v_max, v_sections
  from rolled;

  update public.attempts
     set submitted_at      = now(),
         status            = 'SUBMITTED',
         correct_count     = v_correct,
         wrong_count       = v_wrong,
         skipped_count     = v_skipped,
         score             = v_score,
         max_score         = v_max,
         section_breakdown = v_sections
   where id = v_att.id
   returning * into v_att;

  return v_att;
end;
$$;

grant  execute on function public.submit_attempt(uuid) to authenticated;
revoke execute on function public.submit_attempt(uuid) from anon, public;
