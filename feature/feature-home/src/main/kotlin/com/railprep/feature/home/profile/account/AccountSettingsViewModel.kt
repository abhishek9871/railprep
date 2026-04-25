package com.railprep.feature.home.profile.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.railprep.domain.repository.ProfileRepository
import com.railprep.domain.util.DomainResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountSettingsUiState(
    val busy: Boolean = false,
    val exportedJson: String? = null,
    val error: Boolean = false,
    val deleted: Boolean = false,
)

@HiltViewModel
class AccountSettingsViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AccountSettingsUiState())
    val state: StateFlow<AccountSettingsUiState> = _state.asStateFlow()

    fun exportData() {
        viewModelScope.launch {
            _state.update { it.copy(busy = true, error = false) }
            when (val r = profileRepository.exportMyData()) {
                is DomainResult.Success -> _state.update {
                    it.copy(busy = false, exportedJson = r.value, error = false)
                }
                is DomainResult.Failure -> _state.update { it.copy(busy = false, error = true) }
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _state.update { it.copy(busy = true, error = false) }
            when (profileRepository.deleteMyAccount()) {
                is DomainResult.Success -> _state.update {
                    it.copy(busy = false, deleted = true, error = false)
                }
                is DomainResult.Failure -> _state.update { it.copy(busy = false, error = true) }
            }
        }
    }
}
