package com.railprep.feature.auth.auth

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.railprep.domain.repository.AuthRepository
import com.railprep.domain.util.DomainResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

private const val TAG = "AuthGoogle"

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    /**
     * Runs the full Google flow in viewModelScope rather than a composable-scoped coroutine.
     * Some OEM skins (ColorOS confirmed) dispose the calling composable while the Credential
     * Manager system sheet is up; a rememberCoroutineScope-launched coroutine gets cancelled
     * mid-suspension and the continuation is orphaned. viewModelScope survives that disposal.
     */
    fun startGoogleSignIn(activity: Activity, webClientId: String, onAuthenticated: () -> Unit) {
        _uiState.update { it.copy(googleLoading = true, error = null) }
        viewModelScope.launch {
            try {
                // 25 s timeout guards against OEM cases where getCredential's callback never
                // resumes the suspended coroutine.
                val payload = withTimeout(25_000) {
                    GoogleSignInHelper(activity, webClientId).requestIdToken()
                }
                when (val r = authRepository.signInWithGoogle(payload.idToken, payload.rawNonce)) {
                    is DomainResult.Success -> {
                        _uiState.update { it.copy(googleLoading = false) }
                        onAuthenticated()
                    }
                    is DomainResult.Failure -> {
                        Log.w(TAG, "Supabase rejected Google ID token: ${r.error.message}")
                        _uiState.update {
                            it.copy(googleLoading = false, error = "Couldn't sign in. Try again.")
                        }
                    }
                }
            } catch (e: GoogleSignInError.UserCancelled) {
                _uiState.update { it.copy(googleLoading = false) }
            } catch (e: GoogleSignInError.NoCredential) {
                _uiState.update {
                    it.copy(googleLoading = false, error = "Add a Google account to this device, then try again.")
                }
            } catch (te: TimeoutCancellationException) {
                Log.w(TAG, "Google picker timed out", te)
                withContext(NonCancellable) {
                    _uiState.update {
                        it.copy(googleLoading = false, error = "Google sign-in timed out. Try again.")
                    }
                }
            } catch (ce: CancellationException) {
                withContext(NonCancellable) {
                    _uiState.update { it.copy(googleLoading = false) }
                }
                throw ce
            } catch (t: Throwable) {
                Log.w(TAG, "Google sign-in failed", t)
                _uiState.update {
                    it.copy(googleLoading = false, error = "Couldn't sign in with Google. Try again.")
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class AuthUiState(
    val googleLoading: Boolean = false,
    val error: String? = null,
)
