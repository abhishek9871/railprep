package com.railprep.feature.tests.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.railprep.domain.model.AttemptStatus
import com.railprep.domain.model.ExamTarget
import com.railprep.domain.model.Test
import com.railprep.domain.model.TestKind
import com.railprep.domain.repository.AttemptRepository
import com.railprep.domain.repository.TestsRepository
import com.railprep.domain.util.DomainResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TestsFilter { ALL, CBT1, CBT2, PYQ, SECTIONAL, PYQ_LIBRARY }

/** Per-test summary of the user's attempt history — drives the card subtitle. */
data class TestAttemptStats(
    val submittedCount: Int,
    val bestScore: Float,
    val bestMaxScore: Float,
)

data class TestsListUiState(
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val tests: List<Test> = emptyList(),
    val filter: TestsFilter = TestsFilter.ALL,
    val attemptStats: Map<String, TestAttemptStats> = emptyMap(),
    val error: String? = null,
)

@HiltViewModel
class TestsListViewModel @Inject constructor(
    private val testsRepository: TestsRepository,
    private val attemptRepository: AttemptRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(TestsListUiState())
    val state: StateFlow<TestsListUiState> = _state.asStateFlow()

    init { load(initial = true) }

    fun refresh() = load(initial = false)

    fun setFilter(filter: TestsFilter) = _state.update { it.copy(filter = filter) }

    private fun load(initial: Boolean) {
        viewModelScope.launch {
            _state.update {
                if (initial) it.copy(loading = true, error = null)
                else it.copy(refreshing = true, error = null)
            }
            // Query both CBT-1 and CBT-2 targets — filter chips handle CBT-split client-side.
            val cbt1 = testsRepository.listForTarget(ExamTarget.NtpcCbt1)
            val cbt2 = testsRepository.listForTarget(ExamTarget.NtpcCbt2)
            val merged = buildList {
                if (cbt1 is DomainResult.Success) addAll(cbt1.value)
                if (cbt2 is DomainResult.Success) addAll(cbt2.value)
            }.distinctBy { it.id }.sortedByDescending { it.publishedAt }
            if (cbt1 is DomainResult.Failure && cbt2 is DomainResult.Failure) {
                _state.update { it.copy(loading = false, refreshing = false, error = "load") }
                return@launch
            }

            // User's SUBMITTED attempts → per-test stats for the card subtitle.
            val stats = when (val r = attemptRepository.listMine()) {
                is DomainResult.Success -> r.value
                    .filter { it.status == AttemptStatus.SUBMITTED }
                    .groupBy { it.testId }
                    .mapValues { (_, rows) ->
                        TestAttemptStats(
                            submittedCount = rows.size,
                            bestScore = rows.maxOfOrNull { it.score ?: 0f } ?: 0f,
                            bestMaxScore = rows.firstOrNull { it.maxScore != null }?.maxScore ?: 0f,
                        )
                    }
                is DomainResult.Failure -> emptyMap()
            }

            _state.update {
                it.copy(
                    loading = false,
                    refreshing = false,
                    tests = merged,
                    attemptStats = stats,
                    error = null,
                )
            }
        }
    }
}

fun List<Test>.filteredFor(filter: TestsFilter): List<Test> = when (filter) {
    // ALL hides PYQ_LINK rows from the default tab; there are many of them and they'd
    // drown out the interactive tests. Users find them via the PYQ_LIBRARY filter chip.
    TestsFilter.ALL -> filter { it.kind != TestKind.PYQ_LINK }
    TestsFilter.CBT1 -> filter { it.examTarget == ExamTarget.NtpcCbt1 && it.kind == TestKind.CBT1_FULL }
    TestsFilter.CBT2 -> filter { it.examTarget == ExamTarget.NtpcCbt2 && it.kind == TestKind.CBT2_FULL }
    TestsFilter.PYQ -> filter { it.kind == TestKind.PYQ }
    TestsFilter.SECTIONAL -> filter { it.kind == TestKind.SECTIONAL }
    TestsFilter.PYQ_LIBRARY -> filter { it.kind == TestKind.PYQ_LINK }
}
