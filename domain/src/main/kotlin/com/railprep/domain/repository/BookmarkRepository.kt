package com.railprep.domain.repository

import com.railprep.domain.model.Topic
import com.railprep.domain.util.DomainResult

interface BookmarkRepository {
    /** Returns all topics the current user has bookmarked, in recent-first order. */
    suspend fun listBookmarks(): DomainResult<List<Topic>>

    suspend fun addBookmark(topicId: String): DomainResult<Unit>
    suspend fun removeBookmark(topicId: String): DomainResult<Unit>
    suspend fun isBookmarked(topicId: String): DomainResult<Boolean>
}
