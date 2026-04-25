package com.railprep.feature.auth.snapshot

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import com.railprep.core.design.RailPrepLightColorScheme
import com.railprep.core.design.theme.RailPrepShapes
import com.railprep.core.design.theme.RailPrepTypography

/**
 * Paparazzi JVM layoutlib can't load the bundled variable TTFs or the Downloadable Google Fonts
 * provider. We keep the color scheme + shapes + type SCALE from production but override the
 * font family to the JVM system default so layouts can render. Snapshot coverage focuses on
 * layout + color correctness; typography pixel-match is validated on-device (via the prototype
 * and manual QA).
 */
@Composable
internal fun PaparazziTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = RailPrepLightColorScheme,
        typography = RailPrepTypography.systemFontCopy(),
        shapes = RailPrepShapes,
        content = content,
    )
}

private fun Typography.systemFontCopy(): Typography = copy(
    displayLarge   = displayLarge.copy(fontFamily = FontFamily.Default),
    displayMedium  = displayMedium.copy(fontFamily = FontFamily.Default),
    displaySmall   = displaySmall.copy(fontFamily = FontFamily.Default),
    headlineLarge  = headlineLarge.copy(fontFamily = FontFamily.Default),
    headlineMedium = headlineMedium.copy(fontFamily = FontFamily.Default),
    headlineSmall  = headlineSmall.copy(fontFamily = FontFamily.Default),
    titleLarge     = titleLarge.copy(fontFamily = FontFamily.Default),
    titleMedium    = titleMedium.copy(fontFamily = FontFamily.Default),
    titleSmall     = titleSmall.copy(fontFamily = FontFamily.Default),
    bodyLarge      = bodyLarge.copy(fontFamily = FontFamily.Default),
    bodyMedium     = bodyMedium.copy(fontFamily = FontFamily.Default),
    bodySmall      = bodySmall.copy(fontFamily = FontFamily.Default),
    labelLarge     = labelLarge.copy(fontFamily = FontFamily.Default),
    labelMedium    = labelMedium.copy(fontFamily = FontFamily.Default),
    labelSmall     = labelSmall.copy(fontFamily = FontFamily.Default),
)
