# Content Ops — How self-updating content works

This is the operational heartbeat of RailPrep. The app's value proposition depends on fresh content reaching students reliably.

## Daily rhythm

| Time (IST) | Task | Owner |
|---|---|---|
| 08:00 | Editor publishes 5-10 current-affairs items from last 24h | Editor |
| 09:00 | Auto-push: FCM topic `current-affairs` fires for subscribers | System |
| 10:00 | Daily digest (10-Q quiz built from today's items) unlocks | System (scheduled function) |
| Throughout day | Respond to flagged doubts | Moderator |
| 18:00 | Editor pushes weekly RRB / government job updates | Editor |
| 22:00 | Streak reminder FCM for users who haven't studied today | System |

## Weekly rhythm

- Mon: Publish one new full mock test (75 Q, CBT-1 pattern)
- Wed: Publish one subject-wise sectional mock
- Fri: Publish one PYQ set
- Sat/Sun: Live test at 10:00 AM IST (leaderboard live)

## Monthly rhythm

- Review and update cut-off predictions based on aggregated attempt data
- Audit and archive old articles (>90 days, move to `/archive`)
- Syllabus version bump if RRB publishes changes — update `config.syllabusVersion`

## Sources for current affairs

Editors should curate from (not scrape — editorial judgment matters):
- PIB India press releases
- The Hindu, Indian Express, Livemint, ET
- RailWire (for railway-specific updates)
- RRB regional websites
- Sports headlines from ESPN / official federations
- Award announcements from official sources

**Rewrite every item in your own words.** Never republish copyrighted content.

## Content quality rules

- Title ≤ 70 chars in EN and HI
- Summary: 2-3 sentences
- Body: 150-400 words, markdown, exam-relevance emphasized
- Every article links to 1-3 practice questions tagged with the same topic
- Hindi translation must be reviewed by a human editor — never auto-publish MT output

## Moderation SLAs

- Flagged doubt: first review within 4h
- Abuse report: within 1h during 9am-9pm IST
- Support email: within 24h

## Editor onboarding

Create a Notion or Google Doc with:
- RRB NTPC syllabus reference
- Writing style guide (tone, person, length)
- Hindi romanization conventions
- Embargo rules (don't publish exam answer keys before official release)
- Access credentials (admin panel, image library, stock images)

## Minimum viable editorial team

- **1 senior editor** (full-time) — plans the week, reviews everything, owns Hindi quality
- **1 junior editor/writer** (full-time) — drafts articles, CSVs test questions
- **1 subject expert** (part-time) — validates Math/Reasoning solutions
- **1 moderator** (part-time, evenings) — community doubts

This scales later, but is the minimum for a trustworthy product.
