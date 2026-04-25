# Phase 4 D3 — Content Scale: Completion Report

**Date:** 2026-04-25
**Status:** complete, gate clean, no git commit yet

## Deliverables shipped to DB

### 5 Sectionals (125 Q / 500 options total)

| Slug | Section | Q | Difficulty mix |
|---|---|---|---|
| `sectional-profit-loss-01` | Profit & Loss | 25 | 8E / 12M / 5H |
| `sectional-tsd-01` | Time, Speed & Distance | 25 | **4E / 16M / 5H** ⚠ |
| `sectional-ratio-01` | Ratio & Proportion | 25 | 8E / 12M / 5H |
| `sectional-averages-01` | Averages | 25 | 8E / 12M / 5H |
| `sectional-syllogism-01` | Syllogism (reasoning) | 25 | 8E / 12M / 5H |

All `kind='SECTIONAL'`, `exam_target='NTPC_CBT1'`, `negative_marking_fraction=0.3333`, 25 min each. Every wrong option has a forensic `trap_reason` naming the specific computation/error that produces that exact distractor value (no filler).

### 4 In-house Reasoning Primers (4 topics)

Bilingual EN+HI, ~1000 words each, with worked examples. Stored in chapter "Reasoning Primers" (chapter_id `98972502-6f73-4871-bb70-e21c79edca76`), display_order 5–8, `source='In-house'`, `license='ORIGINAL'`, content in `topics.content_md`:

- Blood Relations
- Coding-Decoding
- Seating Arrangement
- Direction Sense

### Migration `0011_inhouse_primers` (applied)

- `'ORIGINAL'` added to `license_type` enum
- `topics.content_md` column added
- `topics_url_shape` CHECK relaxed: `ARTICLE` accepts `article_url` OR `content_md`

### Tooling hardening

- `src/audit-questions.mjs`: new WARNING-level regex `FILLER_TRAP_RE = /\b(rough estimate|gut feel|estimated|approximation|rounded|guessed)\b/i` — flags vibe-language `trap_reason`s without blocking.
- `src/emit-compact-seed.mjs` (new): minified seed emitter to fit MCP `execute_sql` arg-size limits.

## Final gate state

| Check | Result |
|---|---|
| `audit-questions` over 5 sectionals | **0 errors, 0 warnings** (125 Q audited) |
| `audit-topics` regression | **0 errors, 118/118 NCERT match** |
| Questions count | 48 → 173 (**+125 ✓**) |
| Options count | 192 → 692 (**+500 ✓**) |
| Topics count | 222 → 226 (**+4 ✓**) |
| Sectional tests count | 2 → 7 (**+5 ✓**) |

## Files added/changed

```
tools/content-pipeline/candidates/sectional-{profit-loss,tsd,ratio,averages,syllogism}.json
tools/content-pipeline/primers/{blood-relations,coding-decoding,seating-arrangement,direction-sense}.md
tools/content-pipeline/src/emit-compact-seed.mjs           (new)
tools/content-pipeline/src/audit-questions.mjs              (FILLER_TRAP_RE check added)
tools/content-pipeline/migrations/0011_inhouse_primers.sql
```

## Debt / follow-ups for D4+ planning

1. **TSD difficulty skew:** 4E/16M/5H vs the 8/12/5 target. User accepted skew over forced re-balancing. Decision needed: leave as-is or reauthor 4 EASY items shifted from MEDIUM. Low priority.
2. **`avg-20` (family-age) bug found inline & fixed:** candidate JSON had option A=17 marked correct but explanation derived 19. Both DB and candidate JSON now have A=19 correct with three specific arithmetic-slip distractors. Closed.
3. **No git commit yet** for D3 changes — pending user review before commit/PR.

## Pre-D3 baseline anchors (for re-validation)

If you re-derive from a fresh checkout: pre-D3 had `questions=48, options=192, topics=222`. The 5 sectionals × 25 Q = 125 Q is the full D3 question contribution; the 4 primers are the full topic contribution.

## Confirmed *not* in scope of D3 (do not assume done)

- D1 (streaks + daily digest schema) and D2 (AlarmManager + WorkManager + POST_NOTIFICATIONS) were completed in earlier sessions before D3.
- D4 onwards: not started — planner's call.
