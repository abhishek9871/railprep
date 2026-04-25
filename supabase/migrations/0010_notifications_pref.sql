-- Recovered from Supabase MCP on 2026-04-25.
-- MCP version: 20260425014649
-- Original application order is determined by version timestamp, not filename.

-- Phase 4 D2 — opt-in toggle for the daily reminder.
-- Canonical state lives on profiles; WorkManager schedules/cancels client-side.
alter table public.profiles
  add column if not exists notifications_enabled boolean not null default false;