package com.railprep.feature.home.profile.diag

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.railprep.core.design.tokens.Spacing
import com.railprep.core.design.tokens.TouchTarget
import com.railprep.feature.home.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagScreen(
    onBack: () -> Unit,
    viewModel: DiagViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val clearedMsg = stringResource(R.string.diag_cleared)

    LaunchedEffect(state.cleared) {
        if (state.cleared) {
            snackbarHostState.showSnackbar(clearedMsg)
            viewModel.clearFlag()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.diag_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Spacing.Lg, vertical = Spacing.Lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.Md),
        ) {
            LabelValue(label = stringResource(R.string.diag_version), value = state.appVersion.ifBlank { "—" })
            LabelValue(label = stringResource(R.string.diag_user_id), value = state.userId ?: "—")
            LabelValue(
                label = stringResource(R.string.diag_pdf_cache),
                value = "${state.pdfCacheBytes / 1024L} KB",
            )
            val last = state.lastPdf
            if (last != null) {
                Text(
                    stringResource(R.string.diag_last_pdf_header),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = Spacing.Sm),
                )
                LabelValue(label = stringResource(R.string.diag_last_pdf_outcome), value = last.outcome)
                LabelValue(label = stringResource(R.string.diag_last_pdf_http), value = last.httpCode.toString())
                LabelValue(label = stringResource(R.string.diag_last_pdf_bytes), value = "${last.bytes}")
                LabelValue(label = stringResource(R.string.diag_last_pdf_ct), value = last.contentType ?: "—")
                LabelValue(label = stringResource(R.string.diag_last_pdf_magic), value = last.magicHex.ifBlank { "—" })
                val err = last.error
                if (!err.isNullOrBlank()) {
                    LabelValue(label = stringResource(R.string.diag_last_pdf_err), value = err)
                }
                Text(
                    text = last.url,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            OutlinedButton(
                onClick = { viewModel.refresh() },
                modifier = Modifier.fillMaxWidth().heightIn(min = TouchTarget.Min),
            ) { Text(stringResource(R.string.diag_refresh)) }
            OutlinedButton(
                onClick = { viewModel.clearPdfCache() },
                modifier = Modifier.fillMaxWidth().heightIn(min = TouchTarget.Min),
            ) { Text(stringResource(R.string.diag_clear_cache)) }
        }
    }
}

@Composable
private fun LabelValue(label: String, value: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
