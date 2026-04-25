package com.railprep.feature.auth.onboarding

import com.railprep.domain.repository.LanguageRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val langRepo = mockk<LanguageRepository>(relaxed = true)

    @BeforeEach fun setUp() { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    @AfterEach  fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `onCompleted marks onboarding seen and fires callback`() = runTest {
        val vm = OnboardingViewModel(langRepo)
        var fired = false
        vm.onCompleted { fired = true }
        coVerify { langRepo.markOnboardingSeen() }
        assertEquals(true, fired)
    }
}
