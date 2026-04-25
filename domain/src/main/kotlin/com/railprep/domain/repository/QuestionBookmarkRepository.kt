package com.railprep.domain.repository

import com.railprep.domain.model.QuestionBookmark
import com.railprep.domain.util.DomainResult

interface QuestionBookmarkRepository {
    suspend fun list(): DomainResult<List<QuestionBookmark>>
    suspend fun listStates(questionIds: List<String>): DomainResult<Map<String, String?>>
    suspend fun add(questionId: String, note: String? = null): DomainResult<Unit>
    suspend fun remove(questionId: String): DomainResult<Unit>
    suspend fun updateNote(questionId: String, note: String?): DomainResult<Unit>
    suspend fun isBookmarked(questionId: String): DomainResult<Boolean>
}
