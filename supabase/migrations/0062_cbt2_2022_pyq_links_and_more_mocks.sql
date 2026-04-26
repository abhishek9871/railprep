-- Phase F follow-up: fill CBT-2 with the public 2022 paper set plus two more full mocks.
-- The PDFs are linked and rendered in-app like the existing PYQ library; no question text is copied.
-- The mocks reuse audited RailPrep questions with the CBT-2 split: GA 50 / Math 35 / Reasoning 35.

begin;

insert into public.tests (
  slug, title_en, title_hi, kind, exam_target,
  total_questions, total_minutes, negative_marking_fraction,
  external_url, source_language, source_attribution,
  status
) values
  ('pyq-ntpc-cbt2-2022-05-09-s1', 'RRB NTPC CBT-2 2022 — 09 May 2022, Shift 1', 'RRB NTPC CBT-2 2022 — 09 May 2022, Shift 1', 'PYQ_LINK', 'NTPC_CBT2', 120, 90, 0.3333, 'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/05/16110526/NTPC-CBT-2-2019_09.05.2022_10.30-12.00-PM-Paper.pdf', 'en', 'adda247.com', 'active'),
  ('pyq-ntpc-cbt2-2022-05-09-s2', 'RRB NTPC CBT-2 2022 — 09 May 2022, Shift 2', 'RRB NTPC CBT-2 2022 — 09 May 2022, Shift 2', 'PYQ_LINK', 'NTPC_CBT2', 120, 90, 0.3333, 'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/05/16110525/NTPC-CBT-2-2019_09.05.2022_3.30-5.00-PM-Paper.pdf', 'en', 'adda247.com', 'active'),
  ('pyq-ntpc-cbt2-2022-05-10-s1', 'RRB NTPC CBT-2 2022 — 10 May 2022, Shift 1', 'RRB NTPC CBT-2 2022 — 10 May 2022, Shift 1', 'PYQ_LINK', 'NTPC_CBT2', 120, 90, 0.3333, 'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/05/16110528/NTPC-CBT-2-2019_10.05.2022_10.30-12.00-PM-Paper.pdf', 'en', 'adda247.com', 'active'),
  ('pyq-ntpc-cbt2-2022-06-12-s1', 'RRB NTPC CBT-2 2022 — 12 Jun 2022, Shift 1', 'RRB NTPC CBT-2 2022 — 12 Jun 2022, Shift 1', 'PYQ_LINK', 'NTPC_CBT2', 120, 90, 0.3333, 'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/05/16110530/NTPC-CBT-2-2019_12.06.2022_9.00-10.30-AM-Paper.pdf', 'en', 'adda247.com', 'active'),
  ('pyq-ntpc-cbt2-2022-06-12-s2', 'RRB NTPC CBT-2 2022 — 12 Jun 2022, Shift 2', 'RRB NTPC CBT-2 2022 — 12 Jun 2022, Shift 2', 'PYQ_LINK', 'NTPC_CBT2', 120, 90, 0.3333, 'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/05/16110532/NTPC-CBT-2-2019_12.06.2022_12.45-2.15-PM-Paper.pdf', 'en', 'adda247.com', 'active'),
  ('pyq-ntpc-cbt2-2022-06-13-s1', 'RRB NTPC CBT-2 2022 — 13 Jun 2022, Shift 1', 'RRB NTPC CBT-2 2022 — 13 Jun 2022, Shift 1', 'PYQ_LINK', 'NTPC_CBT2', 120, 90, 0.3333, 'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/05/16110533/NTPC-CBT-2-2019_13.06.2022_9.00-10.30-AM-Paper.pdf', 'en', 'adda247.com', 'active'),
  ('pyq-ntpc-cbt2-2022-06-13-s2', 'RRB NTPC CBT-2 2022 — 13 Jun 2022, Shift 2', 'RRB NTPC CBT-2 2022 — 13 Jun 2022, Shift 2', 'PYQ_LINK', 'NTPC_CBT2', 120, 90, 0.3333, 'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/05/16110537/NTPC-CBT-2-2019_13.06.2022_12.45-2.15-PM-Paper.pdf', 'en', 'adda247.com', 'active'),
  ('pyq-ntpc-cbt2-2022-06-14-s1', 'RRB NTPC CBT-2 2022 — 14 Jun 2022, Shift 1', 'RRB NTPC CBT-2 2022 — 14 Jun 2022, Shift 1', 'PYQ_LINK', 'NTPC_CBT2', 120, 90, 0.3333, 'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/05/16110539/NTPC-CBT-2-2019_14.06.2022_9.00-10.30-AM-Paper.pdf', 'en', 'adda247.com', 'active'),
  ('pyq-ntpc-cbt2-2022-06-14-s2', 'RRB NTPC CBT-2 2022 — 14 Jun 2022, Shift 2', 'RRB NTPC CBT-2 2022 — 14 Jun 2022, Shift 2', 'PYQ_LINK', 'NTPC_CBT2', 120, 90, 0.3333, 'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/05/16110540/NTPC-CBT-2-2019_14.06.2022_12.45-2.15-PM-Paper.pdf', 'en', 'adda247.com', 'active'),
  ('pyq-ntpc-cbt2-2022-06-15-s1', 'RRB NTPC CBT-2 2022 — 15 Jun 2022, Shift 1', 'RRB NTPC CBT-2 2022 — 15 Jun 2022, Shift 1', 'PYQ_LINK', 'NTPC_CBT2', 120, 90, 0.3333, 'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/05/16110544/NTPC-CBT-2-2019_15.06.2022_9.00-10.30-AM-Paper.pdf', 'en', 'adda247.com', 'active'),
  ('pyq-ntpc-cbt2-2022-06-15-s2', 'RRB NTPC CBT-2 2022 — 15 Jun 2022, Shift 2', 'RRB NTPC CBT-2 2022 — 15 Jun 2022, Shift 2', 'PYQ_LINK', 'NTPC_CBT2', 120, 90, 0.3333, 'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/05/16110546/NTPC-CBT-2-2019_15.06.2022_12.45-2.15-PM-Paper.pdf', 'en', 'adda247.com', 'active'),
  ('pyq-ntpc-cbt2-2022-06-15-s3', 'RRB NTPC CBT-2 2022 — 15 Jun 2022, Shift 3', 'RRB NTPC CBT-2 2022 — 15 Jun 2022, Shift 3', 'PYQ_LINK', 'NTPC_CBT2', 120, 90, 0.3333, 'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/05/16110542/NTPC-CBT-2-2019_15.06.2022_4.30-6.00-PM-Paper.pdf', 'en', 'adda247.com', 'active'),
  ('pyq-ntpc-cbt2-2022-06-16-s1', 'RRB NTPC CBT-2 2022 — 16 Jun 2022, Shift 1', 'RRB NTPC CBT-2 2022 — 16 Jun 2022, Shift 1', 'PYQ_LINK', 'NTPC_CBT2', 120, 90, 0.3333, 'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/05/16110550/NTPC-CBT-2-2019_16.06.2022_9.00-10.30-AM-Paper.pdf', 'en', 'adda247.com', 'active'),
  ('pyq-ntpc-cbt2-2022-06-16-s2', 'RRB NTPC CBT-2 2022 — 16 Jun 2022, Shift 2', 'RRB NTPC CBT-2 2022 — 16 Jun 2022, Shift 2', 'PYQ_LINK', 'NTPC_CBT2', 120, 90, 0.3333, 'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/05/16110552/NTPC-CBT-2-2019_16.06.2022_12.45-2.15-PM-Paper.pdf', 'en', 'adda247.com', 'active'),
  ('pyq-ntpc-cbt2-2022-06-16-s3', 'RRB NTPC CBT-2 2022 — 16 Jun 2022, Shift 3', 'RRB NTPC CBT-2 2022 — 16 Jun 2022, Shift 3', 'PYQ_LINK', 'NTPC_CBT2', 120, 90, 0.3333, 'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/05/16110548/NTPC-CBT-2-2019_16.06.2022_4.30-6.00-PM-Paper.pdf', 'en', 'adda247.com', 'active'),
  ('pyq-ntpc-cbt2-2022-06-17-s1', 'RRB NTPC CBT-2 2022 — 17 Jun 2022, Shift 1', 'RRB NTPC CBT-2 2022 — 17 Jun 2022, Shift 1', 'PYQ_LINK', 'NTPC_CBT2', 120, 90, 0.3333, 'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/05/16110555/NTPC-CBT-2-2019_17.06.2022_9.00-10.30-AM-Paper.pdf', 'en', 'adda247.com', 'active'),
  ('pyq-ntpc-cbt2-2022-06-17-s2', 'RRB NTPC CBT-2 2022 — 17 Jun 2022, Shift 2', 'RRB NTPC CBT-2 2022 — 17 Jun 2022, Shift 2', 'PYQ_LINK', 'NTPC_CBT2', 120, 90, 0.3333, 'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/05/16110557/NTPC-CBT-2-2019_17.06.2022_12.45-2.15-PM-Paper.pdf', 'en', 'adda247.com', 'active'),
  ('pyq-ntpc-cbt2-2022-06-17-s3', 'RRB NTPC CBT-2 2022 — 17 Jun 2022, Shift 3', 'RRB NTPC CBT-2 2022 — 17 Jun 2022, Shift 3', 'PYQ_LINK', 'NTPC_CBT2', 120, 90, 0.3333, 'https://www.adda247.com/jobs/wp-content/uploads/sites/22/2025/05/16110554/NTPC-CBT-2-2019_17.06.2022_4.30-6.00-PM-Paper.pdf', 'en', 'adda247.com', 'active')
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

select pg_temp.seed_cbt2_mock('ntpc-cbt2-full-mock-04', 'NTPC CBT-2 Full Mock 04', 'NTPC CBT-2 Full Mock 04', 4);
select pg_temp.seed_cbt2_mock('ntpc-cbt2-full-mock-05', 'NTPC CBT-2 Full Mock 05', 'NTPC CBT-2 Full Mock 05', 5);

commit;
