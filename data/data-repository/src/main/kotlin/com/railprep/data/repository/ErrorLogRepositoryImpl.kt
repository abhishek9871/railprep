package com.railprep.data.repository

import com.railprep.core.common.coroutines.DispatcherProvider
import com.railprep.domain.repository.ErrorLogRepository
import com.railprep.domain.util.DomainError
import com.railprep.domain.util.DomainResult
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ErrorLogRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val dispatchers: DispatcherProvider,
) : ErrorLogRepository {

    override suspend fun logClientError(
        appVersion: String,
        kotlinClass: String,
        message: String,
        stacktrace: String?,
        breadcrumbs: JsonObject,
        occurredAt: Instant,
    ): DomainResult<Unit> = withContext(dispatchers.io) {
        runCatchingNetwork("error_logs/logClientError") {
            supabase.postgrest.rpc(
                "log_client_error",
                buildJsonObject {
                    put("p_app_version", appVersion)
                    put("p_kotlin_class", kotlinClass)
                    put("p_message", message)
                    put("p_stacktrace", stacktrace)
                    put("p_breadcrumbs", breadcrumbs)
                    put("p_occurred_at", occurredAt.toString())
                },
            )
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
