package com.railprep.feature.home.profile.savedquestions

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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.railprep.core.design.tokens.Spacing
import com.railprep.core.design.tokens.TouchTarget
import com.railprep.domain.model.QuestionBookmark
import com.railprep.feature.home.R
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedQuestionsScreen(
    onQuestionClick: (questionId: String) -> Unit,
    onBack: () -> Unit,
    viewModel: SavedQuestionsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val useHi = LocalConfiguration.current.locales.get(0).language == "hi"

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refresh(showLoading = false)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.selectionMode) {
                            stringResource(R.string.saved_questions_selected_fmt, state.selectedQuestionIds.size)
                        } else {
                            stringResource(R.string.saved_questions_title)
                        },
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { if (state.selectionMode) viewModel.clearSelection() else onBack() }) {
                        Icon(
                            if (state.selectionMode) Icons.Filled.Close else Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
                actions = {
                    if (state.selectionMode) {
                        IconButton(
                            onClick = viewModel::removeSelected,
                            enabled = state.selectedQuestionIds.isNotEmpty(),
                        ) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = stringResource(R.string.saved_questions_remove_selected),
                            )
                        }
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

            state.error != null && state.bookmarks.isEmpty() -> Box(
                Modifier.fillMaxSize().padding(innerPadding).padding(Spacing.Lg),
                contentAlignment = Alignment.Center,
            ) { Text(stringResource(R.string.saved_questions_load_error)) }

            else -> Column(Modifier.fillMaxSize().padding(innerPadding)) {
                SavedQuestionsSearchField(
                    query = state.searchQuery,
                    onQueryChange = viewModel::setSearchQuery,
                )
                SavedQuestionFilterRow(
                    filter = state.filter,
                    onChange = viewModel::setFilter,
                )
                val visible = state.bookmarks.filteredFor(state.filter).searchedFor(state.searchQuery, useHi)
                PullToRefreshBox(
                    isRefreshing = state.refreshing,
                    onRefresh = { viewModel.refresh(showLoading = false) },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    if (visible.isEmpty()) {
                        EmptyState(
                            text = if (state.bookmarks.isEmpty()) {
                                stringResource(R.string.saved_questions_empty)
                            } else if (state.searchQuery.isNotBlank()) {
                                stringResource(R.string.saved_questions_search_empty)
                            } else {
                                stringResource(R.string.saved_questions_filter_empty)
                            },
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(Spacing.Lg),
                            verticalArrangement = Arrangement.spacedBy(Spacing.Sm),
                        ) {
                            items(visible, key = { it.questionId }) { bookmark ->
                                SavedQuestionRow(
                                    bookmark = bookmark,
                                    useHi = useHi,
                                    selectionMode = state.selectionMode,
                                    selected = bookmark.questionId in state.selectedQuestionIds,
                                    onClick = { onQuestionClick(bookmark.questionId) },
                                    onLongClick = { viewModel.startSelection(bookmark.questionId) },
                                    onToggleSelected = { viewModel.toggleSelected(bookmark.questionId) },
                                    onRemove = { viewModel.remove(bookmark.questionId) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedQuestionsSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        placeholder = { Text(stringResource(R.string.saved_questions_search_hint)) },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.Lg, vertical = Spacing.Xs),
    )
}

@Composable
private fun SavedQuestionFilterRow(
    filter: SavedQuestionFilter,
    onChange: (SavedQuestionFilter) -> Unit,
) {
    val scroll = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scroll)
            .padding(horizontal = Spacing.Lg, vertical = Spacing.Xs),
        horizontalArrangement = Arrangement.spacedBy(Spacing.Xs),
    ) {
        FilterChipItem(SavedQuestionFilter.ALL, filter, stringResource(R.string.saved_questions_filter_all), onChange)
        FilterChipItem(
            SavedQuestionFilter.WITH_NOTE,
            filter,
            stringResource(R.string.saved_questions_filter_with_note),
            onChange,
        )
        FilterChipItem(
            SavedQuestionFilter.SECTIONALS,
            filter,
            stringResource(R.string.saved_questions_filter_sectionals),
            onChange,
        )
        FilterChipItem(SavedQuestionFilter.PYQ, filter, stringResource(R.string.saved_questions_filter_pyq), onChange)
        FilterChipItem(
            SavedQuestionFilter.DAILY_DIGEST,
            filter,
            stringResource(R.string.saved_questions_filter_daily_digest),
            onChange,
        )
    }
}

@Composable
private fun FilterChipItem(
    value: SavedQuestionFilter,
    current: SavedQuestionFilter,
    label: String,
    onChange: (SavedQuestionFilter) -> Unit,
) {
    FilterChip(
        selected = value == current,
        onClick = { onChange(value) },
        label = { Text(label) },
    )
}

@Composable
private fun EmptyState(text: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.Xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Filled.BookmarkBorder,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(56.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.Md),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SavedQuestionRow(
    bookmark: QuestionBookmark,
    useHi: Boolean,
    selectionMode: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onToggleSelected: () -> Unit,
    onRemove: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = TouchTarget.Min),
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .combinedClickable(
                    onClick = { if (selectionMode) onToggleSelected() else onClick() },
                    onLongClick = onLongClick,
                )
                .padding(Spacing.Md),
        ) {
            if (selectionMode) {
                Checkbox(
                    checked = selected,
                    onCheckedChange = { onToggleSelected() },
                    modifier = Modifier.padding(end = Spacing.Xs),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bookmark.question.localizedStem(useHi),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = bookmark.sourceLabel(useHi),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 2.dp),
                )
                Text(
                    text = bookmark.bookmarkedAt.toLocalDateTime(TimeZone.currentSystemDefault()).date.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
                val note = bookmark.note
                if (!note.isNullOrBlank()) {
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = Spacing.Xs),
                    )
                }
            }
            Spacer(Modifier.size(Spacing.Sm))
            IconButton(onClick = { if (selectionMode) onToggleSelected() else onRemove() }) {
                Icon(
                    Icons.Filled.Bookmark,
                    contentDescription = stringResource(
                        if (selectionMode) R.string.saved_questions_select
                        else R.string.saved_questions_remove,
                    ),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
