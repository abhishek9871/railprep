package com.railprep.feature.home.profile.savedquestions

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.railprep.domain.model.QuestionBookmark
import com.railprep.domain.model.TestKind
import com.railprep.domain.repository.QuestionBookmarkRepository
import com.railprep.domain.util.DomainResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "RailPrepBookmarks"

enum class SavedQuestionFilter { ALL, WITH_NOTE, SECTIONALS, PYQ, DAILY_DIGEST }

data class SavedQuestionsUiState(
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val bookmarks: List<QuestionBookmark> = emptyList(),
    val filter: SavedQuestionFilter = SavedQuestionFilter.ALL,
    val error: String? = null,
)

@HiltViewModel
class SavedQuestionsViewModel @Inject constructor(
    private val questionBookmarkRepository: QuestionBookmarkRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SavedQuestionsUiState())
    val state: StateFlow<SavedQuestionsUiState> = _state.asStateFlow()

    init { refresh(showLoading = true) }

    fun setFilter(filter: SavedQuestionFilter) {
        _state.update { it.copy(filter = filter) }
    }

    fun refresh(showLoading: Boolean = false) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    loading = showLoading,
                    refreshing = !showLoading,
                    error = null,
                )
            }
            when (val result = questionBookmarkRepository.list()) {
                is DomainResult.Success -> _state.update {
                    it.copy(
                        loading = false,
                        refreshing = false,
                        bookmarks = result.value,
                    )
                }
                is DomainResult.Failure -> _state.update {
                    it.copy(
                        loading = false,
                        refreshing = false,
                        error = "load",
                    )
                }
            }
        }
    }

    fun remove(questionId: String) {
        val previous = _state.value.bookmarks
        _state.update { state ->
            state.copy(bookmarks = state.bookmarks.filterNot { it.questionId == questionId })
        }
        viewModelScope.launch {
            when (questionBookmarkRepository.remove(questionId)) {
                is DomainResult.Success -> Log.i(TAG, "remove questionId=$questionId source=saved_list")
                is DomainResult.Failure -> _state.update { it.copy(bookmarks = previous) }
            }
        }
    }
}

fun List<QuestionBookmark>.filteredFor(filter: SavedQuestionFilter): List<QuestionBookmark> = when (filter) {
    SavedQuestionFilter.ALL -> this
    SavedQuestionFilter.WITH_NOTE -> filter { !it.note.isNullOrBlank() }
    SavedQuestionFilter.SECTIONALS -> filter { it.test.kind == TestKind.SECTIONAL }
    SavedQuestionFilter.PYQ -> filter { it.test.kind == TestKind.PYQ }
    SavedQuestionFilter.DAILY_DIGEST -> filter { it.test.kind == TestKind.DAILY_DIGEST }
}
