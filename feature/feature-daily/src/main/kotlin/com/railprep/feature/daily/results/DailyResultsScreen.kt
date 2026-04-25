package com.railprep.feature.daily.results

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.railprep.core.design.tokens.Spacing
import com.railprep.feature.daily.R
import com.railprep.feature.notifications.NotificationsOptInPrompt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyResultsScreen(
    onReview: () -> Unit,
    onDone: () -> Unit,
    viewModel: DailyResultsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.daily_results_title)) }) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        when {
            state.loading -> Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }
            state.attempt == null -> Box(
                Modifier.fillMaxSize().padding(innerPadding).padding(Spacing.Lg),
                contentAlignment = Alignment.Center,
            ) { Text("No attempt yet.") }
            else -> Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(Spacing.Lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.Md),
            ) {
                val attempt = state.attempt!!
                Text(
                    stringResource(R.string.daily_results_score_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    stringResource(R.string.daily_results_of_fmt, attempt.correctCount, attempt.total),
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary,
                )
                val profile = state.profile
                if (profile != null) {
                    val streakText = if (profile.streakCurrent > 1)
                        stringResource(R.string.daily_results_streak_extended_fmt, profile.streakCurrent)
                    else stringResource(R.string.daily_results_streak_started)
                    Text(
                        text = streakText,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
                Spacer(Modifier.weight(1f))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.Sm),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    OutlinedButton(onClick = onDone, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.daily_results_done))
                    }
                    Button(onClick = onReview, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.daily_results_review_cta))
                    }
                }
            }
        }

        NotificationsOptInPrompt(
            visible = state.showNotificationsOptIn,
            onAccepted = { viewModel.acceptNotifications() },
            onDismissed = { viewModel.dismissNotificationsOptIn() },
        )
    }
}
