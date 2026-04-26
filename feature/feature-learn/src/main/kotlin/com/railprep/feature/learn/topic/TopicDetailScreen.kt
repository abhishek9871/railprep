package com.railprep.feature.learn.topic

import android.content.Intent
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.railprep.core.design.tokens.Spacing
import com.railprep.domain.model.ContentType
import com.railprep.domain.model.License
import com.railprep.domain.model.Topic
import com.railprep.feature.learn.R
import com.railprep.feature.learn.pdf.PdfViewer
import com.railprep.feature.learn.youtube.YouTubePlayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicDetailScreen(
    topicId: String,
    onBack: () -> Unit,
    viewModel: TopicDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(topicId) { viewModel.load(topicId) }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.topic?.titleEn ?: stringResource(R.string.learn_topic_title_fallback)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    if (state.topic != null) {
                        IconButton(onClick = { viewModel.toggleBookmark() }) {
                            Icon(
                                imageVector = if (state.bookmarked) Icons.Filled.Bookmark
                                    else Icons.Filled.BookmarkBorder,
                                contentDescription = stringResource(R.string.learn_topic_bookmark),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        when {
            state.loading -> Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }
            state.topic == null -> Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) { Text(stringResource(R.string.learn_topic_load_failed)) }
            else -> {
                val topic = state.topic!!
                Column(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                ) {
                    when (topic.contentType) {
                        ContentType.YT_VIDEO -> {
                            val videoId = topic.externalVideoId
                            if (videoId != null) {
                                YouTubePlayer(
                                    videoId = videoId,
                                    lifecycleOwner = lifecycleOwner,
                                    onError = { viewModel.reportPlayerError() },
                                )
                            }
                        }
                        ContentType.PDF_URL -> PdfBody(
                            pdfFile = state.pdfFile,
                            downloading = state.pdfDownloading,
                            failure = state.pdfFailure,
                            pdfUrl = topic.externalPdfUrl,
                            onRetry = { viewModel.retryPdf() },
                            modifier = Modifier.weight(1f),
                        )
                        ContentType.ARTICLE, ContentType.QUIZ -> ArticleBody(topic)
                    }
                    AttributionFooter(topic)
                }
            }
        }
    }
}

@Composable
private fun PdfBody(
    pdfFile: java.io.File?,
    downloading: Boolean,
    failure: PdfFailure?,
    pdfUrl: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        when {
            pdfFile != null -> PdfViewer(pdfFile, modifier = Modifier.fillMaxSize())
            downloading -> CircularProgressIndicator()
            failure == PdfFailure.NETWORK -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.Sm),
                modifier = Modifier.padding(Spacing.Lg),
            ) {
                Text(
                    stringResource(R.string.learn_pdf_network_error),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Button(onClick = onRetry) {
                    Icon(Icons.Filled.Refresh, contentDescription = null)
                    Spacer(Modifier.size(Spacing.Xs))
                    Text(stringResource(R.string.learn_pdf_retry))
                }
            }
            failure == PdfFailure.UNSUPPORTED -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.Sm),
                modifier = Modifier.padding(Spacing.Lg),
            ) {
                Text(
                    stringResource(R.string.learn_pdf_unsupported),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    stringResource(R.string.learn_pdf_unsupported_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (pdfUrl != null) {
                    Button(onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, pdfUrl.toUri()))
                    }) {
                        Icon(Icons.Filled.OpenInBrowser, contentDescription = null)
                        Spacer(Modifier.size(Spacing.Xs))
                        Text(stringResource(R.string.learn_pdf_open_external))
                    }
                }
            }
            else -> CircularProgressIndicator()
        }
    }
}

@Composable
private fun ArticleBody(topic: Topic) {
    val content = topic.contentMd?.trim().orEmpty()
    SelectionContainer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.Lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.Sm),
        ) {
            if (content.isNotBlank()) {
                MarkdownContent(content)
            } else {
                Text(
                    text = stringResource(R.string.learn_article_external_hint),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MarkdownContent(content: String) {
    content.lineSequence().forEach { rawLine ->
        val line = rawLine.trim()
        when {
            line.isBlank() -> Spacer(Modifier.size(Spacing.Xxs))
            line.startsWith("### ") -> Text(
                cleanInlineMarkdown(line.removePrefix("### ")),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            line.startsWith("## ") -> Text(
                cleanInlineMarkdown(line.removePrefix("## ")),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            line.startsWith("# ") -> Text(
                cleanInlineMarkdown(line.removePrefix("# ")),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
            line.startsWith("- ") || line.startsWith("* ") -> Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.Xs),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("•", style = MaterialTheme.typography.bodyLarge)
                Text(
                    cleanInlineMarkdown(line.drop(2)),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                )
            }
            line.startsWith("|") -> Text(
                cleanInlineMarkdown(line),
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            else -> Text(
                cleanInlineMarkdown(line),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

private fun cleanInlineMarkdown(value: String): String =
    value.replace("**", "").replace("__", "").replace("`", "").replace("*", "")

@Composable
private fun AttributionFooter(topic: Topic) {
    val text = when (topic.license) {
        License.NCERT_LINKED -> stringResource(R.string.attrib_ncert_fmt, topic.source)
        License.GODL_INDIA -> stringResource(R.string.attrib_godl_fmt, topic.source)
        License.CC_BY_SA -> stringResource(R.string.attrib_ccbysa_fmt, topic.source)
        License.YT_STANDARD -> stringResource(R.string.attrib_yt_fmt, topic.source)
        License.PUBLIC_DOMAIN -> stringResource(R.string.attrib_public_fmt, topic.source)
        License.ORIGINAL -> stringResource(R.string.attrib_original_fmt, topic.source)
    }
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(topLeft = 0.dp, topRight = 0.dp, bottomLeft = 0.dp, bottomRight = 0.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(Spacing.Md),
        )
    }
}

private fun RoundedCornerShape(
    topLeft: androidx.compose.ui.unit.Dp,
    topRight: androidx.compose.ui.unit.Dp,
    bottomLeft: androidx.compose.ui.unit.Dp,
    bottomRight: androidx.compose.ui.unit.Dp,
) = androidx.compose.foundation.shape.RoundedCornerShape(
    topStart = topLeft,
    topEnd = topRight,
    bottomStart = bottomLeft,
    bottomEnd = bottomRight,
)
