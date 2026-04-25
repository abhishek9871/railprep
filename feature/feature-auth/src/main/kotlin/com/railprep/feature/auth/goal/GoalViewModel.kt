package com.railprep.feature.auth.goal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.railprep.domain.model.Category
import com.railprep.domain.model.ExamGoal
import com.railprep.domain.model.ExamTarget
import com.railprep.domain.model.Qualification
import com.railprep.domain.repository.AuthRepository
import com.railprep.domain.repository.ProfileRepository
import com.railprep.domain.util.DomainResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import javax.inject.Inject

data class GoalUiState(
    val displayName: String = "",
    val examTarget: ExamTarget? = null,
    val examTargetDate: LocalDate? = null,
    val dailyMinutes: Int = 60,
    val qualification: Qualification? = null,
    val category: Category? = null,
    val dob: LocalDate? = null,
    val loading: Boolean = false,
    val submitEnabled: Boolean = false,
    val errors: Map<String, String> = emptyMap(),
    val done: Boolean = false,
)

@HiltViewModel
class GoalViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalUiState())
    val uiState: StateFlow<GoalUiState> = _uiState.asStateFlow()

    private val today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val targetDateMin: LocalDate = today.plus(DatePeriod(days = 30))
    val targetDateMax: LocalDate = today.plus(DatePeriod(years = 2))
    val dobMaxAllowed: LocalDate = today.minus(DatePeriod(years = 18))

    init {
        prefillFromAuth()
    }

    private fun prefillFromAuth() {
        val name = authRepository.currentUserSync()?.displayName
        if (!name.isNullOrBlank()) {
            mutate { it.copy(displayName = name) }
        }
    }

    fun onDisplayNameChange(v: String) = mutate { it.copy(displayName = v).revalidate() }
    fun onExamTargetChange(v: ExamTarget) = mutate { it.copy(examTarget = v).revalidate() }
    fun onExamTargetDateChange(v: LocalDate) = mutate { it.copy(examTargetDate = v).revalidate() }
    fun onDailyMinutesChange(v: Int) = mutate { it.copy(dailyMinutes = v.coerceIn(30, 240)).revalidate() }
    fun onQualificationChange(v: Qualification) = mutate { it.copy(qualification = v).revalidate() }
    fun onCategoryChange(v: Category) = mutate { it.copy(category = v).revalidate() }
    fun onDobChange(v: LocalDate) = mutate { it.copy(dob = v).revalidate() }

    fun onSubmit(onCompleted: () -> Unit) {
        val s = _uiState.value
        val errs = validate(s)
        if (errs.isNotEmpty()) {
            mutate { it.copy(errors = errs) }
            return
        }
        val goal = ExamGoal(
            examTarget = s.examTarget!!,
            examTargetDate = s.examTargetDate!!,
            dailyMinutes = s.dailyMinutes,
            qualification = s.qualification!!,
            category = s.category!!,
            dob = s.dob!!,
            displayName = s.displayName.trim().ifBlank { null },
        )
        viewModelScope.launch {
            mutate { it.copy(loading = true, errors = emptyMap()) }
            when (val r = profileRepository.completeOnboarding(goal)) {
                is DomainResult.Success -> {
                    mutate { it.copy(loading = false, done = true) }
                    onCompleted()
                }
                is DomainResult.Failure -> {
                    mutate { it.copy(loading = false, errors = mapOf("_general" to r.error.message)) }
                }
            }
        }
    }

    private fun GoalUiState.revalidate(): GoalUiState = copy(
        submitEnabled = validate(this).isEmpty(),
        errors = emptyMap(),
    )

    internal fun validate(s: GoalUiState): Map<String, String> = buildMap {
        if (s.examTarget == null) put("examTarget", "REQUIRED")
        if (s.examTargetDate == null) put("examTargetDate", "REQUIRED")
        else if (s.examTargetDate < targetDateMin || s.examTargetDate > targetDateMax) put("examTargetDate", "OUT_OF_RANGE")
        if (s.dailyMinutes < 30 || s.dailyMinutes > 240) put("dailyMinutes", "OUT_OF_RANGE")
        if (s.qualification == null) put("qualification", "REQUIRED")
        if (s.category == null) put("category", "REQUIRED")
        if (s.dob == null) put("dob", "REQUIRED")
        else if (s.dob > dobMaxAllowed) put("dob", "UNDER_18")
    }

    private fun mutate(transform: (GoalUiState) -> GoalUiState) {
        _uiState.value = transform(_uiState.value)
    }
}
