package com.railprep.feature.auth.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.railprep.core.design.RailPrepTheme
import com.railprep.core.design.tokens.Spacing
import com.railprep.core.design.tokens.TouchTarget
import com.railprep.feature.auth.R

@Composable
fun AuthScreen(
    webClientId: String,
    onAuthenticated: () -> Unit,
    onEmailClicked: () -> Unit,
    onDiagnosticsRequested: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val activity = LocalContext.current.findActivity()

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    AuthContent(
        googleLoading = state.googleLoading,
        onGoogleClick = {
            if (activity != null) {
                viewModel.startGoogleSignIn(activity, webClientId, onAuthenticated)
            }
        },
        onEmailClick = onEmailClicked,
        onTitleLongPress = onDiagnosticsRequested,
        snackbarHostState = snackbarHostState,
    )
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
internal fun AuthContent(
    googleLoading: Boolean,
    onGoogleClick: () -> Unit,
    onEmailClick: () -> Unit,
    snackbarHostState: SnackbarHostState,
    onTitleLongPress: () -> Unit = {},
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Spacing.Lg, vertical = Spacing.Xl),
        ) {
            Spacer(Modifier.weight(0.4f))

            Text(
                stringResource(R.string.auth_title),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.pointerInput(Unit) {
                    detectTapGestures(onLongPress = { onTitleLongPress() })
                },
            )
            Spacer(Modifier.size(Spacing.Xs))
            Text(
                stringResource(R.string.auth_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.weight(1f))

            Button(
                onClick = onGoogleClick,
                enabled = !googleLoading,
                modifier = Modifier.fillMaxWidth().heightIn(min = TouchTarget.Min),
            ) {
                if (googleLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.size(Spacing.Xs))
                }
                Text(stringResource(R.string.auth_continue_google))
            }

            Spacer(Modifier.size(Spacing.Sm))

            OutlinedButton(
                onClick = onEmailClick,
                modifier = Modifier.fillMaxWidth().heightIn(min = TouchTarget.Min),
            ) {
                Text(stringResource(R.string.auth_sign_in_email))
            }

            Spacer(Modifier.size(Spacing.Md))

            Text(
                stringResource(R.string.auth_legal_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.weight(0.1f))
        }
    }
}

@Preview(widthDp = 360, heightDp = 780)
@Composable
private fun AuthPreview() {
    RailPrepTheme {
        AuthContent(
            googleLoading = false,
            onGoogleClick = {},
            onEmailClick = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
