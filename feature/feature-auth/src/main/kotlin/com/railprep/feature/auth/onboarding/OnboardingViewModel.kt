package com.railprep.feature.auth.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.railprep.domain.repository.LanguageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val languageRepository: LanguageRepository,
) : ViewModel() {

    fun onCompleted(onDone: () -> Unit) {
        viewModelScope.launch {
            languageRepository.markOnboardingSeen()
            onDone()
        }
    }
}
