package com.railprep.feature.auth.splash

import com.railprep.domain.model.SupportedLanguage
import com.railprep.domain.model.User
import com.railprep.domain.repository.AuthRepository
import com.railprep.domain.repository.LanguageRepository
import com.railprep.domain.repository.ProfileRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {

    private val authRepo = mockk<AuthRepository>(relaxed = true)
    private val profileRepo = mockk<ProfileRepository>(relaxed = true)
    private val languageRepo = mockk<LanguageRepository>(relaxed = true)

    @BeforeEach fun setUp() { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    @AfterEach  fun tearDown() { Dispatchers.resetMain() }

    private fun newVm() = SplashViewModel(authRepo, profileRepo, languageRepo)

    @Test
    fun `no language yet routes to Language`() {
        every { languageRepo.currentSync() } returns null
        assertEquals(SplashDestination.Language, newVm().decideRoute())
    }

    @Test
    fun `language set but onboarding unseen routes to Onboarding`() {
        every { languageRepo.currentSync() } returns SupportedLanguage.En
        every { languageRepo.hasSeenOnboardingSync() } returns false
        assertEquals(SplashDestination.Onboarding, newVm().decideRoute())
    }

    @Test
    fun `onboarding seen but no session routes to Auth`() {
        every { languageRepo.currentSync() } returns SupportedLanguage.En
        every { languageRepo.hasSeenOnboardingSync() } returns true
        every { authRepo.currentUserSync() } returns null
        assertEquals(SplashDestination.Auth, newVm().decideRoute())
    }

    @Test
    fun `session present but goal unfinished routes to Goal`() {
        every { languageRepo.currentSync() } returns SupportedLanguage.Hi
        every { languageRepo.hasSeenOnboardingSync() } returns true
        every { authRepo.currentUserSync() } returns User("u1", "a@b.com", null, null)
        every { profileRepo.cachedOnboardingComplete() } returns false
        assertEquals(SplashDestination.Goal, newVm().decideRoute())
    }

    @Test
    fun `fully onboarded user routes to Home`() {
        every { languageRepo.currentSync() } returns SupportedLanguage.En
        every { languageRepo.hasSeenOnboardingSync() } returns true
        every { authRepo.currentUserSync() } returns User("u1", "a@b.com", "X", null)
        every { profileRepo.cachedOnboardingComplete() } returns true
        assertEquals(SplashDestination.Home, newVm().decideRoute())
    }
}
