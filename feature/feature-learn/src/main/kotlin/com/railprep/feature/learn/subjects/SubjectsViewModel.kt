package com.railprep.feature.learn.subjects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.railprep.domain.model.Subject
import com.railprep.domain.repository.LearnRepository
import com.railprep.domain.util.DomainResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubjectsUiState(
    val loading: Boolean = true,
    val subjects: List<Subject> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class SubjectsViewModel @Inject constructor(
    private val learnRepository: LearnRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SubjectsUiState())
    val state: StateFlow<SubjectsUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            when (val r = learnRepository.listSubjects()) {
                is DomainResult.Success -> _state.update {
                    it.copy(loading = false, subjects = r.value)
                }
                is DomainResult.Failure -> _state.update {
                    it.copy(loading = false, error = "Couldn't load subjects. Pull to retry.")
                }
            }
        }
    }
}
