package com.railprep.domain.model

import kotlinx.datetime.Instant

data class QuestionBookmark(
    val userId: String,
    val questionId: String,
    val bookmarkedAt: Instant,
    val note: String?,
    val question: Question,
    val section: TestSection,
    val test: Test,
)
