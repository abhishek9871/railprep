package com.railprep.feature.auth.email

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.railprep.core.design.RailPrepTheme
import com.railprep.core.design.theme.Canvas
import com.railprep.core.design.theme.Line
import com.railprep.core.design.theme.Primary
import com.railprep.core.design.theme.SurfaceWhite
import com.railprep.core.design.tokens.Spacing
import com.railprep.core.design.tokens.TouchTarget
import com.railprep.feature.auth.R
import com.railprep.feature.auth.common.AuthBrandLockup
import com.railprep.feature.auth.common.AuthEntryBackdrop
import com.railprep.feature.auth.common.AuthStatRow

@Composable
fun EmailAuthScreen(
    onAuthenticated: () -> Unit,
    onForgotPasswordRequested: (email: String) -> Unit,
    onBack: () -> Unit,
    viewModel: EmailAuthViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.info) {
        // RESET_EMAIL_SENT -> navigate to the OTP entry screen. ACCOUNT_CREATED_CONFIRM_EMAIL
        // stays as a persistent banner so the user doesn't keep hammering Create account.
        if (state.info == EmailAuthViewModel.INFO_RESET_EMAIL_SENT) {
            onForgotPasswordRequested(EmailAuthViewModel.normalizedEmail(state.email))
            viewModel.clearInfo()
        }
    }
    LaunchedEffect(state.generalError) {
        state.generalError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    EmailAuthContent(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onModeChange = viewModel::onModeChange,
        onSubmit = { viewModel.submit(onAuthenticated) },
        onReset = viewModel::sendReset,
        onBack = onBack,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
internal fun EmailAuthContent(
    state: EmailAuthUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onModeChange: (EmailMode) -> Unit,
    onSubmit: () -> Unit,
    onReset: () -> Unit,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val focusManager = LocalFocusManager.current
    var passwordVisible by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Canvas,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Canvas),
        ) {
            AuthEntryBackdrop(modifier = Modifier.fillMaxSize())

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding()
                    .verticalScroll(scrollState)
                    .padding(horizontal = Spacing.Lg, vertical = Spacing.Xl),
                verticalArrangement = Arrangement.spacedBy(Spacing.Md),
            ) {
                AuthBrandLockup()

                Text(
                    text = stringResource(R.string.email_title),
                    style = MaterialTheme.typography.displayMedium.copy(letterSpacing = 0.sp),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = stringResource(R.string.email_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AuthStatRow(modifier = Modifier.padding(top = Spacing.Xs))

                Surface(
                    color = SurfaceWhite,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Line),
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column {
                        TabRow(
                            selectedTabIndex = state.mode.ordinal,
                            containerColor = SurfaceWhite,
                            contentColor = Primary,
                        ) {
                            EmailMode.entries.forEach { mode ->
                                Tab(
                                    selected = state.mode == mode,
                                    onClick = { onModeChange(mode) },
                                    text = {
                                        Text(
                                            when (mode) {
                                                EmailMode.SignIn -> stringResource(R.string.email_tab_sign_in)
                                                EmailMode.CreateAccount -> stringResource(R.string.email_tab_create_account)
                                            },
                                            style = MaterialTheme.typography.titleSmall,
                                        )
                                    },
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spacing.Md),
                            verticalArrangement = Arrangement.spacedBy(Spacing.Md),
                        ) {
                            if (state.info == EmailAuthViewModel.INFO_ACCOUNT_CREATED_CONFIRM_EMAIL) {
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text(
                                        text = stringResource(R.string.email_banner_confirm),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(Spacing.Md),
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = state.email,
                                onValueChange = onEmailChange,
                                label = { Text(stringResource(R.string.email_field_email)) },
                                isError = state.emailError != null,
                                supportingText = state.emailError?.let { { Text(emailErrorText(it)) } },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Primary,
                                    focusedLabelColor = Primary,
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Next,
                                ),
                                modifier = Modifier.fillMaxWidth(),
                            )

                            OutlinedTextField(
                                value = state.password,
                                onValueChange = onPasswordChange,
                                label = { Text(stringResource(R.string.email_field_password)) },
                                isError = state.passwordError != null,
                                supportingText = {
                                    Text(
                                        state.passwordError?.let { passwordErrorText(it) }
                                            ?: if (state.mode == EmailMode.CreateAccount) {
                                                stringResource(R.string.password_helper_create)
                                            } else {
                                                stringResource(R.string.password_helper_sign_in)
                                            },
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Primary,
                                    focusedLabelColor = Primary,
                                ),
                                visualTransformation = if (passwordVisible) {
                                    VisualTransformation.None
                                } else {
                                    PasswordVisualTransformation()
                                },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) {
                                                Icons.Filled.VisibilityOff
                                            } else {
                                                Icons.Filled.Visibility
                                            },
                                            contentDescription = stringResource(R.string.password_toggle_visibility),
                                        )
                                    }
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done,
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        onSubmit()
                                    },
                                ),
                                modifier = Modifier.fillMaxWidth(),
                            )

                            Button(
                                onClick = onSubmit,
                                enabled = !state.loading,
                                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                modifier = Modifier.fillMaxWidth().heightIn(min = TouchTarget.Min + Spacing.Sm),
                            ) {
                                if (state.loading) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(20.dp),
                                    )
                                    Spacer(Modifier.size(Spacing.Xs))
                                }
                                Text(
                                    when (state.mode) {
                                        EmailMode.SignIn -> stringResource(R.string.email_submit_sign_in)
                                        EmailMode.CreateAccount -> stringResource(R.string.email_submit_create_account)
                                    },
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                )
                            }

                            if (state.mode == EmailMode.SignIn) {
                                TextButton(onClick = onReset, modifier = Modifier.fillMaxWidth()) {
                                    Text(stringResource(R.string.email_forgot_password))
                                }
                            }
                        }
                    }
                }

                TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.email_back))
                }
            }
        }
    }
}

@Composable
private fun emailErrorText(code: String): String = when (code) {
    "EMPTY" -> stringResource(R.string.email_err_empty)
    "INVALID" -> stringResource(R.string.email_err_invalid)
    else -> code
}

@Composable
private fun passwordErrorText(code: String): String = when (code) {
    "EMPTY" -> stringResource(R.string.password_err_empty)
    "TOO_SHORT" -> stringResource(R.string.password_err_too_short)
    "WEAK" -> stringResource(R.string.password_err_weak)
    else -> code
}

@Preview(widthDp = 360, heightDp = 780)
@Composable
private fun EmailAuthPreview() {
    RailPrepTheme {
        EmailAuthContent(
            state = EmailAuthUiState(),
            onEmailChange = {},
            onPasswordChange = {},
            onModeChange = {},
            onSubmit = {},
            onReset = {},
            onBack = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
