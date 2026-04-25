package com.railprep.feature.home.profile.performance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.railprep.domain.model.SubjectHint
import com.railprep.domain.model.Topic
import com.railprep.domain.model.TopicAccuracy
import com.railprep.domain.repository.LearnRepository
import com.railprep.domain.repository.TestsRepository
import com.railprep.domain.util.DomainResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PerformanceUiState(
    val loading: Boolean = true,
    val rows: List<TopicAccuracy> = emptyList(),
    val primersByTag: Map<String, Topic> = emptyMap(),
    val error: Boolean = false,
)

@HiltViewModel
class PerformanceViewModel @Inject constructor(
    private val testsRepository: TestsRepository,
    private val learnRepository: LearnRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(PerformanceUiState())
    val state: StateFlow<PerformanceUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = false) }
            when (val r = testsRepository.getTopicAccuracy()) {
                is DomainResult.Failure -> _state.update {
                    it.copy(loading = false, error = true, rows = emptyList(), primersByTag = emptyMap())
                }
                is DomainResult.Success -> {
                    val weakTags = r.value
                        .filter { it.attempted > 0 }
                        .sortedWith(compareBy<TopicAccuracy> { it.accuracyPct }.thenByDescending { it.attempted })
                        .take(3)
                        .map { it.tag }
                    val topics = when (val topicsResult = learnRepository.findByAnyTag(weakTags, limit = 10)) {
                        is DomainResult.Success -> topicsResult.value
                        is DomainResult.Failure -> emptyList()
                    }
                    _state.update {
                        it.copy(
                            loading = false,
                            rows = r.value.sortedForDisplay(),
                            primersByTag = weakTags.associateWith { tag ->
                                topics.firstOrNull { topic -> tag in topic.tags }
                            }.filterValues { it != null }.mapValues { it.value!! },
                            error = false,
                        )
                    }
                }
            }
        }
    }
}

private fun List<TopicAccuracy>.sortedForDisplay(): List<TopicAccuracy> =
    sortedWith(
        compareBy<TopicAccuracy> { it.subjectHint.order() }
            .thenBy { it.accuracyPct }
            .thenByDescending { it.attempted }
            .thenBy { it.tag },
    )

private fun SubjectHint.order(): Int = when (this) {
    SubjectHint.MATH -> 0
    SubjectHint.REASON -> 1
    SubjectHint.GA -> 2
    SubjectHint.GS -> 3
    SubjectHint.ENG -> 4
    SubjectHint.MIXED -> 5
}
