package com.railprep.data.repository

import com.railprep.core.common.coroutines.DispatcherProvider
import com.railprep.data.remote.supabase.AttemptAnswerDto
import com.railprep.data.remote.supabase.AttemptAnswerUpsertDto
import com.railprep.data.remote.supabase.AttemptDto
import com.railprep.data.remote.supabase.toDomain
import com.railprep.domain.model.Attempt
import com.railprep.domain.model.AttemptAnswer
import com.railprep.domain.repository.AttemptRepository
import com.railprep.domain.util.DomainError
import com.railprep.domain.util.DomainResult
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttemptRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val dispatchers: DispatcherProvider,
) : AttemptRepository {

    override suspend fun start(testId: String): DomainResult<Attempt> =
        withContext(dispatchers.io) {
            runCatchingNetwork("attempts/start") {
                // RPC returns a single attempts row. Idempotent: if the user already has an
                // IN_PROGRESS attempt for this test and it's not past deadline, the server
                // returns it verbatim (see start_attempt in 0004_tests.sql).
                supabase.postgrest
                    .rpc("start_attempt", buildJsonObject { put("p_test_id", testId) })
                    .decodeAs<AttemptDto>()
                    .toDomain()
            }
        }

    override suspend fun upsertAnswer(
        attemptId: String,
        questionId: String,
        selectedOptionId: String?,
        flagged: Boolean,
    ): DomainResult<Unit> = withContext(dispatchers.io) {
        runCatchingNetwork("attempt_answers/upsert") {
            supabase.postgrest.from("attempt_answers")
                .upsert(
                    AttemptAnswerUpsertDto(
                        attemptId = attemptId,
                        questionId = questionId,
                        selectedOptionId = selectedOptionId,
                        flagged = flagged,
                    ),
                ) {
                    onConflict = "attempt_id,question_id"
                }
            Unit
        }
    }

    override suspend fun flag(
        attemptId: String,
        questionId: String,
        flagged: Boolean,
    ): DomainResult<Unit> = withContext(dispatchers.io) {
        // Flag is just an upsert with the current selected_option_id preserved. We fetch
        // the row first so we don't clobber a user's selection when toggling flag.
        runCatchingNetwork("attempt_answers/flag") {
            val existing = supabase.postgrest.from("attempt_answers")
                .select(columns = Columns.list("selected_option_id")) {
                    filter {
                        eq("attempt_id", attemptId)
                        eq("question_id", questionId)
                    }
                    limit(1)
                }
                .decodeList<AttemptAnswerSelection>()
                .firstOrNull()

            supabase.postgrest.from("attempt_answers")
                .upsert(
                    AttemptAnswerUpsertDto(
                        attemptId = attemptId,
                        questionId = questionId,
                        selectedOptionId = existing?.selectedOptionId,
                        flagged = flagged,
                    ),
                ) {
                    onConflict = "attempt_id,question_id"
                }
            Unit
        }
    }

    override suspend fun submit(attemptId: String): DomainResult<Attempt> =
        withContext(dispatchers.io) {
            runCatchingNetwork("attempts/submit") {
                supabase.postgrest
                    .rpc("submit_attempt", buildJsonObject { put("p_attempt_id", attemptId) })
                    .decodeAs<AttemptDto>()
                    .toDomain()
            }
        }

    override suspend fun listAnswers(attemptId: String): DomainResult<List<AttemptAnswer>> =
        withContext(dispatchers.io) {
            runCatchingNetwork("attempt_answers/list") {
                supabase.postgrest.from("attempt_answers")
                    .select(columns = Columns.ALL) {
                        filter { eq("attempt_id", attemptId) }
                    }
                    .decodeList<AttemptAnswerDto>()
                    .map { it.toDomain() }
            }
        }

    override suspend fun listMine(): DomainResult<List<Attempt>> =
        withContext(dispatchers.io) {
            runCatchingNetwork("attempts/listMine") {
                supabase.postgrest.from("attempts")
                    .select(columns = Columns.ALL) {
                        order("started_at", Order.DESCENDING)
                        limit(100)
                    }
                    .decodeList<AttemptDto>()
                    .map { it.toDomain() }
            }
        }

    override suspend fun resumeInProgress(): DomainResult<Attempt?> =
        withContext(dispatchers.io) {
            runCatchingNetwork("attempts/resumeInProgress") {
                supabase.postgrest.from("attempts")
                    .select(columns = Columns.ALL) {
                        filter { eq("status", "IN_PROGRESS") }
                        order("started_at", Order.DESCENDING)
                        limit(1)
                    }
                    .decodeList<AttemptDto>()
                    .firstOrNull()
                    ?.toDomain()
            }
        }

    override suspend fun get(attemptId: String): DomainResult<Attempt> =
        withContext(dispatchers.io) {
            runCatchingNetwork("attempts/get") {
                supabase.postgrest.from("attempts")
                    .select(columns = Columns.ALL) {
                        filter { eq("id", attemptId) }
                        limit(1)
                        single()
                    }
                    .decodeAs<AttemptDto>()
                    .toDomain()
            }
        }

    @kotlinx.serialization.Serializable
    private data class AttemptAnswerSelection(
        @kotlinx.serialization.SerialName("selected_option_id")
        val selectedOptionId: String? = null,
    )

    private inline fun <T> runCatchingNetwork(op: String, block: () -> T): DomainResult<T> =
        try {
            DomainResult.Success(block())
        } catch (t: Throwable) {
            DomainResult.Failure(DomainError.Network("$op failed: ${t.message}", t))
        }
}
