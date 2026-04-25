-- Phase 4 content cleanup: replace legacy percentage trap_reason filler language
-- with specific arithmetic paths that produce the distractor values.

with updates(stem_en, label, text_en, trap_reason_en, trap_reason_hi) as (
  values
    (
      'What is 35% of 240?',
      'B',
      '82',
      'Took 30% = 72 and used 5% = 10 instead of 12, giving 72 + 10 = 82.',
      '30% = 72 लिया और 5% को 12 की जगह 10 लिया; 72 + 10 = 82।'
    ),
    (
      'What is 35% of 240?',
      'C',
      '96',
      'Used 40% of 240: 4 × 24 = 96, replacing the required 35%.',
      '35% की जगह 40% लगाया: 4 × 24 = 96।'
    ),
    (
      'What is 35% of 240?',
      'D',
      '72',
      'Used only 30% of 240: 3 × 24 = 72, missing the extra 5%.',
      'सिर्फ 30% लिया: 3 × 24 = 72; extra 5% छूट गया।'
    ),
    (
      'Ramesh spends 75% of his salary. If his salary is increased by 20% and his expenses by 10%, by what percent do his savings change?',
      'D',
      'Increase by 25%',
      'Found savings increase = 12.5, then divided by 50 instead of original savings 25: 12.5/50 = 25%.',
      'savings increase = 12.5 निकाला, फिर original savings 25 की जगह 50 से divide किया: 12.5/50 = 25%।'
    ),
    (
      'If the price of sugar increases by 25%, by what percentage should a family reduce its consumption to keep the expenditure on sugar unchanged?',
      'C',
      '15%',
      'Used 25% price rise minus a 10-point adjustment to get 15%. But 1.25 × 0.85 = 1.0625, so expenditure still rises by 6.25%.',
      '25% price rise में से 10-point adjustment घटाकर 15% लिया। पर 1.25 × 0.85 = 1.0625, इसलिए expenditure फिर भी 6.25% बढ़ता है।'
    ),
    (
      'The population of a town increased from 20000 to 23000. Find the percentage increase.',
      'C',
      '10%',
      'Divided 3000 by 30000 instead of 20000: 3000/30000 × 100 = 10%.',
      '3000 को 20000 की जगह 30000 से divide किया: 3000/30000 × 100 = 10%।'
    ),
    (
      'If 70% of students pass an exam and 24 students fail, how many students appeared?',
      'C',
      '100',
      'Used 24/24 = 1, then converted it to 100; the fail fraction should be 30%, so total = 24/0.30 = 80.',
      '24/24 = 1 करके 100 लिया; fail fraction 30% है, इसलिए total = 24/0.30 = 80।'
    ),
    (
      'A''s income is 20% less than B''s. B''s income is 25% less than C''s. A''s income is what percent of C''s?',
      'C',
      '65%',
      'Subtracted 20 and 25 from 100, then added back 10: 100 - 45 + 10 = 65. The overlap correction should be 5, so A is 60% of C.',
      '100 से 20 और 25 घटाकर 10 वापस जोड़ा: 100 - 45 + 10 = 65। correction 5 होना चाहिए, इसलिए A = 60% of C।'
    ),
    (
      'In a class of 50 students, 60% are boys. If 8 girls are absent on a particular day, what percent of the class present is girls?',
      'D',
      '32%',
      'Used 37.5 as the present-class base instead of 42: 12/37.5 × 100 = 32%.',
      'present class base 42 की जगह 37.5 लिया: 12/37.5 × 100 = 32%।'
    ),
    (
      'The price of a commodity rose by 30%. The expenditure on it increased by only 8%. By what percentage did consumption decrease?',
      'C',
      '20%',
      'Used price ratio 1.35 instead of 1.30: 1.08/1.35 = 0.80, giving a 20% drop.',
      'price ratio 1.30 की जगह 1.35 लिया: 1.08/1.35 = 0.80, इसलिए 20% drop।'
    )
)
update public.options o
set trap_reason_en = u.trap_reason_en,
    trap_reason_hi = u.trap_reason_hi
from public.questions q
join updates u
  on u.stem_en = q.stem_en
where o.question_id = q.id
  and o.label = u.label
  and o.text_en = u.text_en;
