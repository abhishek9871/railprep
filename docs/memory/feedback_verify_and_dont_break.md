---
name: Verify before declaring done; don't break working features while fixing broken ones; stop iterating after 2-3 failed cycles
description: Three collaboration rules the user enforced during Phase 2.5 — apply on every future data migration, risky refactor, or bug hunt
type: feedback
originSessionId: 14001d42-4e8c-4462-9af2-e53ccd128965
---
**1. Verify before declaring done.** User explicitly requested a "genius script" that proves content matches titles rather than accepting a self-reported "looks right". Apply: any time a data migration touches many rows, write a verifier and run it. For content catalogs the ground truth is the source file itself (read the PDF/HTML), not my reasoning about filename conventions or SQL inspection.

   **Why:** In Phase 2.5 I mapped `iess1` to Polity three times in a row because the filename scheme *looked like* it should mean Polity. It actually means Geography. User only found out by clicking topics in the app. A `pdf-parse` page-1 check would have caught it in 2 seconds. `tools/content-pipeline/src/audit-topics.mjs` is the template to mimic.

**2. Don't break what works while fixing what's broken.** User said "without being aggressive and without risking what good we have, fix the stuff please". Apply: prefer **targeted `UPDATE`** migrations over `stale-all-then-reinsert` when only a subset of rows needs fixing. Touch the minimum. Explicitly preserve bookmarks (they reference topic IDs — updating a row preserves them; deleting and reinserting would orphan them).

   **Why:** I once ran a comprehensive re-seed that would have regressed known-good rows while fixing the wrong ones. The user had to talk me back from it.

**3. Stop iterating after 2-3 failed cycles on the same bug.** User got visibly frustrated ("donr act stupid this time", "pull your head out of your ass") when I spent 5+ build-install-test cycles chasing YouTube playback without pausing to check authoritative sources. Apply: after 2-3 unsuccessful cycles, fetch current docs (WebFetch/WebSearch for the relevant API as of current date) or decompile the relevant library — don't keep speculating. The advisor flagged this too during that session.

   **Why:** User is time-constrained. Every build-install-test cycle they watch me do costs minutes. Three cycles is the cap before I should be getting information from outside my head.

**Closing signal:** User accepts pragmatism when I *surface* the tradeoff ("we could fall back to `Intent.ACTION_VIEW` — that breaks inline playback though, is that OK?") but rejects it when I silently corner-cut ("no dont take the pragmatic route mf, make the videos play. research..."). Surface the tradeoff, let them decide.
