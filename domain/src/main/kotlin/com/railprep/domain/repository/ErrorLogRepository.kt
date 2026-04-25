package com.railprep.domain.repository

import com.railprep.domain.util.DomainResult
import kotlinx.serialization.json.JsonObject
import kotlinx.datetime.Instant

interface ErrorLogRepository {
    suspend fun logClientError(
        appVersion: String,
        kotlinClass: String,
        message: String,
        stacktrace: String?,
        breadcrumbs: JsonObject,
        occurredAt: Instant,
    ): DomainResult<Unit>
}
