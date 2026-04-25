-- Recovered from Supabase MCP on 2026-04-25.
-- MCP version: 20260424031732
-- Original application order is determined by version timestamp, not filename.

-- 1. Rename GA/Indian History → History (its content is Class 11 Themes in World History;
--    broadening the label lets both world + Indian material live here. Indian history
--    content can be added later via kips1/kips2 or class 7-8 "Our Pasts" books.)
update public.chapters
  set title_en = 'History', title_hi = 'इतिहास'
  where id = 'df5e8579-bbce-4487-9a11-6589fae2b233';

-- Also update topic titles to be honest about what's in the file
update public.topics set
  title_en = 'Writing and City Life — Mesopotamia (NCERT Class 11 World History Ch 1)',
  title_hi = 'लेखन और शहरी जीवन — मेसोपोटामिया (NCERT कक्षा 11 विश्व इतिहास अध्याय 1)'
  where external_pdf_url = 'https://ncert.nic.in/textbook/pdf/kehs101.pdf';
update public.topics set
  title_en = 'An Empire Across Three Continents — Rome (NCERT Class 11 World History Ch 2)',
  title_hi = 'तीन महाद्वीपों में फैला साम्राज्य — रोम (NCERT कक्षा 11 विश्व इतिहास अध्याय 2)'
  where external_pdf_url = 'https://ncert.nic.in/textbook/pdf/kehs102.pdf';
update public.topics set
  title_en = 'Nomadic Empires — The Mongols (NCERT Class 11 World History Ch 3)',
  title_hi = 'खानाबदोश साम्राज्य — मंगोल (NCERT कक्षा 11 विश्व इतिहास अध्याय 3)'
  where external_pdf_url = 'https://ncert.nic.in/textbook/pdf/kehs103.pdf';
update public.topics set
  title_en = 'The Three Orders — European Feudalism (NCERT Class 11 World History Ch 4)',
  title_hi = 'तीन वर्ग — यूरोपीय सामंतवाद (NCERT कक्षा 11 विश्व इतिहास अध्याय 4)'
  where external_pdf_url = 'https://ncert.nic.in/textbook/pdf/kehs104.pdf';
update public.topics set
  title_en = 'Changing Cultural Traditions — The Renaissance (NCERT Class 11 World History Ch 5)',
  title_hi = 'बदलती सांस्कृतिक परंपराएँ — पुनर्जागरण (NCERT कक्षा 11 विश्व इतिहास अध्याय 5)'
  where external_pdf_url = 'https://ncert.nic.in/textbook/pdf/kehs105.pdf';
update public.topics set
  title_en = 'Displacing Indigenous Peoples — Americas & Australia (NCERT Class 11 World History Ch 6)',
  title_hi = 'मूल निवासियों का विस्थापन — अमेरिका एवं ऑस्ट्रेलिया (NCERT कक्षा 11 विश्व इतिहास अध्याय 6)'
  where external_pdf_url = 'https://ncert.nic.in/textbook/pdf/kehs106.pdf';
update public.topics set
  title_en = 'Paths to Modernisation — Japan & China (NCERT Class 11 World History Ch 7)',
  title_hi = 'आधुनिकीकरण के रास्ते — जापान एवं चीन (NCERT कक्षा 11 विश्व इतिहास अध्याय 7)'
  where external_pdf_url = 'https://ncert.nic.in/textbook/pdf/kehs107.pdf';

-- 2. Move all English Beehive topics to the Comprehension chapter.
--    Beehive is a literature textbook — the "stories" belong under reading comprehension,
--    not grammar or vocabulary. Grammar + Vocabulary chapters stay empty (empty-state UI
--    already handles that) and will be filled with Wikipedia grammar/vocab pages in Part B.
update public.topics
  set chapter_id = 'aeb2635a-97af-40af-be02-254fecfb12c8'
  where external_pdf_url like 'https://ncert.nic.in/textbook/pdf/iebe10%'
    and status = 'active';

-- 3. Mark Class 11/12 Physics + Biology topics stale.
--    They're technically correct physics/biology but far above NTPC scope (electrostatics,
--    thermodynamics, human reproduction at Class 12 level). Better to show an empty-state
--    subsection than bury the user in university-level material.
update public.topics set status = 'stale'
  where external_pdf_url like 'https://ncert.nic.in/textbook/pdf/keph2%'
     or external_pdf_url like 'https://ncert.nic.in/textbook/pdf/leph1%'
     or external_pdf_url like 'https://ncert.nic.in/textbook/pdf/lebo1%';

-- 4. Fix the off-by-one Class 10 Physics labels — jesc110 is Ch 10 (Human Eye), not Ch 9.
update public.topics set
  title_en = 'The Human Eye and the Colourful World (NCERT Class 10 Ch 10)',
  title_hi = 'मानव नेत्र तथा रंगीन संसार (NCERT कक्षा 10 अध्याय 10)'
  where external_pdf_url = 'https://ncert.nic.in/textbook/pdf/jesc110.pdf';
update public.topics set
  title_en = 'Electricity (NCERT Class 10 Ch 11)',
  title_hi = 'विद्युत (NCERT कक्षा 10 अध्याय 11)'
  where external_pdf_url = 'https://ncert.nic.in/textbook/pdf/jesc111.pdf';
update public.topics set
  title_en = 'Magnetic Effects of Electric Current (NCERT Class 10 Ch 12)',
  title_hi = 'विद्युत धारा के चुंबकीय प्रभाव (NCERT कक्षा 10 अध्याय 12)'
  where external_pdf_url = 'https://ncert.nic.in/textbook/pdf/jesc112.pdf';

-- 5. Add Class 10 Ch 9 "Light — Reflection and Refraction" (jesc109) which we skipped before.
insert into public.topics
  (chapter_id, title_en, title_hi, content_type, external_pdf_url, source, license,
   display_order, status, last_verified_at)
values
  ('29ceae82-b170-46fe-bf7d-e5e97710f184',
   'Light — Reflection and Refraction (NCERT Class 10 Ch 9)',
   'प्रकाश — परावर्तन और अपवर्तन (NCERT कक्षा 10 अध्याय 9)',
   'PDF_URL',
   'https://ncert.nic.in/textbook/pdf/jesc109.pdf',
   'NCERT', 'NCERT_LINKED', 61, 'active', now());

-- 6. Also add Class 10 Ch 13 "Our Environment" to Biology chapter (ecology) since we skipped 13.
insert into public.topics
  (chapter_id, title_en, title_hi, content_type, external_pdf_url, source, license,
   display_order, status, last_verified_at)
values
  ('0ef3d9a6-97ed-49f2-b385-0d4ed0aaf8a5',
   'Our Environment (NCERT Class 10 Ch 13)',
   'हमारा पर्यावरण (NCERT कक्षा 10 अध्याय 13)',
   'PDF_URL',
   'https://ncert.nic.in/textbook/pdf/jesc113.pdf',
   'NCERT', 'NCERT_LINKED', 77, 'active', now());

-- 7. Math Class 9 (iemh) titles reflect the old NCERT curriculum; current book is
--    Ganita Manjari with different chapter names. Use generic titles so we don't
--    promise specific content that's been renamed/restructured.
update public.topics set
  title_en = 'Class 9 Mathematics — Chapter 1 (NCERT)',
  title_hi = 'कक्षा 9 गणित — अध्याय 1 (NCERT)'
  where external_pdf_url = 'https://ncert.nic.in/textbook/pdf/iemh101.pdf';
update public.topics set
  title_en = 'Class 9 Mathematics — Chapter 2 (NCERT)',
  title_hi = 'कक्षा 9 गणित — अध्याय 2 (NCERT)'
  where external_pdf_url = 'https://ncert.nic.in/textbook/pdf/iemh102.pdf';
update public.topics set
  title_en = 'Class 9 Mathematics — Chapter 7 (NCERT)',
  title_hi = 'कक्षा 9 गणित — अध्याय 7 (NCERT)'
  where external_pdf_url = 'https://ncert.nic.in/textbook/pdf/iemh107.pdf';
update public.topics set
  title_en = 'Class 9 Mathematics — Chapter 8 (NCERT)',
  title_hi = 'कक्षा 9 गणित — अध्याय 8 (NCERT)'
  where external_pdf_url = 'https://ncert.nic.in/textbook/pdf/iemh108.pdf';