package com.railprep.core.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.railprep.core.design.theme.Accent
import com.railprep.core.design.theme.AccentSoft
import com.railprep.core.design.theme.Canvas
import com.railprep.core.design.theme.Danger
import com.railprep.core.design.theme.DangerSoft
import com.railprep.core.design.theme.Ink
import com.railprep.core.design.theme.Line
import com.railprep.core.design.theme.Line2
import com.railprep.core.design.theme.Muted
import com.railprep.core.design.theme.Primary
import com.railprep.core.design.theme.PrimaryDark
import com.railprep.core.design.theme.PrimarySoft
import com.railprep.core.design.theme.RailPrepShapes
import com.railprep.core.design.theme.RailPrepTypography
import com.railprep.core.design.theme.SurfaceWhite
import com.railprep.core.design.theme.Teal

val RailPrepLightColorScheme = lightColorScheme(
    primary              = Primary,
    onPrimary            = SurfaceWhite,
    primaryContainer     = PrimarySoft,
    onPrimaryContainer   = PrimaryDark,
    secondary            = Accent,
    onSecondary          = SurfaceWhite,
    secondaryContainer   = AccentSoft,
    onSecondaryContainer = Ink,
    tertiary             = Teal,
    onTertiary           = SurfaceWhite,
    background           = Canvas,
    onBackground         = Ink,
    surface              = SurfaceWhite,
    onSurface            = Ink,
    surfaceVariant       = Line2,
    onSurfaceVariant     = Muted,
    outline              = Line,
    outlineVariant       = Line2,
    error                = Danger,
    onError              = SurfaceWhite,
    errorContainer       = DangerSoft,
    onErrorContainer     = Danger,
)

/**
 * Root theme for all RailPrep surfaces. Light-only in Phase 0; dark scheme deferred to Phase 3.
 */
@Composable
fun RailPrepTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = RailPrepLightColorScheme,
        typography = RailPrepTypography,
        shapes = RailPrepShapes,
        content = content,
    )
}
