package com.railprep.data.remote.supabase

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuestionBookmarkRowDto(
    @SerialName("user_id") val userId: String,
    @SerialName("question_id") val questionId: String,
    @SerialName("bookmarked_at") val bookmarkedAt: Instant,
    @SerialName("note") val note: String? = null,
)

@Serializable
data class QuestionBookmarkInsertDto(
    @SerialName("user_id") val userId: String,
    @SerialName("question_id") val questionId: String,
    @SerialName("note") val note: String? = null,
)

@Serializable
data class QuestionBookmarkNotePatchDto(
    @SerialName("note") val note: String? = null,
)
