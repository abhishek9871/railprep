-- Recovered from Supabase MCP on 2026-04-25.
-- MCP version: 20260424040554
-- Original application order is determined by version timestamp, not filename.

-- Titles below are taken verbatim from each PDF's page-2/3 header text, which is the
-- chapter title NCERT renders on the first content page. The book name in the 2024-25
-- edition is "Kaveri" (formerly Beehive); "Ganita Manjari" (formerly Mathematics) for
-- Class 9 maths. Using the correct current names unblocks users who tap a title and
-- expect the file they open to match.

-- Class 8 Geography (Resources and Development) — the 2024-25 rationalisation
-- removed "Mineral and Power Resources" and shifted the remaining chapters by one.
-- Page-1 content of hess403 is "Agriculture", hess404 is "Industries", hess405 is
-- "Human Resources".
update public.topics set
  title_en = 'Agriculture (NCERT Class 8 Geography Ch 3)',
  title_hi = 'कृषि (NCERT कक्षा 8 भूगोल अध्याय 3)'
  where external_pdf_url = 'https://ncert.nic.in/textbook/pdf/hess403.pdf';

update public.topics set
  title_en = 'Industries (NCERT Class 8 Geography Ch 4)',
  title_hi = 'उद्योग (NCERT कक्षा 8 भूगोल अध्याय 4)'
  where external_pdf_url = 'https://ncert.nic.in/textbook/pdf/hess404.pdf';

update public.topics set
  title_en = 'Human Resources (NCERT Class 8 Geography Ch 5)',
  title_hi = 'मानव संसाधन (NCERT कक्षा 8 भूगोल अध्याय 5)'
  where external_pdf_url = 'https://ncert.nic.in/textbook/pdf/hess405.pdf';

-- Class 9 Mathematics (Ganita Manjari, 2024-25) — replace generic "Chapter N" labels
-- with the chapter titles printed on each PDF's content pages.
update public.topics set
  title_en = 'Orienting Yourself: The Use of Coordinates (NCERT Class 9 Mathematics Ch 1)',
  title_hi = 'स्वयं को दिशा देना: निर्देशांकों का उपयोग (NCERT कक्षा 9 गणित अध्याय 1)'
  where external_pdf_url = 'https://ncert.nic.in/textbook/pdf/iemh101.pdf';

update public.topics set
  title_en = 'Introduction to Linear Polynomials (NCERT Class 9 Mathematics Ch 2)',
  title_hi = 'रैखिक बहुपदों का परिचय (NCERT कक्षा 9 गणित अध्याय 2)'
  where external_pdf_url = 'https://ncert.nic.in/textbook/pdf/iemh102.pdf';

update public.topics set
  title_en = 'The Mathematics of Maybe: Introduction to Probability (NCERT Class 9 Mathematics Ch 7)',
  title_hi = 'संभाव्यता का परिचय (NCERT कक्षा 9 गणित अध्याय 7)'
  where external_pdf_url = 'https://ncert.nic.in/textbook/pdf/iemh107.pdf';

update public.topics set
  title_en = 'Predicting What Comes Next: Sequences and Progressions (NCERT Class 9 Mathematics Ch 8)',
  title_hi = 'अनुक्रम एवं श्रेढ़ियाँ (NCERT कक्षा 9 गणित अध्याय 8)'
  where external_pdf_url = 'https://ncert.nic.in/textbook/pdf/iemh108.pdf';

-- Class 9 English (Kaveri, 2024-25) — the 2024-25 edition replaced Beehive's old
-- stories with a completely new set. Titles below are the real chapter names as they
-- appear on each file's content page.
update public.topics set
  title_en = 'Kaveri — How I Taught My Grandmother to Read (NCERT Class 9 English Ch 1)',
  title_hi = 'Kaveri — How I Taught My Grandmother to Read (NCERT कक्षा 9 अंग्रेज़ी अध्याय 1)'
  where external_pdf_url = 'https://ncert.nic.in/textbook/pdf/iebe101.pdf';

update public.topics set
  title_en = 'Kaveri — The Pot Maker (NCERT Class 9 English Ch 2)',
  title_hi = 'Kaveri — The Pot Maker (NCERT कक्षा 9 अंग्रेज़ी अध्याय 2)'
  where external_pdf_url = 'https://ncert.nic.in/textbook/pdf/iebe102.pdf';

update public.topics set
  title_en = 'Kaveri — Winds of Change (NCERT Class 9 English Ch 3)',
  title_hi = 'Kaveri — Winds of Change (NCERT कक्षा 9 अंग्रेज़ी अध्याय 3)'
  where external_pdf_url = 'https://ncert.nic.in/textbook/pdf/iebe103.pdf';

update public.topics set
  title_en = 'Kaveri — Vitamin-M (NCERT Class 9 English Ch 4)',
  title_hi = 'Kaveri — Vitamin-M (NCERT कक्षा 9 अंग्रेज़ी अध्याय 4)'
  where external_pdf_url = 'https://ncert.nic.in/textbook/pdf/iebe104.pdf';

update public.topics set
  title_en = 'Kaveri — The World of Limitless Possibilities (NCERT Class 9 English Ch 5)',
  title_hi = 'Kaveri — The World of Limitless Possibilities (NCERT कक्षा 9 अंग्रेज़ी अध्याय 5)'
  where external_pdf_url = 'https://ncert.nic.in/textbook/pdf/iebe105.pdf';

update public.topics set
  title_en = 'Kaveri — Twin Melodies (NCERT Class 9 English Ch 6)',
  title_hi = 'Kaveri — Twin Melodies (NCERT कक्षा 9 अंग्रेज़ी अध्याय 6)'
  where external_pdf_url = 'https://ncert.nic.in/textbook/pdf/iebe106.pdf';

update public.topics set
  title_en = 'Kaveri — Carrier of Words (NCERT Class 9 English Ch 7)',
  title_hi = 'Kaveri — Carrier of Words (NCERT कक्षा 9 अंग्रेज़ी अध्याय 7)'
  where external_pdf_url = 'https://ncert.nic.in/textbook/pdf/iebe107.pdf';

update public.topics set
  title_en = 'Kaveri — Follow That Dream (NCERT Class 9 English Ch 8)',
  title_hi = 'Kaveri — Follow That Dream (NCERT कक्षा 9 अंग्रेज़ी अध्याय 8)'
  where external_pdf_url = 'https://ncert.nic.in/textbook/pdf/iebe108.pdf';