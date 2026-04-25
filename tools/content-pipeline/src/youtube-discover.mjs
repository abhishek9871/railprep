#!/usr/bin/env node
// YouTube video discovery for RailPrep. Uses YouTube Data API v3 — free tier, 10k units/day.
// IMPORTANT: uses playlistItems.list (1 unit/call) against each channel's "uploads" playlist,
// NOT search.list (100 units/call). This keeps quota well under the daily cap.
//
// Output: sql/topics_yt.sql — INSERT statements mapping videos to topics by subject + a title
// regex filter. Does not choose chapter assignment — that is a manual curation step; this
// discovery stage only surfaces candidate videos.

import { readFile, writeFile, mkdir } from 'node:fs/promises';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import { request } from 'undici';

const __dirname = dirname(fileURLToPath(import.meta.url));
const ROOT = resolve(__dirname, '..');

const API_KEY = process.env.YOUTUBE_API_KEY;
if (!API_KEY) {
  console.error('YOUTUBE_API_KEY not set. See .env.example.');
  process.exit(1);
}

const SUBJECT_KEYWORDS = {
  MATH:            /\b(math|percentage|profit|loss|time.*work|speed.*distance|geometry|algebra|trigonometry|mensuration|simplification|number system|ratio|average|HCF|LCM)\b/i,
  REASONING:       /\b(reasoning|series|analog|classification|coding.decoding|blood relation|direction|syllogism|puzzle|seating|statement)\b/i,
  GA:              /\b(polity|constitution|parliament|history|mughal|geography|river|monsoon|economy|RBI|budget|scheme|yojana|railway)\b/i,
  GEN_SCIENCE:     /\b(physics|chemistry|biology|science|motion|force|light|electricity|cell|tissue|reaction|periodic)\b/i,
  ENGLISH:         /\b(english|grammar|tense|vocabulary|synonym|antonym|idiom|comprehension|voice|sentence)\b/i,
  CURRENT_AFFAIRS: /\b(current affairs|daily|weekly|monthly|today|news|event)\b/i,
};

async function fetchUploadsPlaylist(channelId) {
  const r = await request(
    `https://www.googleapis.com/youtube/v3/channels?part=contentDetails&id=${channelId}&key=${API_KEY}`,
  );
  const body = await r.body.json();
  const playlistId = body.items?.[0]?.contentDetails?.relatedPlaylists?.uploads;
  return playlistId ?? null;
}

async function fetchRecentVideos(playlistId, maxResults = 25) {
  const r = await request(
    `https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&maxResults=${maxResults}&playlistId=${playlistId}&key=${API_KEY}`,
  );
  const body = await r.body.json();
  return (body.items ?? []).map(it => ({
    videoId: it.snippet?.resourceId?.videoId,
    title: it.snippet?.title ?? '',
    publishedAt: it.snippet?.publishedAt,
    channelTitle: it.snippet?.channelTitle,
  })).filter(v => v.videoId);
}

function classifyByKeywords(title, channelSubjects) {
  // Prefer subjects the channel is known for; within those, keyword-match the title.
  const matches = [];
  for (const subj of channelSubjects) {
    const re = SUBJECT_KEYWORDS[subj];
    if (re && re.test(title)) matches.push(subj);
  }
  return matches;
}

function toSql(rows) {
  const esc = s => `'${String(s).replaceAll("'", "''")}'`;
  const lines = [];
  for (const r of rows) {
    lines.push(
      `insert into public.topics (title_en, content_type, external_video_id, source, license, display_order, status, last_verified_at)`,
      `  select ${esc(r.title)}, 'YT_VIDEO', ${esc(r.videoId)}, ${esc(r.channelTitle)}, 'YT_STANDARD', 99, 'active', now()`,
      `  -- chapter assignment is a manual curation step; placeholder only`,
      `  -- from public.chapters where slug = ? limit 1;`,
      `  where false;  -- NOOP until chapter_id is curated`,
      '',
    );
  }
  return lines.join('\n');
}

async function main() {
  const channelsFile = resolve(ROOT, 'channels.json');
  const { channels } = JSON.parse(await readFile(channelsFile, 'utf-8'));
  const all = [];
  for (const c of channels) {
    try {
      const playlistId = await fetchUploadsPlaylist(c.channel_id);
      if (!playlistId) { console.warn(`skip ${c.name}: no uploads playlist`); continue; }
      const videos = await fetchRecentVideos(playlistId, 25);
      for (const v of videos) {
        const subjects = classifyByKeywords(v.title, c.subjects);
        for (const subj of subjects) {
          all.push({ ...v, subject: subj });
        }
      }
      console.log(`${c.name}: ${videos.length} videos`);
    } catch (e) {
      console.error(`error for ${c.name}:`, e.message);
    }
  }
  const outDir = resolve(ROOT, 'sql');
  await mkdir(outDir, { recursive: true });
  await writeFile(resolve(outDir, 'topics_yt.sql'), toSql(all));
  console.log(`wrote sql/topics_yt.sql (${all.length} candidates)`);
}

main().catch(e => { console.error(e); process.exit(1); });
