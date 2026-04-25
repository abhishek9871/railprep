package com.railprep.feature.auth.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.railprep.domain.repository.AuthRepository
import com.railprep.domain.repository.LanguageRepository
import com.railprep.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
            _destination.value = decideRoute()
        }
    }

    internal fun decideRoute(): SplashDestination {
        val hasLanguage = languageRepository.currentSync() != null
        if (!hasLanguage) return SplashDestination.Language

        val hasSeenOnboarding = languageRepository.hasSeenOnboardingSync()
        if (!hasSeenOnboarding) return SplashDestination.Onboarding

        val hasSession = authRepository.currentUserSync() != null
        if (!hasSession) return SplashDestination.Auth

        val onboardingDone = profileRepository.cachedOnboardingComplete()
        return if (onboardingDone) SplashDestination.Home else SplashDestination.Goal
    }

    companion object {
        const val MIN_SPLASH_MS = 600L
    }
}
