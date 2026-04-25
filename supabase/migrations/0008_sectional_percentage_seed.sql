-- Recovered from Supabase MCP on 2026-04-25.
-- MCP version: 20260425010515
-- Original application order is determined by version timestamp, not filename.

do $$
declare
  v_test_id    uuid;
  v_section_id uuid;
  v_q_id       uuid;
begin
  insert into public.tests (slug, title_en, title_hi, kind, exam_target, total_questions, total_minutes, negative_marking_fraction, status)
  values ('sectional-percentage-01', 'Percentage — Sectional', 'प्रतिशत — Sectional', 'SECTIONAL', 'NTPC_CBT1', 15, 25, 0.3333, 'active')
  on conflict (slug) do update set title_en = excluded.title_en, title_hi = excluded.title_hi, total_questions = excluded.total_questions, total_minutes = excluded.total_minutes, updated_at = now()
  returning id into v_test_id;

  delete from public.test_sections where test_id = v_test_id;

  insert into public.test_sections (test_id, title_en, title_hi, question_count, display_order, subject_hint)
  values (v_test_id, 'Percentage', 'प्रतिशत', 15, 0, 'math')
  returning id into v_section_id;

  -- Q1
  insert into public.questions (section_id, display_order, stem_en, stem_hi, explanation_method_en, explanation_concept_en, explanation_method_hi, explanation_concept_hi, difficulty, tags, source, license, status)
  values (v_section_id, 0, 'What is 35% of 240?', '240 का 35% क्या है?',
    '35% = 35/100 = 0.35. 0.35 × 240 = 84. Quicker: 10% of 240 = 24, so 35% = 24 × 3.5 = 84.',
    '''Percent'' means ''per 100''. p% of N = (p × N)/100. The fraction p/100 is the multiplier; multiplying any number N by it scales N down proportionally. 10% chunks are easy mental anchors — split the rate into 10s and remainders.',
    '35% = 35/100 = 0.35। 0.35 × 240 = 84। तेज़ तरीका: 10% of 240 = 24, अतः 35% = 24 × 3.5 = 84।',
    '''percent'' का मतलब ''per 100'' है। p% of N = (p × N)/100। यह fraction p/100 multiplier है जो किसी भी संख्या N को proportional रूप से scale करता है। 10% के chunks सबसे आसान mental anchor हैं — rate को 10 और शेष में बाँटें।',
    'EASY', array['percentage','basic-percentage']::text[], 'Original', 'ORIGINAL', 'active')
  returning id into v_q_id;
  insert into public.options (question_id, label, text_en, text_hi, is_correct, trap_reason_en, trap_reason_hi) values
    (v_q_id, 'A', '84', '84', true, null, null),
    (v_q_id, 'B', '82', '82', false, 'Computed 35×240/100 but rounded to nearest 10 too aggressively.', '35×240/100 निकाला पर 10 के पास round कर दिया।'),
    (v_q_id, 'C', '96', '96', false, 'Used 40% (rounded up the rate).', 'rate को 40% तक round कर दिया।'),
    (v_q_id, 'D', '72', '72', false, 'Used 30% (rounded down the rate).', 'rate को 30% तक round कर दिया।');

  -- Q2
  insert into public.questions (section_id, display_order, stem_en, stem_hi, explanation_method_en, explanation_concept_en, explanation_method_hi, explanation_concept_hi, difficulty, tags, source, license, status)
  values (v_section_id, 1, 'If a number is increased by 20% and then decreased by 20%, what is the net change?', 'किसी संख्या को 20% बढ़ाकर फिर 20% घटाया जाए, तो net change क्या है?',
    'Use the formula: net% = a + b + (a×b)/100, where a = +20, b = -20. Net = 20 - 20 + (20)(-20)/100 = 0 - 4 = -4%. So 4% decrease.',
    'Take 100 as the original. After +20%: 120. After -20% on 120: 120 × 0.80 = 96. Final = 96, drop = 4 on 100 = 4%. The two adjustments do not cancel because the base of the second adjustment (120) is larger than the base of the first (100); the absolute decrease 24 exceeds the absolute increase 20.',
    'formula: net% = a + b + (a×b)/100, जहाँ a = +20, b = -20. Net = 20 - 20 + (20)(-20)/100 = 0 - 4 = -4%। यानी 4% decrease।',
    '100 को मूल मानें। +20% के बाद: 120। 120 पर -20% = 120 × 0.80 = 96। Final = 96, गिरावट = 100 में से 4 = 4%। दोनों adjustments cancel नहीं होते क्योंकि दूसरे adjustment का base (120) पहले के base (100) से बड़ा है; absolute कमी 24 absolute बढ़त 20 से ज़्यादा है।',
    'EASY', array['percentage','successive-percentage']::text[], 'Original', 'ORIGINAL', 'active')
  returning id into v_q_id;
  insert into public.options (question_id, label, text_en, text_hi, is_correct, trap_reason_en, trap_reason_hi) values
    (v_q_id, 'A', '0% (no change)', '0% (कोई बदलाव नहीं)', false, 'Common error — students subtract 20-20 = 0, but the bases differ. The 20% decrease is on the increased number (120%), not on the original.', 'common गलती — 20-20 = 0 कर दिया, लेकिन base अलग हैं।'),
    (v_q_id, 'B', '4% decrease', '4% की कमी', true, null, null),
    (v_q_id, 'C', '4% increase', '4% की बढ़त', false, 'Right magnitude, wrong direction — successive +x% and -x% always result in a net decrease.', 'magnitude सही, दिशा गलत — successive +x% और -x% का net हमेशा कमी।'),
    (v_q_id, 'D', '40% decrease', '40% की कमी', false, 'Incorrectly added the percentages. Successive percentages compound, they do not add.', 'percentages को जोड़ दिया। successive percentages compound होते हैं।');

  -- Q3
  insert into public.questions (section_id, display_order, stem_en, stem_hi, explanation_method_en, explanation_concept_en, explanation_method_hi, explanation_concept_hi, difficulty, tags, source, license, status)
  values (v_section_id, 2, 'A number when increased by 25% becomes 80. What was the original number?', 'एक संख्या को 25% बढ़ाने पर 80 हो जाती है। मूल संख्या क्या थी?',
    'If +25% gives 80, then 80 represents 125% of original. Original = 80 × 100/125 = 80 × 0.8 = 64.',
    'Reverse-percentage problems are about identifying which side of the comparison is the whole (100%). Here ''after a 25% increase'' means the result is 125% of the start. To recover the start, divide the result by the multiplier (1.25), not by adding/subtracting 25 to the result.',
    '+25% पर 80 मिलता है, यानी 80 मूल के 125% के बराबर है। मूल = 80 × 100/125 = 80 × 0.8 = 64।',
    'Reverse-percentage problems में पहचानना है कि comparison में whole (100%) कौन है। यहाँ ''25% बढ़ाने पर'' का मतलब result मूल का 125% है। मूल को निकालने के लिए result को multiplier (1.25) से भाग दें।',
    'EASY', array['percentage','reverse-percentage']::text[], 'Original', 'ORIGINAL', 'active')
  returning id into v_q_id;
  insert into public.options (question_id, label, text_en, text_hi, is_correct, trap_reason_en, trap_reason_hi) values
    (v_q_id, 'A', '60', '60', false, 'Subtracted 25 from 80 = 55, then approximated up. Wrong because 25% of original is not 25 absolute.', '80 से 25 घटाकर 55 निकाला और round किया। गलत क्योंकि मूल का 25% absolute 25 नहीं है।'),
    (v_q_id, 'B', '55', '55', false, 'Subtracted 25 directly from 80 — confusing 25% of original with 25 units.', '80 में से 25 घटाया — मूल का 25% को 25 units समझा।'),
    (v_q_id, 'C', '64', '64', true, null, null),
    (v_q_id, 'D', '100', '100', false, 'Subtracted 25% from 80: 80 - 20 = 60, then over-corrected. The relationship is 80 = 1.25 × x, so x = 64.', '80 से 20 घटाया, फिर over-correct कर दिया। सही: 80 = 1.25 × x, अतः x = 64।');

  -- Q4
  insert into public.questions (section_id, display_order, stem_en, stem_hi, explanation_method_en, explanation_concept_en, explanation_method_hi, explanation_concept_hi, difficulty, tags, source, license, status)
  values (v_section_id, 3, '60% of a number is 144. What is 75% of the same number?', 'किसी संख्या का 60% = 144 है। उसी संख्या का 75% क्या है?',
    'Ratio shortcut: 75% / 60% = 75/60 = 5/4. So 75% = 144 × 5/4 = 180. No need to find the number first.',
    'Both 60% and 75% are different fractions of the same whole. Their ratio (75/60 = 5/4) tells you directly how the second value relates to the first. This cross-percent shortcut saves a step over finding the original number (240) and then 75% of it (180).',
    'Ratio shortcut: 75% / 60% = 75/60 = 5/4। तो 75% = 144 × 5/4 = 180। मूल संख्या निकालने की ज़रूरत नहीं।',
    '60% और 75% दोनों एक ही whole के अलग-अलग fractions हैं। उनका ratio (75/60 = 5/4) सीधे बताता है कि दूसरा value पहले से कितना है।',
    'EASY', array['percentage','ratio-percentage']::text[], 'Original', 'ORIGINAL', 'active')
  returning id into v_q_id;
  insert into public.options (question_id, label, text_en, text_hi, is_correct, trap_reason_en, trap_reason_hi) values
    (v_q_id, 'A', '180', '180', true, null, null),
    (v_q_id, 'B', '240', '240', false, 'This is the original number (60% × 240 = 144), not 75% of it.', 'यह मूल संख्या है (60% × 240 = 144), उसका 75% नहीं।'),
    (v_q_id, 'C', '168', '168', false, 'Added 24 to 144 — used absolute additive logic instead of ratio.', '144 में 24 जोड़ा — ratio की जगह additive logic लगाया।'),
    (v_q_id, 'D', '192', '192', false, 'Added 48 = 144 × 1/3, mistaking 75/60 for 4/3 instead of 5/4.', '144 × 1/3 = 48 जोड़ा, 75/60 को 4/3 समझ लिया, सही 5/4 है।');

  -- Q5
  insert into public.questions (section_id, display_order, stem_en, stem_hi, explanation_method_en, explanation_concept_en, explanation_method_hi, explanation_concept_hi, difficulty, tags, source, license, status)
  values (v_section_id, 4, 'Ramesh spends 75% of his salary. If his salary is increased by 20% and his expenses by 10%, by what percent do his savings change?', 'रमेश अपनी salary का 75% खर्च करता है। यदि उसकी salary 20% बढ़े और expenses 10% बढ़ें, तो savings कितने प्रतिशत बदलेंगी?',
    'Take salary = 100, so expenses = 75 and savings = 25. New salary = 120, new expenses = 75 × 1.10 = 82.5. New savings = 120 - 82.5 = 37.5. Change = (37.5 - 25)/25 = 50%.',
    'Savings are the residual: salary minus expenses. When two different bases (here 100 and 75) grow at different rates, the residual percent change depends on the absolute changes, not the percent changes. Always assume a clean base, compute absolute amounts, and only then convert the change in the residual back to a percent.',
    'salary = 100 मानें, expenses = 75, savings = 25। नई salary = 120, नई expenses = 75 × 1.10 = 82.5। नई savings = 120 - 82.5 = 37.5। Change = (37.5 - 25)/25 = 50%।',
    'Savings residual हैं: salary - expenses। जब दो अलग bases अलग rates पर बढ़ते हैं, तो residual का percent change absolute changes पर निर्भर करता है। हमेशा clean base मान लें, absolute amounts निकालें।',
    'MEDIUM', array['percentage','savings-expenses','salary-expenses']::text[], 'Original', 'ORIGINAL', 'active')
  returning id into v_q_id;
  insert into public.options (question_id, label, text_en, text_hi, is_correct, trap_reason_en, trap_reason_hi) values
    (v_q_id, 'A', 'Increase by 50%', '50% की बढ़त', true, null, null),
    (v_q_id, 'B', 'Increase by 10%', '10% की बढ़त', false, 'Subtracted 10 from 20 — but salary and expenses are different bases, you cannot compare percentages directly.', '20 से 10 घटाया — पर bases अलग हैं।'),
    (v_q_id, 'C', 'Increase by 30%', '30% की बढ़त', false, 'Added 20 + 10 = 30 — wrong direction, expenses cut savings, they do not add.', '20 + 10 = 30 जोड़ा — दिशा गलत।'),
    (v_q_id, 'D', 'Increase by 25%', '25% की बढ़त', false, 'Approximation — actual answer requires computing absolute change in savings = 20 - 7.5 = 12.5 on a savings base of 25.', 'अंदाज़ से — असली में Δsavings = 20-7.5 = 12.5 (savings base 25 पर) निकालना पड़ता है।');

  -- Q6
  insert into public.questions (section_id, display_order, stem_en, stem_hi, explanation_method_en, explanation_concept_en, explanation_method_hi, explanation_concept_hi, difficulty, tags, source, license, status)
  values (v_section_id, 5, 'If the price of sugar increases by 25%, by what percentage should a family reduce its consumption to keep the expenditure on sugar unchanged?', 'यदि चीनी की कीमत 25% बढ़ जाए, तो परिवार को consumption कितने प्रतिशत घटानी चाहिए कि sugar पर expenditure same रहे?',
    'Formula: required reduction% = (rise%)/(100 + rise%) × 100 = 25/125 × 100 = 20%.',
    'Expenditure = price × consumption. To keep it constant when price multiplies by 1.25, consumption must multiply by 1/1.25 = 0.80, i.e., reduce by 20%. The asymmetry between 25% rise and 20% cut comes from the base inversion: rising 25% on the smaller base, cutting 20% on the larger base.',
    'formula: needed cut% = (rise%)/(100 + rise%) × 100 = 25/125 × 100 = 20%।',
    'Expenditure = price × consumption. इसे constant रखने के लिए जब price 1.25 गुना हो, तो consumption को 1/1.25 = 0.80 गुना करना होगा, यानी 20% कम।',
    'MEDIUM', array['percentage','price-consumption','reverse-percentage']::text[], 'Original', 'ORIGINAL', 'active')
  returning id into v_q_id;
  insert into public.options (question_id, label, text_en, text_hi, is_correct, trap_reason_en, trap_reason_hi) values
    (v_q_id, 'A', '25%', '25%', false, 'Symmetric instinct — but cutting consumption by 25% only offsets a small price rise, not 25%.', 'symmetric instinct से — पर 25% consumption cut छोटी price rise को offset करता है।'),
    (v_q_id, 'B', '20%', '20%', true, null, null),
    (v_q_id, 'C', '15%', '15%', false, 'Underestimated — 15% consumption cut leaves expenditure higher than original.', 'अंदाज़ कम — 15% consumption cut से expenditure मूल से ज़्यादा रह जाता है।'),
    (v_q_id, 'D', '30%', '30%', false, 'Overcompensated — 30% consumption cut would leave expenditure lower than original.', 'Overcompensate — 30% consumption cut से expenditure मूल से कम हो जाता है।');

  -- Q7
  insert into public.questions (section_id, display_order, stem_en, stem_hi, explanation_method_en, explanation_concept_en, explanation_method_hi, explanation_concept_hi, difficulty, tags, source, license, status)
  values (v_section_id, 6, 'In an election between two candidates, the loser polled 45% of total valid votes and lost by 4000 votes. How many total valid votes were cast?', 'दो उम्मीदवारों के बीच चुनाव में हारने वाले को 45% valid votes मिले और वह 4000 votes से हारा। कुल valid votes कितने थे?',
    'Loser got 45%, winner got 55%. Margin = 55% - 45% = 10% of total = 4000 votes. So total = 4000/0.10 = 40000.',
    'When two parties split 100% and one has p%, the other has (100-p)%. The margin is the absolute difference (100-2p)%. Convert any margin in absolute units (here 4000 votes) to total by dividing by the margin fractional value.',
    'हारने वाले को 45%, जीतने वाले को 55%। margin = 55% - 45% = total का 10% = 4000 votes। तो total = 4000/0.10 = 40000।',
    'जब दो parties 100% बाँटें और एक को p% मिले, तो दूसरे को (100-p)%। margin absolute difference (100-2p)% होता है।',
    'MEDIUM', array['percentage','elections','percent-difference']::text[], 'Original', 'ORIGINAL', 'active')
  returning id into v_q_id;
  insert into public.options (question_id, label, text_en, text_hi, is_correct, trap_reason_en, trap_reason_hi) values
    (v_q_id, 'A', '20000', '20000', false, 'Used (4000/20%) but with wrong margin — 4000 = 10% of total, not 20%.', '(4000/20%) लगाया पर margin गलत।'),
    (v_q_id, 'B', '40000', '40000', true, null, null),
    (v_q_id, 'C', '8000', '8000', false, 'Used 4000 ÷ 0.5 — confused half-margin shortcut with the percent gap.', '4000 ÷ 0.5 लगाया।'),
    (v_q_id, 'D', '44000', '44000', false, 'Computed 4000/9% (treating 9% wrongly as the margin). Margin is 10%.', '4000/9% लगाया, सही margin 10% है।');

  -- Q8
  insert into public.questions (section_id, display_order, stem_en, stem_hi, explanation_method_en, explanation_concept_en, explanation_method_hi, explanation_concept_hi, difficulty, tags, source, license, status)
  values (v_section_id, 7, 'The population of a town increased from 20000 to 23000. Find the percentage increase.', 'एक कस्बे की जनसंख्या 20000 से 23000 हो गई। percentage increase निकालिए।',
    'Increase = 23000 - 20000 = 3000. % increase = 3000/20000 × 100 = 15%.',
    'Percentage change is always relative to the OLD value (the base). Formula: %change = (new - old)/old × 100. Dividing by the new value (23000) gives the wrong answer because that is not the base of the increase — the increase happened from 20000.',
    'increase = 23000 - 20000 = 3000। % increase = 3000/20000 × 100 = 15%।',
    'Percentage change हमेशा OLD value (base) के सापेक्ष होता है। Formula: %change = (new - old)/old × 100।',
    'EASY', array['percentage','growth-rate']::text[], 'Original', 'ORIGINAL', 'active')
  returning id into v_q_id;
  insert into public.options (question_id, label, text_en, text_hi, is_correct, trap_reason_en, trap_reason_hi) values
    (v_q_id, 'A', '13%', '13%', false, 'Used (3000/23000)×100 — divided by the new value instead of the old.', '(3000/23000)×100 लगाया — नए value से भाग दिया।'),
    (v_q_id, 'B', '15%', '15%', true, null, null),
    (v_q_id, 'C', '10%', '10%', false, 'Estimation — close to 15 but underestimated.', 'अंदाज़ कम — 15 के पास।'),
    (v_q_id, 'D', '1.15%', '1.15%', false, 'Computed 3000/20000 = 0.15 but forgot to multiply by 100.', '3000/20000 = 0.15 निकाला पर × 100 भूल गए।');

  -- Q9
  insert into public.questions (section_id, display_order, stem_en, stem_hi, explanation_method_en, explanation_concept_en, explanation_method_hi, explanation_concept_hi, difficulty, tags, source, license, status)
  values (v_section_id, 8, 'A''s salary is 25% more than B''s salary. By what percentage is B''s salary less than A''s?', 'A की salary B की salary से 25% अधिक है। B की salary A की salary से कितने प्रतिशत कम है?',
    'Shortcut: B less than A by (25/125) × 100 = 20%. Generally, if X is x% more than Y, Y is x/(100+x) × 100% less than X.',
    'Take B = 100, then A = 125. Difference = 25. To express the difference as a percent of A: 25/125 = 20%. The reason 25% is not 20% is the base shift: when comparing A more than B the base is B; when comparing B less than A the base is A. Larger base means smaller percent.',
    'shortcut: B less than A by (25/125) × 100 = 20%।',
    'B = 100 मानें, A = 125। अंतर = 25। A के percent में: 25/125 = 20%। base shift से 25% और 20% अलग हैं।',
    'MEDIUM', array['percentage','comparison-percentage']::text[], 'Original', 'ORIGINAL', 'active')
  returning id into v_q_id;
  insert into public.options (question_id, label, text_en, text_hi, is_correct, trap_reason_en, trap_reason_hi) values
    (v_q_id, 'A', '25%', '25%', false, 'Common error: assumed symmetry. But the bases differ.', 'common गलती: symmetry मान ली।'),
    (v_q_id, 'B', '20%', '20%', true, null, null),
    (v_q_id, 'C', '30%', '30%', false, 'Overcorrection — sensed asymmetry but went the wrong way.', 'Overcorrect — दिशा गलत।'),
    (v_q_id, 'D', '15%', '15%', false, 'Used 25 × 0.6 — wrong shortcut.', '25 × 0.6 लगाया — shortcut गलत।');

  -- Q10
  insert into public.questions (section_id, display_order, stem_en, stem_hi, explanation_method_en, explanation_concept_en, explanation_method_hi, explanation_concept_hi, difficulty, tags, source, license, status)
  values (v_section_id, 9, 'An article is marked at Rs.500. After a discount of 10% and an additional 5% off on the discounted price, what is the final price?', 'एक वस्तु का marked price ₹500 है। पहले 10% discount, फिर discounted price पर 5% और discount। अंतिम कीमत क्या है?',
    'Final = 500 × 0.90 × 0.95 = 500 × 0.855 = Rs.427.50. Equivalent single discount = 1 - 0.855 = 14.5%.',
    'Successive percentage changes multiply, they do not add. Each later discount applies to the already-reduced price. Equivalent single rate for two successive discounts a%, b% is: 1 - (1 - a/100)(1 - b/100). Here 1 - 0.90 × 0.95 = 0.145 = 14.5%, less than 10 + 5 = 15%.',
    'Final = 500 × 0.90 × 0.95 = 500 × 0.855 = ₹427.50। equivalent single discount = 1 - 0.855 = 14.5%।',
    'Successive percentage changes multiply होते हैं। हर अगला discount पहले से कम हुई price पर लगता है।',
    'MEDIUM', array['percentage','successive-discount','discount']::text[], 'Original', 'ORIGINAL', 'active')
  returning id into v_q_id;
  insert into public.options (question_id, label, text_en, text_hi, is_correct, trap_reason_en, trap_reason_hi) values
    (v_q_id, 'A', 'Rs.425.00', '₹425.00', false, 'Subtracted 15% directly from Rs.500 — but successive discounts compound, they do not add.', '₹500 में से 15% सीधे घटाया।'),
    (v_q_id, 'B', 'Rs.427.50', '₹427.50', true, null, null),
    (v_q_id, 'C', 'Rs.450.00', '₹450.00', false, 'Computed only the first discount and stopped.', 'केवल पहला discount निकाला।'),
    (v_q_id, 'D', 'Rs.475.00', '₹475.00', false, 'Took only 5% off the marked price — applied wrong discount.', 'marked price से सिर्फ 5% घटाया।');

  -- Q11
  insert into public.questions (section_id, display_order, stem_en, stem_hi, explanation_method_en, explanation_concept_en, explanation_method_hi, explanation_concept_hi, difficulty, tags, source, license, status)
  values (v_section_id, 10, 'If 70% of students pass an exam and 24 students fail, how many students appeared?', 'यदि 70% छात्र pass हों और 24 छात्र fail हों, तो कुल कितने छात्र appeared थे?',
    'Pass% = 70 ⇒ Fail% = 30. 30% of total = 24 ⇒ total = 24 ÷ 0.30 = 80.',
    'Pass and fail are mutually exclusive and cover all candidates, so they sum to 100%. Whenever you have an absolute count tied to a percent, divide the count by the percent fractional value (here 0.30, NOT 0.70).',
    'Pass% = 70 ⇒ Fail% = 30। total का 30% = 24 ⇒ total = 24 ÷ 0.30 = 80।',
    'Pass और fail mutually exclusive हैं, sum 100%। absolute count को percent fractional value से भाग दें (यहाँ 0.30, 0.70 नहीं)।',
    'EASY', array['percentage','complement-percentage']::text[], 'Original', 'ORIGINAL', 'active')
  returning id into v_q_id;
  insert into public.options (question_id, label, text_en, text_hi, is_correct, trap_reason_en, trap_reason_hi) values
    (v_q_id, 'A', '60', '60', false, 'Used 24 ÷ 0.40 — wrong fail percentage.', '24 ÷ 0.40 लगाया — fail % गलत।'),
    (v_q_id, 'B', '80', '80', true, null, null),
    (v_q_id, 'C', '100', '100', false, 'Estimation slip.', 'अंदाज़ — जल्दबाज़ी।'),
    (v_q_id, 'D', '120', '120', false, 'Used 20% fail rate, but actual is 30%.', '20% fail माना, सही 30%।');

  -- Q12
  insert into public.questions (section_id, display_order, stem_en, stem_hi, explanation_method_en, explanation_concept_en, explanation_method_hi, explanation_concept_hi, difficulty, tags, source, license, status)
  values (v_section_id, 11, 'A''s income is 20% less than B''s. B''s income is 25% less than C''s. A''s income is what percent of C''s?', 'A की income B की income से 20% कम है। B की income C की income से 25% कम है। A की income C की income का कितने प्रतिशत है?',
    'Take C = 100. B = 100 × 0.75 = 75. A = 75 × 0.80 = 60. So A = 60% of C.',
    'Chain percentages of the form X is x% less than Y, Y is y% less than Z compose multiplicatively: A/C = (A/B)(B/C) = (1 - x/100)(1 - y/100). Adding x and y is wrong because each percent is on a different base in the chain.',
    'C = 100 मानें। B = 100 × 0.75 = 75। A = 75 × 0.80 = 60। तो A = C का 60%।',
    'Chain में percentages गुणा से compose होते हैं। x + y जोड़ना गलत है।',
    'HARD', array['percentage','chain-percentage']::text[], 'Original', 'ORIGINAL', 'active')
  returning id into v_q_id;
  insert into public.options (question_id, label, text_en, text_hi, is_correct, trap_reason_en, trap_reason_hi) values
    (v_q_id, 'A', '55%', '55%', false, 'Subtracted 20+25 = 45% directly from 100% — wrong, compound effects.', '20+25 = 45% सीधे घटाया।'),
    (v_q_id, 'B', '60%', '60%', true, null, null),
    (v_q_id, 'C', '65%', '65%', false, 'Estimated by additive feel — actual is 60.', 'अंदाज़ से।'),
    (v_q_id, 'D', '75%', '75%', false, 'Took only the B-vs-C step (75%) and forgot the A-vs-B step.', 'केवल B-vs-C step लिया।');

  -- Q13
  insert into public.questions (section_id, display_order, stem_en, stem_hi, explanation_method_en, explanation_concept_en, explanation_method_hi, explanation_concept_hi, difficulty, tags, source, license, status)
  values (v_section_id, 12, 'In a class of 50 students, 60% are boys. If 8 girls are absent on a particular day, what percent of the class present is girls?', '50 छात्रों की class में 60% लड़के हैं। एक दिन 8 लड़कियाँ absent हैं — present class में लड़कियाँ कितने प्रतिशत हैं?',
    'Total = 50, boys = 30, girls = 20. Absent girls = 8 ⇒ present girls = 12. Present class = 50 - 8 = 42. % = 12/42 × 100 ≈ 28.57%.',
    'When the population composition changes (here through absences), recompute the relevant base. The percent present girls takes present girls as the numerator and present total (not original total) as the denominator.',
    'total = 50, लड़के = 30, लड़कियाँ = 20। absent लड़कियाँ = 8 ⇒ present लड़कियाँ = 12। present class = 50 - 8 = 42। % = 12/42 × 100 ≈ 28.57%।',
    'जब composition बदले, तो relevant base फिर से निकालें। numerator और denominator दोनों present पर आधारित हों।',
    'MEDIUM', array['percentage','ratio-percentage','absenteeism']::text[], 'Original', 'ORIGINAL', 'active')
  returning id into v_q_id;
  insert into public.options (question_id, label, text_en, text_hi, is_correct, trap_reason_en, trap_reason_hi) values
    (v_q_id, 'A', '20%', '20%', false, 'Used 12/60 instead of 12/42 — divided by total class, not present class.', '12/60 लगाया, सही 12/42।'),
    (v_q_id, 'B', 'Approx 28.6%', 'लगभग 28.6%', true, null, null),
    (v_q_id, 'C', '40%', '40%', false, 'This is the original girls% — but absent girls reduced it.', 'मूल girls% यही है — पर absent ने इसे कम कर दिया।'),
    (v_q_id, 'D', '32%', '32%', false, 'Used 12/(50-8) but rounded the calculation in the wrong direction.', '12/(50-8) पर round wrong direction में किया।');

  -- Q14
  insert into public.questions (section_id, display_order, stem_en, stem_hi, explanation_method_en, explanation_concept_en, explanation_method_hi, explanation_concept_hi, difficulty, tags, source, license, status)
  values (v_section_id, 13, 'The price of a commodity rose by 30%. The expenditure on it increased by only 8%. By what percentage did consumption decrease?', 'एक commodity की कीमत 30% बढ़ी। उस पर expenditure सिर्फ 8% बढ़ा। consumption कितने प्रतिशत घटी?',
    'expenditure = price × consumption. So consumption_ratio = expenditure_ratio / price_ratio = 1.08 / 1.30 ≈ 0.8308. Drop ≈ (1 - 0.8308) × 100 ≈ 16.9%.',
    'Expenditure is the product of price and consumption. If price rises by p% and expenditure rises by e%, the consumption multiplier is (1 + e/100)/(1 + p/100). The drop in consumption is whatever short-fall keeps expenditure from rising as much as the price did.',
    'expenditure = price × consumption। consumption_ratio = 1.08/1.30 ≈ 0.8308। drop ≈ 16.9%।',
    'Expenditure = price × consumption। price p% बढ़े, expenditure e% बढ़े, तो consumption multiplier = (1 + e/100)/(1 + p/100)।',
    'HARD', array['percentage','expenditure','price-consumption']::text[], 'Original', 'ORIGINAL', 'active')
  returning id into v_q_id;
  insert into public.options (question_id, label, text_en, text_hi, is_correct, trap_reason_en, trap_reason_hi) values
    (v_q_id, 'A', 'Approx 16.9%', 'लगभग 16.9%', true, null, null),
    (v_q_id, 'B', '22%', '22%', false, 'Subtracted 8 from 30 directly — but expenditure = price × consumption involves ratios.', '30 में से 8 घटाया।'),
    (v_q_id, 'C', '20%', '20%', false, 'Approximation — actual consumption ratio is 1.08/1.30 ≈ 0.831, drop ≈ 16.9%, not 20%.', 'अंदाज़ से — असली drop ≈ 16.9% है।'),
    (v_q_id, 'D', '30%', '30%', false, 'Cancelled the price rise entirely — would mean expenditure did not change.', 'price rise पूरा cancel कर दिया।');

  -- Q15
  insert into public.questions (section_id, display_order, stem_en, stem_hi, explanation_method_en, explanation_concept_en, explanation_method_hi, explanation_concept_hi, difficulty, tags, source, license, status)
  values (v_section_id, 14, 'A number is increased by 50% and then decreased by 50%. The net change is:', 'किसी संख्या को 50% बढ़ाया जाए और फिर 50% घटाया जाए। net change है:',
    'Formula: net% = a + b + (a×b)/100 = 50 + (-50) + (50)(-50)/100 = 0 - 25 = -25%. So 25% decrease.',
    'Take 100, then +50% gives 150, then -50% gives 75. Drop = 25 on 100 = 25%. The asymmetry comes from cutting on a larger base (150) than you grew on (100). General rule: ±x% successive on the same number always nets to a decrease of (x²/100)%.',
    'Formula: net% = a + b + (a×b)/100 = 50 + (-50) + (50)(-50)/100 = 0 - 25 = -25%। यानी 25% की कमी।',
    '100 से +50% = 150, -50% = 75। drop = 25%। asymmetry: कमी बड़े base पर लगी।',
    'EASY', array['percentage','successive-percentage']::text[], 'Original', 'ORIGINAL', 'active')
  returning id into v_q_id;
  insert into public.options (question_id, label, text_en, text_hi, is_correct, trap_reason_en, trap_reason_hi) values
    (v_q_id, 'A', '0% (no change)', '0% (कोई बदलाव नहीं)', false, 'Symmetry trap — 50-50 does not equal zero because bases differ.', 'symmetry trap — bases अलग।'),
    (v_q_id, 'B', '25% decrease', '25% की कमी', true, null, null),
    (v_q_id, 'C', '25% increase', '25% की बढ़त', false, 'Right magnitude, wrong direction.', 'magnitude सही, दिशा गलत।'),
    (v_q_id, 'D', '50% decrease', '50% की कमी', false, 'Took only the second step. The first step inflated the base.', 'केवल दूसरा step लिया।');

end $$;