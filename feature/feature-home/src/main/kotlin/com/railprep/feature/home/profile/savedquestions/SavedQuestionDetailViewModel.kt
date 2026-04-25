package com.railprep.feature.home.profile.savedquestions

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.railprep.domain.model.QuestionBookmark
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

data class SavedQuestionDetailUiState(
    val loading: Boolean = true,
    val bookmark: QuestionBookmark? = null,
    val noteDraft: String = "",
    val saving: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class SavedQuestionDetailViewModel @Inject constructor(
    private val questionBookmarkRepository: QuestionBookmarkRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SavedQuestionDetailUiState())
    val state: StateFlow<SavedQuestionDetailUiState> = _state.asStateFlow()

    fun load(questionId: String) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            when (val result = questionBookmarkRepository.list()) {
                is DomainResult.Success -> {
                    val bookmark = result.value.firstOrNull { it.questionId == questionId }
                    _state.update {
                        it.copy(
                            loading = false,
                            bookmark = bookmark,
                            noteDraft = bookmark?.note.orEmpty(),
                            error = if (bookmark == null) "missing" else null,
                        )
                    }
                }
                is DomainResult.Failure -> _state.update {
                    it.copy(loading = false, error = "load")
                }
            }
        }
    }

    fun updateNoteDraft(value: String) {
        _state.update { it.copy(noteDraft = value) }
    }

    fun saveNote() {
        val bookmark = _state.value.bookmark ?: return
        val note = _state.value.noteDraft.trim().takeIf { it.isNotEmpty() }
        viewModelScope.launch {
            _state.update { it.copy(saving = true, error = null) }
            when (questionBookmarkRepository.updateNote(bookmark.questionId, note)) {
                is DomainResult.Success -> {
                    Log.i(TAG, "edit questionId=${bookmark.questionId} source=saved_detail")
                    _state.update {
                        it.copy(
                            saving = false,
                            bookmark = bookmark.copy(note = note),
                            noteDraft = note.orEmpty(),
                        )
                    }
                }
                is DomainResult.Failure -> _state.update {
                    it.copy(saving = false, error = "save")
                }
            }
        }
    }

    fun remove(onRemoved: () -> Unit) {
        val bookmark = _state.value.bookmark ?: return
        viewModelScope.launch {
            _state.update { it.copy(saving = true, error = null) }
            when (questionBookmarkRepository.remove(bookmark.questionId)) {
                is DomainResult.Success -> {
                    Log.i(TAG, "remove questionId=${bookmark.questionId} source=saved_detail")
                    onRemoved()
                }
                is DomainResult.Failure -> _state.update {
                    it.copy(saving = false, error = "remove")
                }
            }
        }
    }
}
