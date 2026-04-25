package com.railprep.feature.tests.player

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.railprep.core.design.tokens.Radius
import com.railprep.core.design.tokens.Spacing
import com.railprep.core.design.tokens.TouchTarget
import com.railprep.domain.model.Option
import com.railprep.domain.model.Question
import com.railprep.domain.model.SubjectHint
import com.railprep.feature.tests.R
import com.railprep.feature.tests.diag.AttemptDiag
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "TestPlayerScreen"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TestPlayerScreen(
    attemptId: String,
    onSubmitted: () -> Unit,
    onBack: () -> Unit,
    viewModel: TestPlayerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(attemptId) { viewModel.load(attemptId) }
    LaunchedEffect(state.submitted) { if (state.submitted) onSubmitted() }

    var showDiag by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = state.currentSection?.let {
                                stringResource(R.string.player_section_fmt,
                                    if (state.showHi && !it.titleHi.isNullOrBlank()) it.titleHi!! else it.titleEn)
                            } ?: "",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (state.questions.isNotEmpty()) {
                            Text(
                                text = stringResource(
                                    R.string.player_question_index_fmt,
                                    state.currentIndex + 1,
                                    state.totalCount,
                                ),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                },
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
                    TimerBar(
                        remainingMs = state.remainingMs.coerceAtLeast(0L),
                        onLongPress = { showDiag = true },
                    )
                    Spacer(Modifier.width(Spacing.Sm))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        bottomBar = {
            PlayerBottomBar(
                onOpenPalette = { viewModel.openPalette() },
                onPrev = { viewModel.goPrev() },
                onNext = { viewModel.goNext() },
                onSubmit = { viewModel.requestSubmit() },
                prevEnabled = state.currentIndex > 0 && !state.submitting,
                nextEnabled = state.currentIndex < state.questions.size - 1 && !state.submitting,
                submitEnabled = !state.submitting,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        when {
            state.loading -> Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            state.error == "load-attempt" || state.error == "load-content" -> Box(
                Modifier.fillMaxSize().padding(innerPadding).padding(Spacing.Lg),
                contentAlignment = Alignment.Center,
            ) { Text(stringResource(R.string.player_load_error)) }

            state.currentQuestion == null -> Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            else -> QuestionPane(
                question = state.currentQuestion!!,
                selectedOptionId = state.answers[state.currentQuestion!!.id]?.selectedOptionId,
                flagged = state.answers[state.currentQuestion!!.id]?.flagged ?: false,
                showHi = state.showHi,
                onSelect = { optId -> viewModel.selectOption(state.currentQuestion!!.id, optId) },
                onToggleFlag = { viewModel.toggleFlag(state.currentQuestion!!.id) },
                onClear = { viewModel.clearSelection(state.currentQuestion!!.id) },
                modifier = Modifier.fillMaxSize().padding(innerPadding),
            )
        }

        if (state.timeUp) {
            TimeUpOverlay()
        }
    }

    if (state.paletteOpen) {
        QuestionPaletteSheet(
            state = state,
            onDismiss = { viewModel.closePalette() },
            onJump = { idx -> viewModel.jumpTo(idx) },
        )
    }

    if (state.confirmSubmit) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelSubmit() },
            title = { Text(stringResource(R.string.player_confirm_submit_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.player_confirm_submit_body_fmt,
                        state.answeredCount,
                        state.totalCount,
                    ),
                )
            },
            confirmButton = {
                Button(onClick = { viewModel.submit() }) {
                    Text(stringResource(R.string.player_confirm_submit_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelSubmit() }) {
                    Text(stringResource(R.string.player_confirm_submit_cancel))
                }
            },
        )
    }

    if (state.submitting) {
        Box(
            Modifier.fillMaxSize().background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center,
        ) {
            Surface(shape = RoundedCornerShape(Radius.Md), color = MaterialTheme.colorScheme.surface) {
                Row(
                    Modifier.padding(Spacing.Lg),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(Modifier.size(24.dp))
                    Spacer(Modifier.width(Spacing.Md))
                    Text(stringResource(R.string.player_submitting))
                }
            }
        }
    }

    if (showDiag) {
        AttemptDiagDialog(state = state, onClose = { showDiag = false })
    }

    LaunchedEffect(state.error) {
        if (state.error == "submit") Log.w(TAG, "submit failed — user can tap Submit again")
    }
}

@Composable
private fun QuestionPane(
    question: Question,
    selectedOptionId: String?,
    flagged: Boolean,
    showHi: Boolean,
    onSelect: (optionId: String) -> Unit,
    onToggleFlag: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(Spacing.Lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.Md),
    ) {
        item {
            Text(
                text = if (showHi && !question.stemHi.isNullOrBlank()) question.stemHi!! else question.stemEn,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        items(question.options, key = { it.id }) { opt ->
            OptionCard(
                option = opt,
                selected = opt.id == selectedOptionId,
                showHi = showHi,
                onClick = { onSelect(opt.id) },
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.Sm),
            ) {
                OutlinedButton(
                    onClick = onToggleFlag,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        if (flagged) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        null,
                    )
                    Spacer(Modifier.width(Spacing.Xs))
                    Text(
                        if (flagged) stringResource(R.string.player_marked_review)
                        else stringResource(R.string.player_mark_review),
                    )
                }
                OutlinedButton(
                    onClick = onClear,
                    modifier = Modifier.weight(1f),
                    enabled = selectedOptionId != null,
                ) { Text(stringResource(R.string.player_clear)) }
            }
        }
    }
}

@Composable
private fun OptionCard(
    option: Option,
    selected: Boolean,
    showHi: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(Radius.Md),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = TouchTarget.Min),
    ) {
        Row(
            modifier = Modifier.padding(Spacing.Md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(selected = selected, onClick = null)
            Spacer(Modifier.width(Spacing.Sm))
            Text(
                text = "${option.label}.  " +
                    (if (showHi && !option.textHi.isNullOrBlank()) option.textHi!! else option.textEn),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TimerBar(remainingMs: Long, onLongPress: () -> Unit) {
    val secs = (remainingMs / 1000L).coerceAtLeast(0L)
    val mm = secs / 60L
    val ss = secs % 60L
    val urgent = remainingMs in 1..60_000L
    val color = when {
        remainingMs <= 0 -> MaterialTheme.colorScheme.error
        urgent -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
    }
    Surface(
        shape = RoundedCornerShape(Radius.Sm),
        color = color.copy(alpha = 0.12f),
        modifier = Modifier
            .padding(horizontal = Spacing.Xs)
            .combinedClickable(onClick = {}, onLongClick = onLongPress),
    ) {
        Text(
            text = "%02d:%02d".format(mm, ss),
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
            ),
            color = color,
            modifier = Modifier.padding(horizontal = Spacing.Sm, vertical = Spacing.Xxs),
        )
    }
}

@Composable
private fun PlayerBottomBar(
    onOpenPalette: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onSubmit: () -> Unit,
    prevEnabled: Boolean,
    nextEnabled: Boolean,
    submitEnabled: Boolean,
) {
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 6.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.Md, vertical = Spacing.Sm),
            horizontalArrangement = Arrangement.spacedBy(Spacing.Xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onOpenPalette) {
                Icon(Icons.Filled.GridView, contentDescription = stringResource(R.string.player_palette))
            }
            OutlinedButton(onClick = onPrev, enabled = prevEnabled) {
                Text(stringResource(R.string.player_prev))
            }
            OutlinedButton(onClick = onNext, enabled = nextEnabled, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.player_next))
            }
            Button(onClick = onSubmit, enabled = submitEnabled) {
                Text(stringResource(R.string.player_submit))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuestionPaletteSheet(
    state: TestPlayerState,
    onDismiss: () -> Unit,
    onJump: (Int) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(modifier = Modifier.padding(Spacing.Lg)) {
            Text(
                text = stringResource(R.string.palette_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.size(Spacing.Sm))
            PaletteLegend()
            Spacer(Modifier.size(Spacing.Md))
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                verticalArrangement = Arrangement.spacedBy(Spacing.Xs),
                horizontalArrangement = Arrangement.spacedBy(Spacing.Xs),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(state.questions.size) { idx ->
                    val q = state.questions[idx]
                    val a = state.answers[q.id]
                    val status = when {
                        idx == state.currentIndex -> PaletteStatus.CURRENT
                        a?.flagged == true -> PaletteStatus.MARKED
                        a?.selectedOptionId != null -> PaletteStatus.ANSWERED
                        else -> PaletteStatus.SKIPPED
                    }
                    PaletteCell(index = idx + 1, status = status, onClick = { onJump(idx) })
                }
            }
            Spacer(Modifier.size(Spacing.Md))
        }
    }
}

private enum class PaletteStatus { ANSWERED, MARKED, SKIPPED, CURRENT }

@Composable
private fun PaletteCell(index: Int, status: PaletteStatus, onClick: () -> Unit) {
    val (bg, fg) = when (status) {
        PaletteStatus.ANSWERED -> Color(0xFF2E7D32) to Color.White
        PaletteStatus.MARKED   -> Color(0xFF6A1B9A) to Color.White
        PaletteStatus.SKIPPED  -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        PaletteStatus.CURRENT  -> MaterialTheme.colorScheme.primary to Color.White
    }
    Surface(
        onClick = onClick,
        color = bg,
        shape = RoundedCornerShape(Radius.Sm),
        modifier = Modifier.size(44.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = index.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = fg,
            )
        }
    }
}

@Composable
private fun PaletteLegend() {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.Xxs)) {
        LegendRow(Color(0xFF2E7D32), stringResource(R.string.palette_legend_answered))
        LegendRow(Color(0xFF6A1B9A), stringResource(R.string.palette_legend_marked))
        LegendRow(MaterialTheme.colorScheme.surfaceVariant, stringResource(R.string.palette_legend_skipped))
        LegendRow(MaterialTheme.colorScheme.primary, stringResource(R.string.palette_legend_current))
    }
}

@Composable
private fun LegendRow(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(14.dp).background(color, shape = CircleShape),
        )
        Spacer(Modifier.width(Spacing.Xs))
        Text(label, style = MaterialTheme.typography.bodySmall,
             color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun TimeUpOverlay() {
    Box(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.55f)),
        contentAlignment = Alignment.Center,
    ) {
        Surface(shape = RoundedCornerShape(Radius.Md), color = MaterialTheme.colorScheme.surface) {
            Column(
                modifier = Modifier.padding(Spacing.Lg),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.player_time_up),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.size(Spacing.Sm))
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun AttemptDiagDialog(state: TestPlayerState, onClose: () -> Unit) {
    val snapshot = AttemptDiag.last ?: AttemptDiag.Snapshot(
        attemptId = state.attemptId.takeIf { it.isNotEmpty() },
        deadlineEpochMs = state.deadlineEpochMs.takeIf { it > 0 },
        localNowMs = System.currentTimeMillis(),
        lastServerSyncMs = 0L,
        pendingLocalWrites = state.pendingUnsynced,
    )
    val fmt = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(stringResource(R.string.attempt_diag_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.Xxs)) {
                DiagRow(stringResource(R.string.attempt_diag_attempt_id),
                        snapshot.attemptId?.takeLast(8) ?: "—")
                DiagRow(stringResource(R.string.attempt_diag_deadline),
                        snapshot.deadlineEpochMs?.let { fmt.format(Date(it)) } ?: "—")
                DiagRow(stringResource(R.string.attempt_diag_local_now),
                        fmt.format(Date(snapshot.localNowMs)))
                val drift = snapshot.deadlineEpochMs?.let { snapshot.localNowMs - it } ?: 0L
                DiagRow(stringResource(R.string.attempt_diag_drift), "${drift / 1000} s")
                DiagRow(stringResource(R.string.attempt_diag_last_sync),
                        if (snapshot.lastServerSyncMs > 0L) fmt.format(Date(snapshot.lastServerSyncMs))
                        else "—")
                DiagRow(stringResource(R.string.attempt_diag_pending),
                        snapshot.pendingLocalWrites.toString())
            }
        },
        confirmButton = {
            TextButton(onClick = onClose) {
                Text(stringResource(R.string.attempt_diag_close))
            }
        },
    )
}

@Composable
private fun DiagRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall,
             color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall,
             fontFamily = FontFamily.Monospace)
    }
}

/** Utility — unused here but kept for pending enhancements (e.g. show subject-hint color on top bar). */
@Suppress("unused")
private fun SubjectHint?.orMixed(): SubjectHint = this ?: SubjectHint.MIXED
