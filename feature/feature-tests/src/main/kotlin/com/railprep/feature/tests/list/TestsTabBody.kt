package com.railprep.feature.tests.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.railprep.core.design.tokens.Radius
import com.railprep.core.design.tokens.Spacing
import com.railprep.core.design.tokens.TouchTarget
import com.railprep.domain.model.PaperLanguage
import com.railprep.domain.model.QuestionDifficulty
import com.railprep.domain.model.QuestionSearchResult
import com.railprep.domain.model.Test
import com.railprep.domain.model.TestKind
import com.railprep.feature.tests.R

enum class TestsTabMode { PRACTICE, PYQ_LIBRARY }

@Composable
fun TestsTabBody(
    onOpenInstructions: (testId: String) -> Unit,
    onOpenPyqPaper: (testId: String) -> Unit = {},
    onOpenPro: () -> Unit = {},
    mode: TestsTabMode = TestsTabMode.PRACTICE,
    viewModel: TestsListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val filtered = state.tests.filteredFor(mode, state.filter)
    var proDialogTest by remember { mutableStateOf<Test?>(null) }

    Column(Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(horizontal = Spacing.Lg, vertical = Spacing.Md)) {
            Text(
                text = stringResource(
                    if (mode == TestsTabMode.PYQ_LIBRARY) R.string.tests_pyq_title else R.string.tests_list_title,
                ),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = stringResource(
                    if (mode == TestsTabMode.PYQ_LIBRARY) R.string.tests_pyq_subtitle else R.string.tests_list_subtitle,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.Xs),
            )
            if (mode == TestsTabMode.PRACTICE) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    placeholder = { Text(stringResource(R.string.tests_search_hint)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.Md),
                )
            }
        }

        if (mode == TestsTabMode.PRACTICE && state.searchQuery.isBlank()) {
            FilterChipsRow(
                filter = state.filter,
                onChange = { viewModel.setFilter(it) },
            )
        }

        when {
            state.loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            mode == TestsTabMode.PRACTICE && state.searchQuery.isNotBlank() -> SearchResultsPane(
                loading = state.searchLoading,
                results = state.searchResults,
                error = state.searchError,
                onOpen = { onOpenInstructions(it.testId) },
            )
            state.error != null -> Box(
                Modifier.fillMaxSize().padding(Spacing.Lg),
                Alignment.Center,
            ) { Text(stringResource(R.string.tests_load_error)) }
            filtered.isEmpty() -> Box(
                Modifier.fillMaxSize().padding(Spacing.Lg),
                Alignment.Center,
            ) {
                Text(
                    stringResource(
                        if (mode == TestsTabMode.PYQ_LIBRARY) R.string.tests_pyq_empty
                        else R.string.tests_list_empty,
                    ),
                )
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = Spacing.Lg,
                    end = Spacing.Lg,
                    top = Spacing.Md,
                    bottom = Spacing.Xl,
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.Md),
            ) {
                items(filtered, key = { it.id }) { t ->
                    TestCard(
                        test = t,
                        stats = state.attemptStats[t.id],
                        onClick = {
                            // PYQ_LINK rows render the adda247 PDF on-device via PyqPaperScreen;
                            // everything else goes through the Instructions → Player flow.
                            when {
                                t.kind == TestKind.PYQ_LINK -> onOpenPyqPaper(t.id)
                                t.isPro -> proDialogTest = t
                                else -> onOpenInstructions(t.id)
                            }
                        },
                    )
                }
            }
        }
    }

    proDialogTest?.let { test ->
        AlertDialog(
            onDismissRequest = { proDialogTest = null },
            title = { Text(stringResource(R.string.tests_pro_required_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.tests_pro_required_body_fmt,
                        test.titleEn,
                    ),
                )
            },
            confirmButton = {
                Button(onClick = {
                    proDialogTest = null
                    onOpenPro()
                }) {
                    Text(stringResource(R.string.tests_get_pro))
                }
            },
            dismissButton = {
                TextButton(onClick = { proDialogTest = null }) {
                    Text(stringResource(R.string.tests_pro_maybe_later))
                }
            },
        )
    }
}

@Composable
private fun SearchResultsPane(
    loading: Boolean,
    results: List<QuestionSearchResult>,
    error: String?,
    onOpen: (QuestionSearchResult) -> Unit,
) {
    when {
        loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        error != null -> Box(
            Modifier.fillMaxSize().padding(Spacing.Lg),
            Alignment.Center,
        ) { Text(stringResource(R.string.tests_search_error)) }
        results.isEmpty() -> Box(
            Modifier.fillMaxSize().padding(Spacing.Lg),
            Alignment.Center,
        ) { Text(stringResource(R.string.tests_search_empty)) }
        else -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = Spacing.Lg,
                end = Spacing.Lg,
                top = Spacing.Md,
                bottom = Spacing.Xl,
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.Sm),
        ) {
            items(results, key = { it.questionId }) { result ->
                SearchResultCard(result = result, onClick = { onOpen(result) })
            }
        }
    }
}

@Composable
private fun SearchResultCard(result: QuestionSearchResult, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(Radius.Md),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(Spacing.Md), verticalArrangement = Arrangement.spacedBy(Spacing.Xs)) {
            Text(
                text = result.stemEn,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
            )
            Text(
                text = stringResource(
                    R.string.tests_search_result_source_fmt,
                    result.testTitleEn,
                    result.sectionTitleEn,
                    result.displayOrder,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.Xs)) {
                Text(
                    text = difficultyLabel(result.difficulty),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (result.isBookmarked) {
                    Text(
                        text = stringResource(R.string.tests_search_bookmarked),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
                if (result.wasWrong) {
                    Text(
                        text = stringResource(R.string.tests_search_wrong_before),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
private fun difficultyLabel(difficulty: QuestionDifficulty): String = when (difficulty) {
    QuestionDifficulty.EASY -> stringResource(R.string.tests_difficulty_easy)
    QuestionDifficulty.MEDIUM -> stringResource(R.string.tests_difficulty_medium)
    QuestionDifficulty.HARD -> stringResource(R.string.tests_difficulty_hard)
}

@Composable
private fun FilterChipsRow(
    filter: TestsFilter,
    onChange: (TestsFilter) -> Unit,
) {
    val scroll = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scroll)
            .padding(horizontal = Spacing.Lg, vertical = Spacing.Xs),
        horizontalArrangement = Arrangement.spacedBy(Spacing.Xs),
    ) {
        Chip(TestsFilter.ALL, filter, stringResource(R.string.tests_filter_all), onChange)
        Chip(TestsFilter.CBT1, filter, stringResource(R.string.tests_filter_cbt1), onChange)
        Chip(TestsFilter.CBT2, filter, stringResource(R.string.tests_filter_cbt2), onChange)
        Chip(TestsFilter.SECTIONAL, filter, stringResource(R.string.tests_filter_sectional), onChange)
    }
}

@Composable
private fun Chip(
    f: TestsFilter,
    current: TestsFilter,
    label: String,
    onChange: (TestsFilter) -> Unit,
) {
    FilterChip(
        selected = f == current,
        onClick = { onChange(f) },
        label = { Text(label) },
    )
}

@Composable
private fun TestCard(test: Test, stats: TestAttemptStats?, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(Radius.Md),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth().heightIn(min = TouchTarget.Min * 2),
    ) {
        Row(modifier = Modifier.padding(Spacing.Md), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = test.titleEn,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    if (test.isPro) {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(Radius.Xs),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = Spacing.Xs, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Filled.Lock,
                                    null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                )
                                Spacer(Modifier.size(Spacing.Xxs))
                                Text(
                                    stringResource(R.string.tests_card_pro),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                )
                            }
                        }
                    }
                    val lang = test.sourceLanguage
                    if (test.kind == TestKind.PYQ_LINK && lang != null) {
                        Spacer(Modifier.size(Spacing.Xs))
                        LangBadge(lang)
                    }
                }
                if (!test.titleHi.isNullOrBlank()) {
                    Text(
                        text = test.titleHi!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.size(Spacing.Xs))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AccessTime, null, modifier = Modifier.size(16.dp),
                         tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.size(Spacing.Xxs))
                    Text(
                        stringResource(R.string.tests_card_duration_fmt, test.totalMinutes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.size(Spacing.Md))
                    Icon(Icons.Filled.HelpOutline, null, modifier = Modifier.size(16.dp),
                         tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.size(Spacing.Xxs))
                    Text(
                        stringResource(R.string.tests_card_qcount_fmt, test.totalQuestions),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.size(Spacing.Xxs))
                if (test.kind == TestKind.PYQ_LINK) {
                    PyqLinkSubtitle(test)
                } else {
                    AttemptStatsLine(stats)
                }
            }
        }
    }
}

@Composable
private fun LangBadge(lang: PaperLanguage) {
    val (label, container, on) = when (lang) {
        PaperLanguage.EN -> Triple(
            stringResource(R.string.tests_card_lang_en),
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
        )
        PaperLanguage.HI -> Triple(
            stringResource(R.string.tests_card_lang_hi),
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
        )
        PaperLanguage.BILINGUAL -> Triple(
            "EN + हिन्दी",
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
        )
    }
    Surface(
        color = container,
        shape = RoundedCornerShape(Radius.Xs),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = on,
            modifier = Modifier.padding(horizontal = Spacing.Xs, vertical = 2.dp),
        )
    }
}

@Composable
private fun PyqLinkSubtitle(test: Test) {
    val source = test.sourceAttribution ?: return
    Text(
        text = stringResource(R.string.tests_card_pyq_link_subtitle, source),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun AttemptStatsLine(stats: TestAttemptStats?) {
    val text = if (stats == null || stats.submittedCount == 0) {
        stringResource(R.string.tests_card_not_attempted)
    } else {
        val bestStr = fmtScore(stats.bestScore)
        val maxStr = fmtScore(stats.bestMaxScore)
        if (stats.submittedCount == 1) {
            stringResource(R.string.tests_card_best_single_fmt, bestStr, maxStr)
        } else {
            stringResource(
                R.string.tests_card_best_fmt, bestStr, maxStr, stats.submittedCount,
            )
        }
    }
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = if (stats == null || stats.submittedCount == 0)
            MaterialTheme.colorScheme.onSurfaceVariant
        else MaterialTheme.colorScheme.primary,
    )
}

private fun fmtScore(f: Float): String =
    if (f % 1f == 0f) f.toInt().toString() else "%.2f".format(f)
