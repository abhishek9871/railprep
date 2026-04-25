-- =====================================================================
-- 0001_profiles.sql — Phase 1: user profile with onboarding fields.
-- Applies 1:1 to auth.users via a SECURITY DEFINER trigger.
-- =====================================================================

-- 1) Table ---------------------------------------------------------------

create table if not exists public.profiles (
  id                    uuid primary key references auth.users(id) on delete cascade,
  display_name          text,
  avatar_url            text,
  language              text not null default 'en',
  state                 text,
  exam_target           text check (exam_target in ('NTPC_CBT1','NTPC_CBT2')),
  exam_target_date      date,
  daily_minutes         int  not null default 60 check (daily_minutes between 30 and 240),
  qualification         text check (qualification in ('12TH','GRADUATE')),
  category              text check (category in ('UR','OBC','SC','ST','EWS')),
  dob                   date,
  onboarding_complete   boolean not null default false,
  xp                    int not null default 0,
  level                 int not null default 1,
  streak_current        int not null default 0,
  streak_best           int not null default 0,
  last_study_date       date,
  created_at            timestamptz not null default now(),
  updated_at            timestamptz not null default now()
);

comment on table public.profiles is 'One row per auth.users — Phase 1 of the RailPrep schema.';

-- 2) Row-Level Security --------------------------------------------------

alter table public.profiles enable row level security;

-- Owner-only select/update. Inserts are trigger-only; deletes are service-role only.
drop policy if exists "profiles_own_select" on public.profiles;
create policy "profiles_own_select" on public.profiles
  for select using (auth.uid() = id);

drop policy if exists "profiles_own_update" on public.profiles;
create policy "profiles_own_update" on public.profiles
  for update using (auth.uid() = id) with check (auth.uid() = id);

-- 3) Trigger: create a profile row on auth.users insert -----------------

create or replace function public.handle_new_user()
  returns trigger
  language plpgsql
  security definer
  set search_path = public
as $$
begin
  insert into public.profiles (id)
  values (new.id)
  on conflict (id) do nothing;  -- defensive: re-fires or races
  return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
  after insert on auth.users
  for each row execute function public.handle_new_user();

-- 4) Trigger: keep updated_at current -----------------------------------

create or replace function public.set_updated_at()
  returns trigger
  language plpgsql
as $$
begin
  new.updated_at = now();
  return new;
end;
$$;

drop trigger if exists profiles_set_updated_at on public.profiles;
create trigger profiles_set_updated_at
  before update on public.profiles
  for each row execute function public.set_updated_at();
