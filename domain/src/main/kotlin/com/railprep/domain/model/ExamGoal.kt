package com.railprep.domain.model

import kotlinx.datetime.LocalDate

/**
 * Incoming data from the Goal screen — the user's completed answers.
 * Persisted as a [Profile] update by ProfileRepository.
 */
data class ExamGoal(
    val examTarget: ExamTarget,
    val examTargetDate: LocalDate,
    val dailyMinutes: Int,
    val qualification: Qualification,
    val category: Category,
    val dob: LocalDate,
    val displayName: String?,
)
