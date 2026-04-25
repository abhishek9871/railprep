package com.railprep.feature.tests.instructions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.railprep.domain.model.Test
import com.railprep.domain.model.TestSection
import com.railprep.domain.repository.AttemptRepository
import com.railprep.domain.repository.TestsRepository
import com.railprep.domain.util.DomainResult
import com.railprep.feature.tests.work.AutoSubmitScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InstructionsUiState(
    val loading: Boolean = true,
    val test: Test? = null,
    val sections: List<TestSection> = emptyList(),
    val hasActiveAttempt: Boolean = false,
    val starting: Boolean = false,
    val startedAttemptId: String? = null,
    val error: String? = null,
)

@HiltViewModel
class InstructionsViewModel @Inject constructor(
    private val testsRepository: TestsRepository,
    private val attemptRepository: AttemptRepository,
    private val autoSubmitScheduler: AutoSubmitScheduler,
) : ViewModel() {

    private val _state = MutableStateFlow(InstructionsUiState())
    val state: StateFlow<InstructionsUiState> = _state.asStateFlow()

    fun load(testId: String) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            val testRes = testsRepository.get(testId)
            val sectionsRes = testsRepository.listSections(testId)
            if (testRes !is DomainResult.Success || sectionsRes !is DomainResult.Success) {
                _state.update { it.copy(loading = false, error = "load") }
                return@launch
            }
            // Detect whether the user already has an IN_PROGRESS attempt for this test —
            // relabels the CTA from "Start" to "Resume".
            val resumeRes = attemptRepository.resumeInProgress()
            val alreadyActive = (resumeRes as? DomainResult.Success)?.value?.testId == testId
            _state.update {
                it.copy(
                    loading = false,
                    test = testRes.value,
                    sections = sectionsRes.value,
                    hasActiveAttempt = alreadyActive,
                )
            }
        }
    }

    fun start() {
        val testId = _state.value.test?.id ?: return
        if (_state.value.starting) return
        _state.update { it.copy(starting = true, error = null) }
        viewModelScope.launch {
            when (val r = attemptRepository.start(testId)) {
                is DomainResult.Success -> {
                    // Process-death safety net: schedule the auto-submit worker
                    // with initialDelay = deadline + 30s grace. Idempotent: if the
                    // user submits normally first, the worker's status check will
                    // see non-IN_PROGRESS and return success without re-submitting.
                    autoSubmitScheduler.schedule(
                        attemptId = r.value.id,
                        deadlineEpochMs = r.value.serverDeadlineAt.toEpochMilliseconds(),
                    )
                    _state.update { it.copy(starting = false, startedAttemptId = r.value.id) }
                }
                is DomainResult.Failure -> _state.update {
                    val code = if (r.error.message.contains("PRO_REQUIRED", ignoreCase = true)) "pro" else "start"
                    it.copy(starting = false, error = code)
                }
            }
        }
    }

    fun clearStarted() = _state.update { it.copy(startedAttemptId = null) }
}
