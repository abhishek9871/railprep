package com.railprep.feature.tests.instructions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.railprep.core.design.tokens.Radius
import com.railprep.core.design.tokens.Spacing
import com.railprep.domain.model.Test
import com.railprep.domain.model.TestSection
import com.railprep.feature.tests.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructionsScreen(
    testId: String,
    onStartAttempt: (attemptId: String) -> Unit,
    onBack: () -> Unit,
    viewModel: InstructionsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val useHi = LocalConfiguration.current.locales.get(0).language == "hi"

    LaunchedEffect(testId) { viewModel.load(testId) }

    LaunchedEffect(state.startedAttemptId) {
        state.startedAttemptId?.let { aid ->
            viewModel.clearStarted()
            onStartAttempt(aid)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.instructions_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
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

            state.test == null -> Box(
                Modifier.fillMaxSize().padding(innerPadding).padding(Spacing.Lg),
                contentAlignment = Alignment.Center,
            ) { Text(stringResource(R.string.player_load_error)) }

            else -> InstructionsBody(
                test = state.test!!,
                sections = state.sections,
                hasActiveAttempt = state.hasActiveAttempt,
                starting = state.starting,
                error = state.error,
                useHi = useHi,
                onStart = { viewModel.start() },
                modifier = Modifier.fillMaxSize().padding(innerPadding),
            )
        }
    }
}

@Composable
private fun InstructionsBody(
    test: Test,
    sections: List<TestSection>,
    hasActiveAttempt: Boolean,
    starting: Boolean,
    error: String?,
    useHi: Boolean,
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scroll = rememberScrollState()
    Column(
        modifier = modifier.verticalScroll(scroll).padding(PaddingValues(Spacing.Lg)),
        verticalArrangement = Arrangement.spacedBy(Spacing.Md),
    ) {
        Text(
            stringResource(R.string.instructions_header_fmt, test.displayTitle(useHi)),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground,
        )
        val secondaryTitle = test.secondaryTitle(useHi)
        if (!secondaryTitle.isNullOrBlank()) {
            Text(
                secondaryTitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(Radius.Md),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(Spacing.Md),
                   verticalArrangement = Arrangement.spacedBy(Spacing.Xs)) {
                Text(stringResource(R.string.instructions_duration_fmt, test.totalMinutes),
                     style = MaterialTheme.typography.bodyLarge)
                Text(stringResource(R.string.instructions_qcount_fmt, test.totalQuestions),
                     style = MaterialTheme.typography.bodyLarge)
                Text(
                    stringResource(
                        R.string.instructions_neg_fmt,
                        "%.3f".format(test.negativeMarkingFraction),
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        Text(
            stringResource(R.string.instructions_sections_header),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        )
        sections.forEach { s ->
            Text(
                stringResource(
                    R.string.instructions_section_line_fmt,
                    if (useHi && !s.titleHi.isNullOrBlank()) s.titleHi!! else s.titleEn,
                    s.questionCount,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.size(Spacing.Sm))
        Text(
            stringResource(R.string.instructions_rules_header),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        )
        Text(stringResource(R.string.instructions_rule_bilingual),
             style = MaterialTheme.typography.bodyMedium)
        Text(stringResource(R.string.instructions_rule_timer),
             style = MaterialTheme.typography.bodyMedium)
        Text(stringResource(R.string.instructions_rule_neg),
             style = MaterialTheme.typography.bodyMedium)
        Text(stringResource(R.string.instructions_rule_submit),
             style = MaterialTheme.typography.bodyMedium)

        Spacer(Modifier.size(Spacing.Md))

        Button(
            onClick = onStart,
            enabled = !starting,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                if (starting) stringResource(R.string.instructions_starting)
                else if (hasActiveAttempt) stringResource(R.string.instructions_resume)
                else stringResource(R.string.instructions_start),
            )
        }
        if (error == "start" || error == "pro") {
            Text(
                stringResource(
                    if (error == "pro") R.string.instructions_pro_required
                    else R.string.instructions_start_error,
                ),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

private fun Test.displayTitle(useHi: Boolean): String =
    if (useHi && !titleHi.isNullOrBlank()) titleHi!! else titleEn

private fun Test.secondaryTitle(useHi: Boolean): String? = when {
    useHi -> titleEn.takeIf { it.isNotBlank() && it != titleHi }
    else -> titleHi
}
