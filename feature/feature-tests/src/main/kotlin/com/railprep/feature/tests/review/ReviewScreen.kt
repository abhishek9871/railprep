package com.railprep.feature.tests.review

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.railprep.core.design.tokens.Radius
import com.railprep.core.design.tokens.Spacing
import com.railprep.domain.model.AttemptAnswer
import com.railprep.domain.model.Option
import com.railprep.domain.model.Question
import com.railprep.feature.tests.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    attemptId: String,
    onBack: () -> Unit,
    viewModel: ReviewViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var sheetQuestion by remember { mutableStateOf<Question?>(null) }
    LaunchedEffect(attemptId) { viewModel.load(attemptId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.review_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleBilingual() }) {
                        Text(
                            text = if (state.showHi) stringResource(R.string.player_lang_en)
                                   else stringResource(R.string.player_lang_hi),
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

            state.error != null -> Box(
                Modifier.fillMaxSize().padding(innerPadding).padding(Spacing.Lg),
                contentAlignment = Alignment.Center,
            ) { Text(stringResource(R.string.review_load_error)) }

            else -> Column(Modifier.fillMaxSize().padding(innerPadding)) {
                FilterRow(filter = state.filter, onChange = { viewModel.setFilter(it) })
                val visible = state.questions.filter { q ->
                    val a = state.answers[q.id]
                    when (state.filter) {
                        ReviewFilter.ALL -> true
                        ReviewFilter.SKIPPED -> a == null || a.selectedOptionId == null
                        ReviewFilter.WRONG -> {
                            if (a == null || a.selectedOptionId == null) false
                            else q.options.firstOrNull { it.isCorrect }?.id != a.selectedOptionId
                        }
                        ReviewFilter.MARKED -> a?.flagged == true
                    }
                }
                if (visible.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(Spacing.Lg), Alignment.Center) {
                        Text(stringResource(R.string.review_empty_filtered))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = Spacing.Lg, end = Spacing.Lg,
                            top = Spacing.Sm, bottom = Spacing.Xl,
                        ),
                        verticalArrangement = Arrangement.spacedBy(Spacing.Md),
                    ) {
                        items(visible, key = { it.id }) { q ->
                            ReviewCard(
                                question = q,
                                answer = state.answers[q.id],
                                showHi = state.showHi,
                                bookmarked = state.bookmarkNotes.containsKey(q.id),
                                onToggleBookmark = { viewModel.toggleBookmark(q.id) },
                                onLongPress = { sheetQuestion = q },
                            )
                        }
                    }
                }
            }
        }
    }

    sheetQuestion?.let { question ->
        BookmarkSheet(
            question = question,
            showHi = state.showHi,
            initialSaved = state.bookmarkNotes.containsKey(question.id),
            initialNote = state.bookmarkNotes[question.id],
            onSave = { save, note ->
                viewModel.saveBookmark(question.id, save, note)
                sheetQuestion = null
            },
            onDismiss = { sheetQuestion = null },
        )
    }
}

@Composable
private fun FilterRow(filter: ReviewFilter, onChange: (ReviewFilter) -> Unit) {
    val scroll = rememberScrollState()
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(scroll)
            .padding(horizontal = Spacing.Lg, vertical = Spacing.Xs),
        horizontalArrangement = Arrangement.spacedBy(Spacing.Xs),
    ) {
        Chip(ReviewFilter.ALL, filter, stringResource(R.string.review_filter_all), onChange)
        Chip(ReviewFilter.WRONG, filter, stringResource(R.string.review_filter_wrong), onChange)
        Chip(ReviewFilter.SKIPPED, filter, stringResource(R.string.review_filter_skipped), onChange)
        Chip(ReviewFilter.MARKED, filter, stringResource(R.string.review_filter_marked), onChange)
    }
}

@Composable
private fun Chip(
    f: ReviewFilter, current: ReviewFilter, label: String, onChange: (ReviewFilter) -> Unit,
) {
    FilterChip(
        selected = f == current,
        onClick = { onChange(f) },
        label = { Text(label) },
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ReviewCard(
    question: Question,
    answer: AttemptAnswer?,
    showHi: Boolean,
    bookmarked: Boolean,
    onToggleBookmark: () -> Unit,
    onLongPress: () -> Unit,
) {
    val correct = question.options.firstOrNull { it.isCorrect }
    val selected = answer?.selectedOptionId?.let { id -> question.options.firstOrNull { it.id == id } }

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(Radius.Md),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = {}, onLongClick = onLongPress),
    ) {
        Column(modifier = Modifier.padding(Spacing.Md),
               verticalArrangement = Arrangement.spacedBy(Spacing.Xs)) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = if (showHi && !question.stemHi.isNullOrBlank()) question.stemHi!! else question.stemEn,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onToggleBookmark) {
                    Icon(
                        imageVector = if (bookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        contentDescription = stringResource(
                            if (bookmarked) R.string.review_bookmarked else R.string.review_bookmark,
                        ),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Spacer(Modifier.size(Spacing.Xs))
            question.options.forEach { opt ->
                ReviewOptionRow(
                    option = opt,
                    isCorrect = opt.id == correct?.id,
                    isUserSelected = opt.id == selected?.id,
                    showHi = showHi,
                )
                // Trap reason — surfaces only for incorrect options that carry an explanation,
                // and only when the user's eye is most likely there (selected or any wrong option).
                val trap = if (showHi && !opt.trapReasonHi.isNullOrBlank()) opt.trapReasonHi
                           else opt.trapReasonEn
                if (!opt.isCorrect && !trap.isNullOrBlank()) {
                    TrapReason(trap)
                }
            }
            if (answer == null || answer.selectedOptionId == null) {
                Text(
                    stringResource(R.string.review_not_answered),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            // Two-layer explanations (Stage 4 originals): method first, concept second.
            // Falls back to legacy explanationEn/Hi for the 30-Q sample test.
            ExplanationSection(question = question, showHi = showHi)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookmarkSheet(
    question: Question,
    showHi: Boolean,
    initialSaved: Boolean,
    initialNote: String?,
    onSave: (save: Boolean, note: String?) -> Unit,
    onDismiss: () -> Unit,
) {
    var save by remember(question.id, initialSaved) { mutableStateOf(initialSaved) }
    var note by remember(question.id, initialNote) { mutableStateOf(initialNote.orEmpty()) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.Lg, vertical = Spacing.Md),
            verticalArrangement = Arrangement.spacedBy(Spacing.Md),
        ) {
            Text(
                text = stringResource(R.string.review_bookmark_sheet_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = if (showHi && !question.stemHi.isNullOrBlank()) question.stemHi!! else question.stemEn,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.review_bookmark_save_toggle),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                )
                Switch(checked = save, onCheckedChange = { save = it })
            }
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                enabled = save,
                minLines = 2,
                label = { Text(stringResource(R.string.review_bookmark_note)) },
                placeholder = { Text(stringResource(R.string.review_bookmark_note_hint)) },
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    enabled = initialSaved,
                    onClick = { onSave(false, null) },
                ) {
                    Text(stringResource(R.string.review_bookmark_remove))
                }
                Spacer(Modifier.size(Spacing.Sm))
                Button(onClick = { onSave(save, note) }) {
                    Text(stringResource(R.string.review_bookmark_save))
                }
            }
            Spacer(Modifier.size(Spacing.Sm))
        }
    }
}

@Composable
private fun TrapReason(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
        shape = RoundedCornerShape(Radius.Sm),
        modifier = Modifier.fillMaxWidth().padding(start = Spacing.Md),
    ) {
        Column(modifier = Modifier.padding(Spacing.Sm)) {
            Text(
                text = stringResource(R.string.review_trap_label),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
    }
}

@Composable
private fun ExplanationSection(question: Question, showHi: Boolean) {
    val method = if (showHi && !question.explanationMethodHi.isNullOrBlank()) question.explanationMethodHi
                 else question.explanationMethodEn
    val concept = if (showHi && !question.explanationConceptHi.isNullOrBlank()) question.explanationConceptHi
                  else question.explanationConceptEn
    val legacy = if (showHi && !question.explanationHi.isNullOrBlank()) question.explanationHi
                 else question.explanationEn

    val hasMethodOrConcept = !method.isNullOrBlank() || !concept.isNullOrBlank()

    if (hasMethodOrConcept) {
        Spacer(Modifier.size(Spacing.Sm))
        if (!method.isNullOrBlank()) {
            Text(
                stringResource(R.string.review_explanation_method),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = method,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        if (!concept.isNullOrBlank()) {
            Spacer(Modifier.size(Spacing.Xs))
            Text(
                stringResource(R.string.review_explanation_concept),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.tertiary,
            )
            Text(
                text = concept,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    } else if (!legacy.isNullOrBlank()) {
        Spacer(Modifier.size(Spacing.Sm))
        Text(
            stringResource(R.string.review_explanation_header),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = legacy,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ReviewOptionRow(
    option: Option,
    isCorrect: Boolean,
    isUserSelected: Boolean,
    showHi: Boolean,
) {
    val bg = when {
        isCorrect -> Color(0xFFD7EED7)
        isUserSelected && !isCorrect -> Color(0xFFF8D7D7)
        else -> Color.Transparent
    }
    val label = buildString {
        append(option.label).append(".  ")
        append(if (showHi && !option.textHi.isNullOrBlank()) option.textHi else option.textEn)
        if (isCorrect) append("  ✓")
        else if (isUserSelected) append("  ✗")
    }
    Surface(
        color = bg,
        shape = RoundedCornerShape(Radius.Sm),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = Spacing.Sm, vertical = Spacing.Xs),
        )
    }
}
