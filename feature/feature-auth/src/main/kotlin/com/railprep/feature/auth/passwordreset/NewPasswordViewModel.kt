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

private const val TAG = "AuthNewPassword"

@HiltViewModel
class NewPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewPasswordUiState())
    val uiState: StateFlow<NewPasswordUiState> = _uiState.asStateFlow()

    fun onPasswordChange(v: String) =
        _uiState.update { it.copy(password = v, passwordError = null, generalError = null) }

    fun onConfirmChange(v: String) =
        _uiState.update { it.copy(confirm = v, confirmError = null, generalError = null) }

    fun submit(onSuccess: () -> Unit) {
        val s = _uiState.value
        val pwErr = validatePassword(s.password)
        val confirmErr = if (s.confirm != s.password) "MISMATCH" else null
        if (pwErr != null || confirmErr != null) {
            _uiState.update { it.copy(passwordError = pwErr, confirmError = confirmErr) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, generalError = null) }
            try {
                when (val r = authRepository.updatePassword(s.password)) {
                    is DomainResult.Success -> {
                        _uiState.update { it.copy(loading = false) }
                        onSuccess()
                    }
                    is DomainResult.Failure -> {
                        Log.w(TAG, "updatePassword failed: ${r.error.message}")
                        _uiState.update {
                            it.copy(loading = false, generalError = classifyFailure(r.error.message))
                        }
                    }
                }
            } catch (ce: CancellationException) {
                throw ce
            } catch (t: Throwable) {
                Log.w(TAG, "Unexpected failure in submit", t)
                _uiState.update {
                    it.copy(loading = false, generalError = "Couldn't update password. Try again.")
                }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(generalError = null) }

    companion object {
        internal fun validatePassword(p: String): String? {
            if (p.isEmpty()) return "EMPTY"
            if (p.length < 8) return "TOO_SHORT"
            if (!p.any(Char::isLetter) || !p.any(Char::isDigit)) return "WEAK"
            return null
        }

        private fun classifyFailure(raw: String): String {
            val n = raw.lowercase()
            return when {
                "weak" in n || "password should" in n ->
                    "That password is too weak. Use 8+ characters with a mix of letters and numbers."
                "same" in n && "password" in n ->
                    "New password must be different from the old one."
                else -> raw
            }
        }
    }
}

data class NewPasswordUiState(
    val password: String = "",
    val confirm: String = "",
    val loading: Boolean = false,
    val passwordError: String? = null,
    val confirmError: String? = null,
    val generalError: String? = null,
)
