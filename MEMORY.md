# RailPrep — project memory index

Any agent (Codex, Claude Code, fresh sessions) reads this file first.
All long-term memory lives in `docs/memory/`. Update this index when adding files.

## Files

- `docs/memory/MEMORY.md` — original Claude Code memory index (preserved for reference)
- `docs/memory/feedback_trap_reason_quality_bar.md` — content quality rule: trap_reasons must name specific arithmetic/conceptual errors, not vibe language. Audit-questions FILLER_TRAP_RE enforces.
- `docs/memory/feedback_verify_and_dont_break.md` — three rules: verify on device before "done", don't regress while fixing, stop after 3 failed cycles and replan
- `docs/memory/railprep_dev_device_revanced.md` — OPPO CPH2491 + Revanced GmsCore quirks for YouTube playback
- `docs/memory/railprep_ncert_content.md` — NCERT content sourcing, codes, and integration rules
- `docs/memory/railprep_ntpc_syllabus_canonical.md` — canonical syllabus reference for tagging and content scope
- `docs/memory/railprep_phase4_d3_complete.md` — D3 completion state, baseline question/option/topic counts
- `docs/memory/railprep_production_playbook.md` — production-readiness checklist
- `docs/memory/railprep_pyq_library_pivot.md` — decision record for the adda247-CDN PYQ library

## Conventions

- Add new memory files to `docs/memory/`, then update this index.
- File naming: `feedback_*` for cross-cutting rules learned from mistakes, `railprep_*` for project-specific knowledge.
- Reports go in `docs/PHASE_*_REPORT.md` — not memory files.
