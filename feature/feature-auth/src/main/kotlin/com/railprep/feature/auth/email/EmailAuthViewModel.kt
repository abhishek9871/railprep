package com.railprep.feature.auth.email

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.railprep.domain.repository.AuthRepository
import com.railprep.domain.util.DomainResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "AuthEmail"

enum class EmailMode { SignIn, CreateAccount }

data class EmailAuthUiState(
    val mode: EmailMode = EmailMode.SignIn,
    val email: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null,
    val info: String? = null,
)

@HiltViewModel
class EmailAuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmailAuthUiState())
    val uiState: StateFlow<EmailAuthUiState> = _uiState.asStateFlow()

    fun onEmailChange(v: String) = mutate { it.copy(email = v, emailError = null, generalError = null) }
    fun onPasswordChange(v: String) = mutate { it.copy(password = v, passwordError = null, generalError = null) }
    fun onModeChange(mode: EmailMode) = mutate {
        // Preserve ACCOUNT_CREATED_CONFIRM_EMAIL banner across a mode flip — that's the natural
        // "after signup, switch to Sign In" case. Other info values reset when the user moves.
        val keepInfo = it.info == INFO_ACCOUNT_CREATED_CONFIRM_EMAIL
        it.copy(
            mode = mode,
            generalError = null,
            info = if (keepInfo) it.info else null,
        )
    }

    fun submit(onAuthenticated: () -> Unit) {
        val s = _uiState.value
        val emailError = validateEmail(s.email)
        val passwordError = validatePassword(s.password, s.mode)
        if (emailError != null || passwordError != null) {
            mutate { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }
        val email = normalizedEmail(s.email)
        viewModelScope.launch {
            mutate { it.copy(loading = true, generalError = null, info = null) }
            try {
                val result = when (s.mode) {
                    EmailMode.SignIn -> authRepository.signInWithEmail(email, s.password)
                    EmailMode.CreateAccount -> authRepository.createAccountWithEmail(email, s.password)
                }
                when (result) {
                    is DomainResult.Success -> {
                        mutate { it.copy(loading = false) }
                        if (s.mode == EmailMode.CreateAccount && result.value.id.isBlank()) {
                            // Email confirmation required — no session created yet.
                            // Auto-switch the user to Sign In and persist the banner so they
                            // don't keep tapping Create account (which hits 429 rate limits).
                            mutate {
                                it.copy(
                                    mode = EmailMode.SignIn,
                                    info = INFO_ACCOUNT_CREATED_CONFIRM_EMAIL,
                                )
                            }
                        } else {
                            onAuthenticated()
                        }
                    }
                    is DomainResult.Failure -> {
                        Log.w(TAG, "submit failed: ${result.error}")
                        mutate {
                            it.copy(
                                loading = false,
                                generalError = classifyFailure(result.error.message),
                            )
                        }
                    }
                }
            } catch (ce: CancellationException) {
                throw ce
            } catch (t: Throwable) {
                Log.e(TAG, "Unexpected failure in submit (${s.mode})", t)
                mutate {
                    it.copy(
                        loading = false,
                        generalError = "Unexpected: ${t.javaClass.simpleName}: ${t.message}",
                    )
                }
            }
        }
    }

    fun sendReset() {
        val email = normalizedEmail(_uiState.value.email)
        val err = validateEmail(email)
        if (err != null) {
            mutate { it.copy(emailError = err) }
            return
        }
        viewModelScope.launch {
            mutate { it.copy(loading = true, info = null, generalError = null) }
            try {
                val r = authRepository.sendPasswordReset(email)
                when (r) {
                    is DomainResult.Success -> mutate { it.copy(loading = false, info = INFO_RESET_EMAIL_SENT) }
                    is DomainResult.Failure -> {
                        Log.w(TAG, "sendReset failed: ${r.error}")
                        mutate {
                            it.copy(
                                loading = false,
                                generalError = classifyFailure(r.error.message),
                            )
                        }
                    }
                }
            } catch (ce: CancellationException) {
                throw ce
            } catch (t: Throwable) {
                Log.e(TAG, "Unexpected failure in sendReset", t)
                mutate {
                    it.copy(
                        loading = false,
                        generalError = "Unexpected: ${t.javaClass.simpleName}: ${t.message}",
                    )
                }
            }
        }
    }

    fun clearInfo() = mutate { it.copy(info = null) }
    fun clearError() = mutate { it.copy(generalError = null) }

    private fun mutate(transform: (EmailAuthUiState) -> EmailAuthUiState) {
        _uiState.value = transform(_uiState.value)
    }

    companion object {
        internal const val INFO_ACCOUNT_CREATED_CONFIRM_EMAIL = "ACCOUNT_CREATED_CONFIRM_EMAIL"
        internal const val INFO_RESET_EMAIL_SENT = "RESET_EMAIL_SENT"

        internal fun validateEmail(raw: String): String? {
            val e = normalizedEmail(raw)
            if (e.isEmpty()) return "EMPTY"
            if (!e.matches(Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]{2,}$"))) return "INVALID"
            return null
        }

        internal fun validatePassword(p: String, mode: EmailMode = EmailMode.SignIn): String? {
            if (p.isEmpty()) return "EMPTY"
            if (p.length < 8) return "TOO_SHORT"
            if (mode == EmailMode.CreateAccount && (!p.any(Char::isLetter) || !p.any(Char::isDigit))) {
                return "WEAK"
            }
            return null
        }

        internal fun normalizedEmail(raw: String): String = raw.trim().lowercase()

        /**
         * Map common Supabase-side error strings to friendlier copy. The full message from
         * Supabase is still in logcat under tag [TAG]; this just surfaces something humane.
         */
        internal fun classifyFailure(raw: String): String {
            val normalized = raw.lowercase()
            return when {
                "email not confirmed" in normalized ->
                    "Check your email for the confirmation link, then sign in."
                "invalid login credentials" in normalized ->
                    "Wrong email or password. Try again or tap Forgot password."
                "user already registered" in normalized || "already been registered" in normalized ->
                    "An account already exists for this email. Switch to Sign in."
                "rate limit" in normalized || "after" in normalized && "seconds" in normalized ->
                    "Too many tries. Wait a minute and try again."
                else -> raw
            }
        }
    }
}
