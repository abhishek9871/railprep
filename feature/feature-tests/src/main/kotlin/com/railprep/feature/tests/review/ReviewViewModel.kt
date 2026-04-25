package com.railprep.feature.tests.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.railprep.domain.model.Attempt
import com.railprep.domain.model.AttemptAnswer
import com.railprep.domain.model.Question
import com.railprep.domain.model.TestSection
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

enum class ReviewFilter { ALL, WRONG, SKIPPED, MARKED }

data class ReviewUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val attempt: Attempt? = null,
    val sections: List<TestSection> = emptyList(),
    val questions: List<Question> = emptyList(),
    /** questionId -> AttemptAnswer */
    val answers: Map<String, AttemptAnswer> = emptyMap(),
    val showHi: Boolean = false,
    val filter: ReviewFilter = ReviewFilter.ALL,
)

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val attemptRepository: AttemptRepository,
    private val testsRepository: TestsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ReviewUiState())
    val state: StateFlow<ReviewUiState> = _state.asStateFlow()

    fun load(attemptId: String) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            val attemptRes = attemptRepository.get(attemptId)
            if (attemptRes !is DomainResult.Success) {
                _state.update { it.copy(loading = false, error = "load") }
                return@launch
            }
            val attempt = attemptRes.value
            val sectionsRes = testsRepository.listSections(attempt.testId)
            val questionsRes = testsRepository.listQuestions(attempt.testId)
            val answersRes = attemptRepository.listAnswers(attemptId)

            if (sectionsRes !is DomainResult.Success || questionsRes !is DomainResult.Success) {
                _state.update { it.copy(loading = false, error = "load") }
                return@launch
            }
            val sections = sectionsRes.value.sortedBy { it.displayOrder }
            val orderedSectionIds = sections.map { it.id }
            val questions = questionsRes.value.sortedWith(
                compareBy({ orderedSectionIds.indexOf(it.sectionId) }, { it.displayOrder }),
            )
            val answers = (answersRes as? DomainResult.Success)?.value
                ?.associateBy { it.questionId } ?: emptyMap()
            _state.update {
                it.copy(
                    loading = false,
                    attempt = attempt,
                    sections = sections,
                    questions = questions,
                    answers = answers,
                )
            }
        }
    }

    fun setFilter(f: ReviewFilter) = _state.update { it.copy(filter = f) }
    fun toggleBilingual() = _state.update { it.copy(showHi = !it.showHi) }
}

/** Given a question and the user's answer, classify it for filtering. */
fun classifyForFilter(q: Question, a: AttemptAnswer?): ReviewFilter {
    if (a == null || a.selectedOptionId == null) return ReviewFilter.SKIPPED
    if (a.flagged) return ReviewFilter.MARKED
    val correctOptionId = q.options.firstOrNull { it.isCorrect }?.id
    return if (a.selectedOptionId != correctOptionId) ReviewFilter.WRONG else ReviewFilter.ALL
}
