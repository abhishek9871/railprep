package com.railprep.data.repository

import com.railprep.core.common.coroutines.DispatcherProvider
import com.railprep.domain.model.User
import com.railprep.domain.repository.AuthRepository
import com.railprep.domain.repository.AuthState
import com.railprep.domain.util.DomainError
import com.railprep.domain.util.DomainResult
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: Auth,
    private val dispatchers: DispatcherProvider,
) : AuthRepository {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initializing)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Long-lived scope for observing session status. Tied to app lifecycle (Singleton).
    private val scope = CoroutineScope(SupervisorJob() + dispatchers.io)

    init {
        scope.launch { observeSessionStatus() }
    }

    private suspend fun observeSessionStatus() {
        auth.sessionStatus.collect { status ->
            _authState.value = when (status) {
                is SessionStatus.Initializing -> AuthState.Initializing
                is SessionStatus.Authenticated -> status.session.user?.let {
                    AuthState.Authenticated(it.toDomain())
                } ?: AuthState.Unauthenticated
                is SessionStatus.NotAuthenticated -> AuthState.Unauthenticated
                is SessionStatus.RefreshFailure -> AuthState.RefreshFailed(status.cause.toString())
            }
        }
    }

    override fun currentUserSync(): User? =
        auth.currentSessionOrNull()?.user?.toDomain()

    override suspend fun signInWithGoogle(idToken: String, rawNonce: String): DomainResult<User> =
        withContext(dispatchers.io) {
            runCatchingAuth {
                auth.signInWith(IDToken) {
                    this.idToken = idToken
                    this.provider = Google
                    this.nonce = rawNonce
                }
                checkNotNull(currentUserSync()) { "Sign-in completed but no session present" }
            }
        }

    override suspend fun signInWithEmail(email: String, password: String): DomainResult<User> =
        withContext(dispatchers.io) {
            runCatchingAuth {
                auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                checkNotNull(currentUserSync()) { "Sign-in completed but no session present" }
            }
        }

    override suspend fun createAccountWithEmail(email: String, password: String): DomainResult<User> =
        withContext(dispatchers.io) {
            runCatchingAuth {
                auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }
                // If email-confirmations are enabled on the Supabase project, signUp does NOT create
                // a session. currentUserSync() returns null in that case. We surface a generic
                // "placeholder" user with an empty id so the UI can still show success;
                // the Auth screen's "create account" VM treats empty id as "check your email".
                currentUserSync() ?: User(id = "", email = email, displayName = null, avatarUrl = null)
            }
        }

    override suspend fun sendPasswordReset(email: String): DomainResult<Unit> =
        withContext(dispatchers.io) {
            runCatchingAuth {
                auth.resetPasswordForEmail(email)
            }
        }

    override suspend fun verifyRecoveryOtp(email: String, code: String): DomainResult<User> =
        withContext(dispatchers.io) {
            runCatchingAuth {
                auth.verifyEmailOtp(type = OtpType.Email.RECOVERY, email = email, token = code)
                checkNotNull(currentUserSync()) { "Recovery OTP accepted but no session present" }
            }
        }

    override suspend fun updatePassword(newPassword: String): DomainResult<Unit> =
        withContext(dispatchers.io) {
            runCatchingAuth {
                auth.updateUser { password = newPassword }
                Unit
            }
        }

    override suspend fun signOut(): DomainResult<Unit> =
        withContext(dispatchers.io) {
            runCatchingAuth { auth.signOut() }
        }
}

private fun UserInfo.toDomain(): User = User(
    id = id,
    email = email,
    displayName = userMetadata?.get("name")?.toString()?.trim('"')
        ?: userMetadata?.get("full_name")?.toString()?.trim('"'),
    avatarUrl = userMetadata?.get("avatar_url")?.toString()?.trim('"'),
)

private inline fun <T> runCatchingAuth(block: () -> T): DomainResult<T> = try {
    DomainResult.Success(block())
} catch (t: Throwable) {
    DomainResult.Failure(DomainError.Auth(t.message ?: "Auth failed", t))
}
