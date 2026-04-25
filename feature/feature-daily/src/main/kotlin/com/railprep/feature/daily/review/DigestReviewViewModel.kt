package com.railprep.feature.daily.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.railprep.domain.model.Digest
import com.railprep.domain.model.DigestAttempt
import com.railprep.domain.repository.DigestRepository
import com.railprep.domain.util.DomainResult
import com.railprep.feature.daily.digest.todayInIst
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

data class DigestReviewUiState(
    val loading: Boolean = true,
    val date: LocalDate? = null,
    val digest: Digest? = null,
    val attempt: DigestAttempt? = null,
    val showHi: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class DigestReviewViewModel @Inject constructor(
    private val digestRepository: DigestRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(DigestReviewUiState())
    val state: StateFlow<DigestReviewUiState> = _state.asStateFlow()

    init { load() }

    fun toggleBilingual() = _state.update { it.copy(showHi = !it.showHi) }

    private fun load() {
        viewModelScope.launch {
            val today = todayInIst()
            _state.update { it.copy(loading = true, date = today, error = null) }
            val digestRes = digestRepository.loadForDate(today)
            val attemptRes = digestRepository.getMyAttempt(today)
            val digest = (digestRes as? DomainResult.Success)?.value
            val attempt = (attemptRes as? DomainResult.Success)?.value
            _state.update {
                it.copy(
                    loading = false,
                    digest = digest,
                    attempt = attempt,
                    error = if (digest == null) "load" else null,
                )
            }
        }
    }
}
