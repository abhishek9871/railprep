package com.railprep.feature.home.profile.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.railprep.domain.model.ExamGoal
import com.railprep.domain.model.ExamTarget
import com.railprep.domain.model.Profile
import com.railprep.domain.model.Qualification
import com.railprep.domain.model.Category
import com.railprep.domain.repository.ProfileRepository
import com.railprep.domain.util.DomainResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileEditUiState(
    val loading: Boolean = true,
    val displayName: String = "",
    val dailyMinutes: Int = 60,
    val savedOnce: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class ProfileEditViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileEditUiState())
    val state: StateFlow<ProfileEditUiState> = _state.asStateFlow()

    private var original: Profile? = null

    init { load() }

    private fun load() {
        viewModelScope.launch {
            when (val r = profileRepository.getCurrentProfile()) {
                is DomainResult.Success -> {
                    original = r.value
                    _state.update {
                        it.copy(
                            loading = false,
                            displayName = r.value.displayName ?: "",
                            dailyMinutes = r.value.dailyMinutes,
                        )
                    }
                }
                is DomainResult.Failure -> _state.update {
                    it.copy(loading = false, error = "Couldn't load profile.")
                }
            }
        }
    }

    fun onNameChange(v: String) = _state.update { it.copy(displayName = v) }
    fun onMinutesChange(v: Int) = _state.update { it.copy(dailyMinutes = v.coerceIn(15, 240)) }

    fun save(onSaved: () -> Unit) {
        val o = original ?: return
        val s = _state.value
        viewModelScope.launch {
            val goal = ExamGoal(
                displayName = s.displayName.takeIf { it.isNotBlank() },
                examTarget = o.examTarget ?: ExamTarget.NtpcCbt1,
                examTargetDate = o.examTargetDate ?: return@launch,
                dailyMinutes = s.dailyMinutes,
                qualification = o.qualification ?: Qualification.Twelfth,
                category = o.category ?: Category.UR,
                dob = o.dob ?: return@launch,
            )
            when (profileRepository.completeOnboarding(goal)) {
                is DomainResult.Success -> {
                    _state.update { it.copy(savedOnce = true) }
                    onSaved()
                }
                is DomainResult.Failure -> _state.update {
                    it.copy(error = "Couldn't save.")
                }
            }
        }
    }

    fun clearError() = _state.update { it.copy(error = null) }
}
