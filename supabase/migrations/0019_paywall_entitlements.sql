-- Phase F — server-side paywall enforcement.
-- tests.is_pro is no longer client-only. start_attempt() rejects pro tests
-- unless the caller has a current entitlement. Existing users get a one-time
-- 7-day TRIAL for pre-launch testability; new users get TRIAL on signup.

do $$
begin
  create type public.entitlement_kind as enum ('TRIAL', 'PRO_MONTHLY', 'PRO_QUARTERLY');
exception when duplicate_object then null;
end $$;

create table if not exists public.user_entitlements (
  user_id uuid not null references auth.users(id) on delete cascade,
  kind public.entitlement_kind not null,
  granted_at timestamptz not null default now(),
  expires_at timestamptz,
  source text not null,
  primary key (user_id, kind)
);

create index if not exists user_entitlements_active_idx
  on public.user_entitlements (user_id, expires_at);

alter table public.user_entitlements enable row level security;

drop policy if exists "user_entitlements_select_own" on public.user_entitlements;
create policy "user_entitlements_select_own" on public.user_entitlements
  for select to authenticated
  using ((select auth.uid()) = user_id);

create or replace function public.user_has_active_entitlement(p_user_id uuid)
  returns boolean
  language sql
  security definer
  set search_path = public, pg_temp
as $$
  select exists (
    select 1
      from public.user_entitlements e
     where e.user_id = p_user_id
       and (e.expires_at is null or e.expires_at > now())
  );
$$;

revoke execute on function public.user_has_active_entitlement(uuid)
  from public, anon, authenticated;

insert into public.user_entitlements (user_id, kind, granted_at, expires_at, source)
select u.id, 'TRIAL'::public.entitlement_kind, now(), now() + interval '7 days', 'migration_existing_user'
  from auth.users u
on conflict (user_id, kind) do nothing;

create or replace function public.handle_new_user()
  returns trigger
  language plpgsql
  security definer
  set search_path = public
as $$
begin
  insert into public.profiles (id)
  values (new.id)
  on conflict (id) do nothing;

  insert into public.user_entitlements (user_id, kind, granted_at, expires_at, source)
  values (new.id, 'TRIAL', now(), now() + interval '7 days', 'signup_auto_trial')
  on conflict (user_id, kind) do nothing;

  return new;
end;
$$;

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
  v_test_is_pro   boolean;
  v_existing      public.attempts;
  v_new           public.attempts;
begin
  if v_uid is null then
    raise exception 'not authenticated' using errcode = '42501';
  end if;

  select total_minutes, status, kind, is_pro
    into v_total_minutes, v_test_status, v_test_kind, v_test_is_pro
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
  if coalesce(v_test_is_pro, false) and not public.user_has_active_entitlement(v_uid) then
    raise exception 'PRO_REQUIRED' using errcode = 'P0003';
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

  if exists (
    select 1
      from unnest(v_digest.question_ids) as dq(question_id)
      join public.questions q on q.id = dq.question_id
      join public.test_sections s on s.id = q.section_id
      join public.tests t on t.id = s.test_id
     where coalesce(t.is_pro, false)
  ) and not public.user_has_active_entitlement(v_uid) then
    raise exception 'PRO_REQUIRED' using errcode = 'P0003';
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
