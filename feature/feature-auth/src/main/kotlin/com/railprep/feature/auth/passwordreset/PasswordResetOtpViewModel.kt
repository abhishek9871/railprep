package com.railprep.feature.auth.passwordreset

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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "AuthRecoveryOtp"

@HiltViewModel
class PasswordResetOtpViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PasswordResetOtpUiState())
    val uiState: StateFlow<PasswordResetOtpUiState> = _uiState.asStateFlow()

    fun onCodeChange(value: String) {
        // Supabase's OTP length is configurable per-project (commonly 6 or 8 digits). Accept up
        // to 10 digits and let the server decide — no client-side length lockout.
        val digits = value.filter(Char::isDigit).take(10)
        _uiState.update { it.copy(code = digits, codeError = null, generalError = null) }
    }

    fun verify(email: String, onSuccess: () -> Unit) {
        val s = _uiState.value
        if (s.code.length < 6) {
            _uiState.update { it.copy(codeError = "INVALID") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, generalError = null) }
            try {
                when (val r = authRepository.verifyRecoveryOtp(email, s.code)) {
                    is DomainResult.Success -> {
                        _uiState.update { it.copy(loading = false) }
                        onSuccess()
                    }
                    is DomainResult.Failure -> {
                        Log.w(TAG, "verify failed: ${r.error.message}")
                        _uiState.update {
                            it.copy(loading = false, generalError = classifyFailure(r.error.message))
                        }
                    }
                }
            } catch (ce: CancellationException) {
                throw ce
            } catch (t: Throwable) {
                Log.w(TAG, "Unexpected failure in verify", t)
                _uiState.update {
                    it.copy(loading = false, generalError = "Couldn't verify code. Try again.")
                }
            }
        }
    }

    fun resend(email: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, generalError = null, info = null) }
            try {
                when (val r = authRepository.sendPasswordReset(email)) {
                    is DomainResult.Success -> _uiState.update {
                        it.copy(loading = false, info = INFO_CODE_RESENT)
                    }
                    is DomainResult.Failure -> {
                        Log.w(TAG, "resend failed: ${r.error.message}")
                        _uiState.update {
                            it.copy(loading = false, generalError = classifyFailure(r.error.message))
                        }
                    }
                }
            } catch (ce: CancellationException) {
                throw ce
            } catch (t: Throwable) {
                Log.w(TAG, "Unexpected failure in resend", t)
                _uiState.update {
                    it.copy(loading = false, generalError = "Couldn't resend code. Try again.")
                }
            }
        }
    }

    fun clearInfo() = _uiState.update { it.copy(info = null) }
    fun clearError() = _uiState.update { it.copy(generalError = null) }

    companion object {
        internal const val INFO_CODE_RESENT = "CODE_RESENT"

        internal fun classifyFailure(raw: String): String {
            val n = raw.lowercase()
            return when {
                "token has expired" in n || "expired" in n ->
                    "This code has expired. Request a new one."
                "invalid" in n && ("token" in n || "otp" in n) ->
                    "Wrong code. Check your email and try again."
                "rate limit" in n || "after" in n && "seconds" in n ->
                    "Too many tries. Wait a minute and try again."
                else -> raw
            }
        }
    }
}

data class PasswordResetOtpUiState(
    val code: String = "",
    val loading: Boolean = false,
    val codeError: String? = null,
    val generalError: String? = null,
    val info: String? = null,
)
