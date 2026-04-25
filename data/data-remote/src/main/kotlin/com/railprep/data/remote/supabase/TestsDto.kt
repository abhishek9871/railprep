package com.railprep.data.remote.supabase

import com.railprep.domain.model.Attempt
import com.railprep.domain.model.AttemptAnswer
import com.railprep.domain.model.AttemptStatus
import com.railprep.domain.model.ContentStatus
import com.railprep.domain.model.ExamTarget
import com.railprep.domain.model.Option
import com.railprep.domain.model.PaperLanguage
import com.railprep.domain.model.Question
import com.railprep.domain.model.QuestionDifficulty
import com.railprep.domain.model.SectionBreakdown
import com.railprep.domain.model.SubjectHint
import com.railprep.domain.model.Test
import com.railprep.domain.model.TestKind
import com.railprep.domain.model.TestSection
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// -------- Content DTOs (tests / test_sections / questions / options) --------

@Serializable
data class TestDto(
    @SerialName("id") val id: String,
    @SerialName("slug") val slug: String,
    @SerialName("title_en") val titleEn: String,
    @SerialName("title_hi") val titleHi: String? = null,
    @SerialName("kind") val kind: String,
    @SerialName("exam_target") val examTarget: String,
    @SerialName("total_questions") val totalQuestions: Int,
    @SerialName("total_minutes") val totalMinutes: Int,
    @SerialName("negative_marking_fraction") val negativeMarkingFraction: Float,
    @SerialName("is_pro") val isPro: Boolean = false,
    @SerialName("status") val status: String = "active",
    @SerialName("published_at") val publishedAt: Instant,
    @SerialName("external_url") val externalUrl: String? = null,
    @SerialName("source_language") val sourceLanguage: String? = null,
    @SerialName("source_attribution") val sourceAttribution: String? = null,
)

@Serializable
data class TestSectionDto(
    @SerialName("id") val id: String,
    @SerialName("test_id") val testId: String,
    @SerialName("title_en") val titleEn: String,
    @SerialName("title_hi") val titleHi: String? = null,
    @SerialName("question_count") val questionCount: Int,
    @SerialName("display_order") val displayOrder: Int = 0,
    @SerialName("subject_hint") val subjectHint: String,
)

@Serializable
data class OptionDto(
    @SerialName("id") val id: String,
    @SerialName("question_id") val questionId: String,
    @SerialName("label") val label: String,
    @SerialName("text_en") val textEn: String,
    @SerialName("text_hi") val textHi: String? = null,
    @SerialName("is_correct") val isCorrect: Boolean = false,
    @SerialName("trap_reason_en") val trapReasonEn: String? = null,
    @SerialName("trap_reason_hi") val trapReasonHi: String? = null,
)

@Serializable
data class QuestionDto(
    @SerialName("id") val id: String,
    @SerialName("section_id") val sectionId: String,
    @SerialName("display_order") val displayOrder: Int = 0,
    @SerialName("stem_en") val stemEn: String,
    @SerialName("stem_hi") val stemHi: String? = null,
    @SerialName("explanation_en") val explanationEn: String? = null,
    @SerialName("explanation_hi") val explanationHi: String? = null,
    @SerialName("explanation_method_en") val explanationMethodEn: String? = null,
    @SerialName("explanation_concept_en") val explanationConceptEn: String? = null,
    @SerialName("explanation_method_hi") val explanationMethodHi: String? = null,
    @SerialName("explanation_concept_hi") val explanationConceptHi: String? = null,
    @SerialName("difficulty") val difficulty: String = "MEDIUM",
    @SerialName("tags") val tags: List<String> = emptyList(),
    @SerialName("source") val source: String,
    @SerialName("license") val license: String = "ORIGINAL",
    @SerialName("status") val status: String = "active",
    // Nested via PostgREST embedding:  select=*,options(*)
    @SerialName("options") val options: List<OptionDto> = emptyList(),
)

// -------- Attempt DTOs (per-user, server-scored) --------

@Serializable
data class SectionTallyDto(
    @SerialName("section_id") val sectionId: String,
    @SerialName("subject_hint") val subjectHint: String,
    @SerialName("attempted") val attempted: Int,
    @SerialName("correct") val correct: Int,
    @SerialName("wrong") val wrong: Int,
    @SerialName("skipped") val skipped: Int,
    @SerialName("score") val score: Float,
    @SerialName("max_score") val maxScore: Float,
)

@Serializable
data class SectionBreakdownDto(
    @SerialName("sections") val sections: List<SectionTallyDto> = emptyList(),
)

@Serializable
data class AttemptDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("test_id") val testId: String,
    @SerialName("started_at") val startedAt: Instant,
    @SerialName("submitted_at") val submittedAt: Instant? = null,
    @SerialName("server_deadline_at") val serverDeadlineAt: Instant,
    @SerialName("score") val score: Float? = null,
    @SerialName("max_score") val maxScore: Float? = null,
    @SerialName("correct_count") val correctCount: Int? = null,
    @SerialName("wrong_count") val wrongCount: Int? = null,
    @SerialName("skipped_count") val skippedCount: Int? = null,
    @SerialName("section_breakdown") val sectionBreakdown: SectionBreakdownDto? = null,
    @SerialName("status") val status: String,
)

@Serializable
data class AttemptAnswerDto(
    @SerialName("attempt_id") val attemptId: String,
    @SerialName("question_id") val questionId: String,
    @SerialName("selected_option_id") val selectedOptionId: String? = null,
    @SerialName("flagged") val flagged: Boolean = false,
    @SerialName("answered_at") val answeredAt: Instant,
)

@Serializable
data class AttemptAnswerUpsertDto(
    @SerialName("attempt_id") val attemptId: String,
    @SerialName("question_id") val questionId: String,
    @SerialName("selected_option_id") val selectedOptionId: String? = null,
    @SerialName("flagged") val flagged: Boolean = false,
)

// RPC parameter payloads use buildJsonObject inline at the call site (supabase-kt 3.x
// takes JsonObject directly, not @Serializable DTOs).

// -------- Domain mappers --------

fun TestDto.toDomain() = Test(
    id = id,
    slug = slug,
    titleEn = titleEn,
    titleHi = titleHi,
    kind = runCatching { TestKind.valueOf(kind) }.getOrDefault(TestKind.SECTIONAL),
    examTarget = ExamTarget.fromWire(examTarget) ?: ExamTarget.NtpcCbt1,
    totalQuestions = totalQuestions,
    totalMinutes = totalMinutes,
    negativeMarkingFraction = negativeMarkingFraction,
    isPro = isPro,
    status = contentStatusFromWire(status),
    publishedAt = publishedAt,
    externalUrl = externalUrl,
    sourceLanguage = sourceLanguage?.let {
        when (it) {
            "en" -> PaperLanguage.EN
            "hi" -> PaperLanguage.HI
            "bilingual" -> PaperLanguage.BILINGUAL
            else -> null
        }
    },
    sourceAttribution = sourceAttribution,
)

fun TestSectionDto.toDomain() = TestSection(
    id = id,
    testId = testId,
    titleEn = titleEn,
    titleHi = titleHi,
    questionCount = questionCount,
    displayOrder = displayOrder,
    subjectHint = subjectHintFromWire(subjectHint),
)

fun OptionDto.toDomain() = Option(
    id = id,
    questionId = questionId,
    label = label,
    textEn = textEn,
    textHi = textHi,
    isCorrect = isCorrect,
    trapReasonEn = trapReasonEn,
    trapReasonHi = trapReasonHi,
)

fun QuestionDto.toDomain() = Question(
    id = id,
    sectionId = sectionId,
    displayOrder = displayOrder,
    stemEn = stemEn,
    stemHi = stemHi,
    explanationEn = explanationEn,
    explanationHi = explanationHi,
    explanationMethodEn = explanationMethodEn,
    explanationConceptEn = explanationConceptEn,
    explanationMethodHi = explanationMethodHi,
    explanationConceptHi = explanationConceptHi,
    difficulty = runCatching { QuestionDifficulty.valueOf(difficulty) }.getOrDefault(QuestionDifficulty.MEDIUM),
    tags = tags,
    source = source,
    license = license,
    status = contentStatusFromWire(status),
    options = options.sortedBy { it.label }.map { it.toDomain() },
)

fun SectionTallyDto.toDomain() = SectionBreakdown(
    sectionId = sectionId,
    subjectHint = subjectHintFromWire(subjectHint),
    attempted = attempted,
    correct = correct,
    wrong = wrong,
    skipped = skipped,
    score = score,
    maxScore = maxScore,
)

fun AttemptDto.toDomain() = Attempt(
    id = id,
    userId = userId,
    testId = testId,
    startedAt = startedAt,
    submittedAt = submittedAt,
    serverDeadlineAt = serverDeadlineAt,
    score = score,
    maxScore = maxScore,
    correctCount = correctCount,
    wrongCount = wrongCount,
    skippedCount = skippedCount,
    sectionBreakdown = sectionBreakdown?.sections?.map { it.toDomain() },
    status = runCatching { AttemptStatus.valueOf(status) }.getOrDefault(AttemptStatus.IN_PROGRESS),
)

fun AttemptAnswerDto.toDomain() = AttemptAnswer(
    attemptId = attemptId,
    questionId = questionId,
    selectedOptionId = selectedOptionId,
    flagged = flagged,
    answeredAt = answeredAt,
)

private fun contentStatusFromWire(s: String): ContentStatus = when (s) {
    "active" -> ContentStatus.ACTIVE
    "stale" -> ContentStatus.STALE
    "draft" -> ContentStatus.DRAFT
    else -> ContentStatus.ACTIVE
}

private fun subjectHintFromWire(s: String): SubjectHint = when (s) {
    "math" -> SubjectHint.MATH
    "reason" -> SubjectHint.REASON
    "ga" -> SubjectHint.GA
    "gs" -> SubjectHint.GS
    "eng" -> SubjectHint.ENG
    else -> SubjectHint.MIXED
}
