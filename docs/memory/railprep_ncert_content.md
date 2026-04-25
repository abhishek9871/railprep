---
name: NCERT book identities and restructures
description: Verified NCERT URL-code → book-identity map; NCERT restructures chapters between editions so filename conventions are unreliable without verification
type: project
originSessionId: 14001d42-4e8c-4462-9af2-e53ccd128965
---
When seeding NCERT PDFs into the catalog (`tools/content-pipeline/`), filename-level conventions are NOT reliable. Two concrete reasons:

1. **Books are restructured between editions.** The 2024-25 reprint renamed Class 9 Math to "Ganita Manjari" (old "Mathematics") and Class 9 English to "Kaveri" (old "Beehive"), restructured Class 8 Geography (dropped "Mineral and Power Resources" chapter entirely), and redid Class 8 SPL III Ch 5 from "Criminal Justice System" to "Understanding Marginalisation". The 2026-27 reprint of Class 11 Indian Economic Development dropped "Poverty" (old Ch 4) and "Infrastructure" (old Ch 8) — all subsequent chapter numbers shifted by one.

2. **Book-code → subject is non-obvious.** Class 9 Social Science splits four ways: `iess1` = Geography (Contemporary India I), `iess2` = Economics, `iess3` = History (India and Contemporary World I), `iess4` = Polity (Democratic Politics I). Guessing `iess1 = Polity` based on the single-digit suffix silently produces a Polity chapter that opens Geography PDFs. Class 10 uses `jess1..jess4` in the same order. Class 8 has `hess2` (Our Pasts III / History), `hess3` (SPL III / Polity), `hess4` (Resources and Development / Geography). Class 11 Physics Part II (`keph2`) starts at Chapter 8, not Chapter 1 — `keph1` has the early chapters.

Verified URL-code → real book identity (Apr 2026 reprints):
- `iess1`/`jess1`/`hess4` → Geography
- `iess2`/`jess2` → Economics
- `iess3`/`jess3`/`hess2` → History (Indian)
- `iess4`/`jess4`/`hess3` → Polity (Democratic Politics I/II, SPL III)
- `keec1` → Class 11 Indian Economic Development
- `kehs1` → Class 11 Themes in **World** History (not Indian)
- `keps1` → Class 11 Political Theory
- `iebe1` → Class 9 Kaveri (formerly Beehive)
- `iemo1` → Class 9 Moments
- `iemh1` → Class 9 Ganita Manjari (formerly Mathematics)
- `hemh1` → Class 8 Ganita Prakash (formerly Mathematics)
- `jesc1` → Class 10 Science
- `keph2`/`leph1` → Class 11/12 Physics Parts II/I (university-level, too advanced for NTPC)
- `lebo1` → Class 12 Biology Part I (too advanced for NTPC)

**Why:** Phase 2.5's hardest content bug came from assuming `iess1 = Polity` — users saw Polity chapter titles that opened Geography PDFs.

**How to apply:** Before mapping any NCERT URL to a catalog chapter, extract page-1 text via `pdf-parse` to confirm the opening content matches the intended subject. After any re-seed, run `node src/audit-topics.mjs` against `audit/topics.json` — it does token-overlap verification between DB titles and actual PDF content. `118/118 MATCH` is the target. It's the canonical content verifier and should be re-run any time NCERT might have updated the books (their reprints shift chapter names/numbers without warning).
