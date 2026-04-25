package com.railprep.feature.home.profile.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.railprep.domain.model.Topic
import com.railprep.domain.repository.BookmarkRepository
import com.railprep.domain.util.DomainResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BookmarksUiState(
    val loading: Boolean = true,
    val topics: List<Topic> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(BookmarksUiState())
    val state: StateFlow<BookmarksUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            when (val r = bookmarkRepository.listBookmarks()) {
                is DomainResult.Success -> _state.update {
                    it.copy(loading = false, topics = r.value)
                }
                is DomainResult.Failure -> _state.update {
                    it.copy(loading = false, error = "Couldn't load bookmarks.")
                }
            }
        }
    }
}
