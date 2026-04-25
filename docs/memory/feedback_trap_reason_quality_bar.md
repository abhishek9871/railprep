---
name: Trap-reason quality bar for content authoring
description: Every wrong option's trap_reason must name the SPECIFIC arithmetic step or conceptual error that produces THAT exact distractor — no vibe filler
type: feedback
originSessionId: 14001d42-4e8c-4462-9af2-e53ccd128965
---
Every wrong-option `trap_reason` in question content must name the SPECIFIC arithmetic step or conceptual error that produces THAT exact distractor value. No vibe language ("rough estimate", "gut feel", "estimated", "approximation", "rounded", "guessed").

**Why:** User established this rule during D3 sample review (Phase 4, 2026-04-25) after the first sectional draft. A `trap_reason` that says "rough estimate" or "approximation" doesn't help students see what slip they made; the value of `trap_reason` is forensic — naming the exact wrong calculation. Vibe filler signals lazy authoring and was rejected on multiple Q's in the first review pass.

**How to apply:** When authoring or reviewing a question bank (sectionals, primer Q&A, mock tests), for each wrong option either (a) write a `trap_reason` that names the specific computation/misreading that yields THAT value, or (b) redesign the distractor number to one whose specific slip is namable. The rule is enforced as a WARNING (non-blocking) in `tools/content-pipeline/src/audit-questions.mjs` via `FILLER_TRAP_RE = /\b(rough estimate|gut feel|estimated|approximation|rounded|guessed)\b/i`. A clean `audit-questions` run reporting "0 errors, 0 warnings" is the gate before seeding.
