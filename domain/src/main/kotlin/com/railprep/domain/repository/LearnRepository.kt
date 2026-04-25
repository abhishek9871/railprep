package com.railprep.domain.repository

import com.railprep.domain.model.Chapter
import com.railprep.domain.model.Subject
import com.railprep.domain.model.Topic
import com.railprep.domain.util.DomainResult

interface LearnRepository {
    suspend fun listSubjects(): DomainResult<List<Subject>>
    suspend fun listChapters(subjectId: String): DomainResult<List<Chapter>>
    suspend fun listTopics(chapterId: String): DomainResult<List<Topic>>
    suspend fun getTopic(topicId: String): DomainResult<Topic>

    /** Active topics whose `tags` array overlaps any of the given tags.
     *  Backs the post-submit weak-topic recommendation card on Results. */
    suspend fun findByAnyTag(tags: List<String>, limit: Int = 10): DomainResult<List<Topic>>

    /** Marks a topic's status='stale' when the YouTube player or PDF fetch fails. */
    suspend fun reportStale(topicId: String): DomainResult<Unit>
}
