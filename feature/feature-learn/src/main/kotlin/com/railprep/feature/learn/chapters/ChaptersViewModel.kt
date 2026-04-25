package com.railprep.feature.learn.chapters

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.railprep.domain.model.Chapter
import com.railprep.domain.model.Topic
import com.railprep.domain.repository.LearnRepository
import com.railprep.domain.util.DomainResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChaptersUiState(
    val loading: Boolean = true,
    val chapters: List<Chapter> = emptyList(),
    /** topics[chapter.id] → topic list */
    val topics: Map<String, List<Topic>> = emptyMap(),
    val error: String? = null,
)

@HiltViewModel
class ChaptersViewModel @Inject constructor(
    private val learnRepository: LearnRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ChaptersUiState())
    val state: StateFlow<ChaptersUiState> = _state.asStateFlow()

    fun load(subjectId: String) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            when (val r = learnRepository.listChapters(subjectId)) {
                is DomainResult.Success -> {
                    val chapters = r.value
                    // Fetch topics per chapter (sequential — small lists on Phase 2).
                    val topicsMap = mutableMapOf<String, List<Topic>>()
                    for (chapter in chapters) {
                        when (val tr = learnRepository.listTopics(chapter.id)) {
                            is DomainResult.Success -> topicsMap[chapter.id] = tr.value
                            is DomainResult.Failure -> topicsMap[chapter.id] = emptyList()
                        }
                    }
                    _state.update {
                        it.copy(loading = false, chapters = chapters, topics = topicsMap)
                    }
                }
                is DomainResult.Failure -> _state.update {
                    it.copy(loading = false, error = "Couldn't load chapters.")
                }
            }
        }
    }
}
