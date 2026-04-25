-- =====================================================================
-- 0004_tests.sql — Phase 3: mock tests.
--
-- Schema:
--   tests / test_sections / questions / options  (content, read by authenticated)
--   attempts / attempt_answers                    (per-user, owner-scoped RLS)
--
-- Server-authoritative scoring via RPCs:
--   start_attempt(p_test_id)     — creates or resumes IN_PROGRESS attempt;
--                                   expires stale rows past their deadline.
--   submit_attempt(p_attempt_id) — reads attempt_answers, computes score,
--                                   flips status='SUBMITTED'; then a trigger
--                                   makes attempt_answers immutable.
--
-- The section_breakdown JSONB shape is LOCKED. See docs/phase3-partA.md.
-- All consumers (Part B results/review, percentile calc) read from this
-- shape only. Do not add parallel shapes.
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1) Content tables
-- ---------------------------------------------------------------------

create table if not exists public.tests (
  id                          uuid primary key default gen_random_uuid(),
  slug                        text not null unique,
  title_en                    text not null,
  title_hi                    text,
  kind                        text not null check (kind in ('CBT1_FULL','CBT2_FULL','SECTIONAL','PYQ','DAILY_DIGEST')),
  exam_target                 text not null check (exam_target in ('NTPC_CBT1','NTPC_CBT2')),
  total_questions             int  not null check (total_questions > 0),
  total_minutes               int  not null check (total_minutes > 0),
  negative_marking_fraction   real not null default 0.3333 check (negative_marking_fraction >= 0 and negative_marking_fraction <= 1),
  is_pro                      boolean not null default false,
  status                      text not null default 'active' check (status in ('active','stale','draft')),
  published_at                timestamptz not null default now(),
  created_at                  timestamptz not null default now(),
  updated_at                  timestamptz not null default now()
);

comment on table public.tests is 'Phase 3 — mock tests. negative_marking_fraction defaults to 1/3 per RRB NTPC pattern.';

create trigger tests_updated_at
  before update on public.tests
  for each row execute function public.set_updated_at();

create table if not exists public.test_sections (
  id              uuid primary key default gen_random_uuid(),
  test_id         uuid not null references public.tests(id) on delete cascade,
  title_en        text not null,
  title_hi        text,
  question_count  int  not null check (question_count > 0),
  display_order   smallint not null default 0,
  subject_hint    text not null check (subject_hint in ('math','reason','ga','gs','eng','mixed')),
  created_at      timestamptz not null default now()
);

create index test_sections_by_test_order on public.test_sections(test_id, display_order);

create table if not exists public.questions (
  id              uuid primary key default gen_random_uuid(),
  section_id      uuid not null references public.test_sections(id) on delete cascade,
  display_order   smallint not null default 0,
  stem_en         text not null check (length(stem_en) > 0),
  stem_hi         text,
  explanation_en  text,
  explanation_hi  text,
  difficulty      text not null default 'MEDIUM' check (difficulty in ('EASY','MEDIUM','HARD')),
  tags            text[] not null default '{}'::text[],
  source          text not null,
  license         text not null default 'ORIGINAL',
  status          text not null default 'active' check (status in ('active','stale','draft')),
  created_at      timestamptz not null default now(),
  updated_at      timestamptz not null default now()
);

create index questions_by_section_order on public.questions(section_id, status, display_order);

create trigger questions_updated_at
  before update on public.questions
  for each row execute function public.set_updated_at();

create table if not exists public.options (
  id           uuid primary key default gen_random_uuid(),
  question_id  uuid not null references public.questions(id) on delete cascade,
  label        text not null check (label in ('A','B','C','D')),
  text_en      text not null check (length(text_en) > 0),
  text_hi      text,
  is_correct   boolean not null default false,
  created_at   timestamptz not null default now(),
  updated_at   timestamptz not null default now(),
  unique (question_id, label)
);

-- Exactly ONE correct option per question. Declarative, race-free, no trigger.
-- (Partial unique index beats a trigger: serializable under concurrent inserts
-- without LOCK TABLE; a CHECK constraint can't reference sibling rows.)
create unique index options_one_correct_per_question
  on public.options(question_id)
  where is_correct = true;

create trigger options_updated_at
  before update on public.options
  for each row execute function public.set_updated_at();

-- ---------------------------------------------------------------------
-- 2) User attempts
-- ---------------------------------------------------------------------

create table if not exists public.attempts (
  id                   uuid primary key default gen_random_uuid(),
  user_id              uuid not null references auth.users(id) on delete cascade,
  test_id              uuid not null references public.tests(id) on delete restrict,
  started_at           timestamptz not null default now(),
  submitted_at         timestamptz,
  server_deadline_at   timestamptz not null,
  score                real,
  max_score            real,
  correct_count        int,
  wrong_count          int,
  skipped_count        int,
  section_breakdown    jsonb,
  status               text not null default 'IN_PROGRESS' check (status in ('IN_PROGRESS','SUBMITTED','EXPIRED','ABANDONED')),
  created_at           timestamptz not null default now(),
  updated_at           timestamptz not null default now()
);

comment on table public.attempts is
$$Per-user test attempts. section_breakdown JSONB shape is LOCKED:
  { "sections": [ { "section_id": uuid, "subject_hint": str,
                    "attempted": int, "correct": int, "wrong": int,
                    "skipped": int, "score": float, "max_score": float } ] }
Consumers must read from this shape only.$$;

create index attempts_by_user_test on public.attempts(user_id, test_id, status);
create index attempts_in_progress_by_deadline
  on public.attempts(server_deadline_at) where status = 'IN_PROGRESS';

create trigger attempts_updated_at
  before update on public.attempts
  for each row execute function public.set_updated_at();

create table if not exists public.attempt_answers (
  attempt_id          uuid not null references public.attempts(id) on delete cascade,
  question_id         uuid not null references public.questions(id) on delete restrict,
  selected_option_id  uuid references public.options(id) on delete restrict,
  flagged             boolean not null default false,
  answered_at         timestamptz not null default now(),
  primary key (attempt_id, question_id)
);

create index attempt_answers_by_attempt on public.attempt_answers(attempt_id);

-- ---------------------------------------------------------------------
-- 3) Immutability trigger on attempt_answers
--
-- Rule: once attempts.status = 'SUBMITTED', the authenticated role cannot
-- INSERT / UPDATE / DELETE rows in attempt_answers for that attempt.
-- Admin roles (service_role / postgres / supabase_admin) bypass — Phase 6
-- admin tooling and seed scripts need the escape hatch.
-- ---------------------------------------------------------------------

create or replace function public.reject_if_attempt_submitted()
  returns trigger
  language plpgsql
  security invoker
  set search_path = public, pg_temp
as $$
declare
  v_status     text;
  v_attempt_id uuid;
begin
  -- Only the authenticated role (client-facing) is gated. Other roles —
  -- service_role (MCP / edge functions), postgres, supabase_admin — bypass.
  if current_user <> 'authenticated' then
    return coalesce(new, old);
  end if;

  v_attempt_id := coalesce((new).attempt_id, (old).attempt_id);

  -- FOR SHARE: blocks a concurrent submit_attempt from flipping status
  -- while this row write is in flight, without serializing sibling writers.
  select status into v_status
    from public.attempts
   where id = v_attempt_id
   for share;

  if v_status = 'SUBMITTED' then
    raise exception 'attempt % is submitted; answers are immutable', v_attempt_id
      using errcode = '55000';
  end if;

  return coalesce(new, old);
end;
$$;

drop trigger if exists trg_attempt_answers_immutable on public.attempt_answers;
create trigger trg_attempt_answers_immutable
  before insert or update or delete on public.attempt_answers
  for each row execute function public.reject_if_attempt_submitted();

-- ---------------------------------------------------------------------
-- 4) RLS
-- ---------------------------------------------------------------------

alter table public.tests           enable row level security;
alter table public.test_sections   enable row level security;
alter table public.questions       enable row level security;
alter table public.options         enable row level security;
alter table public.attempts        enable row level security;
alter table public.attempt_answers enable row level security;

-- Content tables: authenticated read where active. Pro gate is client-side
-- for Phase 3 (server-side hardening lands with Phase 6 paywall work).
create policy tests_read on public.tests
  for select to authenticated using (status = 'active');

create policy test_sections_read on public.test_sections
  for select to authenticated using (
    exists (select 1 from public.tests t
             where t.id = test_sections.test_id and t.status = 'active')
  );

create policy questions_read on public.questions
  for select to authenticated using (
    status = 'active' and exists (
      select 1 from public.test_sections s
        join public.tests t on t.id = s.test_id
       where s.id = questions.section_id and t.status = 'active'
    )
  );

create policy options_read on public.options
  for select to authenticated using (
    exists (
      select 1 from public.questions q
        join public.test_sections s on s.id = q.section_id
        join public.tests t on t.id = s.test_id
       where q.id = options.question_id
         and q.status = 'active'
         and t.status = 'active'
    )
  );

-- Attempts: owner select/insert/update. No client delete. (select auth.uid())
-- wrapping is the post-2023 Postgres InitPlan idiom — evaluated once per
-- query, not once per row.
create policy attempts_owner_select on public.attempts
  for select to authenticated using ((select auth.uid()) = user_id);

create policy attempts_owner_insert on public.attempts
  for insert to authenticated with check ((select auth.uid()) = user_id);

create policy attempts_owner_update on public.attempts
  for update to authenticated
  using      ((select auth.uid()) = user_id)
  with check ((select auth.uid()) = user_id);

-- attempt_answers: owner via parent attempt. No delete policy — trigger
-- also blocks DELETE from authenticated as belt-and-braces.
create policy attempt_answers_owner_select on public.attempt_answers
  for select to authenticated using (
    exists (select 1 from public.attempts a
             where a.id = attempt_answers.attempt_id
               and a.user_id = (select auth.uid()))
  );

create policy attempt_answers_owner_insert on public.attempt_answers
  for insert to authenticated with check (
    exists (select 1 from public.attempts a
             where a.id = attempt_answers.attempt_id
               and a.user_id = (select auth.uid()))
  );

create policy attempt_answers_owner_update on public.attempt_answers
  for update to authenticated
  using (
    exists (select 1 from public.attempts a
             where a.id = attempt_answers.attempt_id
               and a.user_id = (select auth.uid()))
  )
  with check (
    exists (select 1 from public.attempts a
             where a.id = attempt_answers.attempt_id
               and a.user_id = (select auth.uid()))
  );

-- ---------------------------------------------------------------------
-- 5) RPC: start_attempt(p_test_id) returns attempts
--
-- Idempotency contract:
--   IF an IN_PROGRESS attempt exists for (auth.uid(), p_test_id):
--     and server_deadline_at > now()  → return it (resume)
--     else                             → mark it EXPIRED, create+return new
--   ELSE                               → create+return new
--
-- Clients never choose the deadline. The server computes:
--   server_deadline_at = now() + tests.total_minutes * interval '1 minute'
-- ---------------------------------------------------------------------

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
  v_existing      public.attempts;
  v_new           public.attempts;
begin
  if v_uid is null then
    raise exception 'not authenticated' using errcode = '42501';
  end if;

  select total_minutes, status
    into v_total_minutes, v_test_status
    from public.tests where id = p_test_id;

  if v_total_minutes is null then
    raise exception 'test % not found', p_test_id using errcode = 'P0002';
  end if;
  if v_test_status <> 'active' then
    raise exception 'test % is not active (status=%)', p_test_id, v_test_status
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
    -- Past deadline: EXPIRE and fall through to create a new attempt.
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

-- ---------------------------------------------------------------------
-- 6) RPC: submit_attempt(p_attempt_id) returns attempts
--
-- Server-authoritative scoring. Late submits are accepted (client may have
-- been syncing buffered answers); we score whatever is in attempt_answers.
-- An orphaned IN_PROGRESS attempt past its deadline will be force-submitted
-- by the next call to start_attempt() from the same user on the same test,
-- or manually by admin. A cron sweeper is Phase 6 (see tech debt).
--
-- Tally semantics:
--   attempted = rows in attempt_answers where selected_option_id is not null
--   correct   = attempted ∩ chose an option with is_correct=true
--   wrong     = attempted ∩ chose an option with is_correct=false
--   skipped   = rows in attempt_answers where selected_option_id is null
--               (user visited & explicitly skipped — not the same as 'never visited')
--   score     = correct − wrong * negative_marking_fraction
--   max_score = sum of test_sections.question_count (= total_questions)
-- ---------------------------------------------------------------------

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
      count(aa.question_id) filter (where aa.selected_option_id is not null)                                                                                             as attempted,
      count(aa.question_id) filter (where aa.selected_option_id is not null
                                      and exists (select 1 from public.options o where o.id = aa.selected_option_id and o.is_correct = true))                           as correct,
      count(aa.question_id) filter (where aa.selected_option_id is not null
                                      and exists (select 1 from public.options o where o.id = aa.selected_option_id and o.is_correct = false))                          as wrong,
      count(aa.question_id) filter (where aa.question_id is not null and aa.selected_option_id is null)                                                                  as skipped
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
