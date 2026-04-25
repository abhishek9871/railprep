---
name: RailPrep Phase 3 Part C — PYQ library-link pivot
description: PYQs are NOT extracted into the DB — they are linked to adda247's public CDN and rendered natively in the on-device PDF viewer (same pattern as NCERT books from Phase 2.5).
type: project
originSessionId: 14001d42-4e8c-4462-9af2-e53ccd128965
---
Phase 3 Part C (question bank content) was re-scoped mid-research on 2026-04-24. Original brief wanted PDF→stem+option+key extraction with two-layer EN+HI explanations per question. Three independent failures forced the pivot:
  1. archive.org NTPC PDFs are candidate response-sheet exports (question IDs only, no stems)
  2. Official RRB answer keys are behind `digialm` login — no public PDF
  3. `pdf-parse` mangles Devanagari via legacy font substitution (व→ज, न→ज, द→ि in both RRBCDG notifications and adda247 PDFs)

**Pivot:** PYQs = library-link. Same architectural pattern as NCERT in Phase 2.5.
  - Source: adda247 public CDN (`wpassets.adda247.com` legacy + `www.adda247.com/jobs/wp-content/uploads/...` newer). 200+ papers, no auth, direct PDF, HEAD-verified.
  - Storage: `tests` table extended with `kind='PYQ_LINK'`, `external_url`, `source_language`, `source_attribution`.
  - Render: reuse NCERT PDF viewer (don't fork).
  - Attribution: "Source: adda247" under every listing. We link, we don't rehost.

**What still gets extracted as structured MCQ data:** Stage 4 original sectionals (500 Q across 20 RRB NTPC syllabus topics), with full two-layer EN+HI explanations + trap_reason per option — this is the competitive moat because nobody else ships 500 free bilingual originals with that pedagogy depth.

**Stage 5 scope-shift:** Wikipedia covers only 5-7 of the 12 reasoning topics (Syllogism, Venn_diagram, Clock_angle_problem, Determination_of_the_day_of_the_week, Arithmetic_progression are strong; Argument + Cardinal_direction moderate). The other 6 (Blood relations, Coding-decoding, Direction sense, Analogy, Seating arrangement, Statement-Courses of Action) need in-house primers. Authoring load went from "find Wiki articles" to "write 15-18 bilingual exam-register primers."

**Why:** user directive 2026-04-24 — "render the links into the users device rather that extarcting shit which is of no use. original format will help them. use adda247 to downalod the stuff and do the needful." Extraction was fragile anyway; pivot gave ≥200 renderable papers (vs. a target of 500 individual Q) for near-zero risk.

**How to apply:** When scoping Stage 2-5 or any future content expansion: PYQs = link. Originals = interactive MCQs with full pedagogy. Reasoning primers = mostly in-house, a few Wikipedia. **Full text extraction of PYQ stems/options is ruled out** (three failure modes catalogued). **Numeric answer-key-only extraction is deferred-open** — `docs/phase3-research.md` §4 item 1 keeps it as a Phase 4 option for an in-app "Show Answer" overlay; a future session can legitimately revisit this narrower scope without contradicting the pivot.
