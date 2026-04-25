-- Phase F — user data export and delete-account RPCs.
-- Export returns JSON directly to the authenticated client. Delete removes the
-- auth.users row; existing FK cascades remove profiles, attempts, bookmarks,
-- question_bookmarks, entitlements, and user-scoped error logs.

create or replace function public.export_my_data()
  returns jsonb
  language plpgsql
  security invoker
  set search_path = public, pg_temp
as $$
declare
  v_uid uuid := auth.uid();
  v_payload jsonb;
begin
  if v_uid is null then
    raise exception 'not authenticated' using errcode = '42501';
  end if;

  select jsonb_build_object(
    'exported_at', now(),
    'profile', (
      select to_jsonb(p) from public.profiles p where p.id = v_uid
    ),
    'attempts', coalesce((
      select jsonb_agg(to_jsonb(a) order by a.started_at desc)
        from public.attempts a
       where a.user_id = v_uid
    ), '[]'::jsonb),
    'attempt_answers', coalesce((
      select jsonb_agg(to_jsonb(aa) order by aa.answered_at desc)
        from public.attempt_answers aa
        join public.attempts a on a.id = aa.attempt_id
       where a.user_id = v_uid
    ), '[]'::jsonb),
    'topic_bookmarks', coalesce((
      select jsonb_agg(to_jsonb(b) order by b.created_at desc)
        from public.bookmarks b
       where b.user_id = v_uid
    ), '[]'::jsonb),
    'question_bookmarks', coalesce((
      select jsonb_agg(to_jsonb(qb) order by qb.bookmarked_at desc)
        from public.question_bookmarks qb
       where qb.user_id = v_uid
    ), '[]'::jsonb),
    'daily_digest_attempts', coalesce((
      select jsonb_agg(to_jsonb(da) order by da.digest_date desc)
        from public.digest_attempts da
       where da.user_id = v_uid
    ), '[]'::jsonb),
    'streak_events', coalesce((
      select jsonb_agg(to_jsonb(se) order by se.event_date desc)
        from public.streak_events se
       where se.user_id = v_uid
    ), '[]'::jsonb),
    'entitlements', coalesce((
      select jsonb_agg(to_jsonb(e) order by e.granted_at desc)
        from public.user_entitlements e
       where e.user_id = v_uid
    ), '[]'::jsonb)
  ) into v_payload;

  return v_payload;
end;
$$;

grant execute on function public.export_my_data() to authenticated;
revoke execute on function public.export_my_data() from anon, public;

create or replace function public.delete_my_account()
  returns void
  language plpgsql
  security definer
  set search_path = public, auth, pg_temp
as $$
declare
  v_uid uuid := auth.uid();
begin
  if v_uid is null then
    raise exception 'not authenticated' using errcode = '42501';
  end if;

  delete from auth.users where id = v_uid;
  if not found then
    raise exception 'user not found' using errcode = 'P0002';
  end if;
end;
$$;

grant execute on function public.delete_my_account() to authenticated;
revoke execute on function public.delete_my_account() from anon, public;
