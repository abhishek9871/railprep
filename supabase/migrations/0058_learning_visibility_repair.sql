-- Hide the legacy pre-canonical Reasoning subject content from Learn without deleting it.
-- Canonical reasoning primers live under subjects.slug = 'reasoning'.
update public.topics t
set status = 'stale'
from public.chapters c
join public.subjects s on s.id = c.subject_id
where t.chapter_id = c.id
  and s.slug = 'reason'
  and t.status = 'active';
