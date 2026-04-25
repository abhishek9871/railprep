-- AUTO-GENERATED via tools/content-pipeline/src/emit-pyq-seed.mjs — do not hand-edit.
-- Re-run the emitter after changing pyq-catalog.json, then apply via Supabase MCP.
-- Each row is idempotent on tests.slug.

begin;

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-08-07-s1-en', 'RRB NTPC UG 2025 — 7 Aug 2025, Shift 1 (English)', 'आरआरबी एनटीपीसी यूजी 2025 — 7 अगस्त 2025, शिफ्ट 1 (अंग्रेज़ी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/15182002/RRB-NTPC-UG-Answer-Key-English-07-08-2025-Shift-1-Paper.pdf', 'en', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-08-07-s2-en', 'RRB NTPC UG 2025 — 7 Aug 2025, Shift 2 (English)', 'आरआरबी एनटीपीसी यूजी 2025 — 7 अगस्त 2025, शिफ्ट 2 (अंग्रेज़ी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/15182004/RRB-NTPC-UG-Answer-Key-English-07-08-2025-Shift-2-Paper.pdf', 'en', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-08-07-s2-hi', 'RRB NTPC UG 2025 — 7 Aug 2025, Shift 2 (Hindi)', 'आरआरबी एनटीपीसी यूजी 2025 — 7 अगस्त 2025, शिफ्ट 2 (हिन्दी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/16152226/RRB_NTPC_UG_Answer_Key_Hindi-07-08-2025-Shift-2-Paper-2.pdf', 'hi', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-08-07-s3-en', 'RRB NTPC UG 2025 — 7 Aug 2025, Shift 3 (English)', 'आरआरबी एनटीपीसी यूजी 2025 — 7 अगस्त 2025, शिफ्ट 3 (अंग्रेज़ी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/15182005/RRB-NTPC-UG-Answer-Key-English-07-08-2025-Shift-3-Paper.pdf', 'en', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-08-07-s3-hi', 'RRB NTPC UG 2025 — 7 Aug 2025, Shift 3 (Hindi)', 'आरआरबी एनटीपीसी यूजी 2025 — 7 अगस्त 2025, शिफ्ट 3 (हिन्दी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/16152227/RRB_NTPC_UG_Answer_Key_Hindi-07-08-2025-Shift-3-Paper-2.pdf', 'hi', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-08-08-s1-en', 'RRB NTPC UG 2025 — 8 Aug 2025, Shift 1 (English)', 'आरआरबी एनटीपीसी यूजी 2025 — 8 अगस्त 2025, शिफ्ट 1 (अंग्रेज़ी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/15182006/RRB-NTPC-UG-Answer-Key-English-08-08-2025-Shift-1-Paper.pdf', 'en', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-08-08-s2-en', 'RRB NTPC UG 2025 — 8 Aug 2025, Shift 2 (English)', 'आरआरबी एनटीपीसी यूजी 2025 — 8 अगस्त 2025, शिफ्ट 2 (अंग्रेज़ी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/15182007/RRB-NTPC-UG-Answer-Key-English-08-08-2025-Shift-2-Paper.pdf', 'en', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-08-08-s2-hi', 'RRB NTPC UG 2025 — 8 Aug 2025, Shift 2 (Hindi)', 'आरआरबी एनटीपीसी यूजी 2025 — 8 अगस्त 2025, शिफ्ट 2 (हिन्दी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/16152229/RRB_NTPC_UG_Answer_Key_Hindi-08-08-2025-Shift-2-Paper-2.pdf', 'hi', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-08-08-s3-en', 'RRB NTPC UG 2025 — 8 Aug 2025, Shift 3 (English)', 'आरआरबी एनटीपीसी यूजी 2025 — 8 अगस्त 2025, शिफ्ट 3 (अंग्रेज़ी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/15182008/RRB-NTPC-UG-Answer-Key-English-08-08-2025-Shift-3-Paper.pdf', 'en', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-08-08-s3-hi', 'RRB NTPC UG 2025 — 8 Aug 2025, Shift 3 (Hindi)', 'आरआरबी एनटीपीसी यूजी 2025 — 8 अगस्त 2025, शिफ्ट 3 (हिन्दी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/16152231/RRB_NTPC_UG_Answer_Key_Hindi-08-08-2025-Shift-3-Paper-2.pdf', 'hi', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-08-11-s1-en', 'RRB NTPC UG 2025 — 11 Aug 2025, Shift 1 (English)', 'आरआरबी एनटीपीसी यूजी 2025 — 11 अगस्त 2025, शिफ्ट 1 (अंग्रेज़ी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/15182018/RRB-NTPC-UG-Answer-Key-English-11-08-2025-Shift-1-Paper.pdf', 'en', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-08-11-s1-hi', 'RRB NTPC UG 2025 — 11 Aug 2025, Shift 1 (Hindi)', 'आरआरबी एनटीपीसी यूजी 2025 — 11 अगस्त 2025, शिफ्ट 1 (हिन्दी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/16152242/RRB_NTPC_UG_Answer_Key_Hindi-11-08-2025-Shift-1-Paper-2.pdf', 'hi', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-08-11-s2-en', 'RRB NTPC UG 2025 — 11 Aug 2025, Shift 2 (English)', 'आरआरबी एनटीपीसी यूजी 2025 — 11 अगस्त 2025, शिफ्ट 2 (अंग्रेज़ी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/15182019/RRB-NTPC-UG-Answer-Key-English-11-08-2025-Shift-2-Paper.pdf', 'en', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-08-11-s2-hi', 'RRB NTPC UG 2025 — 11 Aug 2025, Shift 2 (Hindi)', 'आरआरबी एनटीपीसी यूजी 2025 — 11 अगस्त 2025, शिफ्ट 2 (हिन्दी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/16152244/RRB_NTPC_UG_Answer_Key_Hindi-11-08-2025-Shift-2-Paper-2.pdf', 'hi', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-08-11-s3-en', 'RRB NTPC UG 2025 — 11 Aug 2025, Shift 3 (English)', 'आरआरबी एनटीपीसी यूजी 2025 — 11 अगस्त 2025, शिफ्ट 3 (अंग्रेज़ी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/15182020/RRB-NTPC-UG-Answer-Key-English-11-08-2025-Shift-3-Paper.pdf', 'en', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-08-11-s3-hi', 'RRB NTPC UG 2025 — 11 Aug 2025, Shift 3 (Hindi)', 'आरआरबी एनटीपीसी यूजी 2025 — 11 अगस्त 2025, शिफ्ट 3 (हिन्दी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/16152246/RRB_NTPC_UG_Answer_Key_Hindi-11-08-2025-Shift-3-Paper-1-1.pdf', 'hi', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-08-12-s1-en', 'RRB NTPC UG 2025 — 12 Aug 2025, Shift 1 (English)', 'आरआरबी एनटीपीसी यूजी 2025 — 12 अगस्त 2025, शिफ्ट 1 (अंग्रेज़ी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/15182021/RRB-NTPC-UG-Answer-Key-English-12-08-2025-Shift-1-Paper.pdf', 'en', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-08-12-s1-hi', 'RRB NTPC UG 2025 — 12 Aug 2025, Shift 1 (Hindi)', 'आरआरबी एनटीपीसी यूजी 2025 — 12 अगस्त 2025, शिफ्ट 1 (हिन्दी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/16152248/RRB_NTPC_UG_Answer_Key_Hindi-12-08-2025-Shift-1-Paper-2.pdf', 'hi', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-08-12-s2-en', 'RRB NTPC UG 2025 — 12 Aug 2025, Shift 2 (English)', 'आरआरबी एनटीपीसी यूजी 2025 — 12 अगस्त 2025, शिफ्ट 2 (अंग्रेज़ी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/15182023/RRB-NTPC-UG-Answer-Key-English-12-08-2025-Shift-2-Paper.pdf', 'en', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-08-12-s2-hi', 'RRB NTPC UG 2025 — 12 Aug 2025, Shift 2 (Hindi)', 'आरआरबी एनटीपीसी यूजी 2025 — 12 अगस्त 2025, शिफ्ट 2 (हिन्दी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/16152249/RRB_NTPC_UG_Answer_Key_Hindi-12-08-2025-Shift-2-Paper-2.pdf', 'hi', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-08-12-s3-en', 'RRB NTPC UG 2025 — 12 Aug 2025, Shift 3 (English)', 'आरआरबी एनटीपीसी यूजी 2025 — 12 अगस्त 2025, शिफ्ट 3 (अंग्रेज़ी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/15182025/RRB-NTPC-UG-Answer-Key-English-12-08-2025-Shift-3-Paper.pdf', 'en', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-08-12-s3-hi', 'RRB NTPC UG 2025 — 12 Aug 2025, Shift 3 (Hindi)', 'आरआरबी एनटीपीसी यूजी 2025 — 12 अगस्त 2025, शिफ्ट 3 (हिन्दी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/16152251/RRB_NTPC_UG_Answer_Key_Hindi-12-08-2025-Shift-3-Paper-2.pdf', 'hi', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-08-13-s1-en', 'RRB NTPC UG 2025 — 13 Aug 2025, Shift 1 (English)', 'आरआरबी एनटीपीसी यूजी 2025 — 13 अगस्त 2025, शिफ्ट 1 (अंग्रेज़ी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/15182027/RRB-NTPC-UG-Answer-Key-English-13-08-2025-Shift-1-Paper.pdf', 'en', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-08-13-s1-hi', 'RRB NTPC UG 2025 — 13 Aug 2025, Shift 1 (Hindi)', 'आरआरबी एनटीपीसी यूजी 2025 — 13 अगस्त 2025, शिफ्ट 1 (हिन्दी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/16152253/RRB_NTPC_UG_Answer_Key_Hindi-13-08-2025-Shift-1-Paper-2.pdf', 'hi', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-08-13-s2-en', 'RRB NTPC UG 2025 — 13 Aug 2025, Shift 2 (English)', 'आरआरबी एनटीपीसी यूजी 2025 — 13 अगस्त 2025, शिफ्ट 2 (अंग्रेज़ी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/15182028/RRB-NTPC-UG-Answer-Key-English-13-08-2025-Shift-2-Paper.pdf', 'en', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-08-13-s2-hi', 'RRB NTPC UG 2025 — 13 Aug 2025, Shift 2 (Hindi)', 'आरआरबी एनटीपीसी यूजी 2025 — 13 अगस्त 2025, शिफ्ट 2 (हिन्दी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/16152255/RRB_NTPC_UG_Answer_Key_Hindi-13-08-2025-Shift-2-Paper-2.pdf', 'hi', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-08-13-s3-en', 'RRB NTPC UG 2025 — 13 Aug 2025, Shift 3 (English)', 'आरआरबी एनटीपीसी यूजी 2025 — 13 अगस्त 2025, शिफ्ट 3 (अंग्रेज़ी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/15182030/RRB-NTPC-UG-Answer-Key-English-13-08-2025-Shift-3-Paper.pdf', 'en', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-08-13-s3-hi', 'RRB NTPC UG 2025 — 13 Aug 2025, Shift 3 (Hindi)', 'आरआरबी एनटीपीसी यूजी 2025 — 13 अगस्त 2025, शिफ्ट 3 (हिन्दी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/16152257/RRB_NTPC_UG_Answer_Key_Hindi-13-08-2025-Shift-3-Paper-2.pdf', 'hi', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-08-14-s1-en', 'RRB NTPC UG 2025 — 14 Aug 2025, Shift 1 (English)', 'आरआरबी एनटीपीसी यूजी 2025 — 14 अगस्त 2025, शिफ्ट 1 (अंग्रेज़ी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/15182031/RRB-NTPC-UG-Answer-Key-English-14-08-2025-Shift-1-Paper.pdf', 'en', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-08-14-s1-hi', 'RRB NTPC UG 2025 — 14 Aug 2025, Shift 1 (Hindi)', 'आरआरबी एनटीपीसी यूजी 2025 — 14 अगस्त 2025, शिफ्ट 1 (हिन्दी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/16152258/RRB_NTPC_UG_Answer_Key_Hindi-14-08-2025-Shift-1-Paper-2.pdf', 'hi', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-08-14-s2-en', 'RRB NTPC UG 2025 — 14 Aug 2025, Shift 2 (English)', 'आरआरबी एनटीपीसी यूजी 2025 — 14 अगस्त 2025, शिफ्ट 2 (अंग्रेज़ी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/15182033/RRB-NTPC-UG-Answer-Key-English-14-08-2025-Shift-2-Paper.pdf', 'en', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-08-14-s2-hi', 'RRB NTPC UG 2025 — 14 Aug 2025, Shift 2 (Hindi)', 'आरआरबी एनटीपीसी यूजी 2025 — 14 अगस्त 2025, शिफ्ट 2 (हिन्दी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/16152300/RRB_NTPC_UG_Answer_Key_Hindi-14-08-2025-Shift-2-Paper-2.pdf', 'hi', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-08-14-s3-en', 'RRB NTPC UG 2025 — 14 Aug 2025, Shift 3 (English)', 'आरआरबी एनटीपीसी यूजी 2025 — 14 अगस्त 2025, शिफ्ट 3 (अंग्रेज़ी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/15182035/RRB-NTPC-UG-Answer-Key-English-14-08-2025-Shift-3-Paper.pdf', 'en', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-08-14-s3-hi', 'RRB NTPC UG 2025 — 14 Aug 2025, Shift 3 (Hindi)', 'आरआरबी एनटीपीसी यूजी 2025 — 14 अगस्त 2025, शिफ्ट 3 (हिन्दी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/16152302/RRB_NTPC_UG_Answer_Key_Hindi-14-08-2025-Shift-3-Paper-2.pdf', 'hi', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-09-01-s1-en', 'RRB NTPC UG 2025 — 1 Sep 2025, Shift 1 (English)', 'आरआरबी एनटीपीसी यूजी 2025 — 1 सितंबर 2025, शिफ्ट 1 (अंग्रेज़ी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/15181948/RRB-NTPC-UG-Answer-Key-English-01-09-2025-Shift-1-Paper.pdf', 'en', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-09-01-s1-hi', 'RRB NTPC UG 2025 — 1 Sep 2025, Shift 1 (Hindi)', 'आरआरबी एनटीपीसी यूजी 2025 — 1 सितंबर 2025, शिफ्ट 1 (हिन्दी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/16152205/RRB_NTPC_UG_Answer_Key_Hindi-01-09-2025-Shift-1-Paper-2.pdf', 'hi', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-09-01-s2-en', 'RRB NTPC UG 2025 — 1 Sep 2025, Shift 2 (English)', 'आरआरबी एनटीपीसी यूजी 2025 — 1 सितंबर 2025, शिफ्ट 2 (अंग्रेज़ी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/15181949/RRB-NTPC-UG-Answer-Key-English-01-09-2025-Shift-2-Paper.pdf', 'en', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-09-01-s2-hi', 'RRB NTPC UG 2025 — 1 Sep 2025, Shift 2 (Hindi)', 'आरआरबी एनटीपीसी यूजी 2025 — 1 सितंबर 2025, शिफ्ट 2 (हिन्दी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/16152206/RRB_NTPC_UG_Answer_Key_Hindi-01-09-2025-Shift-2-Paper-2.pdf', 'hi', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-09-01-s3-en', 'RRB NTPC UG 2025 — 1 Sep 2025, Shift 3 (English)', 'आरआरबी एनटीपीसी यूजी 2025 — 1 सितंबर 2025, शिफ्ट 3 (अंग्रेज़ी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/15181950/RRB-NTPC-UG-Answer-Key-English-01-09-2025-Shift-3-Paper.pdf', 'en', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-09-01-s3-hi', 'RRB NTPC UG 2025 — 1 Sep 2025, Shift 3 (Hindi)', 'आरआरबी एनटीपीसी यूजी 2025 — 1 सितंबर 2025, शिफ्ट 3 (हिन्दी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/16152208/RRB_NTPC_UG_Answer_Key_Hindi-01-09-2025-Shift-3-Paper-2.pdf', 'hi', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-09-02-s1-en', 'RRB NTPC UG 2025 — 2 Sep 2025, Shift 1 (English)', 'आरआरबी एनटीपीसी यूजी 2025 — 2 सितंबर 2025, शिफ्ट 1 (अंग्रेज़ी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/15181951/RRB-NTPC-UG-Answer-Key-English-02-09-2025-Shift-1-Paper.pdf', 'en', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-09-02-s2-en', 'RRB NTPC UG 2025 — 2 Sep 2025, Shift 2 (English)', 'आरआरबी एनटीपीसी यूजी 2025 — 2 सितंबर 2025, शिफ्ट 2 (अंग्रेज़ी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/15181952/RRB-NTPC-UG-Answer-Key-English-02-09-2025-Shift-2-Paper.pdf', 'en', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-09-02-s3-en', 'RRB NTPC UG 2025 — 2 Sep 2025, Shift 3 (English)', 'आरआरबी एनटीपीसी यूजी 2025 — 2 सितंबर 2025, शिफ्ट 3 (अंग्रेज़ी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/15181954/RRB-NTPC-UG-Answer-Key-English-02-09-2025-Shift-3-Paper.pdf', 'en', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-09-02-s1-hi', 'RRB NTPC UG 2025 — 2 Sep 2025, Shift 1 (Hindi)', 'आरआरबी एनटीपीसी यूजी 2025 — 2 सितंबर 2025, शिफ्ट 1 (हिन्दी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/16152209/RRB_NTPC_UG_Answer_Key_Hindi-02-09-2025-Shift-1-Paper-2.pdf', 'hi', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-09-08-s1-en', 'RRB NTPC UG 2025 — 8 Sep 2025, Shift 1 (English)', 'आरआरबी एनटीपीसी यूजी 2025 — 8 सितंबर 2025, शिफ्ट 1 (अंग्रेज़ी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/15182009/RRB-NTPC-UG-Answer-Key-English-08-09-2025-Shift-1-Paper.pdf', 'en', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-09-08-s2-en', 'RRB NTPC UG 2025 — 8 Sep 2025, Shift 2 (English)', 'आरआरबी एनटीपीसी यूजी 2025 — 8 सितंबर 2025, शिफ्ट 2 (अंग्रेज़ी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/15182010/RRB-NTPC-UG-Answer-Key-English-08-09-2025-Shift-2-Paper.pdf', 'en', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-09-08-s3-en', 'RRB NTPC UG 2025 — 8 Sep 2025, Shift 3 (English)', 'आरआरबी एनटीपीसी यूजी 2025 — 8 सितंबर 2025, शिफ्ट 3 (अंग्रेज़ी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/15182012/RRB-NTPC-UG-Answer-Key-English-08-09-2025-Shift-3-Paper.pdf', 'en', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-09-08-s1-hi', 'RRB NTPC UG 2025 — 8 Sep 2025, Shift 1 (Hindi)', 'आरआरबी एनटीपीसी यूजी 2025 — 8 सितंबर 2025, शिफ्ट 1 (हिन्दी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/16153305/RRB_NTPC_UG_Answer_Key_Hindi-08-09-2025-Shift-1-Paper-3.pdf', 'hi', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-09-09-s1-en', 'RRB NTPC UG 2025 — 9 Sep 2025, Shift 1 (English)', 'आरआरबी एनटीपीसी यूजी 2025 — 9 सितंबर 2025, शिफ्ट 1 (अंग्रेज़ी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/15182013/RRB-NTPC-UG-Answer-Key-English-09-09-2025-Shift-1-Paper.pdf', 'en', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-09-09-s2-en', 'RRB NTPC UG 2025 — 9 Sep 2025, Shift 2 (English)', 'आरआरबी एनटीपीसी यूजी 2025 — 9 सितंबर 2025, शिफ्ट 2 (अंग्रेज़ी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/15182014/RRB-NTPC-UG-Answer-Key-English-09-09-2025-Shift-2-Paper.pdf', 'en', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-ug-2025-09-09-s3-en', 'RRB NTPC UG 2025 — 9 Sep 2025, Shift 3 (English)', 'आरआरबी एनटीपीसी यूजी 2025 — 9 सितंबर 2025, शिफ्ट 3 (अंग्रेज़ी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/09/15182016/RRB-NTPC-UG-Answer-Key-English-09-09-2025-Shift-3-Paper.pdf', 'en', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-set-01-en', 'RRB NTPC Practice Set 01 (English)', 'आरआरबी एनटीपीसी अभ्यास सेट 01 (अंग्रेज़ी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://wpassets.adda247.com/wp-content/uploads/multisite/sites/2/2020/11/07162058/RRB-NTPC-Previous-Year-Paper-01-English.pdf', 'en', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-set-04-en', 'RRB NTPC Practice Set 04 (English)', 'आरआरबी एनटीपीसी अभ्यास सेट 04 (अंग्रेज़ी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://wpassets.adda247.com/wp-content/uploads/multisite/sites/2/2020/12/10213440/RRB-NTPC-Previous-Year-Paper-04-English.pdf', 'en', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-set-05-en', 'RRB NTPC Practice Set 05 (English)', 'आरआरबी एनटीपीसी अभ्यास सेट 05 (अंग्रेज़ी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://wpassets.adda247.com/wp-content/uploads/multisite/sites/2/2020/12/11174601/RRB-NTPC-Previous-Year-Paper-05-English.pdf', 'en', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-set-06-hi', 'RRB NTPC Practice Set 06 (Hindi)', 'आरआरबी एनटीपीसी अभ्यास सेट 06 (हिन्दी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://wpassets.adda247.com/wp-content/uploads/multisite/sites/2/2020/12/12120449/RRB-NTPC-Previous-Year-Paper-06-Hindi.pdf', 'hi', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-set-07-hi', 'RRB NTPC Practice Set 07 (Hindi)', 'आरआरबी एनटीपीसी अभ्यास सेट 07 (हिन्दी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://wpassets.adda247.com/wp-content/uploads/multisite/sites/2/2020/12/15111519/RRB-NTPC-Previous-Year-Paper-07-Hindi.pdf', 'hi', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-set-08-hi', 'RRB NTPC Practice Set 08 (Hindi)', 'आरआरबी एनटीपीसी अभ्यास सेट 08 (हिन्दी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://wpassets.adda247.com/wp-content/uploads/multisite/sites/2/2020/12/15111602/RRB-NTPC-Previous-Year-Paper-08-Hindi.pdf', 'hi', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-set-09-en', 'RRB NTPC Practice Set 09 (English)', 'आरआरबी एनटीपीसी अभ्यास सेट 09 (अंग्रेज़ी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://wpassets.adda247.com/wp-content/uploads/multisite/sites/2/2020/12/16114355/RRB-NTPC-Previous-Year-Paper-09-English.pdf', 'en', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-set-14-hi', 'RRB NTPC Practice Set 14 (Hindi)', 'आरआरबी एनटीपीसी अभ्यास सेट 14 (हिन्दी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://wpassets.adda247.com/wp-content/uploads/multisite/sites/2/2020/12/24121027/RRB-NTPC-Previous-Year-Paper-14-Hindi.pdf', 'hi', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-set-18-en', 'RRB NTPC Practice Set 18 (English)', 'आरआरबी एनटीपीसी अभ्यास सेट 18 (अंग्रेज़ी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://wpassets.adda247.com/wp-content/uploads/multisite/sites/2/2020/12/24121336/RRB-NTPC-Previous-Year-Paper-18-English.pdf', 'en', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-set-19-en', 'RRB NTPC Practice Set 19 (English)', 'आरआरबी एनटीपीसी अभ्यास सेट 19 (अंग्रेज़ी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://wpassets.adda247.com/wp-content/uploads/multisite/sites/2/2020/12/26153014/RRB-NTPC-Previous-Year-Paper-19-English.pdf', 'en', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values (
  'pyq-ntpc-set-20-en', 'RRB NTPC Practice Set 20 (English)', 'आरआरबी एनटीपीसी अभ्यास सेट 20 (अंग्रेज़ी)', 'PYQ_LINK', 'NTPC_CBT1',
  100, 90, 0.3333,
  'https://wpassets.adda247.com/wp-content/uploads/multisite/sites/2/2020/12/26153051/RRB-NTPC-Previous-Year-Paper-20-English.pdf', 'en', 'adda247.com',
  'active'
)
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  updated_at = now();

commit;

-- 62 PYQ_LINK rows upserted.
