-- Recovered from Supabase MCP on 2026-04-25.
-- MCP version: 20260425012925
-- Original application order is determined by version timestamp, not filename.

-- Phase 4 D1 — Daily Digest tables, RLS, and the lazy ensure_today_digest +
-- submit_digest RPCs. No pg_cron, no FCM. Streak state stays canonical on
-- profiles; streak_events is audit-only.

-- ---------------------------------------------------------------------
-- Tables
-- ---------------------------------------------------------------------

create table if not exists public.daily_digests (
  digest_date    date primary key,
  question_ids   uuid[] not null,
  section_plan   jsonb not null default '{"math":2,"reason":2,"ga":3,"gs":2,"eng":1}'::jsonb,
  generated_at   timestamptz not null default now()
);

create table if not exists public.digest_attempts (
  user_id        uuid not null references auth.users(id) on delete cascade,
  digest_date    date not null references public.daily_digests(digest_date) on delete cascade,
  answers        jsonb not null default '[]'::jsonb,
  correct_count  int not null,
  total          int not null,
  submitted_at   timestamptz not null default now(),
  primary key (user_id, digest_date)
);

create index if not exists digest_attempts_user_idx
  on public.digest_attempts (user_id, digest_date desc);

create table if not exists public.streak_events (
  user_id     uuid not null references auth.users(id) on delete cascade,
  event_date  date not null,
  kind        text not null check (kind in
    ('digest_submit','streak_extend','streak_reset','streak_break','digest_short')),
  detail      jsonb,
  created_at  timestamptz not null default now(),
  primary key (user_id, event_date, kind)
);

create index if not exists streak_events_user_idx
  on public.streak_events (user_id, event_date desc);

-- ---------------------------------------------------------------------
-- RLS
-- ---------------------------------------------------------------------

alter table public.daily_digests enable row level security;
drop policy if exists daily_digests_read on public.daily_digests;
create policy daily_digests_read on public.daily_digests
  for select to authenticated using (true);

alter table public.digest_attempts enable row level security;
drop policy if exists digest_attempts_owner_select on public.digest_attempts;
create policy digest_attempts_owner_select on public.digest_attempts
  for select to authenticated using ((select auth.uid()) = user_id);
-- Inserts are via submit_digest() RPC (security invoker), which checks auth.uid();
-- expose insert policy too so the RPC's invoker-mode insert is permitted.
drop policy if exists digest_attempts_owner_insert on public.digest_attempts;
create policy digest_attempts_owner_insert on public.digest_attempts
  for insert to authenticated with check ((select auth.uid()) = user_id);

alter table public.streak_events enable row level security;
drop policy if exists streak_events_owner_select on public.streak_events;
create policy streak_events_owner_select on public.streak_events
  for select to authenticated using ((select auth.uid()) = user_id);
-- Inserts are via the two RPCs only — both run security invoker; the policy
-- below permits owner inserts.
drop policy if exists streak_events_owner_insert on public.streak_events;
create policy streak_events_owner_insert on public.streak_events
  for insert to authenticated with check ((select auth.uid()) = user_id);

-- ---------------------------------------------------------------------
-- RPC: ensure_today_digest(p_date) — idempotent UPSERT-style picker.
--
-- Picks 10 questions per the locked section_plan with weighted random
-- by lowest historical submit count (across attempt_answers ∪
-- daily_digests.question_ids). Dedup vs. previous 7 days. Three-stage
-- fallback so it ALWAYS returns 10 if 10 active questions exist:
--   1) per-section quota with EASY/MEDIUM + dedup
--   2) any-section pool, EASY/MEDIUM + dedup
--   3) any-section pool, any difficulty, ignore dedup
-- Section shortage is logged via streak_events kind='digest_short'.
-- ---------------------------------------------------------------------

create or replace function public.ensure_today_digest(p_date date)
  returns public.daily_digests
  language plpgsql
  security definer
  set search_path = public, pg_temp
as $$
declare
  v_existing       public.daily_digests;
  v_uid            uuid := auth.uid();
  v_section_plan   jsonb := '{"math":2,"reason":2,"ga":3,"gs":2,"eng":1}'::jsonb;
  v_picked         uuid[] := '{}'::uuid[];
  v_section        text;
  v_needed         int;
  v_actual         int;
  v_recent_qs      uuid[];
  v_picks          uuid[];
  v_total_needed   int := 10;
  v_remaining      int;
begin
  select * into v_existing from public.daily_digests where digest_date = p_date;
  if found then
    return v_existing;
  end if;

  select coalesce(array_agg(distinct qid), '{}'::uuid[])
    into v_recent_qs
    from (
      select unnest(question_ids) as qid
        from public.daily_digests
       where digest_date >= p_date - interval '7 days'
         and digest_date <  p_date
    ) r;

  for v_section, v_needed in
    select key, value::int from jsonb_each_text(v_section_plan)
  loop
    select array_agg(qid) into v_picks
      from (
        select q.id as qid
          from public.questions q
          join public.test_sections ts on ts.id = q.section_id
          join public.tests t on t.id = ts.test_id
          left join (
            select aa.question_id, count(*) as att_count
              from public.attempt_answers aa
              join public.attempts a on a.id = aa.attempt_id
             where a.status = 'SUBMITTED'
             group by aa.question_id
          ) ac on ac.question_id = q.id
          left join (
            select unnest(question_ids) as question_id, count(*) as dig_count
              from public.daily_digests
             group by 1
          ) dc on dc.question_id = q.id
         where q.status = 'active'
           and q.difficulty in ('EASY','MEDIUM')
           and t.status = 'active'
           and t.kind <> 'PYQ_LINK'
           and ts.subject_hint = v_section
           and not (q.id = any(v_recent_qs))
         order by (coalesce(ac.att_count, 0) + coalesce(dc.dig_count, 0)) asc, random() asc
         limit v_needed
      ) sub;

    v_actual := coalesce(array_length(v_picks, 1), 0);
    v_picked := v_picked || coalesce(v_picks, '{}'::uuid[]);

    if v_actual < v_needed and v_uid is not null then
      insert into public.streak_events (user_id, event_date, kind, detail)
      values (v_uid, p_date, 'digest_short',
              jsonb_build_object('section', v_section,
                                 'requested', v_needed,
                                 'actual', v_actual))
      on conflict (user_id, event_date, kind) do nothing;
    end if;
  end loop;

  -- Stage 2: fill remainder from any active EASY/MEDIUM, still avoiding dedup.
  v_remaining := v_total_needed - coalesce(array_length(v_picked, 1), 0);
  if v_remaining > 0 then
    select array_agg(qid) into v_picks
      from (
        select q.id as qid
          from public.questions q
          join public.test_sections ts on ts.id = q.section_id
          join public.tests t on t.id = ts.test_id
         where q.status = 'active'
           and q.difficulty in ('EASY','MEDIUM')
           and t.status = 'active'
           and t.kind <> 'PYQ_LINK'
           and not (q.id = any(v_picked))
           and not (q.id = any(v_recent_qs))
         order by random()
         limit v_remaining
      ) sub;
    v_picked := v_picked || coalesce(v_picks, '{}'::uuid[]);
  end if;

  -- Stage 3: ignore dedup if we still don't have 10.
  v_remaining := v_total_needed - coalesce(array_length(v_picked, 1), 0);
  if v_remaining > 0 then
    select array_agg(qid) into v_picks
      from (
        select q.id as qid
          from public.questions q
          join public.test_sections ts on ts.id = q.section_id
          join public.tests t on t.id = ts.test_id
         where q.status = 'active'
           and t.status = 'active'
           and t.kind <> 'PYQ_LINK'
           and not (q.id = any(v_picked))
         order by random()
         limit v_remaining
      ) sub;
    v_picked := v_picked || coalesce(v_picks, '{}'::uuid[]);
  end if;

  insert into public.daily_digests (digest_date, question_ids, section_plan)
  values (p_date, v_picked, v_section_plan)
  on conflict (digest_date) do update
    set question_ids = excluded.question_ids,
        section_plan = excluded.section_plan
  returning * into v_existing;

  return v_existing;
end;
$$;

grant execute on function public.ensure_today_digest(date) to authenticated;

-- ---------------------------------------------------------------------
-- RPC: submit_digest(p_date, p_answers) — server scoring + streak advance.
-- p_answers shape: [{"question_id": uuid, "selected_option_id": uuid|null}, ...]
-- Idempotent on (user_id, digest_date).
-- ---------------------------------------------------------------------

create or replace function public.submit_digest(p_date date, p_answers jsonb)
  returns public.profiles
  language plpgsql
  security invoker
  set search_path = public, pg_temp
as $$
declare
  v_uid         uuid := auth.uid();
  v_digest      public.daily_digests;
  v_total       int;
  v_correct     int := 0;
  v_answer      jsonb;
  v_qid         uuid;
  v_oid         uuid;
  v_is_correct  boolean;
  v_scored      jsonb := '[]'::jsonb;
  v_existing    public.digest_attempts;
  v_profile     public.profiles;
  v_yesterday   date := p_date - interval '1 day';
  v_old_streak  int;
  v_old_best    int;
begin
  if v_uid is null then
    raise exception 'not authenticated' using errcode = '42501';
  end if;

  select * into v_existing from public.digest_attempts
   where user_id = v_uid and digest_date = p_date;
  if found then
    select * into v_profile from public.profiles where id = v_uid;
    return v_profile;
  end if;

  select * into v_digest from public.daily_digests where digest_date = p_date;
  if not found then
    raise exception 'no digest for date %', p_date using errcode = 'P0002';
  end if;

  v_total := coalesce(array_length(v_digest.question_ids, 1), 0);

  for v_answer in select * from jsonb_array_elements(p_answers)
  loop
    v_qid := (v_answer->>'question_id')::uuid;
    v_oid := nullif(v_answer->>'selected_option_id', '')::uuid;

    if not (v_qid = any(v_digest.question_ids)) then
      continue;
    end if;

    if v_oid is null then
      v_is_correct := false;
    else
      select is_correct into v_is_correct from public.options where id = v_oid;
      v_is_correct := coalesce(v_is_correct, false);
    end if;

    if v_is_correct then v_correct := v_correct + 1; end if;

    v_scored := v_scored || jsonb_build_array(jsonb_build_object(
      'question_id', v_qid,
      'selected_option_id', v_oid,
      'is_correct', v_is_correct
    ));
  end loop;

  insert into public.digest_attempts (user_id, digest_date, answers, correct_count, total, submitted_at)
  values (v_uid, p_date, v_scored, v_correct, v_total, now());

  select * into v_profile from public.profiles where id = v_uid for update;
  v_old_streak := coalesce(v_profile.streak_current, 0);
  v_old_best   := coalesce(v_profile.streak_best, 0);

  if v_profile.last_study_date = p_date then
    null;
  elsif v_profile.last_study_date = v_yesterday then
    update public.profiles
       set streak_current   = v_old_streak + 1,
           streak_best      = greatest(v_old_best, v_old_streak + 1),
           last_study_date  = p_date,
           updated_at       = now()
     where id = v_uid;
    insert into public.streak_events (user_id, event_date, kind, detail)
    values (v_uid, p_date, 'streak_extend',
            jsonb_build_object('to', v_old_streak + 1, 'from', v_old_streak))
    on conflict (user_id, event_date, kind) do nothing;
  else
    update public.profiles
       set streak_current   = 1,
           streak_best      = greatest(v_old_best, 1),
           last_study_date  = p_date,
           updated_at       = now()
     where id = v_uid;
    if v_old_streak > 0 then
      insert into public.streak_events (user_id, event_date, kind, detail)
      values (v_uid, p_date, 'streak_break',
              jsonb_build_object('broken_at', v_old_streak,
                                 'last_study_date', v_profile.last_study_date))
      on conflict (user_id, event_date, kind) do nothing;
    end if;
  end if;

  insert into public.streak_events (user_id, event_date, kind, detail)
  values (v_uid, p_date, 'digest_submit',
          jsonb_build_object('correct', v_correct, 'total', v_total))
  on conflict (user_id, event_date, kind) do nothing;

  select * into v_profile from public.profiles where id = v_uid;
  return v_profile;
end;
$$;

grant execute on function public.submit_digest(date, jsonb) to authenticated;