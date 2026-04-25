package com.railprep.domain.util

/**
 * Explicit success/failure wrapper. Used across repositories instead of `kotlin.Result`
 * (which isn't trivially serializable) or raw exceptions (which leak infra concerns into VMs).
 */
sealed interface DomainResult<out T> {
    data class Success<T>(val value: T) : DomainResult<T>
    data class Failure(val error: DomainError) : DomainResult<Nothing>
}

sealed class DomainError(open val message: String, open val cause: Throwable? = null) {
    data class Network(override val message: String, override val cause: Throwable? = null) : DomainError(message, cause)
    data class Auth(override val message: String, override val cause: Throwable? = null) : DomainError(message, cause)
    data class NotFound(override val message: String = "Not found", override val cause: Throwable? = null) : DomainError(message, cause)
    data class Validation(val field: String, override val message: String) : DomainError(message)
    data class Unknown(override val message: String, override val cause: Throwable? = null) : DomainError(message, cause)
}

inline fun <T> runCatchingDomain(block: () -> T): DomainResult<T> = try {
    DomainResult.Success(block())
} catch (t: Throwable) {
    DomainResult.Failure(DomainError.Unknown(t.message ?: "Unknown error", t))
}
