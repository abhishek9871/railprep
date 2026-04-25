package com.railprep.core.design.tokens

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Spacing scale from design-tokens.json. Named with dp step for readability. */
object Spacing {
    val None: Dp = 0.dp
    val Xxs: Dp = 4.dp
    val Xs:  Dp = 8.dp
    val Sm:  Dp = 12.dp
    val Md:  Dp = 16.dp
    val Ml:  Dp = 20.dp
    val Lg:  Dp = 24.dp
    val Xl:  Dp = 32.dp
    val Xxl: Dp = 40.dp
    val Xxxl: Dp = 48.dp
}

object Radius {
    val Xs:   Dp = 6.dp
    val Sm:   Dp = 10.dp
    val Md:   Dp = 14.dp
    val Lg:   Dp = 20.dp
    val Xl:   Dp = 28.dp
    val Pill: Dp = 999.dp
}

/**
 * Elevation in dp as Compose expects. The raw token values use pixel/alpha shadow specs
 * (e.g. "0 8px 20px rgba(43,62,168,0.35)") — Compose doesn't accept those directly,
 * so we translate to conservative dp equivalents. Callers that need tinted shadows
 * compose them via [androidx.compose.foundation.shadow] + color.
 */
object Elevation {
    val None:     Dp = 0.dp
    val Card:     Dp = 1.dp
    val Lifted:   Dp = 4.dp
    val Floating: Dp = 8.dp
    val Modal:    Dp = 20.dp
}

object TouchTarget {
    val Min: Dp = 44.dp
}
