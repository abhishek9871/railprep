# Admin Panel Spec

A **separate web app** for content editors to publish articles, tests, notifications, and manage users. Not required at app launch day-one, but needed within 2 weeks of launch to sustain the self-updating content promise.

## Stack
- Next.js 14 (App Router) + TypeScript
- Tailwind CSS + shadcn/ui
- Firebase Web SDK (Auth + Firestore + Storage)
- TipTap rich-text editor for articles (Markdown export)
- Hosted on Vercel or Firebase Hosting
- Access gated by Firebase custom claim `{ admin: true }` — set via a one-off script for the founding editors.

## Screens

1. **Login** — Google sign-in only for editors.
2. **Dashboard** — counts: articles published this week, pending test reviews, open doubts, active users today.
3. **Articles**
   - List (filter by category, status draft/published)
   - Create/edit form: title (EN+HI), summary (EN+HI), body (rich text EN+HI side-by-side), cover image upload, category, tags, priority, linked questions
   - Preview mobile render
   - "Generate Hindi translation" button → Cloud Function using Google Translate API (editor reviews before publish)
   - Publish / Schedule-for-later
4. **Tests**
   - List with attempt counts
   - Create test: pick sections, upload question bank CSV, or create questions inline
   - Question editor: stem (EN+HI), 4 options (EN+HI), correct key, solution (EN+HI), tags, difficulty, PYQ metadata
   - CSV import template provided
5. **Exam notifications** — form matching `ExamNotification` model; attach official PDF.
6. **Broadcasts** — push a notification to all users or a segment (state, plan tier, last active).
7. **Cut-off predictions** — manual override on auto-computed values.
8. **Users** — search by phone, view subscription, grant/revoke Pro (support use case), issue refunds (logs action).
9. **Moderation** — flagged doubts/answers queue; remove, warn, ban.
10. **Analytics link-outs** — buttons to GA4, Crashlytics, Firestore console.

## Build later
Do NOT build the admin panel in the same Claude Code session as the Android app. After the Android MVP is working, spin up a second project for the admin panel with its own `CLAUDE.md`. Keep concerns separate.
