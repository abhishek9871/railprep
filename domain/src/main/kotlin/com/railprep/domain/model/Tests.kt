package com.railprep.domain.model

import kotlinx.datetime.Instant

enum class TestKind { CBT1_FULL, CBT2_FULL, SECTIONAL, PYQ, DAILY_DIGEST, PYQ_LINK }

/** `external_url` / `source_language` / `source_attribution` are meaningful only
 *  for kind=PYQ_LINK rows — real paper PDF rendered on-device via PdfViewer.
 *  For all other kinds these are null. */
enum class PaperLanguage { EN, HI, BILINGUAL }

// ExamTarget (NtpcCbt1 / NtpcCbt2) is declared in Profile.kt — reuse it.

enum class QuestionDifficulty { EASY, MEDIUM, HARD }

enum class SubjectHint { MATH, REASON, GA, GS, ENG, MIXED }

enum class AttemptStatus { IN_PROGRESS, SUBMITTED, EXPIRED, ABANDONED }

enum class ContentStatus { ACTIVE, STALE, DRAFT }

data class Test(
    val id: String,
    val slug: String,
    val titleEn: String,
    val titleHi: String?,
    val kind: TestKind,
    val examTarget: ExamTarget,
    val totalQuestions: Int,
    val totalMinutes: Int,
    val negativeMarkingFraction: Float,
    val isPro: Boolean,
    val status: ContentStatus,
    val publishedAt: Instant,
    // Populated only when kind = PYQ_LINK. See PaperLanguage / docs/phase3-research.md §2.2.
    val externalUrl: String? = null,
    val sourceLanguage: PaperLanguage? = null,
    val sourceAttribution: String? = null,
)

data class TestSection(
    val id: String,
    val testId: String,
    val titleEn: String,
    val titleHi: String?,
    val questionCount: Int,
    val displayOrder: Int,
    val subjectHint: SubjectHint,
)

data class Question(
    val id: String,
    val sectionId: String,
    val displayOrder: Int,
    val stemEn: String,
    val stemHi: String?,
    val explanationEn: String?,
    val explanationHi: String?,
    /** Two-layer pedagogy (Stage 4 originals): method = exam-shortcut; concept = first-principles
     *  derivation. Either may be null on legacy rows that pre-date 0005_pyq_links_and_explanations. */
    val explanationMethodEn: String?,
    val explanationConceptEn: String?,
    val explanationMethodHi: String?,
    val explanationConceptHi: String?,
    val difficulty: QuestionDifficulty,
    val tags: List<String>,
    val source: String,
    val license: String,
    val status: ContentStatus,
    val options: List<Option>,
)

data class Option(
    val id: String,
    val questionId: String,
    val label: String,
    val textEn: String,
    val textHi: String?,
    val isCorrect: Boolean,
    /** Why a student picks this wrong option — surfaces under the option in Review.
     *  Null for the correct option and for legacy options without trap analysis. */
    val trapReasonEn: String? = null,
    val trapReasonHi: String? = null,
)

/**
 * Per-section result lifted from [Attempt.sectionBreakdown].
 * Shape is locked in 0004_tests.sql; do not invent parallel shapes.
 */
data class SectionBreakdown(
    val sectionId: String,
    val subjectHint: SubjectHint,
    val attempted: Int,
    val correct: Int,
    val wrong: Int,
    val skipped: Int,
    val score: Float,
    val maxScore: Float,
)

data class Attempt(
    val id: String,
    val userId: String,
    val testId: String,
    val startedAt: Instant,
    val submittedAt: Instant?,
    val serverDeadlineAt: Instant,
    val score: Float?,
    val maxScore: Float?,
    val correctCount: Int?,
    val wrongCount: Int?,
    val skippedCount: Int?,
    val sectionBreakdown: List<SectionBreakdown>?,
    val status: AttemptStatus,
)

data class AttemptAnswer(
    val attemptId: String,
    val questionId: String,
    val selectedOptionId: String?,
    val flagged: Boolean,
    val answeredAt: Instant,
)
