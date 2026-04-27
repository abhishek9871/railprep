package com.railprep.feature.home.profile.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.railprep.core.design.tokens.Spacing
import com.railprep.feature.home.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    privacyPolicyUrl: String,
    termsUrl: String,
    supportEmail: String,
) {
    val uriHandler = LocalUriHandler.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.about_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Spacing.Lg)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.Md),
        ) {
            Text(
                text = stringResource(R.string.about_intro),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.size(Spacing.Sm))
            Section(title = stringResource(R.string.about_sources_header)) {
                BulletLine(stringResource(R.string.about_src_rrb))
                BulletLine(stringResource(R.string.about_src_ncert))
                BulletLine(stringResource(R.string.about_src_youtube))
                BulletLine(stringResource(R.string.about_src_pib))
                BulletLine(stringResource(R.string.about_src_datagov))
            }
            Section(title = stringResource(R.string.about_licenses_header)) {
                BulletLine(stringResource(R.string.about_license_yt))
                BulletLine(stringResource(R.string.about_license_ncert))
                BulletLine(stringResource(R.string.about_license_godl))
                BulletLine(stringResource(R.string.about_license_cc))
            }
            Section(title = stringResource(R.string.about_legal_header)) {
                if (privacyPolicyUrl.isNotBlank()) {
                    OutlinedButton(onClick = { uriHandler.openUri(privacyPolicyUrl) }) {
                        Text(stringResource(R.string.about_privacy_policy))
                    }
                } else {
                    BulletLine(stringResource(R.string.about_privacy_missing))
                }
                if (termsUrl.isNotBlank()) {
                    OutlinedButton(onClick = { uriHandler.openUri(termsUrl) }) {
                        Text(stringResource(R.string.about_terms))
                    }
                } else {
                    BulletLine(stringResource(R.string.about_terms_missing))
                }
                BulletLine(
                    if (supportEmail.isBlank()) stringResource(R.string.about_support_missing)
                    else stringResource(R.string.about_support_fmt, supportEmail),
                )
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.fillMaxWidth(),
    )
    content()
}

@Composable
private fun BulletLine(text: String) {
    Text(
        text = "• $text",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
