# Phase 3 — Part C — Stage 1 Research (2026-04-24)

**Purpose.** Source-hunting and pattern-verification before any content work. Closes out Stage 1 of the Part C brief with a go/no-go recommendation for the planner.

**Pivot note (locked during Stage 1).** During research, the PYQ extraction path collapsed on three independent failures (archive.org PDFs are response-sheet exports with no stems; official RRB answer keys are `digialm`-login-gated; Hindi Devanagari is mangled by systematic font-substitution in `pdf-parse`). The user adjudicated mid-research: **treat PYQs the same way NCERT books are treated in Phase 2.5 — render the original PDFs on-device via URL pointers, do NOT extract stems/options/keys into the DB.** This doc is written around that pivot. The extraction-pipeline artefacts from earlier in the research (`pdf-extract-test.mjs`, `pdf-dump.mjs`) stay in `tools/content-pipeline/src/` as diagnostic utilities, not production pipeline.

## 0 · Verdict (TL;DR)

**GO** for Part C with revised scope — **but three real costs the pivot created need planner sign-off before Stage 2 starts**, not punted to later. If the answer to any is "no," Stage 2-3 as scoped below becomes partially wasted work.

### 0.1 Three costs the pivot created (planner must see before approving)

**Cost A — Stage 5 authoring ballooned from ~12h to 30-50h.** The brief assumed 25 reasoning primers could come from Wikipedia. Reality: only 5-7 of the 12 reasoning topics have usable Wikipedia articles; the other 15-18 need in-house primers written in **bilingual exam-register** (500-800 words EN + HI each, vetted against the glossary). That's 30-50 hours of focused content writing, not coding. The Stage 3 extraction debt was paid, quietly, with Stage 5 scope. Two options the planner should choose between:
  - (a) accept the 30-50h and ship full bilingual in-house primers as the Phase-4-ready reasoning library
  - (b) ship only the 5-7 Wikipedia-linked articles + "coming soon" placeholders; defer in-house primers to Phase 4

**Cost B — PYQ-as-interactive-practice is a real loss, not a neutral trade.** Competitors (testbook, adda247 paid tier) ship interactive PYQ quizzes: stem + options + auto-score + review. Under our library-link model we ship readable PYQs only — student reads the PDF, has no auto-scored attempt. If the brief's Stage 3 originally targeted 500 PYQ Q as interactive, and the brief's Stage 4 keeps 500 original Q, **total interactive question volume is ~500, down from the brief's implicit ~1000**. The originals are now the only place a student gets auto-scored practice. Defensible (extraction was broken) but name it. If the planner wants the full ~1000 interactive-practice number preserved, Stage 4 needs to scale to ~1000 Q (halves the per-Q budget or doubles Stage 4's wall-clock).

**Cost C — adda247 legal posture is a planner-level decision, not my call.** NCERT content is explicitly open-licensed (GoI NDSAP/Creative Commons). Adda247's papers are **memory-based reconstructions of copyrighted RRB exams**, re-hosted by adda247 on their CDN. We're linking, not rehosting — but the underlying content is derivative work without a clear license. Risk posture is materially different from NCERT linking. **This question needs a decision before Stage 2 starts**, because Stage 2 builds the catalog UI *around* adda247 URLs. If legal review later says "no" at pre-Play-Store, Stages 2-3 of UI+seed work get thrown out. Options:
  - (a) proceed at the user's risk, same as the user's directive implied; add a Play-Store-ready takedown flow (de-link + retain metadata as dead entries)
  - (b) contact adda247 for explicit linking permission in writing before Stage 2
  - (c) pivot to a narrower source (RRB's own answer-key objection-tracker PDFs are authoritative but only contain answer keys, not stems)

### 0.2 Threshold scorecard (revised to name what the pivot actually did)

| Threshold (set 2026-04-24 before research) | Status | Evidence |
|---|---|---|
| ≥8 text-extractable PDFs across ≥3 years | **Obsoleted by pivot** — library model doesn't extract | 200+ renderable PDFs across 2017/2019-2021/2022/2025/2026 on adda247 public CDN |
| ≥1 bilingual paper for glossary anchor | **Redefined under pivot — partially met** | Original threshold was "one paper with EN+HI interleaved for side-by-side glossary reading." Reality: adda247 publishes EN and HI as *separate* paired PDFs (e.g. Paper 01-EN + Paper 06-HI from same reconstruction set; CBT-1/2 2021/2025 dated papers in both langs). Glossary author must cross-reference two PDFs visually — doable, slower than a single bilingual paper would have been. Acceptable for Stage 4 but weaker than the original check. |
| ≥8/12 reasoning topics usable Wikipedia coverage | **NOT MET — 5 strong, 2 moderate, 6 in-house** | §1.3; drives Cost A above |

**Aggregator-scope contradiction** (§0.1 Cost C): user directive picked adda247 as PYQ source. Still needs legal sign-off before Stage 2 starts; the directive approved the approach, not the legal posture.

---

## 1 · Research findings

### 1.1 PYQ PDF inventory (adda247 public CDN)

Adda247 publishes RRB NTPC paper PDFs on two asset hosts: legacy `wpassets.adda247.com` (Cloudfront+S3, 2020-21 uploads) and `www.adda247.com/jobs/wp-content/uploads/...` (newer 2025-26 uploads). **Both are public, no auth, direct PDF. HEAD-verified downloadable.**

**Total accessible volume: 200+ paper PDFs.** The breakdown from adda247's [index page](https://www.adda247.com/jobs/rrb-ntpc-previous-year-question-paper/):

| Exam cycle | Dates | Shifts covered | Languages | Approx. paper count |
|---|---|---|---|---|
| **CBT-1 Graduate CEN 05/2024** | 5–24 Jun 2025 (20 dates) | Shift 1/2/3 daily | EN + HI | ~120 |
| **CBT-1 Graduate CEN 05/2024 re-exam** | 16–27 Mar 2026 | Multi-shift | EN + HI | ~30 |
| **CBT-2 Graduate CEN 05/2024** | 13 Oct 2025 | 2 shifts | EN + HI | 4 |
| **CBT-1 Undergraduate CEN 06/2024** | 7 Aug – 9 Sep 2025 (13 dates) | Shift 1/2/3 | EN + HI (+ separate answer-key PDFs) | ~80 + keys |
| **CBT-2 Undergraduate CEN 06/2024** | 20 Dec 2025 | Multiple | EN + HI | ~6 |
| **CBT-1 2021** (CEN 01/2019) | 30 Dec 2020 – 8 Apr 2021 | All shifts | EN (60+) and HI (80+) | ~140 |
| **CBT-2 2022** (CEN 01/2019 Stage-2) | 9–17 Jun 2022 (9 dates) | 3 shifts × 9 | EN + HI | ~54 |
| **CBT-2 2017** (original notification) | 17–19 Jan 2017 | Shift 1/2/3 | EN + HI | ~18 |
| **Compilation set 01–20** (Dec 2020) | — | — | Mixed (01-EN, 04-EN, 05-EN, 06-HI, 07-HI, 08-HI, 09-EN, 14-HI, 18-EN, 19-EN, 20-EN confirmed) | 20 |

**Verified download + extraction test (2 papers):**

| Paper | URL | Size | Pages | Chars | Clean-ratio | Verdict |
|---|---|---|---|---|---|---|
| Paper 01 (EN) | `https://wpassets.adda247.com/wp-content/uploads/multisite/sites/2/2020/11/07162058/RRB-NTPC-Previous-Year-Paper-01-English.pdf` | 592 KB | 17 | 20,204 | 0.72 | text-extractable, clean stems+options |
| Paper 07 (HI) | `https://wpassets.adda247.com/wp-content/uploads/multisite/sites/2/2020/12/15111519/RRB-NTPC-Previous-Year-Paper-07-Hindi.pdf` | 592 KB | 17 | 18,568 | 0.71 | text-extractable, Devanagari present but with systematic font-substitution (व→ज, न→ज, द→ि at some positions) — human-readable rendered in a PDF viewer, but NOT reliable for automated text extraction |

**Content quality (hand-verified spot-check of Paper 07):** Q1 syllogism (All apples are leaves…), Q2 coding-decoding (TRIPLE→SQHOKD), Q3 GA (X-rays), Q5 algebra (simplify 5x(x+2)+4x), Q86 compound interest, Q87 GA (first woman PM of Sri Lanka), Q88-90 seating-arrangement-with-direction, Q91 mathematical-operators-substitution, Q92 ecology (ecotone), Q93 time-and-work. **Exactly on the RRB NTPC CBT-1 syllabus.** Mix of difficulty EASY-to-MEDIUM. Reconstruction quality is professional.

**No answer-key inside the compilation PDFs 01-20.** Adda247 publishes per-date answer-key PDFs separately (example URL seen: `RRB-NTPC-UG-Answer-Key-English-07-08-2025-Shift-1-Paper.pdf`). Under the library-link model this is a *feature* — users get both the paper and the key as browseable resources — not a blocker.

**Legal posture.** Adda247 hosts these on public CDN with no robots.txt, Terms-of-Service, or download-auth barrier. We link; we do not rehost. Attribution "Source: adda247" beneath every listing. Same pattern as Phase 2.5 NCERT where we link to `ncert.nic.in/textbook/pdf/*.pdf`. No CC attribution license needed (we're not redistributing content, we're linking to a public URL). If adda247 migrates URLs in the future, our catalog has broken links — handled the same way NCERT URL migrations are (see `audit-topics.mjs` pattern, §2.3).

**Why NOT archive.org (failed candidate source, rejected):** The sole archive.org hit (`rrb-ntpc-question-paper-2021-pdf-in-english`, Tesseract-OCR'd 1.9 MB 38-page PDF) turned out to be a **candidate's personal response-sheet export** from the `digialm` portal — contains only question IDs, option IDs, and the candidate's chosen option. **Zero stems, zero option text, zero answer keys.** Useless for either extraction OR library-link display (students can't read the questions). Written up to prevent future re-examination.

**Why NOT GDrive aggregators (examstocks, qmaths, freshersnow — rejected):** The Google Drive mirror links require clicking through the "virus scan" HTML confirmation gate for any file >25 MB. HEAD-probe of `https://drive.google.com/uc?export=download&id=1A3gQsWm-5-mG-YmPWAEteNxqakRe7BBI` (qmaths' 2019 EN pack, 50 papers) returned a 2.5 KB HTML gate page, not a PDF. A confirm-param workaround exists but is fragile across Google Drive UI changes. Use only if adda247 coverage has gaps.

**Why NOT cracku / prepp / testbook (rejected):** Cracku requires signup login. Prepp.in's paper URL returned 403 on direct fetch (bot-detection). Testbook's PDF links route through a subscription quiz-runner, not a direct download.

### 1.2 RRB NTPC pattern — authoritative (as of 2026-04-24)

Confirmed verbatim from RRBCDG-published Detailed CEN 06/2024 PDF (English, 79 pages, 159,177 chars extracted cleanly, clean-ratio 0.79):

**CBT-1 (1st Stage)** — identical for Graduate and Undergraduate posts:

| Subject | Questions | Marks | Duration |
|---|---|---|---|
| General Awareness | 40 | 40 | — |
| Mathematics | 30 | 30 | — |
| General Intelligence & Reasoning | 30 | 30 | — |
| **Total** | **100** | **100** | **90 min** (120 min for PwBD with scribe) |

Negative marking: 1/3 per wrong answer. Screening nature; used to shortlist 15× vacancies for CBT-2. Minimum eligibility % to be shortlisted: **UR 40, EWS 40, OBC-NCL 30, SC 30, ST 25** (relaxed by 2 marks for PwBD in shortage).

**CBT-2 (2nd Stage)**:

| Subject | Questions | Marks |
|---|---|---|
| General Awareness | 50 | 50 |
| Mathematics | 35 | 35 |
| General Intelligence & Reasoning | 35 | 35 |
| **Total** | **120** | **120** | **90 min** (120 min PwBD) |

Negative marking: 1/3 per wrong. Final merit ranking is based on **CBT-2 only** (normalized).

**Authoritative syllabus (verbatim from CEN 06/2024 PDF, §13.1):**

- **Math**: Number System, Decimals, Fractions, LCM, HCF, Ratio and Proportions, Percentage, Mensuration, Time and Work, Time and Distance, Simple and Compound Interest, Profit and Loss, Elementary Algebra, Geometry and Trigonometry, Elementary Statistics.
- **General Intelligence & Reasoning**: Analogies, Completion of Number and Alphabetical Series, Coding and Decoding, Mathematical Operations, Similarities and Differences, Relationships, Analytical Reasoning, Syllogism, Jumbling, Venn Diagrams, Puzzle, Data Sufficiency, **Statement-Conclusion**, **Statement-Courses of Action**, Decision Making, Maps, Interpretation of Graphs.
- **General Awareness**: Current Events, Games/Sports, Art & Culture of India, Indian Literature, Monuments, General Science (up to 10th CBSE), History of India & Freedom Struggle, Physical/Social/Economic Geography, Indian Polity & Governance (Constitution + political system), Scientific/Technological Developments (Space, Nuclear), UN & World Orgs, Environmental Issues, Basics of Computers, Abbreviations, Transport Systems, Indian Economy, Famous Personalities, Flagship Govt Programs, Flora & Fauna, Important Govt Organizations.

> **Correction to Part C brief §5.1 reasoning-topic list.** The brief listed "Statement-argument" and "Statement-assumption" — those are SSC/IBPS/Bank-PO reasoning categories. The RRB NTPC official syllabus uses **Statement-Conclusion** and **Statement-Courses of Action**. Planner should update the Stage 5 primer list accordingly.

**CEN calendar landscape (April 2026):**

| CEN | Level | Status | Key dates |
|---|---|---|---|
| CEN 05/2024 | NTPC Graduate | CBT-2 complete (13 Oct 2025), CBAT+CBTST 28 Dec 2025, rescheduled 11 Feb 2026, final result phase | Effectively closed |
| CEN 06/2024 | NTPC Undergraduate | CBT-1 7 Aug–9 Sep 2025, CBT-2 Dec 2025, CBTST 13 Feb 2026, result out 21 Nov 2025 | Effectively closed |
| **CEN 07/2025** | **NTPC Undergraduate** | **CBT-1 starts 7 May 2026** | **LIVE — highest student demand** |

The user brief said "CEN 06/2024 is running right now" — that was true when drafted, no longer. **CEN 07/2025 is the relevant live notification** for students installing the app this month. The pattern is identical across all three CENs (locked by the 2024 pattern document), so no scope impact — but user-facing copy ("aimed at CEN 07/2025 UG CBT-1") is tighter.

### 1.3 Wikipedia reasoning article audit (12 topics)

Decision rule: "usable primer" = opening paragraph defines the concept for a first-time learner + has ≥1 worked example + covers the most common exam question variants. "In-house required" = article missing, too philosophical, or too specialised.

| # | Topic (user brief) | Wikipedia candidate | Verdict | Notes |
|---|---|---|---|---|
| 1 | Syllogism | [Syllogism](https://en.wikipedia.org/wiki/Syllogism) | ✅ usable | ~8,500w. 24 valid forms with Venn diagrams. Theoretical-heavy; supplement with a "how to attack Only/Some/All" in-house primer. |
| 2 | Blood relations | *(none)* | ❌ in-house required | No Wikipedia article on exam-style "coded blood relations" reasoning. |
| 3 | Coding-decoding | *(closest: [Ciphertext](https://en.wikipedia.org/wiki/Ciphertext))* | ❌ in-house required | Ciphertext is about cryptography, not exam letter-shift codes. |
| 4 | Direction sense | *(closest: [Cardinal direction](https://en.wikipedia.org/wiki/Cardinal_direction))* | ❌ in-house required (Cardinal direction is background-only; no worked sequential-movement examples) |
| 5 | Number series | [Arithmetic progression](https://en.wikipedia.org/wiki/Arithmetic_progression) | ✅ usable | Covers nth-term + sum formulas + pattern recognition. |
| 6 | Alphabet series | *(fold into Arithmetic progression)* | ⚠️ shared | Letter-position indexing is an arithmetic-progression special case; can be a short addendum to #5's primer. No dedicated article. |
| 7 | Analogy | [Analogy](https://en.wikipedia.org/wiki/Analogy) | ❌ in-house required | ~8,000w but overwhelmingly philosophical (Aristotle, Aquinas, Kant). Useless for exam pattern-matching. |
| 8 | Clocks | [Clock angle problem](https://en.wikipedia.org/wiki/Clock_angle_problem) | ✅ usable | ~1,200w with formulas + 2 worked examples + overlap section. |
| 9 | Calendars | [Determination of the day of the week](https://en.wikipedia.org/wiki/Determination_of_the_day_of_the_week) | ✅ usable (strong) | ~15,000w. Zeller's + Doomsday + Gauss methods with worked examples. |
| 10 | Seating arrangement | *(none)* | ❌ in-house required | Indian-exam-specific topic, no academic article. |
| 11 | Venn diagrams | [Venn diagram](https://en.wikipedia.org/wiki/Venn_diagram) | ✅ usable (strong) | ~4,500w covering 2-set/3-set overlap + practical examples. |
| 12a | Statement-Conclusion ← (corrected from "Statement-argument") | *(closest: [Argument](https://en.wikipedia.org/wiki/Argument))* | ⚠️ moderate | Formal-logic framing + fallacies section. Supplement needed for "follows / doesn't follow" format. |
| 12b | Statement-Courses of Action ← (corrected from "Statement-assumption") | *(none; Statement–assumption returns 404)* | ❌ in-house required | Not a Wikipedia concept. |

**Totals:** 5 strong Wikipedia picks (Syllogism, Venn, Clock angle, Day-of-week, Arithmetic-progression), 2 moderate (Argument, and Alphabet-series riding on Arithmetic-progression), **6 in-house primers required** (Blood relations, Coding-decoding, Direction sense, Analogy, Seating arrangement, Statement-Courses of Action).

**Stage-5 scope shift vs. the brief.** Brief §5.1 said "25 articles total" with Wikipedia-biased sourcing. Reality: 5-7 can come from Wikipedia; the remaining 15-20 must be in-house primers (~500-800 words each, written in exam-register Hindi+English). That's ~3-4× more authoring than originally scoped. Compensating: the in-house primers become a **harder competitive moat** than the Wikipedia links (adda247/testbook/careerpower all have similar primers but charge for them; we can ship ours free + bilingual + offline + mobile-first).

### 1.4 Exam-register Hindi glossary — anchor sources and constraint

**Original brief premise** (1.4 of user prompt): "document the canonical Hindi term for each of these English technical terms. Use current RRB bilingual official papers as the authority." This assumed such papers existed. **Reality:** official bilingual PYQ papers do not exist as single files — RRB publishes EN and HI as separate PDFs.

**Anchor sources located:**

| Source | URL | Size | Extractability (pdf-parse) |
|---|---|---|---|
| RRBCDG Detailed CEN 06/2024 (English, authoritative) | `https://www.rrbcdg.gov.in/uploads/2024/06-NTPCUG/Detailed%20CEN%2006-2024%20NTPC.pdf` | 2.85 MB | 79 pp / 159,177 chars / clean 0.79 — **clean extraction** |
| RRBCDG Detailed CEN 06/2024 (Hindi, authoritative) | `https://www.rrbcdg.gov.in/uploads/2024/06-NTPCUG/Detailed%20CEN%2006-2024%20NTPC-Hindi.pdf` | 3.54 MB | 81 pp / 150,889 chars / clean 0.61 — **text-based but legacy font encoding**; Devanagari glyphs systematically re-mapped (pdf-parse emits `Ǔनदȶ श` for `निर्देश`, `¢ै Ǔतज` for `क्षैतिज`). Visually correct when rendered in a PDF viewer; broken when read as text. |
| Adda247 Paper 07 (Hindi) | — | 592 KB | 17 pp / 18,568 chars. Devanagari with different substitution bugs (`व→ज`, `न→ज`, `द→ि`). Visually correct in PDF viewer. |

**The broken-extraction finding is a feature, not a bug, under the pivot.** Since we're no longer machine-processing Hindi text (the PDF renders natively on device), the glossary is only needed for two narrow cases:

1. **Stage 4 original-Q authoring** — when a human content author writes an original Hindi stem, which Hindi term do they use for "percentage" / "simple interest" / "constitution" / etc.? Exam-register vs. scholarly Hindi matters for student comprehension.
2. **UI copy** — any Hindi interface strings (instructions, buttons, etc.) should use the same register.

**Glossary extraction method (revised):** A content author opens the RRBCDG Hindi notification PDF side-by-side with the English one in a PDF viewer (renders correctly — the mangling is only in extract). Walks page by page. Records the canonical Hindi term for each term on the brief's glossary list. This is a **~2-hour manual task**, not a pipeline stage. Output is a single markdown file, `docs/exam-register-hi-glossary.md`, created in Stage 2 and referenced by all Stage 4 authors.

**Preliminary glossary (seed, from spot-checks of RRBCDG + adda247 Paper 07 PDFs; confirm during Stage 2):**

| English term | Hindi (Devanagari) | Register rule |
|---|---|---|
| percentage | प्रतिशत | Keep **both** — narrative Hindi uses प्रतिशत, numeric contexts inline-English is also fine ("10% की दर") |
| simple interest | साधारण ब्याज | Narrative Hindi |
| compound interest | चक्रवृद्धि ब्याज | Narrative Hindi (confirmed: adda247 Paper 07 Q86 uses this) |
| profit / loss | लाभ / हानि | Narrative Hindi |
| ratio / proportion | अनुपात / समानुपात | Narrative Hindi |
| average | औसत | Narrative Hindi |
| speed, distance, time | गति, दूरी, समय | Narrative Hindi |
| statement | कथन | Narrative Hindi (confirmed: adda247 Paper 07 Q1) |
| conclusion | निष्कर्ष | Narrative Hindi (confirmed) |
| syllogism | न्यायवाक्य (formal) / inline EN "syllogism" (common) | Use inline EN for brevity in exam context |
| Venn diagram | वेन आरेख (formal) / inline EN "Venn diagram" (common) | Inline EN |
| constitution | संविधान | Narrative Hindi |
| parliament | संसद | Narrative Hindi |
| SI, CI, km/h, ₹, % | *as-is* | Keep English/symbols inline |
| variable names (x, y, A, B) | *as-is* | Keep Latin |

**Gate for Stage 4:** no original Hindi content ships without the glossary being locked in. Deviations require a `// NOTE: deviating from glossary because X` comment.

---

## 2 · Scope-pivot implications for Stages 2-5

### 2.1 Stage 2 — audit + glossary + schema decisions

**In scope:**
- `tools/content-pipeline/src/audit-questions.mjs` — still required; gates **only** Stage 4 original content and the existing 30-Q sample, not PYQ library links.
- `docs/exam-register-hi-glossary.md` — manually authored (~2 hours) from RRBCDG EN+HI PDFs. Single source of truth for Stage 4 authors.
- Schema change (migration `0005`): either (a) extend `tests` table with `kind='PYQ_LINK'` + `external_url` + `source_language` + `source_attribution` columns, OR (b) reuse the `learning_topics` table pattern (has `external_url` already for NCERT). **Recommendation: (a) — PYQ papers should appear in the Tests tab, not the Learn tab, because students look for them there.** A `PYQ_LINK` kind means the card opens a PDF viewer instead of the test player.
- Client-side PDF viewer reuse: feature-tests module gets a new composable `PyqPaperScreen` that reuses the existing NCERT PDF viewer (check `feature-learn` for the exact composable — reuse, don't fork).

**Out of scope (dropped from brief):**
- Schema migration `0005` that extends `questions` with `explanation_method_en/concept_en/method_hi/concept_hi` and `options.trap_reason_*` — these columns were meant for Stage 3 extracted PYQs. Under the library-link model, PYQs don't have stems in our DB, so they don't have option-level trap analysis. **Keep these columns for Stage 4 originals only** — apply to the existing schema as originally designed, just not for Stage 3.

### 2.2 Stage 3 — PYQ library ingestion (replaces PDF extraction)

**In scope:**
- Curate a catalog of 30-50 adda247 papers covering the most exam-relevant slices: CBT-1 UG 2025 (most recent, most matches CEN 07/2025), CBT-1 Graduate 2025, CBT-2 2022, a selection from 2021. Each listing: year, shift, date, language (EN/HI), RRB level (Graduate/Undergraduate), source URL, paper-number-in-our-catalog.
- `tools/content-pipeline/src/seed-pyq-library.mjs` — new script; writes INSERTs into the migration. Catalog lives in JSON (`tools/content-pipeline/pyq-catalog.json`, hand-curated; not scraped).
- Link-check script: `tools/content-pipeline/src/audit-pyq-links.mjs` — HEAD every catalog URL, flag any that return 403/404/non-PDF. Run after seed, run weekly in future via a wakeup scheduler.
- Attribution UI: PYQ card subtitle = "Source: adda247 · [Year] Shift [N]". Tapping opens the PDF viewer.

**Out of scope (dropped):**
- `candidates/pyq-{year}-{shift}.json` text-extraction pipeline.
- Two-layer explanations for PYQs (method + concept + trap analysis) — there are no stems in our DB to explain.
- Hindi translation of PYQ stems — original PDFs render natively.

**Gate 3 deliverable (revised):** 30-50 PYQ papers seeded as `PYQ_LINK` entries, `audit-pyq-links.mjs` clean (all 200 OK), screenshot showing PDF viewer opening an adda247 paper on device.

### 2.3 Stage 4 — Original sectionals (the competitive moat)

**UNCHANGED from user brief with two scope-ups** (since Stage 3's auth debt paid itself off):

- **500 Q across 20 topic sectionals**, 25 Q each. Stage-ups from brief:
  1. **Add trap_analysis to every option** (was: "preferably"; now: "mandatory"). Without PYQ explanations, originals carry the full pedagogy load.
  2. **Add method + concept in both EN and HI** for every Q. Students studying in Hindi shouldn't get thinner explanations.
- Difficulty mix per sectional: 8 EASY / 12 MEDIUM / 5 HARD (unchanged).
- audit-questions.mjs clean before INSERT (unchanged).

**Topic list adjusted to match RRB NTPC official syllabus** (correction from §1.3):

| # | Topic | Subject |
|---|---|---|
| 1 | Syllogism | Reasoning |
| 2 | Blood relations | Reasoning |
| 3 | Coding-decoding | Reasoning |
| 4 | Direction sense | Reasoning |
| 5 | Number series | Reasoning |
| 6 | Alphabet series | Reasoning |
| 7 | Analogy | Reasoning |
| 8 | Clocks | Reasoning |
| 9 | Calendars | Reasoning |
| 10 | Seating arrangement / Puzzle | Reasoning |
| 11 | Venn diagrams | Reasoning |
| 12 | **Statement-Conclusion** | Reasoning |
| 13 | **Statement-Courses of Action** | Reasoning |
| 14 | Mathematical Operations (operator substitution) | Reasoning |
| 15 | Percentage | Math |
| 16 | Simple + Compound Interest | Math |
| 17 | Profit and Loss | Math |
| 18 | Time & Work, Time & Distance | Math |
| 19 | Ratio and Proportion, Average | Math |
| 20 | Geometry + Mensuration | Math |

GA is **intentionally not in Stage 4** — originals don't work for GA (current events shift daily; needs a separate feed pipeline in a future phase).

### 2.4 Stage 5 — Reasoning primers + weak-topic routing

**In scope (revised mix):**
- 5-7 Wikipedia-backed articles (Syllogism, Venn, Clock angle, Day-of-week, Arithmetic-progression, Argument-for-statement-conclusion, optionally Cardinal-direction).
- **15-18 in-house primers** (500-800 words, bilingual, exam-register) covering: Blood relations, Coding-decoding letter-shift, Coding-decoding symbol-based, Direction sense routes, Analogy patterns (word/letter/number), Seating arrangement linear/circular/square, Statement-Courses of Action, Mathematical Operations substitution, Alphabet series addendum.
- **Weak-topic routing** (UNCHANGED from brief — this is the "makes-the-app-feel-smart" feature): post-submit, compute weak tags from wrong answers, top 3 tags → recommend primers + originals with matching tags.

**Out of scope (same as brief):** image-bearing PYQ questions, CBT-2 sectionals (too ambitious for first ship), ranking/percentile.

---

## 3 · Competitive differentiation (why this ships better than adda247)

This matters for Stage 2-5 scope decisions — every piece should either do something the competitors don't, or do it radically cleaner.

| Differentiator | RailPrep | adda247 | testbook | Impact |
|---|---|---|---|---|
| **All PYQs in one place** | ✅ 200+ papers, one catalog, filtered by year/shift/lang/level | ❌ scattered across many blog posts, hard to find | ❌ behind subscription quiz-player | Volume is free via link-layer; competitors can't out-collect us |
| **Offline download** | ✅ reuse NCERT offline cache pattern | ❌ re-downloads every view | ❌ online-only | Train commute use-case |
| **Bilingual first-class** | ✅ same card lists EN + HI variant; one-tap switch | ✅ but not cross-linked | ~ inconsistent | Students switch mid-attempt |
| **Interactive sectionals with 2-layer EN+HI explanations** | ✅ 500 Q with method + concept + trap per option, bilingual | ❌ stem-only "solutions" | 💰 paywalled | **Nobody else ships this** — Stage 4 moat |
| **Weak-topic routing to primers** | ✅ post-submit tag analysis → primer + originals | ❌ | ~ paid tier | Stage 5 moat |
| **No ads, no signup wall, no paywall** | ✅ | ❌ ads + push for paid | ❌ paywalled after trial | UX moat |
| **Mobile-first rendering** | ✅ native Compose PDF viewer, no pinch-zoom wrestling | ~ mobile-web responsive | ~ app but noisy | |

---

## 4 · What we do NOT have (open issues — planner adjudicates)

1. **Answer keys as tappable overlays.** Under the library-link model, users must open two PDFs (the paper + the key) and cross-reference. Not ideal. Two options:
   - (a) Accept the friction in Stage 3; revisit in a future phase.
   - (b) Scrape adda247 key PDFs → parse → surface correct answer number under "Show Answer" tap on a per-Q basis in an in-app overlay. This is *partial* extraction (numeric keys only, not stems) — much safer than full extraction; no Hindi text processing needed.
   - **Recommendation: (a) for Stage 3, keep (b) as a Phase 4 enhancement.** Stage 3 should ship fast with the library working; answer-key overlay is a separate feature that deserves its own scope.

2. **CBT-2 support in the app UI.** Sample test is CBT-1 shaped (100Q/90min). The adda247 library includes CBT-2 papers (120Q/90min). Adding CBT-2 as a test `kind` with correct question count requires a small schema + UI tweak (probably 4-6 hours). Do we do this in Stage 2, or defer? **Recommendation: include in Stage 2**, it's small and makes the PYQ catalog usable without caveats.

3. **Adda247 link-rot risk.** If adda247 migrates their CDN structure (e.g., `wpassets.adda247.com` → `cdn.adda247.com`), all our URLs break at once. Mitigation: `audit-pyq-links.mjs` runs weekly via wakeup scheduler; broken links flagged + auto-fetched via the `https://www.adda247.com/jobs/rrb-ntpc-previous-year-question-paper/` index page. Acceptable risk; same as NCERT URL migrations were handled.

4. **Attribution + DMCA posture.** We're linking, not rehosting. But if adda247 sends a takedown, we de-link and keep the catalog metadata as dead entries. **Add a legal-review TODO** before public listing on Play Store. Not a Stage 3 blocker.

5. **The 30-Q sample test stays "active"** through Stage 3 (users need something to run against while library + sectionals are being built). Stale it at the end of Stage 4 (not Stage 3 as brief said), since before Stage 4 the sample is the only interactive test available.

---

## 5 · Proposed Stage 2-5 execution order (revised)

Gate between each. Advisor-verified numeric thresholds stay (carrying ≥ into Stage 5 = MET).

1. **Stage 2** (3-5 hours)
   1. Build `audit-questions.mjs` + fixture tests (unchanged from brief).
   2. Manually author `docs/exam-register-hi-glossary.md` from RRBCDG EN+HI PDFs (~2h).
   3. Schema migration `0005`: extend `tests` with `PYQ_LINK` kind + `external_url` + `source_language` + `source_attribution`; extend `questions` with `explanation_method_en/hi + explanation_concept_en/hi`; extend `options` with `trap_reason_en/hi`. (Covers Stage 3 + Stage 4 needs in one migration.)
   4. Add CBT-2 as a test kind variant (100Q vs. 120Q handled via existing `total_minutes` + `total_questions` columns; probably just a section-count allowance).
2. **Stage 3** (4-6 hours)
   1. Curate `tools/content-pipeline/pyq-catalog.json` (30-50 papers hand-picked from adda247; verified HEAD).
   2. Build `seed-pyq-library.mjs` + `audit-pyq-links.mjs`.
   3. Apply migration + run seed.
   4. Add `PyqPaperScreen` composable reusing NCERT PDF viewer.
   5. Extend TestCard to branch: `PYQ_LINK` → PDF viewer; everything else → test player.
   6. Screenshot on device.
3. **Stage 4** (largest — 15-25 hours authoring)
   1. Write `audit-questions.mjs` fixture + run it green.
   2. Author 20 topic sectionals, 25 Q each, fully enriched (method + concept EN + HI + trap analysis).
   3. Seed via targeted INSERT migration.
   4. Extend ReviewScreen to show method/concept as tabs + trap_reason under each wrong option.
   5. Screenshot.
4. **Stage 5** (**30-50 hours** — this is the pivot's biggest cost; see §0.1 Cost A)
   1. 5-7 Wikipedia-linked articles (reuse Phase 2.5 WebView primer pattern) — ~3 hours.
   2. **15-18 in-house bilingual primers** (500-800 words, EN + HI, exam-register per glossary) — ~25-45 hours. Each primer: (a) research the topic's exam variants from a sample of adda247 PYQs; (b) write EN draft; (c) write HI version enforcing the glossary; (d) review pass. This is the actual content work.
   3. Weak-topic routing query + card on ResultsScreen — ~3 hours coding.
   4. Final regression: `audit-topics.mjs` + `audit-questions.mjs` + `audit-pyq-links.mjs` all clean. Stale the 30-Q sample.
5. **Stage 6** — `docs/PHASE_3_REPORT.md` in Phase 2.5 style (~2 hours).

**Budget if total is tight (Cost A mitigation):** planner picks one of:
  - **Full ambition** — 30-50h, ships full bilingual in-house primer library; biggest competitive moat.
  - **Trimmed** (~6-10h) — ship only the 5-7 Wikipedia-linked articles + "Coming soon" placeholders for the in-house topics; defer in-house authoring to Phase 4. Weak-topic routing still ships (routes to the placeholder for topics without a primer yet, which is a reasonable degraded state).
  - **Middle path** (~15-20h) — 5-7 Wiki articles + 4-6 *English-only* in-house primers for the highest-frequency reasoning topics (Blood relations, Coding-decoding, Seating arrangement, Direction sense); Hindi versions deferred to Phase 4.

---

## Appendix A · Verified URLs

All URLs below were HEAD-checked during Stage 1 research (2026-04-24). Listed for planner reference and for Stage 2/3 seed curation.

**Official RRB:**
- https://www.rrbcdg.gov.in/2024-05-ntpcg.php — CEN 05/2024 Graduate overview
- https://www.rrbcdg.gov.in/2024-06-ntpcug.php — CEN 06/2024 Undergraduate overview
- https://www.rrbcdg.gov.in/uploads/2024/06-NTPCUG/Detailed%20CEN%2006-2024%20NTPC.pdf — Authoritative pattern + syllabus (EN)
- https://www.rrbcdg.gov.in/uploads/2024/06-NTPCUG/Detailed%20CEN%2006-2024%20NTPC-Hindi.pdf — Authoritative pattern + syllabus (HI)

**Adda247 PYQ catalog (sample — full inventory hits 200+ via their index page):**
- https://www.adda247.com/jobs/rrb-ntpc-previous-year-question-paper/ — Index (discovery point for all papers)
- https://wpassets.adda247.com/wp-content/uploads/multisite/sites/2/2020/11/07162058/RRB-NTPC-Previous-Year-Paper-01-English.pdf — Paper 01 EN (verified text-extractable, 17pp, 20KB chars)
- https://wpassets.adda247.com/wp-content/uploads/multisite/sites/2/2020/12/15111519/RRB-NTPC-Previous-Year-Paper-07-Hindi.pdf — Paper 07 HI (verified text-extractable, 17pp, 18KB chars, Hindi font-sub present)
- https://wpassets.adda247.com/wp-content/uploads/multisite/sites/2/2020/12/12120449/RRB-NTPC-Previous-Year-Paper-06-Hindi.pdf
- https://wpassets.adda247.com/wp-content/uploads/multisite/sites/2/2020/12/15111602/RRB-NTPC-Previous-Year-Paper-08-Hindi.pdf
- https://wpassets.adda247.com/wp-content/uploads/multisite/sites/2/2020/12/24121027/RRB-NTPC-Previous-Year-Paper-14-Hindi.pdf
- https://wpassets.adda247.com/wp-content/uploads/multisite/sites/2/2020/12/10213440/RRB-NTPC-Previous-Year-Paper-04-English.pdf
- https://wpassets.adda247.com/wp-content/uploads/multisite/sites/2/2020/12/11174601/RRB-NTPC-Previous-Year-Paper-05-English.pdf
- https://wpassets.adda247.com/wp-content/uploads/multisite/sites/2/2020/12/16114355/RRB-NTPC-Previous-Year-Paper-09-English.pdf
- https://wpassets.adda247.com/wp-content/uploads/multisite/sites/2/2020/12/24121336/RRB-NTPC-Previous-Year-Paper-18-English.pdf
- https://wpassets.adda247.com/wp-content/uploads/multisite/sites/2/2020/12/26153014/RRB-NTPC-Previous-Year-Paper-19-English.pdf
- https://wpassets.adda247.com/wp-content/uploads/multisite/sites/2/2020/12/26153051/RRB-NTPC-Previous-Year-Paper-20-English.pdf

**Wikipedia (usable reasoning primers):**
- https://en.wikipedia.org/wiki/Syllogism
- https://en.wikipedia.org/wiki/Venn_diagram
- https://en.wikipedia.org/wiki/Clock_angle_problem
- https://en.wikipedia.org/wiki/Determination_of_the_day_of_the_week
- https://en.wikipedia.org/wiki/Arithmetic_progression
- https://en.wikipedia.org/wiki/Argument *(moderate-use only, for statement-conclusion)*
- https://en.wikipedia.org/wiki/Cardinal_direction *(background-only, not a standalone primer)*

**Rejected sources (documented to prevent rework):**
- https://archive.org/details/rrb-ntpc-question-paper-2021-pdf-in-english — candidate response-sheet; no stems.
- https://drive.google.com/uc?export=download&id=* — GDrive virus-scan gate; fragile.
- https://cracku.in/railways-ntpc-previous-papers — login-wall.
- https://prepp.in/paper/* — 403 on direct fetch.

## Appendix B · Extraction evidence (artefacts in `/tmp/phase3-research/`)

| File | Source | Chars | Verdict |
|---|---|---|---|
| `ntpc2021.pdf` | archive.org | 25,066 | response-sheet, no stems |
| `cen06-en.pdf` | RRBCDG | 159,177 | authoritative EN, clean extraction |
| `cen06-hi.pdf` | RRBCDG | 150,889 | authoritative HI, legacy-font-mangled text |
| `adda247-01-en.pdf` | wpassets.adda247.com | 20,204 | memory-based EN paper, clean stems+options |
| `adda247-07-hi.pdf` | wpassets.adda247.com | 18,568 | memory-based HI paper, font-substitution mangled |

Diagnostic scripts: `tools/content-pipeline/src/pdf-extract-test.mjs`, `pdf-dump.mjs`.

---

**End of Stage 1 research. Awaiting `go stage2` directive from planner.**
