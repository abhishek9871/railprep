package com.railprep.feature.home.profile.savedquestions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.railprep.core.design.tokens.Radius
import com.railprep.core.design.tokens.Spacing
import com.railprep.domain.model.Option
import com.railprep.domain.model.QuestionBookmark
import com.railprep.feature.home.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedQuestionDetailScreen(
    questionId: String,
    onBack: () -> Unit,
    onRemoved: () -> Unit,
    viewModel: SavedQuestionDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val useHi = LocalConfiguration.current.locales.get(0).language == "hi"
    LaunchedEffect(questionId) { viewModel.load(questionId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.saved_question_detail_title)) },
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

            state.bookmark == null -> Box(
                Modifier.fillMaxSize().padding(innerPadding).padding(Spacing.Lg),
                contentAlignment = Alignment.Center,
            ) { Text(stringResource(R.string.saved_question_missing)) }

            else -> SavedQuestionDetailContent(
                bookmark = state.bookmark!!,
                noteDraft = state.noteDraft,
                saving = state.saving,
                useHi = useHi,
                onNoteChange = viewModel::updateNoteDraft,
                onSaveNote = viewModel::saveNote,
                onRemove = { viewModel.remove(onRemoved) },
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun SavedQuestionDetailContent(
    bookmark: QuestionBookmark,
    noteDraft: String,
    saving: Boolean,
    useHi: Boolean,
    onNoteChange: (String) -> Unit,
    onSaveNote: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val question = bookmark.question
    val focusManager = LocalFocusManager.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val method = question.localizedMethod(useHi).orEmpty()
    val concept = question.localizedConcept(useHi).orEmpty()

    LazyColumn(
        modifier = modifier.fillMaxSize().imePadding(),
        contentPadding = PaddingValues(Spacing.Lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.Md),
    ) {
        item {
            Text(
                text = bookmark.sourceLabel(useHi),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = question.localizedStem(useHi),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = Spacing.Xs),
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.Xs)) {
                question.options.forEach { option ->
                    SavedQuestionOption(option = option, useHi = useHi)
                }
            }
        }
        item {
            OutlinedTextField(
                value = noteDraft,
                onValueChange = onNoteChange,
                label = { Text(stringResource(R.string.saved_question_note)) },
                minLines = 2,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus(force = true)
                        onSaveNote()
                    },
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.Sm),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = {
                        focusManager.clearFocus(force = true)
                        onRemove()
                    },
                    enabled = !saving,
                ) {
                    Text(stringResource(R.string.saved_questions_remove))
                }
                Spacer(Modifier.size(Spacing.Sm))
                Button(
                    onClick = {
                        focusManager.clearFocus(force = true)
                        onSaveNote()
                    },
                    enabled = !saving,
                ) {
                    Text(stringResource(R.string.saved_question_save_note))
                }
            }
        }
        item {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(stringResource(R.string.saved_question_method_tab)) },
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(stringResource(R.string.saved_question_concept_tab)) },
                )
            }
            Text(
                text = if (selectedTab == 0) method else concept,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = Spacing.Md),
            )
        }
    }
}

@Composable
private fun SavedQuestionOption(option: Option, useHi: Boolean) {
    val bg = if (option.isCorrect) Color(0xFFD7EED7) else MaterialTheme.colorScheme.surfaceVariant
    Surface(
        color = bg,
        shape = RoundedCornerShape(Radius.Sm),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(Spacing.Sm)) {
            Text(
                text = buildString {
                    append(option.label).append(".  ")
                    append(option.localizedText(useHi))
                    if (option.isCorrect) append("  ✓")
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (option.isCorrect) FontWeight.SemiBold else FontWeight.Normal,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
            val trap = option.localizedTrapReason(useHi)
            if (!option.isCorrect && !trap.isNullOrBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(Radius.Sm),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.Xs),
                ) {
                    Text(
                        text = trap,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(Spacing.Sm),
                    )
                }
            }
        }
    }
}
