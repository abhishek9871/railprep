package com.railprep.feature.tests.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.railprep.domain.model.Attempt
import com.railprep.domain.model.Test
import com.railprep.domain.model.WeakTopicRecommendation
import com.railprep.domain.repository.AttemptRepository
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

data class ResultsUiState(
    val loading: Boolean = true,
    val attempt: Attempt? = null,
    val test: Test? = null,
    val sectionTitles: Map<String, String> = emptyMap(),
    /** Top-N tags the user got wrong, paired to a matching primer/topic. */
    val weakRecommendations: List<WeakTopicRecommendation> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class ResultsViewModel @Inject constructor(
    private val attemptRepository: AttemptRepository,
    private val testsRepository: TestsRepository,
    private val learnRepository: LearnRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ResultsUiState())
    val state: StateFlow<ResultsUiState> = _state.asStateFlow()

    fun load(attemptId: String) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            val attemptRes = attemptRepository.get(attemptId)
            if (attemptRes !is DomainResult.Success) {
                _state.update { it.copy(loading = false, error = "load") }
                return@launch
            }
            val attempt = attemptRes.value
            val testRes = testsRepository.get(attempt.testId)
            val sectionsRes = testsRepository.listSections(attempt.testId)

            val sectionTitles = (sectionsRes as? DomainResult.Success)?.value
                ?.associate { it.id to it.titleEn } ?: emptyMap()

            _state.update {
                it.copy(
                    loading = false,
                    attempt = attempt,
                    test = (testRes as? DomainResult.Success)?.value,
                    sectionTitles = sectionTitles,
                )
            }

            // Weak-topic routing: secondary load (results screen renders without it). Quietly
            // drops if anything fails — never blocks the primary results UI.
            loadWeakRecommendations(attempt.testId, attemptId)
        }
    }

    private suspend fun loadWeakRecommendations(testId: String, attemptId: String) {
        // 1) wrong-answered question ids → tags
        val questionsRes = testsRepository.listQuestions(testId)
        if (questionsRes !is DomainResult.Success) return
        val questions = questionsRes.value.associateBy { it.id }

        val answersRes = attemptRepository.listAnswers(attemptId)
        if (answersRes !is DomainResult.Success) return
        val answers = answersRes.value

        val tagMissCounts = HashMap<String, Int>()
        for (a in answers) {
            val q = questions[a.questionId] ?: continue
            // Skipped (no option chosen) doesn't count as "wrong" for routing —
            // weak-topic routing should reflect what the user attempted and got wrong,
            // signalling a real misconception, not a time-budget skip.
            val selectedId = a.selectedOptionId ?: continue
            val correctId = q.options.firstOrNull { it.isCorrect }?.id
            if (correctId != null && selectedId != correctId) {
                for (tag in q.tags) tagMissCounts.merge(tag, 1, Int::plus)
            }
        }

        if (tagMissCounts.isEmpty()) return

        // Top 3 weak tags ordered by miss count desc — keeps the recommendation card brief.
        val topTags = tagMissCounts.entries.sortedByDescending { it.value }.take(3).map { it.key }

        val topicsRes = learnRepository.findByAnyTag(topTags, limit = 6)
        if (topicsRes !is DomainResult.Success) return

        // For each weak tag, pick the first topic that matches it. Skip tags that have
        // no matching topic (no primer authored yet).
        val recs = topTags.mapNotNull { tag ->
            val topic = topicsRes.value.firstOrNull { it.tags.contains(tag) } ?: return@mapNotNull null
            WeakTopicRecommendation(
                tag = tag,
                missCount = tagMissCounts[tag] ?: 0,
                topic = topic,
            )
        }
        _state.update { it.copy(weakRecommendations = recs) }
    }
}
