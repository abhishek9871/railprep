package com.railprep.data.repository

import com.railprep.core.common.coroutines.DispatcherProvider
import com.railprep.data.remote.supabase.DailyDigestDto
import com.railprep.data.remote.supabase.DigestAttemptDto
import com.railprep.data.remote.supabase.ProfileDto
import com.railprep.data.remote.supabase.QuestionDto
import com.railprep.data.remote.supabase.toDomain
import com.railprep.domain.model.Digest
import com.railprep.domain.model.DigestAttempt
import com.railprep.domain.model.Profile
import com.railprep.domain.repository.DigestRepository
import com.railprep.domain.util.DomainError
import com.railprep.domain.util.DomainResult
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DigestRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val dispatchers: DispatcherProvider,
) : DigestRepository {

    override suspend fun loadForDate(date: LocalDate): DomainResult<Digest> =
        withContext(dispatchers.io) {
            runCatchingNetwork("digest/loadForDate") {
                // Step 1: idempotent server-side picker.
                val digestRow = supabase.postgrest
                    .rpc("ensure_today_digest", buildJsonObject { put("p_date", date.toString()) })
                    .decodeAs<DailyDigestDto>()

                // Step 2: fetch the full Question rows + options for the picked ids.
                val questions = supabase.postgrest.from("questions")
                    .select(columns = Columns.raw("*,options(*)")) {
                        filter { isIn("id", digestRow.questionIds) }
                    }
                    .decodeList<QuestionDto>()
                    .map { it.toDomain() }

                // Preserve the server-picked order (questionIds is the canonical sequence).
                val byId = questions.associateBy { it.id }
                val ordered = digestRow.questionIds.mapNotNull { byId[it] }

                Digest(
                    date = digestRow.digestDate,
                    questions = ordered,
                    sectionPlan = parseSectionPlan(digestRow),
                )
            }
        }

    override suspend fun submit(
        date: LocalDate,
        answers: List<Pair<String, String?>>,
    ): DomainResult<Profile> = withContext(dispatchers.io) {
        runCatchingNetwork("digest/submit") {
            val payload = buildJsonArray {
                answers.forEach { (qid, oid) ->
                    add(buildJsonObject {
                        put("question_id", qid)
                        put("selected_option_id", oid ?: "")
                    })
                }
            }
            supabase.postgrest
                .rpc("submit_digest", buildJsonObject {
                    put("p_date", date.toString())
                    put("p_answers", payload)
                })
                .decodeAs<ProfileDto>()
                .toDomain()
        }
    }

    override suspend fun getMyAttempt(date: LocalDate): DomainResult<DigestAttempt?> =
        withContext(dispatchers.io) {
            runCatchingNetwork("digest/getMyAttempt") {
                val uid = supabase.auth.currentUserOrNull()?.id ?: return@runCatchingNetwork null
                supabase.postgrest.from("digest_attempts")
                    .select(columns = Columns.ALL) {
                        filter {
                            eq("user_id", uid)
                            eq("digest_date", date.toString())
                        }
                        limit(1)
                    }
                    .decodeList<DigestAttemptDto>()
                    .firstOrNull()
                    ?.toDomain()
            }
        }

    private fun parseSectionPlan(digest: DailyDigestDto): Map<String, Int> {
        val plan = digest.sectionPlan ?: return emptyMap()
        return runCatching {
            (plan as? JsonObject)?.mapValues { (it.value as? JsonPrimitive)?.intOrZero() ?: 0 }
                ?: emptyMap()
        }.getOrDefault(emptyMap())
    }

    private fun JsonPrimitive.intOrZero(): Int = content.toIntOrNull() ?: 0

    private inline fun <T> runCatchingNetwork(op: String, block: () -> T): DomainResult<T> =
        try {
            DomainResult.Success(block())
        } catch (t: Throwable) {
            DomainResult.Failure(DomainError.Network("$op failed: ${t.message}", t))
        }
}
