package com.railprep.feature.auth.language

import app.cash.turbine.test
import com.railprep.domain.model.SupportedLanguage
import com.railprep.domain.repository.LanguageRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LanguageViewModelTest {

    private val langRepo = mockk<LanguageRepository>(relaxed = true)

    @BeforeEach fun setUp() { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    @AfterEach  fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `picking a supported language selects it`() = runTest {
        val vm = LanguageViewModel(langRepo)
        vm.onLanguageClicked(SupportedLanguage.Hi)
        assertEquals(SupportedLanguage.Hi, vm.selected.value)
    }

    @Test
    fun `picking an unsupported language falls back to English and emits message`() = runTest {
        val vm = LanguageViewModel(langRepo)
        vm.messages.test {
            vm.onLanguageClicked(SupportedLanguage.Bn)
            val msg = awaitItem()
            assertEquals(SupportedLanguage.En, vm.selected.value)
            assertEquals(true, msg is LanguageMessage.FallingBackToEnglish)
        }
    }

    @Test
    fun `continue persists selected language`() = runTest {
        coEvery { langRepo.setLanguage(any()) } returns Unit
        val vm = LanguageViewModel(langRepo)
        vm.onLanguageClicked(SupportedLanguage.Hi)
        var doneFired = false
        vm.onContinueClicked { doneFired = true }
        coVerify { langRepo.setLanguage(SupportedLanguage.Hi) }
        assertEquals(true, doneFired)
    }
}
