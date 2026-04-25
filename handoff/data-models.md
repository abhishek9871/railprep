# RailPrep — Data Models

All timestamps are ISO-8601 UTC. IDs are server-generated ULIDs unless noted.

## User
```kotlin
data class User(
  val id: String,
  val phone: String,          // +91XXXXXXXXXX
  val name: String,
  val email: String?,
  val avatarUrl: String?,
  val language: String,       // ISO code: en, hi, bn, ta, te, mr, gu, pa
  val state: String?,         // Indian state
  val examGoal: ExamGoal,
  val eligibility: Eligibility,
  val subscription: Subscription,
  val stats: UserStats,
  val createdAt: Instant,
  val lastActiveAt: Instant
)

data class ExamGoal(
  val targetExam: String,      // "NTPC_CBT1_2026"
  val targetDate: LocalDate?,
  val dailyMinutes: Int,       // 30..240
  val timeline: String         // "1_MONTH" | "2_3_MONTHS" | "4_6_MONTHS" | "EXPLORING"
)

data class Eligibility(
  val qualification: String,   // "12TH" | "GRADUATE"
  val dob: LocalDate,
  val category: String         // UR | OBC | SC | ST | EWS
)

data class Subscription(
  val tier: String,            // "FREE" | "PRO"
  val planId: String?,
  val startedAt: Instant?,
  val expiresAt: Instant?,
  val autoRenew: Boolean
)

data class UserStats(
  val xp: Int,
  val level: Int,
  val streakCurrent: Int,
  val streakBest: Int,
  val lastStudyDate: LocalDate?,
  val testsAttempted: Int,
  val totalAccuracy: Double,   // 0..1
  val allIndiaRank: Int?
)
```

## Subject / Chapter / Topic
```kotlin
data class Subject(
  val id: String,              // "math" | "reason" | "ga" | "eng" | "gs" | "ca"
  val name: LocalizedString,
  val slug: String,
  val iconKey: String,
  val chapterCount: Int,
  val order: Int
)

data class Chapter(
  val id: String,
  val subjectId: String,
  val name: LocalizedString,
  val slug: String,
  val order: Int,
  val topicCount: Int,
  val estimatedMinutes: Int,
  val isPro: Boolean
)

data class Topic(
  val id: String,
  val chapterId: String,
  val name: LocalizedString,
  val order: Int,
  val contentType: String,     // "VIDEO" | "PDF" | "ARTICLE" | "QUIZ"
  val durationMinutes: Int,
  val videoUrl: String?,       // HLS
  val pdfUrl: String?,
  val transcriptUrl: String?,
  val captionsUrl: String?,    // WebVTT
  val summary: String?,
  val isPro: Boolean,
  val updatedAt: Instant
)

data class LocalizedString(val en: String, val hi: String?, val other: Map<String, String> = emptyMap())
```

## Tests & Questions
```kotlin
data class Test(
  val id: String,
  val kind: String,            // "FULL" | "SECTIONAL" | "PYQ" | "LIVE" | "DAILY_QUIZ"
  val title: LocalizedString,
  val pattern: String,         // "NTPC_CBT1" | "NTPC_CBT2"
  val durationSec: Int,
  val sections: List<TestSection>,
  val totalQuestions: Int,
  val totalMarks: Double,
  val negativeMarking: Double, // 0.33 for NTPC
  val isPro: Boolean,
  val liveStartAt: Instant?,   // null = on-demand
  val attemptsCount: Int,      // public counter
  val difficulty: String,      // "EASY" | "MODERATE" | "HARD" | "ACTUAL"
  val createdAt: Instant,
  val publishedAt: Instant
)

data class TestSection(
  val id: String,
  val subjectId: String,
  val name: String,
  val questionCount: Int,
  val marksPerQ: Double
)

data class Question(
  val id: String,
  val testId: String?,
  val sectionId: String?,
  val topicId: String?,
  val stem: LocalizedString,
  val options: List<Option>,   // A,B,C,D
  val correctKey: String,      // "A"|"B"|"C"|"D"
  val solution: LocalizedString,
  val solutionVideoUrl: String?,
  val difficulty: String,
  val tags: List<String>,
  val appearedIn: List<String> // ["NTPC-2021-Shift-3"]
)

data class Option(val key: String, val text: LocalizedString)
```

## Attempts
```kotlin
data class TestAttempt(
  val id: String,
  val userId: String,
  val testId: String,
  val startedAt: Instant,
  val submittedAt: Instant?,
  val durationUsedSec: Int,
  val answers: Map<String, AttemptAnswer>, // questionId -> answer
  val score: Double,
  val correctCount: Int,
  val wrongCount: Int,
  val skippedCount: Int,
  val sectionScores: Map<String, Double>,
  val rank: Int?,
  val percentile: Double?,
  val syncState: String        // "PENDING" | "SYNCED" (for offline)
)

data class AttemptAnswer(
  val selectedKey: String?,
  val markedForReview: Boolean,
  val timeSpentSec: Int
)
```

## Dynamic content
```kotlin
data class Article(
  val id: String,
  val slug: String,
  val title: LocalizedString,
  val summary: LocalizedString,
  val body: LocalizedString,        // Markdown
  val coverImageUrl: String?,
  val audioUrl: String?,            // TTS or recorded
  val category: String,             // "NATIONAL" | "INTL" | "ECONOMY" | "SPORTS" | "RAILWAYS" | "AWARDS"
  val tags: List<String>,
  val publishedAt: Instant,
  val readMinutes: Int,
  val likelyQuestions: List<String>,// linked Question.ids
  val source: String,
  val priority: Int                 // 0=normal, 1=hot, 2=hero
)

data class ExamNotification(
  val id: String,
  val title: LocalizedString,
  val exam: String,                 // "NTPC_2026"
  val publishedAt: Instant,
  val officialPdfUrl: String,
  val applyStart: LocalDate?,
  val applyEnd: LocalDate?,
  val examStartWindow: String,
  val feeText: LocalizedString,
  val eligibilityText: LocalizedString,
  val ageMin: Int,
  val ageMax: Int,
  val posts: List<ExamPost>,
  val totalVacancies: Int
)

data class ExamPost(val name: String, val level: String, val vacancies: Int)

data class AppNotification(
  val id: String,
  val userId: String,
  val kind: String,    // "EXAM" | "RESULT" | "CONTENT" | "STREAK" | "DOUBT_REPLY"
  val title: String,
  val body: String,
  val deeplink: String,
  val iconKey: String,
  val readAt: Instant?,
  val createdAt: Instant,
  val pinned: Boolean
)

data class CutoffPrediction(
  val exam: String,
  val category: String,     // UR, OBC, SC, ST, EWS
  val expectedMin: Double,
  val expectedMax: Double,
  val sampleSize: Int,
  val updatedAt: Instant
)
```

## Community
```kotlin
data class Doubt(
  val id: String,
  val userId: String,
  val subjectId: String,
  val topicId: String?,
  val body: String,
  val imageUrls: List<String>,
  val tags: List<String>,
  val answerCount: Int,
  val likeCount: Int,
  val resolvedAnswerId: String?,
  val trending: Boolean,
  val createdAt: Instant
)

data class DoubtAnswer(
  val id: String,
  val doubtId: String,
  val userId: String,
  val body: String,
  val imageUrls: List<String>,
  val likeCount: Int,
  val markedHelpful: Boolean,
  val isVerifiedExpert: Boolean,
  val createdAt: Instant
)
```

## Firestore collections layout
```
/users/{userId}
/users/{userId}/attempts/{attemptId}
/users/{userId}/bookmarks/{itemId}
/users/{userId}/downloads/{itemId}
/users/{userId}/notifications/{notifId}
/subjects/{subjectId}
/subjects/{subjectId}/chapters/{chapterId}
/subjects/{subjectId}/chapters/{chapterId}/topics/{topicId}
/tests/{testId}
/tests/{testId}/questions/{questionId}
/articles/{articleId}
/examNotifications/{notifId}
/doubts/{doubtId}
/doubts/{doubtId}/answers/{answerId}
/leaderboards/{period}/entries/{userId}   // period: daily|weekly|alltime
/cutoffs/{exam}
/config/appConfig                        // Remote Config mirror
```
