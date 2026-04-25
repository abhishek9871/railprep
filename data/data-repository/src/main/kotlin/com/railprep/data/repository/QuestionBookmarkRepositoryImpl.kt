package com.railprep.data.repository

import com.railprep.core.common.coroutines.DispatcherProvider
import com.railprep.data.remote.supabase.QuestionBookmarkInsertDto
import com.railprep.data.remote.supabase.QuestionBookmarkNotePatchDto
import com.railprep.data.remote.supabase.QuestionBookmarkRowDto
import com.railprep.data.remote.supabase.QuestionDto
import com.railprep.data.remote.supabase.TestDto
import com.railprep.data.remote.supabase.TestSectionDto
import com.railprep.data.remote.supabase.toDomain
import com.railprep.domain.model.QuestionBookmark
import com.railprep.domain.repository.QuestionBookmarkRepository
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
class QuestionBookmarkRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val dispatchers: DispatcherProvider,
) : QuestionBookmarkRepository {

    override suspend fun list(): DomainResult<List<QuestionBookmark>> =
        withContext(dispatchers.io) {
            val userId = currentUserIdOrFail() ?: return@withContext DomainResult.Failure(
                DomainError.Auth("No active session"),
            )
            runCatchingNetwork("question_bookmarks/list") {
                val rows = supabase.postgrest.from("question_bookmarks")
                    .select(columns = Columns.ALL) {
                        filter { eq("user_id", userId) }
                        order("bookmarked_at", Order.DESCENDING)
                    }
                    .decodeList<QuestionBookmarkRowDto>()

                if (rows.isEmpty()) return@runCatchingNetwork emptyList()

                val questionIds = rows.map { it.questionId }
                val questions = supabase.postgrest.from("questions")
                    .select(columns = Columns.raw("*,options(*)")) {
                        filter {
                            isIn("id", questionIds)
                            eq("status", "active")
                        }
                    }
                    .decodeList<QuestionDto>()
                    .associateBy { it.id }

                val sectionIds = questions.values.map { it.sectionId }.distinct()
                val sections = if (sectionIds.isEmpty()) {
                    emptyMap()
                } else {
                    supabase.postgrest.from("test_sections")
                        .select(columns = Columns.ALL) {
                            filter { isIn("id", sectionIds) }
                        }
                        .decodeList<TestSectionDto>()
                        .associateBy { it.id }
                }

                val testIds = sections.values.map { it.testId }.distinct()
                val tests = if (testIds.isEmpty()) {
                    emptyMap()
                } else {
                    supabase.postgrest.from("tests")
                        .select(columns = Columns.ALL) {
                            filter { isIn("id", testIds) }
                        }
                        .decodeList<TestDto>()
                        .associateBy { it.id }
                }

                rows.mapNotNull { row ->
                    val questionDto = questions[row.questionId] ?: return@mapNotNull null
                    val sectionDto = sections[questionDto.sectionId] ?: return@mapNotNull null
                    val testDto = tests[sectionDto.testId] ?: return@mapNotNull null
                    QuestionBookmark(
                        userId = row.userId,
                        questionId = row.questionId,
                        bookmarkedAt = row.bookmarkedAt,
                        note = row.note,
                        question = questionDto.toDomain(),
                        section = sectionDto.toDomain(),
                        test = testDto.toDomain(),
                    )
                }
            }
        }

    override suspend fun add(questionId: String, note: String?): DomainResult<Unit> =
        withContext(dispatchers.io) {
            val userId = currentUserIdOrFail() ?: return@withContext DomainResult.Failure(
                DomainError.Auth("No active session"),
            )
            runCatchingNetwork("question_bookmarks/add") {
                supabase.postgrest.from("question_bookmarks").insert(
                    QuestionBookmarkInsertDto(
                        userId = userId,
                        questionId = questionId,
                        note = note.normalizedNote(),
                    ),
                )
                Unit
            }
        }

    override suspend fun listStates(questionIds: List<String>): DomainResult<Map<String, String?>> =
        withContext(dispatchers.io) {
            val userId = currentUserIdOrFail() ?: return@withContext DomainResult.Failure(
                DomainError.Auth("No active session"),
            )
            if (questionIds.isEmpty()) return@withContext DomainResult.Success(emptyMap())
            runCatchingNetwork("question_bookmarks/listStates") {
                supabase.postgrest.from("question_bookmarks")
                    .select(columns = Columns.list("question_id", "note")) {
                        filter {
                            eq("user_id", userId)
                            isIn("question_id", questionIds)
                        }
                    }
                    .decodeList<QuestionBookmarkStateRow>()
                    .associate { it.questionId to it.note }
            }
        }

    override suspend fun remove(questionId: String): DomainResult<Unit> =
        withContext(dispatchers.io) {
            val userId = currentUserIdOrFail() ?: return@withContext DomainResult.Failure(
                DomainError.Auth("No active session"),
            )
            runCatchingNetwork("question_bookmarks/remove") {
                supabase.postgrest.from("question_bookmarks").delete {
                    filter {
                        eq("user_id", userId)
                        eq("question_id", questionId)
                    }
                }
                Unit
            }
        }

    override suspend fun updateNote(questionId: String, note: String?): DomainResult<Unit> =
        withContext(dispatchers.io) {
            val userId = currentUserIdOrFail() ?: return@withContext DomainResult.Failure(
                DomainError.Auth("No active session"),
            )
            runCatchingNetwork("question_bookmarks/updateNote") {
                supabase.postgrest.from("question_bookmarks").update(
                    QuestionBookmarkNotePatchDto(note = note.normalizedNote()),
                ) {
                    filter {
                        eq("user_id", userId)
                        eq("question_id", questionId)
                    }
                }
                Unit
            }
        }

    override suspend fun isBookmarked(questionId: String): DomainResult<Boolean> =
        withContext(dispatchers.io) {
            val userId = currentUserIdOrFail() ?: return@withContext DomainResult.Failure(
                DomainError.Auth("No active session"),
            )
            runCatchingNetwork("question_bookmarks/isBookmarked") {
                supabase.postgrest.from("question_bookmarks")
                    .select(columns = Columns.list("question_id")) {
                        filter {
                            eq("user_id", userId)
                            eq("question_id", questionId)
                        }
                        limit(1)
                    }
                    .decodeList<QuestionBookmarkIdRow>()
                    .isNotEmpty()
            }
        }

    @kotlinx.serialization.Serializable
    private data class QuestionBookmarkIdRow(
        @kotlinx.serialization.SerialName("question_id") val questionId: String,
    )

    @kotlinx.serialization.Serializable
    private data class QuestionBookmarkStateRow(
        @kotlinx.serialization.SerialName("question_id") val questionId: String,
        @kotlinx.serialization.SerialName("note") val note: String? = null,
    )

    private fun String?.normalizedNote(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

    private fun currentUserIdOrFail(): String? =
        supabase.auth.currentSessionOrNull()?.user?.id

    private inline fun <T> runCatchingNetwork(op: String, block: () -> T): DomainResult<T> =
        try {
            DomainResult.Success(block())
        } catch (t: Throwable) {
            DomainResult.Failure(DomainError.Network("$op failed: ${t.message}", t))
        }
}
