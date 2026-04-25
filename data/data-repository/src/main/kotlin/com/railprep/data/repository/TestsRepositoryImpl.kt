package com.railprep.data.repository

import com.railprep.core.common.coroutines.DispatcherProvider
import com.railprep.data.remote.supabase.QuestionDto
import com.railprep.data.remote.supabase.QuestionSearchResultDto
import com.railprep.data.remote.supabase.TestDto
import com.railprep.data.remote.supabase.TestSectionDto
import com.railprep.data.remote.supabase.TopicAccuracyDto
import com.railprep.data.remote.supabase.toDomain
import com.railprep.domain.model.ExamTarget
import com.railprep.domain.model.Question
import com.railprep.domain.model.QuestionSearchResult
import com.railprep.domain.model.Test
import com.railprep.domain.model.TestSection
import com.railprep.domain.model.TopicAccuracy
import com.railprep.domain.repository.TestsRepository
import com.railprep.domain.util.DomainError
import com.railprep.domain.util.DomainResult
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestsRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val dispatchers: DispatcherProvider,
) : TestsRepository {

    override suspend fun listForTarget(examTarget: ExamTarget): DomainResult<List<Test>> =
        withContext(dispatchers.io) {
            runCatchingNetwork("tests/listForTarget") {
                supabase.postgrest.from("tests")
                    .select(columns = Columns.ALL) {
                        filter {
                            eq("exam_target", examTarget.wire)
                            eq("status", "active")
                        }
                        order("published_at", Order.DESCENDING)
                    }
                    .decodeList<TestDto>()
                    .map { it.toDomain() }
            }
        }

    override suspend fun get(testId: String): DomainResult<Test> = withContext(dispatchers.io) {
        runCatchingNetwork("tests/get") {
            supabase.postgrest.from("tests")
                .select(columns = Columns.ALL) {
                    filter { eq("id", testId) }
                    limit(1)
                    single()
                }
                .decodeAs<TestDto>()
                .toDomain()
        }
    }

    override suspend fun listSections(testId: String): DomainResult<List<TestSection>> =
        withContext(dispatchers.io) {
            runCatchingNetwork("tests/listSections") {
                supabase.postgrest.from("test_sections")
                    .select(columns = Columns.ALL) {
                        filter { eq("test_id", testId) }
                        order("display_order", Order.ASCENDING)
                    }
                    .decodeList<TestSectionDto>()
                    .map { it.toDomain() }
            }
        }

    override suspend fun listQuestions(testId: String): DomainResult<List<Question>> =
        withContext(dispatchers.io) {
            runCatchingNetwork("tests/listQuestions") {
                // Embed options via PostgREST: questions?select=*,options(*)&section_id=in.(...)
                val sectionIds = supabase.postgrest.from("test_sections")
                    .select(columns = Columns.list("id")) {
                        filter { eq("test_id", testId) }
                    }
                    .decodeList<SectionIdRow>()
                    .map { it.id }

                if (sectionIds.isEmpty()) return@runCatchingNetwork emptyList()

                supabase.postgrest.from("questions")
                    .select(columns = Columns.raw("*,options(*)")) {
                        filter {
                            isIn("section_id", sectionIds)
                            eq("status", "active")
                        }
                        order("section_id", Order.ASCENDING)
                        order("display_order", Order.ASCENDING)
                    }
                    .decodeList<QuestionDto>()
                    .map { it.toDomain() }
            }
        }

    override suspend fun search(query: String): DomainResult<List<Test>> =
        withContext(dispatchers.io) {
            runCatchingNetwork("tests/search") {
                val pattern = "%${query.replace("%", "\\%").replace("_", "\\_")}%"
                supabase.postgrest.from("tests")
                    .select(columns = Columns.ALL) {
                        filter {
                            eq("status", "active")
                            ilike("title_en", pattern)
                        }
                        order("published_at", Order.DESCENDING)
                        limit(50)
                    }
                    .decodeList<TestDto>()
                    .map { it.toDomain() }
            }
        }

    override suspend fun searchQuestions(
        query: String,
        filters: Map<String, String>,
        limit: Int,
        offset: Int,
    ): DomainResult<List<QuestionSearchResult>> =
        withContext(dispatchers.io) {
            runCatchingNetwork("tests/searchQuestions") {
                supabase.postgrest.rpc(
                    "search_questions",
                    buildJsonObject {
                        put("p_query", query)
                        put("p_filters", filters.toJsonObject())
                        put("p_limit", limit)
                        put("p_offset", offset)
                    },
                )
                    .decodeList<QuestionSearchResultDto>()
                    .map { it.toDomain() }
            }
        }

    override suspend fun getTopicAccuracy(): DomainResult<List<TopicAccuracy>> =
        withContext(dispatchers.io) {
            runCatchingNetwork("tests/getTopicAccuracy") {
                supabase.postgrest.rpc("get_topic_accuracy")
                    .decodeList<TopicAccuracyDto>()
                    .map { it.toDomain() }
            }
        }

    @kotlinx.serialization.Serializable
    private data class SectionIdRow(val id: String)

    private inline fun <T> runCatchingNetwork(op: String, block: () -> T): DomainResult<T> =
        try {
            DomainResult.Success(block())
        } catch (t: Throwable) {
            DomainResult.Failure(DomainError.Network("$op failed: ${t.message}", t))
        }

    private fun Map<String, String>.toJsonObject(): JsonObject = buildJsonObject {
        for ((key, value) in this@toJsonObject) {
            if (value.isNotBlank()) put(key, value)
        }
    }
}
