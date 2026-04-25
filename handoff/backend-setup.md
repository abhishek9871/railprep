# Backend Setup — Firebase + Cloud Functions

## 1. Create Firebase project

1. Go to https://console.firebase.google.com → **Add project** → name it `railprep-prod`.
2. Enable Google Analytics (default account is fine).
3. Upgrade to **Blaze (pay-as-you-go)** plan — required for Cloud Functions and outbound network calls. Set a budget alert at ₹5,000/mo initially.

## 2. Enable services

| Service | Settings |
|---|---|
| **Authentication** | Enable Phone provider. Add test numbers for dev (`+91 99999 99999 → 123456`). Enable Google & Facebook providers post-launch if needed. |
| **Firestore Database** | Create in **asia-south1 (Mumbai)** for latency. Start in production mode; rules go in `firestore.rules`. |
| **Cloud Storage** | Same region. Default bucket for media + user uploads. |
| **Cloud Functions** | Deploy to `asia-south1`. |
| **Cloud Messaging (FCM)** | For push notifications. |
| **Remote Config** | For feature flags + dynamic promos. |
| **Crashlytics** | Enable in app. |
| **Analytics** | Link to BigQuery if you want raw event streaming (optional, ~₹500/mo extra). |
| **App Check** | Enable with Play Integrity provider. Protects all APIs from scraping. |

## 3. Security rules

### `firestore.rules` (skeleton)
```
rules_version = '2';
service cloud.firestore {
  match /databases/{db}/documents {

    function signedIn() { return request.auth != null; }
    function isOwner(uid) { return signedIn() && request.auth.uid == uid; }
    function isAdmin()    { return signedIn() && request.auth.token.admin == true; }
    function isPro() {
      return signedIn() &&
        get(/databases/$(db)/documents/users/$(request.auth.uid)).data.subscription.tier == 'PRO';
    }

    match /users/{uid} {
      allow read: if isOwner(uid) || isAdmin();
      allow create: if isOwner(uid);
      allow update: if isOwner(uid) &&
        !('subscription' in request.resource.data.diff(resource.data).affectedKeys()); // only server updates subscription
      match /{sub=**} { allow read, write: if isOwner(uid); }
    }

    match /subjects/{s} { allow read: if signedIn(); allow write: if isAdmin(); }
    match /subjects/{s}/chapters/{c}        { allow read: if signedIn(); allow write: if isAdmin(); }
    match /subjects/{s}/chapters/{c}/topics/{t} {
      allow read: if signedIn() && (!resource.data.isPro || isPro() || isAdmin());
      allow write: if isAdmin();
    }

    match /tests/{id} {
      allow read: if signedIn() && (!resource.data.isPro || isPro() || isAdmin());
      allow write: if isAdmin();
      match /questions/{q} { allow read: if signedIn(); allow write: if isAdmin(); }
    }

    match /articles/{id}        { allow read: if signedIn(); allow write: if isAdmin(); }
    match /examNotifications/{id}{ allow read: if signedIn(); allow write: if isAdmin(); }
    match /cutoffs/{id}         { allow read: if signedIn(); allow write: if isAdmin(); }
    match /config/{id}          { allow read: if signedIn(); allow write: if isAdmin(); }

    match /doubts/{id} {
      allow read: if signedIn();
      allow create: if signedIn();
      allow update: if isOwner(resource.data.userId) || isAdmin();
      allow delete: if isOwner(resource.data.userId) || isAdmin();
      match /answers/{aid} {
        allow read: if signedIn();
        allow create: if signedIn();
        allow update, delete: if isOwner(resource.data.userId) || isAdmin();
      }
    }
  }
}
```

### `storage.rules`
- User uploads (doubts images): path `users/{uid}/*` — owner write, authenticated read.
- Public media (articles, videos): path `public/*` — admin write, authenticated read via signed URLs.

## 4. Cloud Functions layout

```
functions/
  src/
    index.ts            # register all exports
    auth/
      onUserCreate.ts   # seed user doc, send welcome notification
    payments/
      createOrder.ts    # Razorpay order creation
      verifyPayment.ts  # HTTPS callable — verify signature, activate Pro
      webhook.ts        # Razorpay webhook — handle failures/refunds
    content/
      publishArticle.ts # admin-callable; triggers FCM topic push
      syncCutoffs.ts    # scheduled — recompute cut-offs from attempt stats
    tests/
      scoreAttempt.ts   # callable — server-authoritative scoring + rank
      computeLeaderboard.ts # scheduled every 5 min
    notifications/
      sendBroadcast.ts
      onDoubtAnswer.ts  # firestore trigger — notify doubt owner
    sync/
      bundleDelta.ts
```

## 5. Key functions to implement

### scoreAttempt (callable)
Input: `attemptId`. Server reads attempt, grades against `correctKey`, writes `score/rank/percentile`, updates user stats and leaderboard entry atomically.

### verifyPayment (callable)
Input: `{ orderId, paymentId, signature, planId }`. Server verifies Razorpay HMAC, updates user subscription, returns the new Subscription object.

### computeLeaderboard (scheduled, every 5 min)
Aggregates recent attempts into `/leaderboards/{period}/entries/{userId}`.

### publishArticle (admin callable)
Admin app calls this. Function writes article, optionally sends FCM to topic `current-affairs`, and generates TTS audio via Google Cloud TTS if `audioUrl` is null.

## 6. Indexes

In `firestore.indexes.json`:
- articles: `publishedAt desc, category asc`
- tests: `kind asc, publishedAt desc`
- doubts: `subjectId asc, createdAt desc`; `trending desc, createdAt desc`
- notifications: `userId asc, createdAt desc`

## 7. Environments

Create **two** Firebase projects: `railprep-dev` and `railprep-prod`. Use Firebase flavors in the Android build so debug builds point at dev. Keep production data clean.

## 8. Cost expectations (rough, INR/month)

| Users | Firestore | Functions | Storage | Total |
|---|---|---|---|---|
| 1K   | ₹200 | ₹100 | ₹100 | ~₹400 |
| 10K  | ₹1,500 | ₹800 | ₹500 | ~₹3,000 |
| 100K | ₹12,000 | ₹6,000 | ₹3,000 | ~₹25,000 |

Hot spots: unread test questions (cache client-side), leaderboard reads (aggregate, don't live-listen per user).
