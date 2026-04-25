package com.railprep.data.repository

import com.railprep.core.common.coroutines.DispatcherProvider
import com.railprep.data.remote.supabase.ChapterDto
import com.railprep.data.remote.supabase.SubjectDto
import com.railprep.data.remote.supabase.TopicDto
import com.railprep.data.remote.supabase.toDomain
import com.railprep.domain.model.Chapter
import com.railprep.domain.model.Subject
import com.railprep.domain.model.Topic
import com.railprep.domain.repository.LearnRepository
import com.railprep.domain.util.DomainError
import com.railprep.domain.util.DomainResult
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LearnRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val dispatchers: DispatcherProvider,
) : LearnRepository {

    override suspend fun listSubjects(): DomainResult<List<Subject>> = withContext(dispatchers.io) {
        runCatchingNetwork("subjects") {
            supabase.postgrest.from("subjects")
                .select(columns = Columns.ALL) {
                    order("display_order", Order.ASCENDING)
                }
                .decodeList<SubjectDto>()
                .map { it.toDomain() }
        }
    }

    override suspend fun listChapters(subjectId: String): DomainResult<List<Chapter>> =
        withContext(dispatchers.io) {
            runCatchingNetwork("chapters") {
                supabase.postgrest.from("chapters")
                    .select(columns = Columns.ALL) {
                        filter { eq("subject_id", subjectId) }
                        order("display_order", Order.ASCENDING)
                    }
                    .decodeList<ChapterDto>()
                    .map { it.toDomain() }
            }
        }

    override suspend fun listTopics(chapterId: String): DomainResult<List<Topic>> =
        withContext(dispatchers.io) {
            runCatchingNetwork("topics") {
                supabase.postgrest.from("topics")
                    .select(columns = Columns.ALL) {
                        filter {
                            eq("chapter_id", chapterId)
                            eq("status", "active")
                        }
                        order("display_order", Order.ASCENDING)
                    }
                    .decodeList<TopicDto>()
                    .map { it.toDomain() }
            }
        }

    override suspend fun getTopic(topicId: String): DomainResult<Topic> =
        withContext(dispatchers.io) {
            runCatchingNetwork("topic") {
                supabase.postgrest.from("topics")
                    .select(columns = Columns.ALL) {
                        filter { eq("id", topicId) }
                        limit(1)
                        single()
                    }
                    .decodeAs<TopicDto>()
                    .toDomain()
            }
        }

    override suspend fun findByAnyTag(tags: List<String>, limit: Int): DomainResult<List<Topic>> =
        withContext(dispatchers.io) {
            if (tags.isEmpty()) return@withContext DomainResult.Success(emptyList())
            runCatchingNetwork("topics/findByAnyTag") {
                // PostgREST overlap operator on text[] columns. The PG query becomes
                //   topics.tags && '{tag1,tag2,...}'::text[]
                // which uses the GIN index added in 0007.
                val tagLiteral = tags.joinToString(prefix = "{", postfix = "}") {
                    it.replace(",", "")
                }
                supabase.postgrest.from("topics")
                    .select(columns = Columns.ALL) {
                        filter {
                            eq("status", "active")
                            filter("tags", io.github.jan.supabase.postgrest.query.filter.FilterOperator.OV, tagLiteral)
                        }
                        order("display_order", Order.ASCENDING)
                        limit(limit.toLong())
                    }
                    .decodeList<TopicDto>()
                    .map { it.toDomain() }
            }
        }

    override suspend fun reportStale(topicId: String): DomainResult<Unit> =
        withContext(dispatchers.io) {
            // Best-effort: if the current user's JWT doesn't grant update on topics (they don't —
            // only service_role does), this returns Failure but the app doesn't surface it. The
            // pipeline's HEAD-checker is the canonical way to flip status; this is just a hint.
            runCatchingNetwork("reportStale") {
                supabase.postgrest.from("topics")
                    .update(mapOf("status" to "stale")) {
                        filter { eq("id", topicId) }
                    }
                Unit
            }
        }

    private inline fun <T> runCatchingNetwork(op: String, block: () -> T): DomainResult<T> =
        try {
            DomainResult.Success(block())
        } catch (t: Throwable) {
            DomainResult.Failure(DomainError.Network("$op failed: ${t.message}", t))
        }
}
