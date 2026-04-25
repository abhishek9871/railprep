-- Recovered from Supabase MCP on 2026-04-25.
-- MCP version: 20260424042251
-- Original application order is determined by version timestamp, not filename.

-- Audit script (audit-topics.mjs) caught 5 mismatches in the 2026-27 reprint of
-- Class 11 Indian Economic Development (keec1) — NCERT dropped the "Poverty" and
-- "Infrastructure" chapters and shifted everything by one. Plus one mismatch in the
-- 2024-25 Class 8 SPL III (hess3) where Ch 5 "Criminal Justice" was replaced by
-- "Understanding Marginalisation". Titles below are verbatim from each PDF's
-- opening content page.

update public.topics set
  title_en = 'Human Capital Formation in India (NCERT Class 11 Economics Ch 4)',
  title_hi = 'भारत में मानव पूंजी निर्माण (NCERT कक्षा 11 अर्थशास्त्र अध्याय 4)'
  where external_pdf_url = 'https://ncert.nic.in/textbook/pdf/keec104.pdf';

update public.topics set
  title_en = 'Rural Development (NCERT Class 11 Economics Ch 5)',
  title_hi = 'ग्रामीण विकास (NCERT कक्षा 11 अर्थशास्त्र अध्याय 5)'
  where external_pdf_url = 'https://ncert.nic.in/textbook/pdf/keec105.pdf';

update public.topics set
  title_en = 'Employment: Growth, Informalisation and Other Issues (NCERT Class 11 Economics Ch 6)',
  title_hi = 'रोज़गार: संवृद्धि, अनौपचारीकरण और अन्य मुद्दे (NCERT कक्षा 11 अर्थशास्त्र अध्याय 6)'
  where external_pdf_url = 'https://ncert.nic.in/textbook/pdf/keec106.pdf';

update public.topics set
  title_en = 'Environment and Sustainable Development (NCERT Class 11 Economics Ch 7)',
  title_hi = 'पर्यावरण एवं सतत विकास (NCERT कक्षा 11 अर्थशास्त्र अध्याय 7)'
  where external_pdf_url = 'https://ncert.nic.in/textbook/pdf/keec107.pdf';

update public.topics set
  title_en = 'Comparative Development Experiences of India and its Neighbours (NCERT Class 11 Economics Ch 8)',
  title_hi = 'भारत और उसके पड़ोसी देशों के तुलनात्मक विकास अनुभव (NCERT कक्षा 11 अर्थशास्त्र अध्याय 8)'
  where external_pdf_url = 'https://ncert.nic.in/textbook/pdf/keec108.pdf';

update public.topics set
  title_en = 'Understanding Marginalisation (NCERT Class 8 SPL III Ch 5)',
  title_hi = 'हाशियाकरण की समझ (NCERT कक्षा 8 अध्याय 5)'
  where external_pdf_url = 'https://ncert.nic.in/textbook/pdf/hess305.pdf';