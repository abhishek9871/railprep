package com.railprep.feature.learn.chapters

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayCircleOutline
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
import com.railprep.core.design.tokens.Spacing
import com.railprep.core.design.tokens.TouchTarget
import com.railprep.domain.model.Chapter
import com.railprep.domain.model.ContentType
import com.railprep.domain.model.Topic
import com.railprep.feature.learn.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChaptersScreen(
    subjectId: String,
    subjectTitle: String,
    onTopicClick: (topicId: String) -> Unit,
    onBack: () -> Unit,
    viewModel: ChaptersViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val useHi = LocalConfiguration.current.locales.get(0).language == "hi"

    LaunchedEffect(subjectId) { viewModel.load(subjectId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(subjectTitle) },
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
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }
            state.error != null -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(Spacing.Lg),
                contentAlignment = Alignment.Center,
            ) {
                Text(state.error!!, style = MaterialTheme.typography.bodyLarge)
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(Spacing.Lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.Md),
            ) {
                items(state.chapters, key = { it.id }) { chapter ->
                    ChapterCard(
                        chapter = chapter,
                        topics = state.topics[chapter.id].orEmpty(),
                        useHi = useHi,
                        onTopicClick = onTopicClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun ChapterCard(
    chapter: Chapter,
    topics: List<Topic>,
    useHi: Boolean,
    onTopicClick: (String) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(Spacing.Md)) {
            Text(
                text = if (useHi) chapter.titleHi else chapter.titleEn,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.size(Spacing.Sm))
            if (topics.isEmpty()) {
                Text(
                    text = stringResource(R.string.learn_chapter_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                topics.forEach { topic ->
                    TopicRow(topic = topic, useHi = useHi, onClick = { onTopicClick(topic.id) })
                    Spacer(Modifier.size(Spacing.Xs))
                }
            }
        }
    }
}

@Composable
private fun TopicRow(topic: Topic, useHi: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = TouchTarget.Min),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.Md, vertical = Spacing.Sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = when (topic.contentType) {
                    ContentType.YT_VIDEO -> Icons.Filled.PlayCircleOutline
                    ContentType.PDF_URL -> Icons.Filled.PictureAsPdf
                    else -> Icons.Filled.PlayCircleOutline
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.size(Spacing.Md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (useHi && !topic.titleHi.isNullOrBlank()) topic.titleHi!! else topic.titleEn,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = topic.source,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
