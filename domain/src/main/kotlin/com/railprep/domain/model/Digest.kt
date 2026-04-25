package com.railprep.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

/**
 * The 10 questions that make up a calendar day's digest. Picked once per day by
 * `ensure_today_digest()` (lazy, on the first authenticated read of the day).
 *
 * `questions` are full Question rows with options; the server stores only the
 * `question_ids` array, so the client resolves them in a follow-up read.
 */
data class Digest(
    val date: LocalDate,
    val questions: List<Question>,
    val sectionPlan: Map<String, Int>,
)

/** Server-scored result of one user's submission for one day. */
data class DigestAttempt(
    val userId: String,
    val date: LocalDate,
    val answers: List<DigestAnswer>,
    val correctCount: Int,
    val total: Int,
    val submittedAt: Instant,
)

data class DigestAnswer(
    val questionId: String,
    val selectedOptionId: String?,
    val isCorrect: Boolean,
)
