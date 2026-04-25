-- Recovered from Supabase MCP on 2026-04-25.
-- MCP version: 20260424061537
-- Original application order is determined by version timestamp, not filename.

-- Phase 3 — sample test seed (NTPC CBT-1 Mock 01).
-- 30 original hand-written questions, bilingual EN + HI (exam-register, code-switched).
-- Original content — source='Sample', license='ORIGINAL'.

do $$
declare
  v_test uuid;
  v_math uuid;
  v_reas uuid;
  v_ga   uuid;
  v_q    uuid;
begin
  insert into public.tests (slug, title_en, title_hi, kind, exam_target,
                            total_questions, total_minutes, negative_marking_fraction,
                            is_pro, status)
  values ('ntpc-cbt1-sample-01',
          'NTPC CBT-1 Mock 01 (Sample)',
          'एनटीपीसी CBT-1 मॉक 01 (नमूना)',
          'CBT1_FULL', 'NTPC_CBT1',
          30, 20, 0.3333, false, 'active')
  returning id into v_test;

  insert into public.test_sections (test_id, title_en, title_hi, question_count, display_order, subject_hint)
  values (v_test, 'Mathematics', 'गणित', 10, 1, 'math') returning id into v_math;
  insert into public.test_sections (test_id, title_en, title_hi, question_count, display_order, subject_hint)
  values (v_test, 'General Intelligence & Reasoning', 'सामान्य बुद्धि एवं तर्कशक्ति', 10, 2, 'reason') returning id into v_reas;
  insert into public.test_sections (test_id, title_en, title_hi, question_count, display_order, subject_hint)
  values (v_test, 'General Awareness', 'सामान्य जागरूकता', 10, 3, 'ga') returning id into v_ga;

  -- ============ MATH (10) ============

  insert into public.questions (section_id, display_order, stem_en, stem_hi, explanation_en, explanation_hi, difficulty, tags, source, license)
  values (v_math, 1, 'What is 45% of 240?', '240 का 45% क्या है?',
          '45/100 × 240 = 108.', '45/100 × 240 = 108.',
          'EASY', array['percentage','arithmetic'], 'Sample', 'ORIGINAL') returning id into v_q;
  insert into public.options (question_id, label, text_en, text_hi, is_correct) values
    (v_q,'A','108','108',true), (v_q,'B','96','96',false), (v_q,'C','112','112',false), (v_q,'D','120','120',false);

  insert into public.questions (section_id, display_order, stem_en, stem_hi, explanation_en, explanation_hi, difficulty, tags, source, license)
  values (v_math, 2, 'Find the simple interest on ₹5000 at 8% per annum for 3 years.',
          '₹5000 पर 8% वार्षिक दर से 3 वर्षों का साधारण ब्याज ज्ञात कीजिए।',
          'SI = P×R×T / 100 = 5000×8×3/100 = 1200.', 'SI = P×R×T / 100 = 5000×8×3/100 = 1200.',
          'EASY', array['simple_interest'], 'Sample', 'ORIGINAL') returning id into v_q;
  insert into public.options (question_id, label, text_en, text_hi, is_correct) values
    (v_q,'A','₹1200','₹1200',true), (v_q,'B','₹1000','₹1000',false), (v_q,'C','₹1500','₹1500',false), (v_q,'D','₹1300','₹1300',false);

  insert into public.questions (section_id, display_order, stem_en, stem_hi, explanation_en, explanation_hi, difficulty, tags, source, license)
  values (v_math, 3, 'The average of the first 10 natural numbers is:',
          'प्रथम 10 प्राकृतिक संख्याओं का औसत क्या है?',
          'Sum = 55; 55/10 = 5.5.', 'योग = 55; 55/10 = 5.5.',
          'EASY', array['average'], 'Sample', 'ORIGINAL') returning id into v_q;
  insert into public.options (question_id, label, text_en, text_hi, is_correct) values
    (v_q,'A','5.5','5.5',true), (v_q,'B','5','5',false), (v_q,'C','6','6',false), (v_q,'D','6.5','6.5',false);

  insert into public.questions (section_id, display_order, stem_en, stem_hi, explanation_en, explanation_hi, difficulty, tags, source, license)
  values (v_math, 4, 'Two numbers are in ratio 3:5 and their sum is 80. The smaller is:',
          'दो संख्याओं का अनुपात 3:5 है और उनका योग 80 है। छोटी संख्या क्या है?',
          '3x + 5x = 80 → x = 10 → smaller = 30.', '3x + 5x = 80 → x = 10 → छोटी = 30.',
          'EASY', array['ratio'], 'Sample', 'ORIGINAL') returning id into v_q;
  insert into public.options (question_id, label, text_en, text_hi, is_correct) values
    (v_q,'A','30','30',true), (v_q,'B','25','25',false), (v_q,'C','35','35',false), (v_q,'D','40','40',false);

  insert into public.questions (section_id, display_order, stem_en, stem_hi, explanation_en, explanation_hi, difficulty, tags, source, license)
  values (v_math, 5, 'A shopkeeper buys an item for ₹200 and sells it for ₹250. Profit % is:',
          'एक दुकानदार ने वस्तु ₹200 में खरीदी और ₹250 में बेची। लाभ प्रतिशत क्या है?',
          'Profit = 50; 50/200 × 100 = 25%.', 'लाभ = 50; 50/200 × 100 = 25%.',
          'EASY', array['profit_loss','percentage'], 'Sample', 'ORIGINAL') returning id into v_q;
  insert into public.options (question_id, label, text_en, text_hi, is_correct) values
    (v_q,'A','25%','25%',true), (v_q,'B','20%','20%',false), (v_q,'C','30%','30%',false), (v_q,'D','50%','50%',false);

  insert into public.questions (section_id, display_order, stem_en, stem_hi, explanation_en, explanation_hi, difficulty, tags, source, license)
  values (v_math, 6, 'A car covers 120 km in 3 hours. Its average speed (in km/h) is:',
          'एक कार 3 घंटे में 120 किमी की दूरी तय करती है। औसत गति (किमी/घंटा) क्या है?',
          '120 / 3 = 40 km/h.', '120 / 3 = 40 किमी/घंटा.',
          'EASY', array['speed_distance'], 'Sample', 'ORIGINAL') returning id into v_q;
  insert into public.options (question_id, label, text_en, text_hi, is_correct) values
    (v_q,'A','40','40',true), (v_q,'B','30','30',false), (v_q,'C','50','50',false), (v_q,'D','36','36',false);

  insert into public.questions (section_id, display_order, stem_en, stem_hi, explanation_en, explanation_hi, difficulty, tags, source, license)
  values (v_math, 7, 'The area of a rectangle of length 12 m and breadth 8 m is:',
          '12 मीटर लंबाई और 8 मीटर चौड़ाई वाले आयत का क्षेत्रफल क्या है?',
          '12 × 8 = 96 sq m.', '12 × 8 = 96 वर्ग मीटर.',
          'EASY', array['mensuration'], 'Sample', 'ORIGINAL') returning id into v_q;
  insert into public.options (question_id, label, text_en, text_hi, is_correct) values
    (v_q,'A','96 sq m','96 वर्ग मीटर',true), (v_q,'B','20 sq m','20 वर्ग मीटर',false), (v_q,'C','40 sq m','40 वर्ग मीटर',false), (v_q,'D','80 sq m','80 वर्ग मीटर',false);

  insert into public.questions (section_id, display_order, stem_en, stem_hi, explanation_en, explanation_hi, difficulty, tags, source, license)
  values (v_math, 8, 'Compound interest on ₹10,000 at 10% per annum for 2 years is:',
          '₹10,000 पर 10% वार्षिक दर से 2 वर्षों का चक्रवृद्धि ब्याज क्या है?',
          'CI = P[(1+R/100)^T − 1] = 10000 × 0.21 = 2100.',
          'CI = P[(1+R/100)^T − 1] = 10000 × 0.21 = 2100.',
          'MEDIUM', array['compound_interest'], 'Sample', 'ORIGINAL') returning id into v_q;
  insert into public.options (question_id, label, text_en, text_hi, is_correct) values
    (v_q,'A','₹2100','₹2100',true), (v_q,'B','₹2000','₹2000',false), (v_q,'C','₹2200','₹2200',false), (v_q,'D','₹1900','₹1900',false);

  insert into public.questions (section_id, display_order, stem_en, stem_hi, explanation_en, explanation_hi, difficulty, tags, source, license)
  values (v_math, 9, 'Pipe A fills a tank in 6 hours and pipe B in 4 hours. Together they fill it in:',
          'पाइप A एक टंकी को 6 घंटे में और पाइप B 4 घंटे में भरता है। दोनों एक साथ कितने घंटे में भरेंगे?',
          '1/6 + 1/4 = 5/12 → 12/5 = 2.4 hours.', '1/6 + 1/4 = 5/12 → 12/5 = 2.4 घंटे.',
          'MEDIUM', array['time_work'], 'Sample', 'ORIGINAL') returning id into v_q;
  insert into public.options (question_id, label, text_en, text_hi, is_correct) values
    (v_q,'A','2.4 hours','2.4 घंटे',true), (v_q,'B','2 hours','2 घंटे',false), (v_q,'C','3 hours','3 घंटे',false), (v_q,'D','2.5 hours','2.5 घंटे',false);

  insert into public.questions (section_id, display_order, stem_en, stem_hi, explanation_en, explanation_hi, difficulty, tags, source, license)
  values (v_math, 10, 'The value of 2^5 × 2^3 ÷ 2^6 is:',
          '2^5 × 2^3 ÷ 2^6 का मान क्या है?',
          '2^(5+3−6) = 2^2 = 4.', '2^(5+3−6) = 2^2 = 4.',
          'EASY', array['exponents'], 'Sample', 'ORIGINAL') returning id into v_q;
  insert into public.options (question_id, label, text_en, text_hi, is_correct) values
    (v_q,'A','4','4',true), (v_q,'B','8','8',false), (v_q,'C','2','2',false), (v_q,'D','16','16',false);

  -- ============ REASONING (10) ============

  insert into public.questions (section_id, display_order, stem_en, stem_hi, explanation_en, explanation_hi, difficulty, tags, source, license)
  values (v_reas, 1, 'Find the next term: 2, 6, 12, 20, 30, ?',
          'अगला पद ज्ञात कीजिए: 2, 6, 12, 20, 30, ?',
          'Differences: 4, 6, 8, 10, 12 → 30 + 12 = 42.',
          'अंतर: 4, 6, 8, 10, 12 → 30 + 12 = 42.',
          'EASY', array['number_series'], 'Sample', 'ORIGINAL') returning id into v_q;
  insert into public.options (question_id, label, text_en, text_hi, is_correct) values
    (v_q,'A','42','42',true), (v_q,'B','40','40',false), (v_q,'C','44','44',false), (v_q,'D','36','36',false);

  insert into public.questions (section_id, display_order, stem_en, stem_hi, explanation_en, explanation_hi, difficulty, tags, source, license)
  values (v_reas, 2, 'If ABC is coded as CBA, how is DEF coded?',
          'यदि ABC को CBA लिखा जाता है, तो DEF को कैसे लिखा जाएगा?',
          'Reverse the letters: DEF → FED.', 'अक्षर उलट दीजिए: DEF → FED.',
          'EASY', array['coding_decoding'], 'Sample', 'ORIGINAL') returning id into v_q;
  insert into public.options (question_id, label, text_en, text_hi, is_correct) values
    (v_q,'A','FED','FED',true), (v_q,'B','EFD','EFD',false), (v_q,'C','FDE','FDE',false), (v_q,'D','DFE','DFE',false);

  insert into public.questions (section_id, display_order, stem_en, stem_hi, explanation_en, explanation_hi, difficulty, tags, source, license)
  values (v_reas, 3, 'A is B''s brother and C is A''s mother. How is C related to B?',
          'A, B का भाई है और C, A की माँ है। C, B की क्या है?',
          'Same mother — C is B''s mother too.', 'समान माँ — C, B की भी माँ है.',
          'EASY', array['blood_relations'], 'Sample', 'ORIGINAL') returning id into v_q;
  insert into public.options (question_id, label, text_en, text_hi, is_correct) values
    (v_q,'A','Mother','माँ',true), (v_q,'B','Aunt','मौसी',false), (v_q,'C','Sister','बहन',false), (v_q,'D','Grandmother','दादी',false);

  insert into public.questions (section_id, display_order, stem_en, stem_hi, explanation_en, explanation_hi, difficulty, tags, source, license)
  values (v_reas, 4, 'Starting facing North, turn right, then right again. You now face:',
          'उत्तर की ओर मुख करके दाएं मुड़ें, फिर दाएं मुड़ें। अब आपका मुख किस दिशा में है?',
          'N → E → S.', 'उत्तर → पूर्व → दक्षिण.',
          'EASY', array['direction_sense'], 'Sample', 'ORIGINAL') returning id into v_q;
  insert into public.options (question_id, label, text_en, text_hi, is_correct) values
    (v_q,'A','South','दक्षिण',true), (v_q,'B','East','पूर्व',false), (v_q,'C','West','पश्चिम',false), (v_q,'D','North','उत्तर',false);

  insert into public.questions (section_id, display_order, stem_en, stem_hi, explanation_en, explanation_hi, difficulty, tags, source, license)
  values (v_reas, 5, 'Find the odd one out: 9, 16, 25, 36, 44',
          'विषम पद ज्ञात कीजिए: 9, 16, 25, 36, 44',
          'All others are perfect squares.', 'अन्य सभी पूर्ण वर्ग हैं.',
          'EASY', array['odd_one_out'], 'Sample', 'ORIGINAL') returning id into v_q;
  insert into public.options (question_id, label, text_en, text_hi, is_correct) values
    (v_q,'A','44','44',true), (v_q,'B','36','36',false), (v_q,'C','25','25',false), (v_q,'D','9','9',false);

  insert into public.questions (section_id, display_order, stem_en, stem_hi, explanation_en, explanation_hi, difficulty, tags, source, license)
  values (v_reas, 6, 'All dogs are animals. No animals are plants. Conclusion:',
          'सभी कुत्ते जानवर हैं। कोई भी जानवर पौधा नहीं है। निष्कर्ष:',
          'Universal negative chains: No dogs are plants.',
          'सार्वभौमिक नकारात्मक श्रृंखला: कोई कुत्ता पौधा नहीं है.',
          'MEDIUM', array['syllogism'], 'Sample', 'ORIGINAL') returning id into v_q;
  insert into public.options (question_id, label, text_en, text_hi, is_correct) values
    (v_q,'A','No dogs are plants','कोई कुत्ता पौधा नहीं है',true),
    (v_q,'B','Some dogs are plants','कुछ कुत्ते पौधे हैं',false),
    (v_q,'C','All plants are dogs','सभी पौधे कुत्ते हैं',false),
    (v_q,'D','None of the above','उपरोक्त में से कोई नहीं',false);

  insert into public.questions (section_id, display_order, stem_en, stem_hi, explanation_en, explanation_hi, difficulty, tags, source, license)
  values (v_reas, 7, 'If DELHI is coded EFMIJ, MUMBAI is coded as:',
          'यदि DELHI को EFMIJ लिखा जाता है, तो MUMBAI को कैसे लिखा जाएगा?',
          'Each letter +1. M→N, U→V, M→N, B→C, A→B, I→J.',
          'प्रत्येक अक्षर +1. M→N, U→V, M→N, B→C, A→B, I→J.',
          'MEDIUM', array['coding_decoding'], 'Sample', 'ORIGINAL') returning id into v_q;
  insert into public.options (question_id, label, text_en, text_hi, is_correct) values
    (v_q,'A','NVNCBJ','NVNCBJ',true), (v_q,'B','NVNCCJ','NVNCCJ',false), (v_q,'C','NUNCBJ','NUNCBJ',false), (v_q,'D','OVNCBJ','OVNCBJ',false);

  insert into public.questions (section_id, display_order, stem_en, stem_hi, explanation_en, explanation_hi, difficulty, tags, source, license)
  values (v_reas, 8, 'At 3:15, the angle between the hour and minute hands is:',
          '3:15 बजे घंटे और मिनट की सुइयों के बीच का कोण:',
          'Hour hand at 97.5°, minute hand at 90°; diff = 7.5°.',
          'घंटे की सुई 97.5° पर, मिनट की सुई 90° पर; अंतर = 7.5°.',
          'MEDIUM', array['clocks'], 'Sample', 'ORIGINAL') returning id into v_q;
  insert into public.options (question_id, label, text_en, text_hi, is_correct) values
    (v_q,'A','7.5°','7.5°',true), (v_q,'B','0°','0°',false), (v_q,'C','15°','15°',false), (v_q,'D','22.5°','22.5°',false);

  insert into public.questions (section_id, display_order, stem_en, stem_hi, explanation_en, explanation_hi, difficulty, tags, source, license)
  values (v_reas, 9, 'If Jan 1, 2020 was a Wednesday, Jan 1, 2021 was a:',
          'यदि 1 जनवरी 2020 बुधवार था, तो 1 जनवरी 2021 कौन सा दिन था?',
          '2020 is a leap year = 366 days = 2 odd days. Wed + 2 = Fri.',
          '2020 लीप वर्ष = 366 दिन = 2 विषम दिन। बुध + 2 = शुक्र.',
          'MEDIUM', array['calendars'], 'Sample', 'ORIGINAL') returning id into v_q;
  insert into public.options (question_id, label, text_en, text_hi, is_correct) values
    (v_q,'A','Friday','शुक्रवार',true), (v_q,'B','Thursday','गुरुवार',false), (v_q,'C','Saturday','शनिवार',false), (v_q,'D','Wednesday','बुधवार',false);

  insert into public.questions (section_id, display_order, stem_en, stem_hi, explanation_en, explanation_hi, difficulty, tags, source, license)
  values (v_reas, 10, 'Find the missing term: AZ, BY, CX, ?',
          'लुप्त पद ज्ञात कीजिए: AZ, BY, CX, ?',
          'First letter +1 (A,B,C,D), second letter −1 (Z,Y,X,W) → DW.',
          'प्रथम अक्षर +1 (A,B,C,D), द्वितीय अक्षर −1 (Z,Y,X,W) → DW.',
          'EASY', array['letter_series'], 'Sample', 'ORIGINAL') returning id into v_q;
  insert into public.options (question_id, label, text_en, text_hi, is_correct) values
    (v_q,'A','DW','DW',true), (v_q,'B','DV','DV',false), (v_q,'C','EW','EW',false), (v_q,'D','DU','DU',false);

  -- ============ GENERAL AWARENESS (10) ============

  insert into public.questions (section_id, display_order, stem_en, stem_hi, explanation_en, explanation_hi, difficulty, tags, source, license)
  values (v_ga, 1, 'The capital of India is:',
          'भारत की राजधानी क्या है?',
          'New Delhi has been the capital since 1911.',
          '1911 से नई दिल्ली भारत की राजधानी है.',
          'EASY', array['polity','geography'], 'Sample', 'ORIGINAL') returning id into v_q;
  insert into public.options (question_id, label, text_en, text_hi, is_correct) values
    (v_q,'A','New Delhi','नई दिल्ली',true), (v_q,'B','Mumbai','मुंबई',false), (v_q,'C','Kolkata','कोलकाता',false), (v_q,'D','Chennai','चेन्नई',false);

  insert into public.questions (section_id, display_order, stem_en, stem_hi, explanation_en, explanation_hi, difficulty, tags, source, license)
  values (v_ga, 2, 'India attained independence in the year:',
          'भारत किस वर्ष स्वतंत्र हुआ?',
          '15 August 1947.', '15 अगस्त 1947.',
          'EASY', array['history'], 'Sample', 'ORIGINAL') returning id into v_q;
  insert into public.options (question_id, label, text_en, text_hi, is_correct) values
    (v_q,'A','1947','1947',true), (v_q,'B','1945','1945',false), (v_q,'C','1950','1950',false), (v_q,'D','1942','1942',false);

  insert into public.questions (section_id, display_order, stem_en, stem_hi, explanation_en, explanation_hi, difficulty, tags, source, license)
  values (v_ga, 3, 'The author of the Indian National Anthem is:',
          'भारत के राष्ट्रगान के रचयिता हैं:',
          'Jana Gana Mana was written by Rabindranath Tagore.',
          'जन गण मन रवींद्रनाथ टैगोर द्वारा रचित है.',
          'EASY', array['culture','history'], 'Sample', 'ORIGINAL') returning id into v_q;
  insert into public.options (question_id, label, text_en, text_hi, is_correct) values
    (v_q,'A','Rabindranath Tagore','रवींद्रनाथ टैगोर',true),
    (v_q,'B','Bankim Chandra Chatterjee','बंकिम चंद्र चटर्जी',false),
    (v_q,'C','Sarojini Naidu','सरोजिनी नायडू',false),
    (v_q,'D','Subramania Bharati','सुब्रमण्यम भारती',false);

  insert into public.questions (section_id, display_order, stem_en, stem_hi, explanation_en, explanation_hi, difficulty, tags, source, license)
  values (v_ga, 4, 'The first Indian to travel to space was:',
          'अंतरिक्ष में जाने वाले प्रथम भारतीय थे:',
          'Rakesh Sharma, aboard Soyuz T-11 in 1984.',
          'राकेश शर्मा, 1984 में Soyuz T-11 से.',
          'EASY', array['science','current_affairs'], 'Sample', 'ORIGINAL') returning id into v_q;
  insert into public.options (question_id, label, text_en, text_hi, is_correct) values
    (v_q,'A','Rakesh Sharma','राकेश शर्मा',true),
    (v_q,'B','Kalpana Chawla','कल्पना चावला',false),
    (v_q,'C','Sunita Williams','सुनीता विलियम्स',false),
    (v_q,'D','Vikram Sarabhai','विक्रम साराभाई',false);

  insert into public.questions (section_id, display_order, stem_en, stem_hi, explanation_en, explanation_hi, difficulty, tags, source, license)
  values (v_ga, 5, 'The Taj Mahal is located in:',
          'ताज महल किस शहर में स्थित है?',
          'In Agra, Uttar Pradesh.', 'आगरा, उत्तर प्रदेश में.',
          'EASY', array['geography','culture'], 'Sample', 'ORIGINAL') returning id into v_q;
  insert into public.options (question_id, label, text_en, text_hi, is_correct) values
    (v_q,'A','Agra','आगरा',true), (v_q,'B','Delhi','दिल्ली',false), (v_q,'C','Jaipur','जयपुर',false), (v_q,'D','Lucknow','लखनऊ',false);

  insert into public.questions (section_id, display_order, stem_en, stem_hi, explanation_en, explanation_hi, difficulty, tags, source, license)
  values (v_ga, 6, 'The headquarters of Western Railway is located in:',
          'पश्चिम रेलवे का मुख्यालय कहाँ स्थित है?',
          'At Churchgate, Mumbai.', 'चर्चगेट, मुंबई में.',
          'MEDIUM', array['railways'], 'Sample', 'ORIGINAL') returning id into v_q;
  insert into public.options (question_id, label, text_en, text_hi, is_correct) values
    (v_q,'A','Mumbai','मुंबई',true), (v_q,'B','Delhi','दिल्ली',false), (v_q,'C','Kolkata','कोलकाता',false), (v_q,'D','Chennai','चेन्नई',false);

  insert into public.questions (section_id, display_order, stem_en, stem_hi, explanation_en, explanation_hi, difficulty, tags, source, license)
  values (v_ga, 7, 'In Bangladesh, the river Ganga is known as:',
          'बांग्लादेश में गंगा नदी किस नाम से जानी जाती है?',
          'Its main distributary there is Padma.',
          'वहाँ की मुख्य वितरिका पद्मा है.',
          'MEDIUM', array['geography'], 'Sample', 'ORIGINAL') returning id into v_q;
  insert into public.options (question_id, label, text_en, text_hi, is_correct) values
    (v_q,'A','Padma','पद्मा',true), (v_q,'B','Yamuna','यमुना',false), (v_q,'C','Brahmaputra','ब्रह्मपुत्र',false), (v_q,'D','Meghna','मेघना',false);

  insert into public.questions (section_id, display_order, stem_en, stem_hi, explanation_en, explanation_hi, difficulty, tags, source, license)
  values (v_ga, 8, 'India hosted the G20 Summit in the year:',
          'भारत ने किस वर्ष G20 शिखर सम्मेलन की मेज़बानी की?',
          'Held in New Delhi in September 2023.',
          'सितंबर 2023 में नई दिल्ली में आयोजित हुआ.',
          'EASY', array['current_affairs'], 'Sample', 'ORIGINAL') returning id into v_q;
  insert into public.options (question_id, label, text_en, text_hi, is_correct) values
    (v_q,'A','2023','2023',true), (v_q,'B','2022','2022',false), (v_q,'C','2024','2024',false), (v_q,'D','2021','2021',false);

  insert into public.questions (section_id, display_order, stem_en, stem_hi, explanation_en, explanation_hi, difficulty, tags, source, license)
  values (v_ga, 9, 'The currency of Japan is:',
          'जापान की मुद्रा क्या है?',
          'Japanese Yen (¥).', 'जापानी येन (¥).',
          'EASY', array['economy','world'], 'Sample', 'ORIGINAL') returning id into v_q;
  insert into public.options (question_id, label, text_en, text_hi, is_correct) values
    (v_q,'A','Yen','येन',true), (v_q,'B','Won','वॉन',false), (v_q,'C','Dollar','डॉलर',false), (v_q,'D','Rupee','रुपया',false);

  insert into public.questions (section_id, display_order, stem_en, stem_hi, explanation_en, explanation_hi, difficulty, tags, source, license)
  values (v_ga, 10, 'The Great Barrier Reef lies off the coast of:',
          'ग्रेट बैरियर रीफ किस देश के तट पर स्थित है?',
          'Off the Queensland coast, Australia.',
          'ऑस्ट्रेलिया के क्वींसलैंड तट के पास.',
          'MEDIUM', array['world_geography'], 'Sample', 'ORIGINAL') returning id into v_q;
  insert into public.options (question_id, label, text_en, text_hi, is_correct) values
    (v_q,'A','Australia','ऑस्ट्रेलिया',true), (v_q,'B','India','भारत',false), (v_q,'C','South Africa','दक्षिण अफ्रीका',false), (v_q,'D','Brazil','ब्राज़ील',false);

end $$;
