package com.railprep.core.design.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font as GoogleFontsFont
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.railprep.core.design.R

private val GoogleFontsProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

// --- Display (Plus Jakarta Sans): Downloadable primary + bundled variable-font fallback. ---
private val displayWeights = listOf(
    FontWeight.Medium, FontWeight.SemiBold, FontWeight.Bold, FontWeight.ExtraBold, FontWeight.Black,
)

val DisplayFontFamily: FontFamily = FontFamily(
    buildList {
        // Bundled (primary) — instant rendering, works offline, works in Paparazzi JVM tests.
        // The variable TTF covers every weight; the weight annotation routes Compose correctly.
        displayWeights.forEach { w ->
            add(Font(resId = R.font.plus_jakarta_sans, weight = w, style = FontStyle.Normal))
        }
        // Downloadable (safety net) — shipped so future Google Fonts updates can override
        // without a new APK. Not invoked while bundled resolves cleanly.
        displayWeights.forEach { w ->
            add(GoogleFontsFont(googleFont = GoogleFont("Plus Jakarta Sans"), fontProvider = GoogleFontsProvider, weight = w, style = FontStyle.Normal))
        }
    },
)

// --- Body (Inter): same layering. ---
private val bodyWeights = listOf(
    FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold, FontWeight.Bold, FontWeight.ExtraBold,
)

val BodyFontFamily: FontFamily = FontFamily(
    buildList {
        bodyWeights.forEach { w ->
            add(Font(resId = R.font.inter, weight = w, style = FontStyle.Normal))
        }
        bodyWeights.forEach { w ->
            add(GoogleFontsFont(googleFont = GoogleFont("Inter"), fontProvider = GoogleFontsProvider, weight = w, style = FontStyle.Normal))
        }
    },
)

// Devanagari stays Downloadable-only in Phase 1 (no bundled fallback).
// See NOTICE.md and README's deferred list.
val DevanagariFontFamily: FontFamily = FontFamily(
    GoogleFontsFont(googleFont = GoogleFont("Noto Sans Devanagari"), fontProvider = GoogleFontsProvider, weight = FontWeight.Normal),
    GoogleFontsFont(googleFont = GoogleFont("Noto Sans Devanagari"), fontProvider = GoogleFontsProvider, weight = FontWeight.Medium),
    GoogleFontsFont(googleFont = GoogleFont("Noto Sans Devanagari"), fontProvider = GoogleFontsProvider, weight = FontWeight.SemiBold),
    GoogleFontsFont(googleFont = GoogleFont("Noto Sans Devanagari"), fontProvider = GoogleFontsProvider, weight = FontWeight.Bold),
)

// Scale mirrors design-tokens.json → Material3 Typography slots.
val RailPrepTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = DisplayFontFamily, fontWeight = FontWeight.ExtraBold,
        fontSize = 34.sp, lineHeight = (34 * 1.15).sp, letterSpacing = (-1.0).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = DisplayFontFamily, fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp, lineHeight = (28 * 1.2).sp, letterSpacing = (-0.8).sp,
    ),
    displaySmall = TextStyle(
        fontFamily = DisplayFontFamily, fontWeight = FontWeight.ExtraBold,
        fontSize = 24.sp, lineHeight = (24 * 1.2).sp, letterSpacing = (-0.6).sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = DisplayFontFamily, fontWeight = FontWeight.ExtraBold,
        fontSize = 22.sp, lineHeight = (22 * 1.25).sp, letterSpacing = (-0.6).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = DisplayFontFamily, fontWeight = FontWeight.ExtraBold,
        fontSize = 18.sp, lineHeight = (18 * 1.3).sp, letterSpacing = (-0.4).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = DisplayFontFamily, fontWeight = FontWeight.Bold,
        fontSize = 15.sp, lineHeight = (15 * 1.35).sp, letterSpacing = (-0.3).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = DisplayFontFamily, fontWeight = FontWeight.Bold,
        fontSize = 16.sp, lineHeight = (16 * 1.3).sp, letterSpacing = (-0.2).sp,
    ),
    titleMedium = TextStyle(
        fontFamily = BodyFontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, lineHeight = (14 * 1.4).sp, letterSpacing = 0.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = BodyFontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp, lineHeight = (13 * 1.4).sp, letterSpacing = 0.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = BodyFontFamily, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = (16 * 1.5).sp, letterSpacing = 0.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = BodyFontFamily, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = (14 * 1.5).sp, letterSpacing = 0.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = BodyFontFamily, fontWeight = FontWeight.Medium,
        fontSize = 13.sp, lineHeight = (13 * 1.45).sp, letterSpacing = 0.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = BodyFontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp, lineHeight = (13 * 1.4).sp, letterSpacing = 0.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = BodyFontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp, lineHeight = (11 * 1.4).sp, letterSpacing = 0.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = BodyFontFamily, fontWeight = FontWeight.Bold,
        fontSize = 10.sp, lineHeight = (10 * 1.3).sp, letterSpacing = 0.5.sp,
    ),
)
