package com.railprep.feature.daily.digest

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.railprep.core.design.tokens.Radius
import com.railprep.core.design.tokens.Spacing
import com.railprep.domain.model.Option
import com.railprep.domain.model.Question
import com.railprep.feature.daily.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyDigestScreen(
    onSubmitted: () -> Unit,
    onBack: () -> Unit,
    viewModel: DailyDigestViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.submitted) {
        if (state.submitted) onSubmitted()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.daily_player_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleBilingual() }) {
                        Text(
                            text = if (state.showHi) stringResource(R.string.daily_player_lang_en)
                                   else stringResource(R.string.daily_player_lang_hi),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                        )
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

            state.digest == null -> Box(
                Modifier.fillMaxSize().padding(innerPadding).padding(Spacing.Lg),
                contentAlignment = Alignment.Center,
            ) { Text(stringResource(R.string.daily_player_load_error)) }

            else -> {
                val digest = state.digest!!
                val questions = digest.questions
                val total = questions.size
                val safeIndex = state.currentIndex.coerceIn(0, (total - 1).coerceAtLeast(0))
                val current = questions.getOrNull(safeIndex)
                Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                    LinearProgressIndicator(
                        progress = { if (total == 0) 0f else (safeIndex + 1).toFloat() / total },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = stringResource(
                            R.string.daily_player_question_index_fmt,
                            safeIndex + 1, total,
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(Spacing.Md),
                    )
                    if (current != null) {
                        QuestionBody(
                            question = current,
                            selectedOptionId = state.answers[current.id],
                            onSelect = { optId -> viewModel.selectOption(current.id, optId) },
                            showHi = state.showHi,
                            modifier = Modifier.weight(1f, fill = true),
                        )
                    }
                    NavRow(
                        canPrev = safeIndex > 0,
                        canNext = safeIndex < total - 1,
                        onPrev = { viewModel.goPrev() },
                        onNext = { viewModel.goNext() },
                        onSubmit = { viewModel.requestSubmit() },
                        submitting = state.submitting,
                    )
                }

                if (state.confirmSubmitVisible) {
                    val answered = state.answers.count { it.value != null }
                    AlertDialog(
                        onDismissRequest = { viewModel.cancelSubmit() },
                        title = { Text(stringResource(R.string.daily_player_confirm_title)) },
                        text = {
                            Text(stringResource(
                                R.string.daily_player_confirm_body_fmt, answered, total,
                            ))
                        },
                        confirmButton = {
                            TextButton(onClick = { viewModel.confirmSubmit() }) {
                                Text(stringResource(R.string.daily_player_confirm_submit))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { viewModel.cancelSubmit() }) {
                                Text(stringResource(R.string.daily_player_confirm_keep))
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestionBody(
    question: Question,
    selectedOptionId: String?,
    onSelect: (String?) -> Unit,
    showHi: Boolean,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = Spacing.Lg, end = Spacing.Lg,
            top = Spacing.Sm, bottom = Spacing.Lg,
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.Sm),
    ) {
        item {
            Text(
                text = if (showHi && !question.stemHi.isNullOrBlank()) question.stemHi!!
                       else question.stemEn,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        items(question.options, key = { it.id }) { opt ->
            OptionRow(
                option = opt,
                selected = opt.id == selectedOptionId,
                showHi = showHi,
                onClick = { onSelect(opt.id) },
            )
        }
    }
}

@Composable
private fun OptionRow(
    option: Option,
    selected: Boolean,
    showHi: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(Radius.Md),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = "${option.label}.  " +
                if (showHi && !option.textHi.isNullOrBlank()) option.textHi!! else option.textEn,
            style = MaterialTheme.typography.bodyLarge,
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(Spacing.Md),
        )
    }
}

@Composable
private fun NavRow(
    canPrev: Boolean,
    canNext: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onSubmit: () -> Unit,
    submitting: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(Spacing.Md),
        horizontalArrangement = Arrangement.spacedBy(Spacing.Sm),
    ) {
        OutlinedButton(
            onClick = onPrev,
            enabled = canPrev,
            modifier = Modifier.weight(1f),
        ) {
            Text(stringResource(R.string.daily_player_prev))
        }
        if (canNext) {
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.daily_player_next))
            }
        } else {
            Button(
                onClick = onSubmit,
                enabled = !submitting,
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    if (submitting) stringResource(R.string.daily_player_submitting)
                    else stringResource(R.string.daily_player_submit),
                )
            }
        }
    }
}

