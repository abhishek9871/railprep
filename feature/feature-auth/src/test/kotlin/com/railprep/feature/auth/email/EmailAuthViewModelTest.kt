package com.railprep.feature.auth.email

import com.railprep.domain.model.User
import com.railprep.domain.repository.AuthRepository
import com.railprep.domain.util.DomainError
import com.railprep.domain.util.DomainResult
import io.mockk.coEvery
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
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EmailAuthViewModelTest {

    private val authRepo = mockk<AuthRepository>(relaxed = true)

    @BeforeEach fun setUp() { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    @AfterEach  fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `email validation catches empty and invalid`() {
        assertEquals("EMPTY", EmailAuthViewModel.validateEmail(""))
        assertEquals("INVALID", EmailAuthViewModel.validateEmail("foo"))
        assertEquals("INVALID", EmailAuthViewModel.validateEmail("foo@bar"))
        assertNull(EmailAuthViewModel.validateEmail("a@b.co"))
    }

    @Test
    fun `password validation enforces min 8`() {
        assertEquals("EMPTY", EmailAuthViewModel.validatePassword(""))
        assertEquals("TOO_SHORT", EmailAuthViewModel.validatePassword("1234567"))
        assertNull(EmailAuthViewModel.validatePassword("12345678"))
    }

    @Test
    fun `submit with bad inputs populates error fields and does not call repo`() = runTest {
        val vm = EmailAuthViewModel(authRepo)
        vm.onEmailChange("bad")
        vm.onPasswordChange("x")
        vm.submit { /* should not fire */ }
        val s = vm.uiState.value
        assertEquals("INVALID", s.emailError)
        assertEquals("TOO_SHORT", s.passwordError)
        coVerify(exactly = 0) { authRepo.signInWithEmail(any(), any()) }
    }

    @Test
    fun `sign-in success invokes onAuthenticated`() = runTest {
        coEvery { authRepo.signInWithEmail("a@b.co", "12345678") } returns
            DomainResult.Success(User("u", "a@b.co", null, null))

        val vm = EmailAuthViewModel(authRepo)
        vm.onEmailChange("a@b.co")
        vm.onPasswordChange("12345678")
        var ok = false
        vm.submit { ok = true }
        assertEquals(true, ok)
    }

    @Test
    fun `sign-in failure sets generalError`() = runTest {
        coEvery { authRepo.signInWithEmail(any(), any()) } returns
            DomainResult.Failure(DomainError.Auth("Invalid credentials"))

        val vm = EmailAuthViewModel(authRepo)
        vm.onEmailChange("a@b.co")
        vm.onPasswordChange("12345678")
        vm.submit { /* won't fire */ }
        assertEquals("Invalid credentials", vm.uiState.value.generalError)
    }

    @Test
    fun `create-account with empty id sets info flag for email confirmation`() = runTest {
        coEvery { authRepo.createAccountWithEmail(any(), any()) } returns
            DomainResult.Success(User("", "a@b.co", null, null))

        val vm = EmailAuthViewModel(authRepo)
        vm.onModeChange(EmailMode.CreateAccount)
        vm.onEmailChange("a@b.co")
        vm.onPasswordChange("12345678")
        vm.submit { /* should not fire when email-confirm required */ }
        assertEquals("ACCOUNT_CREATED_CONFIRM_EMAIL", vm.uiState.value.info)
    }

    @Test
    fun `sendReset sends email and flips info flag`() = runTest {
        coEvery { authRepo.sendPasswordReset("a@b.co") } returns DomainResult.Success(Unit)

        val vm = EmailAuthViewModel(authRepo)
        vm.onEmailChange("a@b.co")
        vm.sendReset()
        assertEquals("RESET_EMAIL_SENT", vm.uiState.value.info)
    }
}
