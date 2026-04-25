#!/usr/bin/env node
// PIB RSS feed stub — Phase 4 work.
//
// The feature-feed module lands in Phase 4 with the articles table. Keeping this file so the
// pipeline structure is complete, but it's a no-op until Phase 4 adds the schema. Running it
// now prints the RSS items so you can eyeball the format.

import { request } from 'undici';

const FEEDS = [
  { name: 'PIB English (all-India)', url: 'https://www.pib.gov.in/ViewRss.aspx?reg=0&lang=1' },
  { name: 'PIB Hindi (all-India)',   url: 'https://www.pib.gov.in/ViewRss.aspx?reg=0&lang=2' },
];

async function probe(feed) {
  try {
    const r = await request(feed.url);
    const text = await r.body.text();
    const itemCount = (text.match(/<item\b/g) ?? []).length;
    console.log(`${feed.name}: HTTP ${r.statusCode}, ${itemCount} <item> entries`);
  } catch (e) {
    console.error(`${feed.name}: ${e.message}`);
  }
}

async function main() {
  console.log('PIB feed probe (no DB writes — articles table lands in Phase 4).');
  for (const f of FEEDS) await probe(f);
}

main();
