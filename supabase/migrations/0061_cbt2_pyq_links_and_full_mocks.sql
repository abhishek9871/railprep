-- Phase F: CBT-2 must not be an empty surface.
-- Public Adda247-hosted PYQ PDFs are linked like the existing CBT-1 PYQ library:
-- RailPrep renders the original PDF in-app and does not copy competitor-owned question text.
-- Attemptable CBT-2 mocks reuse existing audited RailPrep questions with the official
-- CBT-2 split: GA 50 / Math 35 / Reasoning 35, 120 Q, 90 min, -1/3.

begin;

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values
  ('pyq-ntpc-cbt2-grad-2025-10-13-s1-en', 'RRB NTPC Graduate CBT-2 2025 — 13 Oct 2025, Shift 1 (English)', 'RRB NTPC Graduate CBT-2 2025 — 13 Oct 2025, Shift 1 (English)', 'PYQ_LINK', 'NTPC_CBT2', 120, 90, 0.3333, 'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/10/29184941/RRB-NTPC-CBT-2-Question-Paper-Held-on-13-Oct-2025-S1-Eng.pdf.pdf', 'en', 'adda247.com', 'active'),
  ('pyq-ntpc-cbt2-grad-2025-10-13-s1-hi', 'RRB NTPC Graduate CBT-2 2025 — 13 Oct 2025, Shift 1 (Hindi)', 'RRB NTPC Graduate CBT-2 2025 — 13 Oct 2025, Shift 1 (Hindi)', 'PYQ_LINK', 'NTPC_CBT2', 120, 90, 0.3333, 'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/10/29184943/RRB-NTPC-CBT-2-Question-Paper-Held-on-13-Oct-2025-S1-Hindi.pdf.pdf', 'hi', 'adda247.com', 'active'),
  ('pyq-ntpc-cbt2-grad-2025-10-13-s2-en', 'RRB NTPC Graduate CBT-2 2025 — 13 Oct 2025, Shift 2 (English)', 'RRB NTPC Graduate CBT-2 2025 — 13 Oct 2025, Shift 2 (English)', 'PYQ_LINK', 'NTPC_CBT2', 120, 90, 0.3333, 'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/10/29184945/RRB-NTPC-CBT-2-Question-Paper-Held-on-13-Oct-2025-S2-Eng.pdf', 'en', 'adda247.com', 'active'),
  ('pyq-ntpc-cbt2-grad-2025-10-13-s2-hi', 'RRB NTPC Graduate CBT-2 2025 — 13 Oct 2025, Shift 2 (Hindi)', 'RRB NTPC Graduate CBT-2 2025 — 13 Oct 2025, Shift 2 (Hindi)', 'PYQ_LINK', 'NTPC_CBT2', 120, 90, 0.3333, 'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/10/29184948/RRB-NTPC-CBT-2-Question-Paper-Held-on-13-Oct-2025-S2-Hindi.pdf.pdf', 'hi', 'adda247.com', 'active'),
  ('pyq-ntpc-cbt2-ug-2025-12-20-s1-en', 'RRB NTPC UG CBT-2 2025 — 20 Dec 2025 (English)', 'RRB NTPC UG CBT-2 2025 — 20 Dec 2025 (English)', 'PYQ_LINK', 'NTPC_CBT2', 120, 90, 0.3333, 'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/12/24171445/RRB-NTPC-UG-CBT-2-Question-Paper-20-Dec-2025-4.30-6-English.pdf', 'en', 'adda247.com', 'active'),
  ('pyq-ntpc-cbt2-ug-2025-12-20-s1-hi', 'RRB NTPC UG CBT-2 2025 — 20 Dec 2025 (Hindi)', 'RRB NTPC UG CBT-2 2025 — 20 Dec 2025 (Hindi)', 'PYQ_LINK', 'NTPC_CBT2', 120, 90, 0.3333, 'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/12/24171446/RRB-NTPC-UG-CBT-2-Question-Paper-20-Dec-2025-4.30-6-Hindi.pdf', 'hi', 'adda247.com', 'active')
on conflict (slug) do update set
  title_en = excluded.title_en,
  title_hi = excluded.title_hi,
  total_questions = excluded.total_questions,
  total_minutes = excluded.total_minutes,
  negative_marking_fraction = excluded.negative_marking_fraction,
  external_url = excluded.external_url,
  source_language = excluded.source_language,
  source_attribution = excluded.source_attribution,
  exam_target = excluded.exam_target,
  status = excluded.status,
  updated_at = now();

create or replace function pg_temp.seed_cbt2_section(
  p_test_id uuid,
  p_subject text,
  p_display_order smallint,
  p_title_en text,
  p_title_hi text,
  p_count int,
  p_offset int
) returns void
language plpgsql
as $$
declare
  v_section_id uuid;
begin
  select id into v_section_id
    from public.test_sections
   where test_id = p_test_id and display_order = p_display_order
   limit 1;

  if v_section_id is null then
    insert into public.test_sections (test_id, title_en, title_hi, question_count, display_order, subject_hint)
    values (p_test_id, p_title_en, p_title_hi, p_count, p_display_order, p_subject)
    returning id into v_section_id;
  end if;

  if exists (select 1 from public.questions where section_id = v_section_id) then
    return;
  end if;

  create temporary table if not exists tmp_cbt2_question_map (
    old_id uuid not null,
    new_id uuid not null,
    new_section_id uuid not null,
    display_order smallint not null
  ) on commit drop;

  truncate table tmp_cbt2_question_map;

  insert into tmp_cbt2_question_map (old_id, new_id, new_section_id, display_order)
  with ranked as (
    select
      q.id,
      row_number() over (order by md5(q.id::text)) as rn
    from public.questions q
    join public.test_sections s on s.id = q.section_id
    join public.tests t on t.id = s.test_id
    where q.status = 'active'
      and t.status = 'active'
      and t.kind <> 'PYQ_LINK'
      and t.kind <> 'DAILY_DIGEST'
      and t.slug not like 'ntpc-cbt2-full-mock-%'
      and s.subject_hint = p_subject
  ),
  chosen as (
    select id, row_number() over (order by rn) as display_order
    from ranked
    where rn > p_offset and rn <= p_offset + p_count
  )
  select id, gen_random_uuid(), v_section_id, display_order::smallint
    from chosen;

  if (select count(*) from tmp_cbt2_question_map) <> p_count then
    raise exception 'not enough % questions to seed CBT-2 mock section', p_subject using errcode = 'P0002';
  end if;

  insert into public.questions (
    id, section_id, display_order, stem_en, stem_hi,
    explanation_en, explanation_hi,
    explanation_method_en, explanation_concept_en,
    explanation_method_hi, explanation_concept_hi,
    difficulty, tags, source, license, status
  )
  select
    m.new_id, m.new_section_id, m.display_order,
    q.stem_en, q.stem_hi,
    q.explanation_en, q.explanation_hi,
    q.explanation_method_en, q.explanation_concept_en,
    q.explanation_method_hi, q.explanation_concept_hi,
    q.difficulty, q.tags, q.source, q.license, q.status
  from tmp_cbt2_question_map m
  join public.questions q on q.id = m.old_id;

  insert into public.options (
    id, question_id, label, text_en, text_hi, is_correct, trap_reason_en, trap_reason_hi
  )
  select
    gen_random_uuid(), m.new_id,
    o.label, o.text_en, o.text_hi, o.is_correct, o.trap_reason_en, o.trap_reason_hi
  from tmp_cbt2_question_map m
  join public.options o on o.question_id = m.old_id;
end;
$$;

create or replace function pg_temp.seed_cbt2_mock(
  p_slug text,
  p_title_en text,
  p_title_hi text,
  p_seed int
) returns void
language plpgsql
as $$
declare
  v_test_id uuid;
begin
  insert into public.tests (
    slug, title_en, title_hi, kind, exam_target,
    total_questions, total_minutes, negative_marking_fraction,
    is_pro, status
  )
  values (
    p_slug, p_title_en, p_title_hi, 'CBT2_FULL', 'NTPC_CBT2',
    120, 90, 0.3333,
    false, 'active'
  )
  on conflict (slug) do update set
    title_en = excluded.title_en,
    title_hi = excluded.title_hi,
    kind = excluded.kind,
    exam_target = excluded.exam_target,
    total_questions = excluded.total_questions,
    total_minutes = excluded.total_minutes,
    negative_marking_fraction = excluded.negative_marking_fraction,
    is_pro = excluded.is_pro,
    status = excluded.status,
    updated_at = now()
  returning id into v_test_id;

  perform pg_temp.seed_cbt2_section(v_test_id, 'ga', 1::smallint, 'General Awareness', 'General Awareness', 50, (p_seed - 1) * 50);
  perform pg_temp.seed_cbt2_section(v_test_id, 'math', 2::smallint, 'Mathematics', 'Mathematics', 35, (p_seed - 1) * 35);
  perform pg_temp.seed_cbt2_section(v_test_id, 'reason', 3::smallint, 'General Intelligence & Reasoning', 'General Intelligence & Reasoning', 35, (p_seed - 1) * 35);
end;
$$;

select pg_temp.seed_cbt2_mock('ntpc-cbt2-full-mock-01', 'NTPC CBT-2 Full Mock 01', 'NTPC CBT-2 Full Mock 01', 1);
select pg_temp.seed_cbt2_mock('ntpc-cbt2-full-mock-02', 'NTPC CBT-2 Full Mock 02', 'NTPC CBT-2 Full Mock 02', 2);
select pg_temp.seed_cbt2_mock('ntpc-cbt2-full-mock-03', 'NTPC CBT-2 Full Mock 03', 'NTPC CBT-2 Full Mock 03', 3);

commit;
