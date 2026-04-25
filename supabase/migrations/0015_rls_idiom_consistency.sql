-- Phase F — RLS idiom consistency.
-- Legacy Phase 1/2 policies used bare auth.uid(). All user-scoped policies
-- now use the InitPlan-friendly `(select auth.uid())` form used by later phases.

drop policy if exists "profiles_own_select" on public.profiles;
create policy "profiles_own_select" on public.profiles
  for select to authenticated
  using ((select auth.uid()) = id);

drop policy if exists "profiles_own_update" on public.profiles;
create policy "profiles_own_update" on public.profiles
  for update to authenticated
  using ((select auth.uid()) = id)
  with check ((select auth.uid()) = id);

drop policy if exists "bookmarks_select" on public.bookmarks;
create policy "bookmarks_select" on public.bookmarks
  for select to authenticated
  using ((select auth.uid()) = user_id);

drop policy if exists "bookmarks_insert" on public.bookmarks;
create policy "bookmarks_insert" on public.bookmarks
  for insert to authenticated
  with check ((select auth.uid()) = user_id);

drop policy if exists "bookmarks_delete" on public.bookmarks;
create policy "bookmarks_delete" on public.bookmarks
  for delete to authenticated
  using ((select auth.uid()) = user_id);
