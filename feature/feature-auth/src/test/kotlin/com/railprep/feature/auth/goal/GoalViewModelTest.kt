package com.railprep.feature.auth.goal

import com.railprep.domain.model.Category
import com.railprep.domain.model.ExamTarget
import com.railprep.domain.model.Qualification
import com.railprep.domain.repository.AuthRepository
import com.railprep.domain.repository.ProfileRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GoalViewModelTest {

    private val profileRepo = mockk<ProfileRepository>(relaxed = true)
    private val authRepo = mockk<AuthRepository>(relaxed = true)

    @BeforeEach fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { authRepo.currentUserSync() } returns null
    }
    @AfterEach fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `empty state is invalid - all five required fields flagged`() {
        val vm = GoalViewModel(profileRepo, authRepo)
        val errs = vm.validate(vm.uiState.value)
        assertEquals(5, errs.size)  // examTarget, examTargetDate, qualification, category, dob (dailyMinutes default 60 OK)
    }

    @Test
    fun `target date must be within 30 days and 2 years`() {
        val vm = GoalViewModel(profileRepo, authRepo)
        val tooSoon = vm.targetDateMin.plus(DatePeriod(days = -1))
        val tooLate = vm.targetDateMax.plus(DatePeriod(days = 1))

        vm.onExamTargetDateChange(tooSoon)
        assertEquals("OUT_OF_RANGE", vm.validate(vm.uiState.value)["examTargetDate"])

        vm.onExamTargetDateChange(tooLate)
        assertEquals("OUT_OF_RANGE", vm.validate(vm.uiState.value)["examTargetDate"])

        vm.onExamTargetDateChange(vm.targetDateMin.plus(DatePeriod(days = 1)))
        assertNull(vm.validate(vm.uiState.value)["examTargetDate"])
    }

    @Test
    fun `dob must be at least 18 years ago`() {
        val vm = GoalViewModel(profileRepo, authRepo)
        // exactly 18y ago -> OK
        vm.onDobChange(vm.dobMaxAllowed)
        assertNull(vm.validate(vm.uiState.value)["dob"])
        // 1 day short of 18y -> UNDER_18
        vm.onDobChange(vm.dobMaxAllowed.plus(DatePeriod(days = 1)))
        assertEquals("UNDER_18", vm.validate(vm.uiState.value)["dob"])
    }

    @Test
    fun `filling all fields validly enables submit`() {
        val vm = GoalViewModel(profileRepo, authRepo)
        vm.onExamTargetChange(ExamTarget.NtpcCbt1)
        vm.onExamTargetDateChange(vm.targetDateMin.plus(DatePeriod(days = 30)))
        vm.onDailyMinutesChange(60)
        vm.onQualificationChange(Qualification.Graduate)
        vm.onCategoryChange(Category.UR)
        vm.onDobChange(vm.dobMaxAllowed)
        assertEquals(true, vm.uiState.value.submitEnabled)
    }
}
