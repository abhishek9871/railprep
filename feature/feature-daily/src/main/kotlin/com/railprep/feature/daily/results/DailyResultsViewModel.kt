package com.railprep.feature.daily.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.railprep.domain.model.DigestAttempt
import com.railprep.domain.model.Profile
import com.railprep.domain.repository.DigestRepository
import com.railprep.domain.repository.ProfileRepository
import com.railprep.domain.util.DomainResult
import com.railprep.feature.notifications.DigestReminderScheduler
import com.railprep.feature.daily.digest.todayInIst
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

data class DailyResultsUiState(
    val loading: Boolean = true,
    val date: LocalDate? = null,
    val attempt: DigestAttempt? = null,
    val profile: Profile? = null,
    /** True exactly once per install: after the first digest submit ever, if the user hasn't
     *  already decided on notifications. Gates the opt-in dialog (not any system-level prompt). */
    val showNotificationsOptIn: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class DailyResultsViewModel @Inject constructor(
    private val digestRepository: DigestRepository,
    private val profileRepository: ProfileRepository,
    private val reminderScheduler: DigestReminderScheduler,
) : ViewModel() {

    private val _state = MutableStateFlow(DailyResultsUiState())
    val state: StateFlow<DailyResultsUiState> = _state.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            val today = todayInIst()
            _state.update { it.copy(loading = true, date = today, error = null) }
            val attemptRes = digestRepository.getMyAttempt(today)
            val profileRes = profileRepository.getCurrentProfile()
            val attempt = (attemptRes as? DomainResult.Success)?.value
            val profile = (profileRes as? DomainResult.Success)?.value
            // LOAD-BEARING INVARIANT: streakBest == 1 && streakCurrent == 1 is the ONLY signal
            // that distinguishes a user's first-ever submit from every subsequent submit. It is
            // the gate that keeps POST_NOTIFICATIONS from being prompted at app launch or on
            // re-starts after a streak break. If a future phase ever seeds streak columns via a
            // migration (backfill from attempt history, gifted streaks, anything), this prompt
            // will silently disappear — because streakBest will no longer be 1 at first submit.
            // Change this predicate alongside any migration that writes to streak_best.
            val isFirstSubmit = attempt != null &&
                profile?.notificationsEnabled == false &&
                profile.streakBest == 1 &&
                profile.streakCurrent == 1
            _state.update {
                it.copy(
                    loading = false,
                    attempt = attempt,
                    profile = profile,
                    showNotificationsOptIn = isFirstSubmit,
                    error = if (attempt == null && attemptRes is DomainResult.Failure) "load" else null,
                )
            }
        }
    }

    /** User granted system permission AND our dialog. Flip flag + enqueue worker. */
    fun acceptNotifications() {
        viewModelScope.launch {
            _state.update { it.copy(showNotificationsOptIn = false) }
            when (val r = profileRepository.setNotificationsEnabled(true)) {
                is DomainResult.Success -> {
                    reminderScheduler.enable()
                    _state.update { it.copy(profile = r.value) }
                }
                is DomainResult.Failure -> { /* silent — user can retry from Profile */ }
            }
        }
    }

    fun dismissNotificationsOptIn() = _state.update { it.copy(showNotificationsOptIn = false) }
}
