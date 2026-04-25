package com.railprep.feature.tests.results

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.railprep.domain.model.SectionBreakdown
import com.railprep.domain.model.WeakTopicRecommendation
import com.railprep.feature.tests.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    attemptId: String,
    onReview: () -> Unit,
    onDone: () -> Unit,
    onOpenTopic: (topicId: String) -> Unit = {},
    viewModel: ResultsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(attemptId) { viewModel.load(attemptId) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.results_title)) })
        },
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
            ) { Text(stringResource(R.string.results_load_error)) }

            else -> ResultsBody(
                attempt = state.attempt!!,
                sectionTitles = state.sectionTitles,
                weakRecommendations = state.weakRecommendations,
                onReview = onReview,
                onDone = onDone,
                onOpenTopic = onOpenTopic,
                modifier = Modifier.fillMaxSize().padding(innerPadding),
            )
        }
    }
}

@Composable
private fun ResultsBody(
    attempt: com.railprep.domain.model.Attempt,
    sectionTitles: Map<String, String>,
    weakRecommendations: List<WeakTopicRecommendation>,
    onReview: () -> Unit,
    onDone: () -> Unit,
    onOpenTopic: (topicId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(Spacing.Lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.Md),
    ) {
        Text(
            stringResource(R.string.results_score_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            stringResource(
                R.string.results_of_fmt,
                fmtScore(attempt.score ?: 0f),
                fmtScore(attempt.maxScore ?: 0f),
            ),
            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary,
        )

        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(Radius.Md),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(Spacing.Md),
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                StatColumn(stringResource(R.string.results_breakdown_correct),
                           (attempt.correctCount ?: 0).toString(),
                           MaterialTheme.colorScheme.primary)
                StatColumn(stringResource(R.string.results_breakdown_wrong),
                           (attempt.wrongCount ?: 0).toString(),
                           MaterialTheme.colorScheme.error)
                StatColumn(stringResource(R.string.results_breakdown_skipped),
                           (attempt.skippedCount ?: 0).toString(),
                           MaterialTheme.colorScheme.onSurface)
            }
        }

        Text(
            stringResource(R.string.results_section_header),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        )
        attempt.sectionBreakdown.orEmpty().forEach { b ->
            SectionBar(
                title = sectionTitles[b.sectionId] ?: b.subjectHint.name,
                b = b,
            )
        }

        if (weakRecommendations.isNotEmpty()) {
            Spacer(Modifier.size(Spacing.Sm))
            WeakTopicCard(
                recommendations = weakRecommendations,
                onOpenTopic = onOpenTopic,
            )
        }

        Spacer(Modifier.weight(1f))
        Text(
            stringResource(R.string.results_percentile_wait),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.Sm)) {
            OutlinedButton(onClick = onDone, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.results_done))
            }
            Button(onClick = onReview, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.results_review_cta))
            }
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
             color = color)
        Text(label, style = MaterialTheme.typography.bodySmall,
             color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SectionBar(title: String, b: SectionBreakdown) {
    val fraction = if (b.maxScore > 0f) (b.score / b.maxScore).coerceIn(0f, 1f) else 0f
    // Zero-score sections render muted — a blue-accent track with an accent
    // dot at x=0 reads as "at max" at a glance, which is the opposite of
    // what actually happened. Mute the whole component for 0-fill.
    val isZero = b.score <= 0f
    val indicatorColor =
        if (isZero) MaterialTheme.colorScheme.outlineVariant
        else MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, style = MaterialTheme.typography.bodyMedium,
                 color = MaterialTheme.colorScheme.onSurface)
            Text(
                stringResource(
                    R.string.results_section_row_fmt,
                    title, fmtScore(b.score), fmtScore(b.maxScore),
                ).substringAfter("— "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        // Both color and trackColor are muted when score is zero; the M3 stop
        // indicator inherits from the progress colour, so at isZero it's also
        // the neutral outlineVariant tone — no "false full" signal.
        LinearProgressIndicator(
            progress = { fraction },
            color = indicatorColor,
            trackColor = trackColor,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun WeakTopicCard(
    recommendations: List<WeakTopicRecommendation>,
    onOpenTopic: (topicId: String) -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = RoundedCornerShape(Radius.Md),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(Spacing.Md),
               verticalArrangement = Arrangement.spacedBy(Spacing.Xs)) {
            Text(
                stringResource(R.string.results_weak_header),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            Text(
                stringResource(R.string.results_weak_subhead),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            recommendations.forEach { rec ->
                Surface(
                    onClick = { onOpenTopic(rec.topic.id) },
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(Radius.Sm),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(Spacing.Sm)) {
                        Text(
                            text = rec.topic.titleEn,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = stringResource(R.string.results_weak_row_fmt, rec.tag, rec.missCount),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

private fun fmtScore(f: Float): String =
    if (f % 1f == 0f) f.toInt().toString() else "%.2f".format(f)
