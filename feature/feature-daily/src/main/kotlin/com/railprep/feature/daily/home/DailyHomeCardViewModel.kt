package com.railprep.feature.daily.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.railprep.domain.model.Profile
import com.railprep.domain.repository.DigestRepository
import com.railprep.domain.repository.ProfileRepository
import com.railprep.domain.util.DomainResult
import com.railprep.feature.daily.digest.todayInIst
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DailyHomeCardUiState(
    val loading: Boolean = true,
    val profile: Profile? = null,
    val alreadyDone: Boolean = false,
    val correctCount: Int = 0,
    val total: Int = 0,
    val error: String? = null,
)

@HiltViewModel
class DailyHomeCardViewModel @Inject constructor(
    private val digestRepository: DigestRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(DailyHomeCardUiState())
    val state: StateFlow<DailyHomeCardUiState> = _state.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            val today = todayInIst()
            val attemptRes = digestRepository.getMyAttempt(today)
            val profileRes = profileRepository.getCurrentProfile()
            val attempt = (attemptRes as? DomainResult.Success)?.value
            val profile = (profileRes as? DomainResult.Success)?.value
            _state.update {
                it.copy(
                    loading = false,
                    profile = profile,
                    alreadyDone = attempt != null,
                    correctCount = attempt?.correctCount ?: 0,
                    total = attempt?.total ?: 0,
                    error = if (attemptRes is DomainResult.Failure &&
                        profileRes is DomainResult.Failure) "load" else null,
                )
            }
        }
    }
}
