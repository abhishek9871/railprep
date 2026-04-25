package com.railprep.feature.home.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.railprep.domain.model.SupportedLanguage
import com.railprep.domain.repository.AuthRepository
import com.railprep.domain.repository.AuthState
import com.railprep.domain.repository.BookmarkRepository
import com.railprep.domain.repository.LanguageRepository
import com.railprep.domain.repository.ProfileRepository
import com.railprep.domain.repository.QuestionBookmarkRepository
import com.railprep.domain.util.DomainResult
import com.railprep.feature.notifications.DigestReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val displayName: String? = null,
    val email: String? = null,
    val language: String = "en",
    val streakCurrent: Int = 0,
    val streakBest: Int = 0,
    val notificationsEnabled: Boolean = false,
    val topicBookmarkCount: Int = 0,
    val savedQuestionCount: Int = 0,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val languageRepository: LanguageRepository,
    private val profileRepository: ProfileRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val questionBookmarkRepository: QuestionBookmarkRepository,
    private val reminderScheduler: DigestReminderScheduler,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        observeAuth()
        observeLanguage()
        observeProfile()
        refreshBookmarkCounts()
    }

    private fun observeProfile() {
        viewModelScope.launch {
            profileRepository.observeCurrentProfile().collect { p ->
                if (p != null) {
                    _state.update {
                        it.copy(
                            streakCurrent = p.streakCurrent,
                            streakBest = p.streakBest,
                            notificationsEnabled = p.notificationsEnabled,
                        )
                    }
                }
            }
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            when (val r = profileRepository.setNotificationsEnabled(enabled)) {
                is DomainResult.Success -> {
                    _state.update { it.copy(notificationsEnabled = enabled) }
                    if (enabled) reminderScheduler.enable() else reminderScheduler.disable()
                }
                is DomainResult.Failure -> { /* silent — toggle stays at old value */ }
            }
        }
    }

    fun refreshBookmarkCounts() {
        viewModelScope.launch {
            val topicCount = when (val r = bookmarkRepository.listBookmarks()) {
                is DomainResult.Success -> r.value.size
                is DomainResult.Failure -> _state.value.topicBookmarkCount
            }
            val questionCount = when (val r = questionBookmarkRepository.list()) {
                is DomainResult.Success -> r.value.size
                is DomainResult.Failure -> _state.value.savedQuestionCount
            }
            _state.update {
                it.copy(
                    topicBookmarkCount = topicCount,
                    savedQuestionCount = questionCount,
                )
            }
        }
    }

    private fun observeAuth() {
        viewModelScope.launch {
            authRepository.authState.collect { s ->
                if (s is AuthState.Authenticated) {
                    _state.update {
                        it.copy(displayName = s.user.displayName, email = s.user.email)
                    }
                }
            }
        }
    }

    private fun observeLanguage() {
        viewModelScope.launch {
            languageRepository.observeCurrent().collect { lang ->
                _state.update { it.copy(language = lang?.code ?: "en") }
            }
        }
    }

    fun setLanguage(code: String) {
        val lang = SupportedLanguage.fromCode(code) ?: return
        viewModelScope.launch { languageRepository.setLanguage(lang) }
    }

    fun signOut(onSignedOut: () -> Unit) {
        viewModelScope.launch {
            when (authRepository.signOut()) {
                is DomainResult.Success, is DomainResult.Failure -> onSignedOut()
            }
        }
    }
}
