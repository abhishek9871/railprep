-- =====================================================================
-- 0005_pyq_links_and_explanations.sql — Phase 3 Part C.
--
-- Two concerns in one migration:
--   (1) PYQ library-link model — `tests` gets a new `PYQ_LINK` kind with
--       external_url / source_language / source_attribution columns. Rows
--       with this kind point at adda247-hosted PDFs rendered on-device;
--       they are NEVER attempted via start_attempt (guarded below).
--   (2) Two-layer explanations for Stage 4 originals — questions gain
--       method + concept bilingual columns; options gain trap_reason
--       bilingual columns. The original explanation_en/hi columns are
--       kept for the 30-Q sample test's back-compat; Stage 4 originals
--       must use method/concept instead.
-- =====================================================================

-- (1) PYQ_LINK kind + link columns ------------------------------------

alter table public.tests drop constraint if exists tests_kind_check;
alter table public.tests add constraint tests_kind_check
  check (kind in (
    'CBT1_FULL','CBT2_FULL','SECTIONAL','PYQ','DAILY_DIGEST','PYQ_LINK'
  ));

alter table public.tests
  add column if not exists external_url       text,
  add column if not exists source_language    text,
  add column if not exists source_attribution text;

alter table public.tests drop constraint if exists tests_source_language_check;
alter table public.tests add constraint tests_source_language_check
  check (source_language is null or source_language in ('en','hi','bilingual'));

-- Shape guard: PYQ_LINK rows must carry external_url; other kinds must not.
alter table public.tests drop constraint if exists tests_pyq_link_url_shape;
alter table public.tests add constraint tests_pyq_link_url_shape
  check (
    (kind = 'PYQ_LINK' and external_url is not null)
    or (kind <> 'PYQ_LINK' and external_url is null)
  );

-- (2) Extend questions with 2-layer explanations ---------------------

alter table public.questions
  add column if not exists explanation_method_en  text,
  add column if not exists explanation_concept_en text,
  add column if not exists explanation_method_hi  text,
  add column if not exists explanation_concept_hi text;

-- (3) Extend options with trap analysis ------------------------------

alter table public.options
  add column if not exists trap_reason_en text,
  add column if not exists trap_reason_hi text;

-- (4) Guard start_attempt: PYQ_LINK rows are not attemptable --------
--
-- We re-emit the entire function (CREATE OR REPLACE); the only new
-- logic is the kind check right after loading the test row. Kept
-- identical otherwise to 0004's definition.

create or replace function public.start_attempt(p_test_id uuid)
  returns public.attempts
  language plpgsql
  security invoker
  set search_path = public, pg_temp
as $$
declare
  v_uid           uuid := auth.uid();
  v_total_minutes int;
  v_test_status   text;
  v_test_kind     text;
  v_existing      public.attempts;
  v_new           public.attempts;
begin
  if v_uid is null then
    raise exception 'not authenticated' using errcode = '42501';
  end if;

  select total_minutes, status, kind
    into v_total_minutes, v_test_status, v_test_kind
    from public.tests where id = p_test_id;

  if v_total_minutes is null then
    raise exception 'test % not found', p_test_id using errcode = 'P0002';
  end if;
  if v_test_status <> 'active' then
    raise exception 'test % is not active (status=%)', p_test_id, v_test_status
      using errcode = 'P0001';
  end if;
  if v_test_kind = 'PYQ_LINK' then
    raise exception 'test % is a PYQ_LINK and is not attemptable; render externally', p_test_id
      using errcode = 'P0001';
  end if;

  select * into v_existing
    from public.attempts
   where user_id = v_uid
     and test_id = p_test_id
     and status  = 'IN_PROGRESS'
   order by started_at desc
   limit 1
   for update;

  if found then
    if v_existing.server_deadline_at > now() then
      return v_existing;
    end if;
    update public.attempts
       set status       = 'EXPIRED',
           submitted_at = coalesce(submitted_at, now())
     where id = v_existing.id;
  end if;

  insert into public.attempts (user_id, test_id, started_at, server_deadline_at, status)
  values (v_uid, p_test_id, now(),
          now() + (v_total_minutes || ' minutes')::interval,
          'IN_PROGRESS')
  returning * into v_new;

  return v_new;
end;
$$;

grant  execute on function public.start_attempt(uuid) to authenticated;
revoke execute on function public.start_attempt(uuid) from anon, public;
