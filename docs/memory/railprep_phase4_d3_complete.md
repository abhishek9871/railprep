---
name: Phase 4 D3 (Content Scale) complete
description: D3 finished 2026-04-25 — 5 sectionals (125 Q / 500 options) + 4 in-house bilingual primers seeded; gate clean; minor TSD difficulty-mix debt
type: project
originSessionId: 14001d42-4e8c-4462-9af2-e53ccd128965
---
Phase 4 D3 (Content Scale) was completed on 2026-04-25.

**What is in DB now (don't re-author or re-seed):**

5 sectionals (slugs ending `-01`), 25 Q each, total 125 Q / 500 options:
- `sectional-profit-loss-01`
- `sectional-tsd-01` (Time / Speed / Distance)
- `sectional-ratio-01`
- `sectional-averages-01`
- `sectional-syllogism-01`

4 in-house bilingual reasoning primers, stored as `topics` rows in chapter "Reasoning Primers" (chapter_id `98972502-6f73-4871-bb70-e21c79edca76`) at display_order 5-8, with `source='In-house'`, `license='ORIGINAL'`, `content_type='ARTICLE'`, content in `topics.content_md`:
- Blood Relations
- Coding-Decoding
- Seating Arrangement
- Direction Sense

**Why:** User required quality-over-count, with every wrong option's `trap_reason` naming a specific arithmetic step (no vibe filler — see separate `feedback_trap_reason_quality_bar.md`). License `ORIGINAL` was added in migration `0011_inhouse_primers` specifically for in-house content; same migration added `topics.content_md` and relaxed `topics_url_shape` CHECK to allow ARTICLE with `content_md` OR `article_url`.

**How to apply:** Before authoring new content in this exam-prep area, check whether a matching slug or chapter is already populated. Pre-D3 baseline: questions=48, options=192, topics=222. Post-D3: questions=173, options=692, topics=226 (+125 / +500 / +4). Slug uniqueness is enforced.

**Known debt:** TSD difficulty mix is 4E / 16M / 5H, deviating from the 8E/12M/5H target — flagged during authoring; the user accepted skew rather than reduce quality. `avg-20` (family-age) had a bug discovered & patched inline during seed: candidate JSON had option A=17 correct but explanation derived 19; both DB and candidate JSON now have A=19 correct. No git commit was made for D3 changes — left for user review.
