package com.railprep.feature.auth.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.railprep.domain.repository.AuthRepository
import com.railprep.domain.repository.AuthState
import com.railprep.domain.repository.LanguageRepository
import com.railprep.domain.repository.ProfileRepository
import com.railprep.domain.util.DomainResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

sealed interface SplashDestination {
    data object Language : SplashDestination
    data object Onboarding : SplashDestination
    data object Auth : SplashDestination
    data object Goal : SplashDestination
    data object Home : SplashDestination
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val languageRepository: LanguageRepository,
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination?>(null)
    val destination: StateFlow<SplashDestination?> = _destination.asStateFlow()

    init {
        viewModelScope.launch {
            delay(MIN_SPLASH_MS)
            _destination.value = decideRouteForLaunch()
        }
    }

    private suspend fun decideRouteForLaunch(): SplashDestination {
        val preAuthDestination = decidePreAuthRoute()
        if (preAuthDestination != null) return preAuthDestination

        val settledState = withTimeoutOrNull(AUTH_SETTLE_TIMEOUT_MS) {
            authRepository.authState
                .filter { it !is AuthState.Initializing }
                .first()
        }
        val sessionUser = when (settledState) {
            is AuthState.Authenticated -> settledState.user
            else -> authRepository.currentUserSync()
        }
        if (sessionUser == null) return SplashDestination.Auth

        return when (val profile = profileRepository.getCurrentProfile()) {
            is DomainResult.Success -> {
                if (profile.value.onboardingComplete) SplashDestination.Home else SplashDestination.Goal
            }
            is DomainResult.Failure -> {
                // Network failure should not dump a returning user back into setup if the
                // last known profile was complete. The server fetch above refreshes this
                // cache whenever connectivity is healthy.
                if (profileRepository.cachedOnboardingComplete()) {
                    SplashDestination.Home
                } else {
                    SplashDestination.Goal
                }
            }
        }
    }

    internal fun decideRoute(): SplashDestination {
        decidePreAuthRoute()?.let { return it }

        val hasSession = authRepository.currentUserSync() != null
        if (!hasSession) return SplashDestination.Auth

        val onboardingDone = profileRepository.cachedOnboardingComplete()
        return if (onboardingDone) SplashDestination.Home else SplashDestination.Goal
    }

    private fun decidePreAuthRoute(): SplashDestination? {
        val hasLanguage = languageRepository.currentSync() != null
        if (!hasLanguage) return SplashDestination.Language

        val hasSeenOnboarding = languageRepository.hasSeenOnboardingSync()
        if (!hasSeenOnboarding) return SplashDestination.Onboarding
        return null
    }

    companion object {
        const val MIN_SPLASH_MS = 600L
        private const val AUTH_SETTLE_TIMEOUT_MS = 3_000L
    }
}
