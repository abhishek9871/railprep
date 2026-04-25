package com.railprep.data.remote.supabase

import com.railprep.domain.model.DigestAnswer
import com.railprep.domain.model.DigestAttempt
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class DailyDigestDto(
    @SerialName("digest_date") val digestDate: LocalDate,
    @SerialName("question_ids") val questionIds: List<String>,
    @SerialName("section_plan") val sectionPlan: JsonElement? = null,
    @SerialName("generated_at") val generatedAt: Instant,
)

@Serializable
data class DigestAttemptDto(
    @SerialName("user_id") val userId: String,
    @SerialName("digest_date") val digestDate: LocalDate,
    @SerialName("answers") val answers: List<DigestAnswerDto> = emptyList(),
    @SerialName("correct_count") val correctCount: Int,
    @SerialName("total") val total: Int,
    @SerialName("submitted_at") val submittedAt: Instant,
)

@Serializable
data class DigestAnswerDto(
    @SerialName("question_id") val questionId: String,
    @SerialName("selected_option_id") val selectedOptionId: String? = null,
    @SerialName("is_correct") val isCorrect: Boolean = false,
)

fun DigestAnswerDto.toDomain() = DigestAnswer(
    questionId = questionId,
    selectedOptionId = selectedOptionId,
    isCorrect = isCorrect,
)

fun DigestAttemptDto.toDomain() = DigestAttempt(
    userId = userId,
    date = digestDate,
    answers = answers.map { it.toDomain() },
    correctCount = correctCount,
    total = total,
    submittedAt = submittedAt,
)
