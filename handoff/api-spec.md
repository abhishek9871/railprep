# RailPrep — API Specification

Base URL: `https://api.railprep.app/v1`
Auth: Firebase ID token in `Authorization: Bearer <token>` header.
All responses JSON. Errors: `{ "error": { "code": "string", "message": "string" } }` + HTTP status.

---

## Auth

### POST /auth/send-otp
```json
{ "phone": "+919876543210" }
→ { "sessionId": "abc", "expiresIn": 300 }
```

### POST /auth/verify-otp
```json
{ "sessionId": "abc", "otp": "1234" }
→ { "idToken": "...", "refreshToken": "...", "user": {User}, "isNewUser": true }
```

### POST /auth/refresh
```json
{ "refreshToken": "..." } → { "idToken": "..." }
```

### POST /users/me/onboarding
Complete onboarding (language, examGoal, eligibility). Returns User.

---

## Learning

### GET /subjects
List all subjects (cacheable 1h).

### GET /subjects/{id}/chapters

### GET /chapters/{id}/topics

### GET /topics/{id}
Full topic incl. signed video/PDF URLs (15-min TTL).

### POST /progress/topic/{topicId}
```json
{ "progressPct": 0.65, "lastPositionSec": 245 }
```

---

## Tests

### GET /tests?kind=FULL&limit=20&cursor=
Paginated test list.

### GET /tests/{id}
Test metadata.

### GET /tests/{id}/questions
Returns question set. For LIVE tests only accessible during the window.

### POST /attempts
Start an attempt.
```json
{ "testId": "..." } → { "attemptId": "...", "expiresAt": "..." }
```

### PATCH /attempts/{id}
Save partial state (every 30s while attempting).
```json
{ "answers": {...}, "durationUsedSec": 1820 }
```

### POST /attempts/{id}/submit
```json
→ {
  "score": 72.67, "correctCount": 78, "wrongCount": 16, "skippedCount": 6,
  "rank": 842, "percentile": 92.3,
  "sectionScores": {...}, "weakTopics": [...]
}
```

### GET /attempts/{id}/review
Question-by-question breakdown with solutions.

---

## Dynamic content (self-updating)

### GET /articles?category=&since=&limit=
Current affairs feed. `since` = ISO timestamp for delta sync.
Response has `hasMore`, `nextCursor`, and `serverTime` for next `since`.

### GET /articles/{id}

### GET /daily-digest?date=2026-04-23
Curated 10-Q quiz from the day's articles.

### GET /exam-notifications?exam=NTPC
Latest official exam notifications.

### GET /cutoffs/{exam}
Predicted cut-offs by category.

### GET /config
Remote config snapshot: feature flags, syllabus version, festival themes, maintenance banner.
**Client should poll every app resume or listen via FCM topic `config-updates`.**

---

## Notifications

### GET /notifications?unreadOnly=true
### POST /notifications/{id}/read
### POST /devices/register — register FCM token

---

## Doubts

### GET /doubts?subjectId=&filter=trending|mine|resolved
### POST /doubts — create
### GET /doubts/{id}
### POST /doubts/{id}/answers
### POST /answers/{id}/like
### POST /doubts/{id}/resolve (owner only)

---

## Leaderboard

### GET /leaderboard?period=weekly&scope=india|state|friends
Returns top 100 + user's own rank entry.

---

## Payments (Razorpay)

### GET /plans
```json
[
  { "id": "pro_1m", "name": "Pro · 1 month",  "price": 19900, "currency": "INR", "durationDays": 30 },
  { "id": "pro_6m", "name": "Pro · 6 months", "price": 79900, "currency": "INR", "durationDays": 180, "badge": "BEST VALUE" }
]
```

### POST /payments/order
```json
{ "planId": "pro_6m" } → { "orderId": "order_xxx", "razorpayKey": "rzp_...", "amount": 79900 }
```

### POST /payments/verify
```json
{ "orderId": "...", "paymentId": "...", "signature": "..." } → { "subscription": {Subscription} }
```

### POST /payments/webhook (Razorpay → server)

---

## Offline sync

### POST /sync/attempts
Batched upload of attempts submitted offline.
```json
{ "attempts": [ {AttemptPayload}, ... ] } → { "synced": [...ids] }
```

### GET /sync/bundle?lastSyncAt=
Delta bundle: new articles, test metadata, notifications.

---

## Admin / Content Ops (separate admin web app)

- POST /admin/articles · publish article
- POST /admin/tests · publish test
- POST /admin/notifications/broadcast
- POST /admin/exam-notifications

Admin auth uses Firebase custom claims `{ admin: true }`.

---

## Rate limits
- Auth: 5 req/min per phone
- Read APIs: 120 req/min per user
- Write APIs: 30 req/min per user
- Doubts post: 10/hour per user

## Caching headers
- `/subjects`, `/chapters/*`, `/topics/*`: `Cache-Control: private, max-age=3600`
- `/articles`, `/tests/*`: `Cache-Control: private, max-age=300`
- Signed media URLs: do not cache beyond TTL
