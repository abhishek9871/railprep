package com.railprep.feature.tests.pyq

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.railprep.core.design.tokens.Spacing
import com.railprep.feature.tests.R
import com.railprep.feature.learn.pdf.PdfViewer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PyqPaperScreen(
    testId: String,
    onBack: () -> Unit,
    viewModel: PyqPaperViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(testId) { viewModel.load(testId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.test?.titleEn ?: "PYQ Paper",
                        maxLines = 1,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Box(
                modifier = Modifier.fillMaxSize().weight(1f, fill = true),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    state.loading -> CircularProgressIndicator()
                    state.pdfFile != null -> PdfViewer(state.pdfFile!!, modifier = Modifier.fillMaxSize())
                    state.pdfDownloading -> Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.Sm),
                    ) {
                        CircularProgressIndicator()
                        Text("Downloading paper…", style = MaterialTheme.typography.bodyMedium)
                    }
                    state.failure == PyqPaperFailure.NETWORK -> Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.Sm),
                        modifier = Modifier.padding(Spacing.Lg),
                    ) {
                        Text("Network error. Check your connection and retry.",
                             style = MaterialTheme.typography.bodyLarge)
                        Button(onClick = { viewModel.retryPdf() }) {
                            Icon(Icons.Filled.Refresh, contentDescription = null)
                            Spacer(Modifier.size(Spacing.Xs))
                            Text("Retry")
                        }
                    }
                    state.failure == PyqPaperFailure.UNSUPPORTED -> Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.Sm),
                        modifier = Modifier.padding(Spacing.Lg),
                    ) {
                        Text("Couldn't open this paper in-app.",
                             style = MaterialTheme.typography.bodyLarge)
                        val url = state.test?.externalUrl
                        if (url != null) {
                            Button(onClick = {
                                context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                            }) {
                                Icon(Icons.Filled.OpenInBrowser, contentDescription = null)
                                Spacer(Modifier.size(Spacing.Xs))
                                Text("Open in browser")
                            }
                        }
                    }
                    state.failure == PyqPaperFailure.NOT_A_PYQ_LINK -> Text(
                        "This test isn't a PYQ paper — open it from the Tests list instead.",
                        modifier = Modifier.padding(Spacing.Lg),
                    )
                    state.failure == PyqPaperFailure.NOT_FOUND -> Text(
                        "Paper not found.",
                        modifier = Modifier.padding(Spacing.Lg),
                    )
                    else -> CircularProgressIndicator()
                }
            }
            AttributionFooter(state.test?.sourceAttribution)
        }
    }
}

@Composable
private fun AttributionFooter(attribution: String?) {
    val source = attribution ?: return
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(R.string.tests_pyq_footer_fmt, source),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(Spacing.Md),
        )
    }
}
