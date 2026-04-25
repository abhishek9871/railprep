package com.railprep.domain.repository

import com.railprep.domain.model.ExamTarget
import com.railprep.domain.model.Question
import com.railprep.domain.model.QuestionSearchResult
import com.railprep.domain.model.Test
import com.railprep.domain.model.TestSection
import com.railprep.domain.model.TopicAccuracy
import com.railprep.domain.util.DomainResult

interface TestsRepository {
    suspend fun listForTarget(examTarget: ExamTarget): DomainResult<List<Test>>
    suspend fun get(testId: String): DomainResult<Test>
    suspend fun listSections(testId: String): DomainResult<List<TestSection>>
    /** Questions + their options for every section of the test, ready to serve the player. */
    suspend fun listQuestions(testId: String): DomainResult<List<Question>>
    suspend fun search(query: String): DomainResult<List<Test>>
    suspend fun searchQuestions(
        query: String,
        filters: Map<String, String> = emptyMap(),
        limit: Int = 50,
        offset: Int = 0,
    ): DomainResult<List<QuestionSearchResult>>

    suspend fun getTopicAccuracy(): DomainResult<List<TopicAccuracy>>
}
