package com.railprep.feature.auth.language

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.railprep.domain.model.SupportedLanguage
import com.railprep.domain.repository.LanguageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LanguageViewModel @Inject constructor(
    private val languageRepository: LanguageRepository,
) : ViewModel() {

    private val _selected = MutableStateFlow<SupportedLanguage>(SupportedLanguage.En)
    val selected: StateFlow<SupportedLanguage> = _selected.asStateFlow()

    private val _messages = MutableSharedFlow<LanguageMessage>(extraBufferCapacity = 1)
    val messages: SharedFlow<LanguageMessage> = _messages.asSharedFlow()

    fun onLanguageClicked(language: SupportedLanguage) {
        if (language.phase1Supported) {
            _selected.value = language
        } else {
            _selected.value = SupportedLanguage.En
            _messages.tryEmit(LanguageMessage.FallingBackToEnglish(language))
        }
    }

    fun onContinueClicked(onDone: () -> Unit) {
        viewModelScope.launch {
            languageRepository.setLanguage(_selected.value)
            onDone()
        }
    }
}

sealed interface LanguageMessage {
    data class FallingBackToEnglish(val attempted: SupportedLanguage) : LanguageMessage
}
