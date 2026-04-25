package com.railprep.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.railprep.domain.repository.AuthRepository
import com.railprep.domain.repository.AuthState
import com.railprep.domain.util.DomainResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val displayName: String? = null,
    val email: String? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = authRepository.authState
        .map { state ->
            when (state) {
                is AuthState.Authenticated -> HomeUiState(
                    displayName = state.user.displayName,
                    email = state.user.email,
                )
                else -> HomeUiState()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = HomeUiState(),
        )

    fun signOut(onSignedOut: () -> Unit) {
        viewModelScope.launch {
            when (authRepository.signOut()) {
                is DomainResult.Success -> onSignedOut()
                is DomainResult.Failure -> { /* ignore; session clears regardless */ onSignedOut() }
            }
        }
    }
}
