#!/usr/bin/env node
// Applies generated SQL files to the Supabase project. Uses the PostgREST REST endpoint with
// the service_role key. Intended for CI only — local dev should use the Supabase MCP or the
// SQL editor in the dashboard.
//
// Usage: node src/apply-to-supabase.mjs [sql/file1.sql sql/file2.sql ...]
// Defaults to applying every file in sql/*.sql if no args given.

import { readFile, readdir } from 'node:fs/promises';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import { request } from 'undici';

const __dirname = dirname(fileURLToPath(import.meta.url));
const ROOT = resolve(__dirname, '..');

const URL_BASE = process.env.SUPABASE_URL;
const KEY = process.env.SUPABASE_SERVICE_KEY;

if (!URL_BASE || !KEY) {
  console.error('SUPABASE_URL and SUPABASE_SERVICE_KEY must be set. See .env.example.');
  process.exit(1);
}

async function applySql(sql) {
  // Supabase supports arbitrary SQL only via the Management API or the pg_meta RPC; the public
  // REST endpoint is row-oriented. Simplest path: RPC a helper function installed as the service
  // role. Recommended pattern:
  //   create or replace function public._pipeline_apply(q text) returns void
  //   language plpgsql security definer as $$ begin execute q; end $$;
  // then call: POST /rest/v1/rpc/_pipeline_apply with { q: sql }.
  const resp = await request(`${URL_BASE}/rest/v1/rpc/_pipeline_apply`, {
    method: 'POST',
    headers: {
      'apikey': KEY,
      'Authorization': `Bearer ${KEY}`,
      'Content-Type': 'application/json',
      'Prefer': 'return=minimal',
    },
    body: JSON.stringify({ q: sql }),
  });
  if (resp.statusCode >= 400) {
    const text = await resp.body.text();
    throw new Error(`HTTP ${resp.statusCode}: ${text}`);
  }
}

async function main() {
  const args = process.argv.slice(2);
  let files;
  if (args.length > 0) {
    files = args.map(a => resolve(process.cwd(), a));
  } else {
    const sqlDir = resolve(ROOT, 'sql');
    const entries = await readdir(sqlDir);
    files = entries.filter(f => f.endsWith('.sql')).map(f => resolve(sqlDir, f));
  }
  for (const f of files) {
    console.log(`apply ${f}`);
    const sql = await readFile(f, 'utf-8');
    await applySql(sql);
  }
  console.log('done.');
}

main().catch(e => { console.error(e); process.exit(1); });
