package com.railprep.core.design.theme

import androidx.compose.ui.graphics.Color

// --- Neutrals (from design-tokens.json) ---
val Ink         = Color(0xFF0E1422)
val Ink2        = Color(0xFF2A3040)
val Muted       = Color(0xFF6B7085)
val Line        = Color(0xFFE7E8EE)
val Line2       = Color(0xFFF1F2F6)
val Canvas      = Color(0xFFFAFAFB)
val SurfaceWhite = Color(0xFFFFFFFF)

// --- Brand ---
val Primary     = Color(0xFF2B3EA8)
val PrimarySoft = Color(0xFFEEF0FB)
val PrimaryDark = Color(0xFF1B2A7C)
val Accent      = Color(0xFFF59A2E)
val AccentSoft  = Color(0xFFFFF3E0)

// --- Status ---
val Success     = Color(0xFF16A34A)
val SuccessSoft = Color(0xFFE7F7EC)
val Danger      = Color(0xFFDC2626)
val DangerSoft  = Color(0xFFFDEAEA)

// --- Semantic accents ---
val Violet      = Color(0xFF7C3AED)
val VioletSoft  = Color(0xFFF1EBFE)
val Teal        = Color(0xFF0891B2)
val TealSoft    = Color(0xFFE0F2F7)

object SubjectColors {
    object Math   { val bg = Color(0xFFEEF0FB); val fg = Color(0xFF2B3EA8); val dot = Color(0xFF2B3EA8) }
    object Reason { val bg = Color(0xFFF1EBFE); val fg = Color(0xFF6D28D9); val dot = Color(0xFF7C3AED) }
    object Ga     { val bg = Color(0xFFE7F7EC); val fg = Color(0xFF15803D); val dot = Color(0xFF16A34A) }
    object Gs     { val bg = Color(0xFFFFF3E0); val fg = Color(0xFFB45309); val dot = Color(0xFFF59A2E) }
    object Ca     { val bg = Color(0xFFFDEAEA); val fg = Color(0xFFB91C1C); val dot = Color(0xFFDC2626) }
    object Eng    { val bg = Color(0xFFE0F2F7); val fg = Color(0xFF0E7490); val dot = Color(0xFF0891B2) }
}
