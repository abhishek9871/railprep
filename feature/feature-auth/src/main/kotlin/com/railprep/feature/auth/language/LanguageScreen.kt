package com.railprep.feature.auth.language

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.railprep.core.design.RailPrepTheme
import com.railprep.core.design.theme.Accent
import com.railprep.core.design.theme.AccentSoft
import com.railprep.core.design.theme.Canvas
import com.railprep.core.design.theme.Line
import com.railprep.core.design.theme.Primary
import com.railprep.core.design.theme.PrimarySoft
import com.railprep.core.design.theme.Success
import com.railprep.core.design.theme.SuccessSoft
import com.railprep.core.design.theme.SurfaceWhite
import com.railprep.core.design.tokens.Radius
import com.railprep.core.design.tokens.Spacing
import com.railprep.core.design.tokens.TouchTarget
import com.railprep.domain.model.SupportedLanguage
import com.railprep.feature.auth.R
import com.railprep.feature.auth.common.AuthBrandLockup
import com.railprep.feature.auth.common.AuthEntryBackdrop

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
                is LanguageMessage.FallingBackToEnglish -> snackbarHostState.showSnackbar(fallingBackMsg)
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
        containerColor = Canvas,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Canvas),
        ) {
            AuthEntryBackdrop(modifier = Modifier.fillMaxSize())

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = Spacing.Lg, vertical = Spacing.Xl),
                verticalArrangement = Arrangement.spacedBy(Spacing.Md),
            ) {
                AuthBrandLockup()

                Spacer(Modifier.size(Spacing.Sm))

                Text(
                    text = stringResource(R.string.language_title),
                    style = MaterialTheme.typography.displayMedium.copy(letterSpacing = 0.sp),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = stringResource(R.string.language_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Surface(
                    color = SurfaceWhite,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Line),
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(Spacing.Md),
                        verticalArrangement = Arrangement.spacedBy(Spacing.Sm),
                    ) {
                        SupportedLanguage.entries
                            .filter { it.phase1Supported }
                            .forEach { language ->
                                LanguageCard(
                                    language = language,
                                    isSelected = language == selected,
                                    onClick = { onLanguageClick(language) },
                                )
                            }
                    }
                }

                Surface(
                    color = AccentSoft,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(R.string.language_sync_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(Spacing.Md),
                    )
                }

                Spacer(Modifier.weight(1f))

                Button(
                    onClick = onContinueClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    modifier = Modifier.fillMaxWidth().heightIn(min = TouchTarget.Min + Spacing.Sm),
                ) {
                    Text(
                        text = stringResource(R.string.language_continue),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    )
                }
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
    val background = if (isSelected) PrimarySoft else SurfaceWhite
    val sample = when (language) {
        SupportedLanguage.En -> stringResource(R.string.language_choice_en_sample)
        SupportedLanguage.Hi -> stringResource(R.string.language_choice_hi_sample)
        else -> stringResource(R.string.language_coming_soon)
    }

    Surface(
        color = background,
        shape = RoundedCornerShape(Radius.Sm),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 96.dp)
            .clip(RoundedCornerShape(Radius.Sm))
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(Spacing.Md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.Md),
        ) {
            LanguageCodeBadge(language = language, isSelected = isSelected)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = language.nativeName,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = language.englishName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = sample,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (isSelected) {
                Surface(
                    color = SuccessSoft,
                    shape = CircleShape,
                    modifier = Modifier.size(32.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = Success,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LanguageCodeBadge(
    language: SupportedLanguage,
    isSelected: Boolean,
) {
    Surface(
        color = if (isSelected) Primary else Accent,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.size(52.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = language.code.uppercase(),
                style = MaterialTheme.typography.titleLarge.copy(
                    color = SurfaceWhite,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.sp,
                ),
            )
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
