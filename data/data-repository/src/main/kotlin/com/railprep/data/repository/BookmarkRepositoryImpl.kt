package com.railprep.data.repository

import com.railprep.core.common.coroutines.DispatcherProvider
import com.railprep.data.remote.supabase.BookmarkRowDto
import com.railprep.data.remote.supabase.TopicDto
import com.railprep.data.remote.supabase.toDomain
import com.railprep.domain.model.Topic
import com.railprep.domain.repository.BookmarkRepository
import com.railprep.domain.util.DomainError
import com.railprep.domain.util.DomainResult
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val dispatchers: DispatcherProvider,
) : BookmarkRepository {

    override suspend fun listBookmarks(): DomainResult<List<Topic>> = withContext(dispatchers.io) {
        val userId = currentUserIdOrFail() ?: return@withContext DomainResult.Failure(
            DomainError.Auth("No active session"),
        )
        runCatching {
            // Two round trips: bookmark IDs then topic rows. Acceptable for Phase 2 (bookmarks
            // lists are small); Phase 3 can swap to a Postgres view or RPC.
            val rows = supabase.postgrest.from("bookmarks")
                .select(columns = Columns.raw("topic_id")) {
                    filter { eq("user_id", userId) }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<BookmarkRowDto>()
            if (rows.isEmpty()) return@runCatching emptyList<Topic>()
            val ids = rows.map { it.topicId }
            val topics = supabase.postgrest.from("topics")
                .select(columns = Columns.ALL) {
                    filter {
                        isIn("id", ids)
                        eq("status", "active")
                    }
                }
                .decodeList<TopicDto>()
                .map { it.toDomain(bookmarked = true) }
            // Preserve bookmark creation order (most recent first).
            topics.sortedBy { ids.indexOf(it.id) }
        }.fold(
            onSuccess = { DomainResult.Success(it) },
            onFailure = { DomainResult.Failure(DomainError.Network("bookmarks failed: ${it.message}", it)) },
        )
    }

    override suspend fun addBookmark(topicId: String): DomainResult<Unit> =
        withContext(dispatchers.io) {
            val userId = currentUserIdOrFail() ?: return@withContext DomainResult.Failure(
                DomainError.Auth("No active session"),
            )
            runCatching {
                supabase.postgrest.from("bookmarks").insert(
                    mapOf("user_id" to userId, "topic_id" to topicId),
                )
                Unit
            }.fold(
                onSuccess = { DomainResult.Success(Unit) },
                onFailure = { DomainResult.Failure(DomainError.Network("addBookmark: ${it.message}", it)) },
            )
        }

    override suspend fun removeBookmark(topicId: String): DomainResult<Unit> =
        withContext(dispatchers.io) {
            val userId = currentUserIdOrFail() ?: return@withContext DomainResult.Failure(
                DomainError.Auth("No active session"),
            )
            runCatching {
                supabase.postgrest.from("bookmarks").delete {
                    filter {
                        eq("user_id", userId)
                        eq("topic_id", topicId)
                    }
                }
                Unit
            }.fold(
                onSuccess = { DomainResult.Success(Unit) },
                onFailure = { DomainResult.Failure(DomainError.Network("removeBookmark: ${it.message}", it)) },
            )
        }

    override suspend fun isBookmarked(topicId: String): DomainResult<Boolean> =
        withContext(dispatchers.io) {
            val userId = currentUserIdOrFail() ?: return@withContext DomainResult.Failure(
                DomainError.Auth("No active session"),
            )
            runCatching {
                val rows = supabase.postgrest.from("bookmarks")
                    .select(columns = Columns.raw("topic_id")) {
                        filter {
                            eq("user_id", userId)
                            eq("topic_id", topicId)
                        }
                        limit(1)
                    }
                    .decodeList<BookmarkRowDto>()
                rows.isNotEmpty()
            }.fold(
                onSuccess = { DomainResult.Success(it) },
                onFailure = { DomainResult.Failure(DomainError.Network("isBookmarked: ${it.message}", it)) },
            )
        }

    private fun currentUserIdOrFail(): String? =
        supabase.auth.currentSessionOrNull()?.user?.id
}
