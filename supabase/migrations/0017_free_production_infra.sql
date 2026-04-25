-- Phase F — free production infrastructure.
-- All infra stays inside Supabase free-tier primitives: pg_cron, bundled HTTP,
-- RLS-protected tables, and short-retention logs.

-- ---------------------------------------------------------------------
-- Heartbeat: keeps the free Supabase project active and leaves only the
-- last 30 rows for operational confirmation.
-- ---------------------------------------------------------------------

create table if not exists public.heartbeat (
  id bigserial primary key,
  run_at timestamptz not null default now()
);

alter table public.heartbeat enable row level security;

create or replace function public.run_heartbeat()
  returns void
  language plpgsql
  security definer
  set search_path = public, pg_temp
as $$
begin
  insert into public.heartbeat default values;

  delete from public.heartbeat
   where id not in (
     select id from public.heartbeat order by run_at desc, id desc limit 30
   );
end;
$$;

revoke execute on function public.run_heartbeat() from public, anon, authenticated;

-- ---------------------------------------------------------------------
-- PYQ_LINK link watchdog. We link to third-party public PDFs instead of
-- re-hosting; stale-over-delete applies when links fail.
-- ---------------------------------------------------------------------

create table if not exists public.watchdog_runs (
  id uuid primary key default gen_random_uuid(),
  run_at timestamptz not null default now(),
  total_checked int not null,
  failed_count int not null,
  failed_slugs jsonb not null default '[]'::jsonb
);

create index if not exists watchdog_runs_recent_idx
  on public.watchdog_runs (run_at desc);

alter table public.watchdog_runs enable row level security;

create or replace function public.run_pyq_link_watchdog()
  returns public.watchdog_runs
  language plpgsql
  security definer
  set search_path = public, extensions, pg_temp
as $$
declare
  v_row record;
  v_resp extensions.http_response;
  v_total int := 0;
  v_failed jsonb := '[]'::jsonb;
  v_failed_count int := 0;
  v_reason text;
  v_run public.watchdog_runs;
begin
  perform extensions.http_set_curlopt('CURLOPT_TIMEOUT_MS', '8000');
  perform extensions.http_set_curlopt('CURLOPT_FOLLOWLOCATION', '1');
  perform extensions.http_set_curlopt('CURLOPT_MAXREDIRS', '3');

  for v_row in
    select id, slug, external_url
      from public.tests
     where kind = 'PYQ_LINK'
       and status = 'active'
       and external_url is not null
     order by slug
  loop
    v_total := v_total + 1;
    v_reason := null;

    begin
      v_resp := extensions.http_head(v_row.external_url);

      if v_resp.status < 200 or v_resp.status >= 400 then
        v_reason := 'http_' || v_resp.status::text;
      elsif coalesce(v_resp.content_type, '') !~* 'pdf' then
        -- Some CDNs omit content-type on HEAD. Confirm with a GET before
        -- staling, because false-staling an exam paper is worse than one
        -- extra request in a daily maintenance job.
        v_resp := extensions.http_get(v_row.external_url);
        if v_resp.status < 200 or v_resp.status >= 400 then
          v_reason := 'get_http_' || v_resp.status::text;
        elsif coalesce(v_resp.content_type, '') !~* 'pdf' then
          v_reason := 'non_pdf_' || coalesce(nullif(v_resp.content_type, ''), 'unknown');
        end if;
      end if;
    exception when others then
      v_reason := 'exception_' || sqlstate;
    end;

    if v_reason is not null then
      v_failed_count := v_failed_count + 1;
      v_failed := v_failed || jsonb_build_array(jsonb_build_object(
        'slug', v_row.slug,
        'reason', v_reason
      ));

      update public.tests
         set status = 'stale',
             updated_at = now()
       where id = v_row.id;
    end if;
  end loop;

  perform extensions.http_reset_curlopt();

  insert into public.watchdog_runs (total_checked, failed_count, failed_slugs)
  values (v_total, v_failed_count, v_failed)
  returning * into v_run;

  return v_run;
end;
$$;

revoke execute on function public.run_pyq_link_watchdog() from public, anon, authenticated;

-- ---------------------------------------------------------------------
-- Self-hosted error logs. No third-party SDK, no paid service. The app
-- must scrub PII before calling log_client_error().
-- ---------------------------------------------------------------------

create table if not exists public.error_logs (
  id uuid primary key default gen_random_uuid(),
  user_id uuid references auth.users(id) on delete cascade,
  app_version text,
  kotlin_class text not null,
  message text not null,
  stacktrace text,
  breadcrumbs jsonb not null default '{}'::jsonb,
  occurred_at timestamptz not null default now(),
  created_at timestamptz not null default now()
);

create index if not exists error_logs_created_idx
  on public.error_logs (created_at desc);

create index if not exists error_logs_user_created_idx
  on public.error_logs (user_id, created_at desc)
  where user_id is not null;

alter table public.error_logs enable row level security;

drop policy if exists "error_logs_insert_own_or_anon" on public.error_logs;
create policy "error_logs_insert_own_or_anon" on public.error_logs
  for insert to authenticated
  with check (user_id is null or (select auth.uid()) = user_id);

create or replace function public.log_client_error(
  p_app_version text,
  p_kotlin_class text,
  p_message text,
  p_stacktrace text default null,
  p_breadcrumbs jsonb default '{}'::jsonb,
  p_occurred_at timestamptz default now()
)
  returns uuid
  language plpgsql
  security invoker
  set search_path = public, pg_temp
as $$
declare
  v_id uuid;
begin
  if auth.uid() is null then
    raise exception 'not authenticated' using errcode = '42501';
  end if;

  insert into public.error_logs (
    user_id, app_version, kotlin_class, message, stacktrace, breadcrumbs, occurred_at
  ) values (
    auth.uid(),
    left(coalesce(p_app_version, ''), 80),
    left(coalesce(p_kotlin_class, 'unknown'), 200),
    left(coalesce(p_message, 'unknown'), 1000),
    nullif(left(coalesce(p_stacktrace, ''), 12000), ''),
    coalesce(p_breadcrumbs, '{}'::jsonb),
    coalesce(p_occurred_at, now())
  )
  returning id into v_id;

  return v_id;
end;
$$;

grant execute on function public.log_client_error(text, text, text, text, jsonb, timestamptz)
  to authenticated;
revoke execute on function public.log_client_error(text, text, text, text, jsonb, timestamptz)
  from anon, public;

create or replace function public.purge_error_logs()
  returns void
  language sql
  security definer
  set search_path = public, pg_temp
as $$
  delete from public.error_logs
   where created_at < now() - interval '7 days';
$$;

revoke execute on function public.purge_error_logs() from public, anon, authenticated;

-- ---------------------------------------------------------------------
-- Orphan attempt sweeper. The client still owns normal submit; this only
-- cleans long-idle attempts past the server-owned deadline.
-- ---------------------------------------------------------------------

create or replace function public.sweep_orphan_attempts()
  returns int
  language plpgsql
  security definer
  set search_path = public, pg_temp
as $$
declare
  v_count int;
begin
  update public.attempts
     set status = 'EXPIRED',
         submitted_at = coalesce(submitted_at, now()),
         updated_at = now()
   where status = 'IN_PROGRESS'
     and server_deadline_at < now() - interval '1 hour';

  get diagnostics v_count = row_count;
  return v_count;
end;
$$;

revoke execute on function public.sweep_orphan_attempts() from public, anon, authenticated;

-- ---------------------------------------------------------------------
-- Scheduled jobs. Times are UTC; 22:30 UTC = 04:00 IST, 00:30 UTC =
-- 06:00 IST.
-- ---------------------------------------------------------------------

do $$
begin
  perform cron.unschedule('railprep-heartbeat');
exception when others then null;
end $$;
select cron.schedule('railprep-heartbeat', '30 0 * * *', $$select public.run_heartbeat();$$);

do $$
begin
  perform cron.unschedule('railprep-pyq-link-watchdog');
exception when others then null;
end $$;
select cron.schedule('railprep-pyq-link-watchdog', '30 22 * * *', $$select public.run_pyq_link_watchdog();$$);

do $$
begin
  perform cron.unschedule('railprep-error-logs-ttl');
exception when others then null;
end $$;
select cron.schedule('railprep-error-logs-ttl', '45 0 * * *', $$select public.purge_error_logs();$$);

do $$
begin
  perform cron.unschedule('railprep-attempt-sweeper');
exception when others then null;
end $$;
select cron.schedule('railprep-attempt-sweeper', '15 * * * *', $$select public.sweep_orphan_attempts();$$);
