package com.railprep.feature.home.profile.performance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.railprep.domain.model.TopicAccuracy
import com.railprep.feature.home.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceScreen(
    onBack: () -> Unit,
    onOpenTopic: (String) -> Unit,
    viewModel: PerformanceViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.performance_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        when {
            state.loading -> Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }
            state.error -> Box(
                Modifier.fillMaxSize().padding(innerPadding).padding(Spacing.Lg),
                contentAlignment = Alignment.Center,
            ) {
                OutlinedButton(onClick = { viewModel.refresh() }) {
                    Text(stringResource(R.string.performance_retry))
                }
            }
            state.rows.isEmpty() -> EmptyPerformance(Modifier.fillMaxSize().padding(innerPadding))
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(Spacing.Lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.Sm),
            ) {
                item {
                    Text(
                        text = stringResource(R.string.performance_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                items(state.rows, key = { "${it.subjectHint}-${it.tag}" }) { row ->
                    AccuracyRow(
                        row = row,
                        onOpenPrimer = state.primersByTag[row.tag]?.let { topic ->
                            { onOpenTopic(topic.id) }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyPerformance(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(Spacing.Xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Filled.BarChart,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.performance_empty_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = Spacing.Md),
        )
        Text(
            text = stringResource(R.string.performance_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.Xs),
        )
    }
}

@Composable
private fun AccuracyRow(row: TopicAccuracy, onOpenPrimer: (() -> Unit)?) {
    Surface(
        shape = RoundedCornerShape(Radius.Md),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(Spacing.Md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = row.tag,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(
                            R.string.performance_attempted_fmt,
                            row.correct,
                            row.attempted,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = stringResource(R.string.performance_pct_fmt, row.accuracyPct),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.height(Spacing.Sm))
            LinearProgressIndicator(
                progress = { (row.accuracyPct / 100f).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
            )
            if (onOpenPrimer != null) {
                Spacer(Modifier.height(Spacing.Sm))
                Button(onClick = onOpenPrimer) {
                    Text(stringResource(R.string.performance_study_primer))
                }
            }
        }
    }
}
