-- Phase F — free scheduler/network extensions.
-- pg_cron drives heartbeat, stale-attempt sweeps, TTL cleanup, and link checks.
-- pg_net is enabled for future async HTTP jobs; synchronous watchdog logic may
-- use the bundled http extension if pg_net cannot expose response status inline.

create extension if not exists pg_cron with schema pg_catalog;
create extension if not exists pg_net with schema extensions;
create extension if not exists http with schema extensions;
