package com.railprep.domain.repository

import com.railprep.domain.model.User
import com.railprep.domain.util.DomainResult
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    /** Observes authentication state. Emits Initializing on cold start until the stored session is loaded. */
    val authState: StateFlow<AuthState>

    /** Non-suspending snapshot: was a session present at the last check? Used by Splash for sync routing. */
    fun currentUserSync(): User?

    /**
     * Native Google sign-in. Caller performs the Credential Manager flow and passes the Google ID token
     * plus the **raw** nonce (Google received the hashed nonce; Supabase receives raw for server-side check).
     */
    suspend fun signInWithGoogle(idToken: String, rawNonce: String): DomainResult<User>

    suspend fun signInWithEmail(email: String, password: String): DomainResult<User>
    suspend fun createAccountWithEmail(email: String, password: String): DomainResult<User>

    /**
     * Sends the user a 6-digit recovery code by email. Template is configured server-side to
     * emit `{{ .Token }}` — avoids magic-link pre-fetch / OEM email-scanner issues.
     */
    suspend fun sendPasswordReset(email: String): DomainResult<Unit>

    /** Consumes the 6-digit recovery code and establishes a recovery session. */
    suspend fun verifyRecoveryOtp(email: String, code: String): DomainResult<User>

    /** Updates the current user's password. Requires an authenticated session. */
    suspend fun updatePassword(newPassword: String): DomainResult<Unit>

    suspend fun signOut(): DomainResult<Unit>
}

sealed interface AuthState {
    data object Initializing : AuthState
    data class Authenticated(val user: User) : AuthState
    data object Unauthenticated : AuthState
    data class RefreshFailed(val reason: String) : AuthState
}
