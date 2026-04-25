package com.railprep.feature.daily.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.railprep.core.design.tokens.Radius
import com.railprep.core.design.tokens.Spacing
import com.railprep.feature.daily.R

@Composable
fun DailyHomeCard(
    onOpenPlayer: () -> Unit,
    onOpenReview: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DailyHomeCardViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(Radius.Md),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(Spacing.Md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.daily_card_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f),
                )
                StreakChip(streak = state.profile?.streakCurrent ?: 0)
            }
            Spacer(Modifier.size(Spacing.Xs))
            val subtitle = when {
                state.loading -> " "
                state.alreadyDone -> stringResource(
                    R.string.daily_card_subtitle_done_fmt,
                    state.correctCount, state.total,
                )
                state.error != null -> stringResource(R.string.daily_card_load_error)
                else -> stringResource(R.string.daily_card_subtitle_unstarted)
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(Modifier.size(Spacing.Sm))
            if (state.alreadyDone) {
                OutlinedButton(
                    onClick = onOpenReview,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.daily_card_cta_review))
                }
            } else {
                Button(
                    onClick = onOpenPlayer,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.daily_card_cta_start))
                }
            }
        }
    }
}

@Composable
private fun StreakChip(streak: Int) {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = RoundedCornerShape(Radius.Xs),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.Sm, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.Xxs),
        ) {
            Icon(
                Icons.Filled.LocalFireDepartment,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            val label = if (streak > 0)
                stringResource(R.string.daily_streak_label_fmt, streak)
            else stringResource(R.string.daily_streak_no_streak)
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        }
    }
}

