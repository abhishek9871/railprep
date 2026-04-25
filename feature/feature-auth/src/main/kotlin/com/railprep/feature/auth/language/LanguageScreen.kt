package com.railprep.feature.auth.language

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.railprep.core.design.RailPrepTheme
import com.railprep.core.design.theme.Line
import com.railprep.core.design.theme.Primary
import com.railprep.core.design.theme.PrimarySoft
import com.railprep.core.design.tokens.Radius
import com.railprep.core.design.tokens.Spacing
import com.railprep.core.design.tokens.TouchTarget
import com.railprep.domain.model.SupportedLanguage
import com.railprep.feature.auth.R
import androidx.compose.runtime.LaunchedEffect

@Composable
fun LanguageScreen(
    onContinue: () -> Unit,
    viewModel: LanguageViewModel = hiltViewModel(),
) {
    val selected by viewModel.selected.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val fallingBackMsg = stringResource(R.string.language_coming_soon)

    LaunchedEffect(Unit) {
        viewModel.messages.collect { msg ->
            when (msg) {
                is LanguageMessage.FallingBackToEnglish -> {
                    snackbarHostState.showSnackbar(fallingBackMsg)
                }
            }
        }
    }

    LanguageContent(
        selected = selected,
        onLanguageClick = viewModel::onLanguageClicked,
        onContinueClick = { viewModel.onContinueClicked(onContinue) },
        snackbarHostState = snackbarHostState,
    )
}

@Composable
internal fun LanguageContent(
    selected: SupportedLanguage,
    onLanguageClick: (SupportedLanguage) -> Unit,
    onContinueClick: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Spacing.Lg, vertical = Spacing.Xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.Lg),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.Xs)) {
                Text(
                    stringResource(R.string.language_title),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    stringResource(R.string.language_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(Spacing.Sm),
                horizontalArrangement = Arrangement.spacedBy(Spacing.Sm),
                modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
            ) {
                items(SupportedLanguage.entries) { lang ->
                    LanguageCard(
                        language = lang,
                        isSelected = lang == selected,
                        onClick = { onLanguageClick(lang) },
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = onContinueClick,
                modifier = Modifier.fillMaxWidth().heightIn(min = TouchTarget.Min),
            ) {
                Text(stringResource(R.string.language_continue))
            }
        }
    }
}

@Composable
private fun LanguageCard(
    language: SupportedLanguage,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (isSelected) Primary else Line
    Surface(
        color = if (isSelected) PrimarySoft else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(Radius.Md),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp)
            .clip(RoundedCornerShape(Radius.Md))
            .border(2.dp, borderColor, RoundedCornerShape(Radius.Md))
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(Spacing.Md),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = language.nativeName,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = language.englishName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (!language.phase1Supported) {
                    Spacer(Modifier.size(Spacing.Xs))
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(Radius.Pill),
                    ) {
                        Text(
                            stringResource(R.string.language_coming_soon_badge),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Preview(widthDp = 360, heightDp = 780)
@Composable
private fun LanguagePreview() {
    RailPrepTheme {
        LanguageContent(
            selected = SupportedLanguage.En,
            onLanguageClick = {},
            onContinueClick = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
