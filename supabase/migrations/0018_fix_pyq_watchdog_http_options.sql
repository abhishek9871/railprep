-- Phase F — fix PYQ watchdog for Supabase http extension runtime options.
-- Some curl options, including FOLLOWLOCATION, cannot be set at runtime in
-- this environment. Keep the checker deterministic and avoid curl option setup.

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

  insert into public.watchdog_runs (total_checked, failed_count, failed_slugs)
  values (v_total, v_failed_count, v_failed)
  returning * into v_run;

  return v_run;
end;
$$;

revoke execute on function public.run_pyq_link_watchdog() from public, anon, authenticated;
