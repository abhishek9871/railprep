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
    val searchQuery: String = "",
    val selectionMode: Boolean = false,
    val selectedQuestionIds: Set<String> = emptySet(),
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

    fun setSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun startSelection(questionId: String) {
        _state.update {
            it.copy(
                selectionMode = true,
                selectedQuestionIds = it.selectedQuestionIds + questionId,
            )
        }
    }

    fun toggleSelected(questionId: String) {
        _state.update { state ->
            val next = if (questionId in state.selectedQuestionIds) {
                state.selectedQuestionIds - questionId
            } else {
                state.selectedQuestionIds + questionId
            }
            state.copy(
                selectionMode = next.isNotEmpty(),
                selectedQuestionIds = next,
            )
        }
    }

    fun clearSelection() {
        _state.update { it.copy(selectionMode = false, selectedQuestionIds = emptySet()) }
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
                    val nextSelected = it.selectedQuestionIds.intersect(result.value.map { bookmark -> bookmark.questionId }.toSet())
                    it.copy(
                        loading = false,
                        refreshing = false,
                        bookmarks = result.value,
                        selectedQuestionIds = nextSelected,
                        selectionMode = nextSelected.isNotEmpty(),
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
            state.copy(
                bookmarks = state.bookmarks.filterNot { it.questionId == questionId },
                selectedQuestionIds = state.selectedQuestionIds - questionId,
                selectionMode = (state.selectedQuestionIds - questionId).isNotEmpty(),
            )
        }
        viewModelScope.launch {
            when (questionBookmarkRepository.remove(questionId)) {
                is DomainResult.Success -> Log.i(TAG, "remove questionId=$questionId source=saved_list")
                is DomainResult.Failure -> _state.update { it.copy(bookmarks = previous) }
            }
        }
    }

    fun removeSelected() {
        val ids = _state.value.selectedQuestionIds
        if (ids.isEmpty()) return
        val previous = _state.value.bookmarks
        _state.update { state ->
            state.copy(
                bookmarks = state.bookmarks.filterNot { it.questionId in ids },
                selectionMode = false,
                selectedQuestionIds = emptySet(),
            )
        }
        viewModelScope.launch {
            val failed = mutableSetOf<String>()
            ids.forEach { questionId ->
                when (questionBookmarkRepository.remove(questionId)) {
                    is DomainResult.Success -> Log.i(TAG, "remove questionId=$questionId source=saved_bulk")
                    is DomainResult.Failure -> failed += questionId
                }
            }
            if (failed.isNotEmpty()) {
                _state.update { it.copy(bookmarks = previous, error = "remove") }
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

fun List<QuestionBookmark>.searchedFor(query: String, useHi: Boolean): List<QuestionBookmark> {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return this
    return filter { bookmark ->
        val question = bookmark.question
        val haystack = buildString {
            append(question.localizedStem(useHi)).append(' ')
            append(question.stemEn).append(' ')
            question.stemHi?.let { append(it).append(' ') }
            question.options.forEach { option ->
                append(option.textEn).append(' ')
                option.textHi?.let { append(it).append(' ') }
            }
            bookmark.note?.let { append(it).append(' ') }
            append(bookmark.sourceLabel(useHi))
        }
        haystack.contains(trimmed, ignoreCase = true)
    }
}
