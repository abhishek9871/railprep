package com.railprep.core.design.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.railprep.core.design.RailPrepTheme
import com.railprep.core.design.theme.Accent
import com.railprep.core.design.theme.AccentSoft
import com.railprep.core.design.theme.Canvas
import com.railprep.core.design.theme.Danger
import com.railprep.core.design.theme.Ink
import com.railprep.core.design.theme.Muted
import com.railprep.core.design.theme.Primary
import com.railprep.core.design.theme.PrimaryDark
import com.railprep.core.design.theme.PrimarySoft
import com.railprep.core.design.theme.SubjectColors
import com.railprep.core.design.theme.Success
import com.railprep.core.design.theme.Teal
import com.railprep.core.design.theme.Violet
import com.railprep.core.design.tokens.Spacing

private data class Swatch(val name: String, val color: Color, val hex: String)

@Preview(name = "Palette", widthDp = 360, heightDp = 820, showBackground = true, backgroundColor = 0xFFFAFAFB)
@Composable
private fun PalettePreview() {
    val neutrals = listOf(
        Swatch("Ink", Ink, "#0E1422"),
        Swatch("Muted", Muted, "#6B7085"),
        Swatch("Canvas", Canvas, "#FAFAFB"),
    )
    val brand = listOf(
        Swatch("Primary", Primary, "#2B3EA8"),
        Swatch("PrimarySoft", PrimarySoft, "#EEF0FB"),
        Swatch("PrimaryDark", PrimaryDark, "#1B2A7C"),
        Swatch("Accent", Accent, "#F59A2E"),
        Swatch("AccentSoft", AccentSoft, "#FFF3E0"),
    )
    val semantic = listOf(
        Swatch("Success", Success, "#16A34A"),
        Swatch("Danger", Danger, "#DC2626"),
        Swatch("Violet", Violet, "#7C3AED"),
        Swatch("Teal", Teal, "#0891B2"),
    )
    val subjects = listOf(
        Swatch("Math", SubjectColors.Math.dot, "#2B3EA8"),
        Swatch("Reason", SubjectColors.Reason.dot, "#7C3AED"),
        Swatch("GA", SubjectColors.Ga.dot, "#16A34A"),
        Swatch("GS", SubjectColors.Gs.dot, "#F59A2E"),
        Swatch("CA", SubjectColors.Ca.dot, "#DC2626"),
        Swatch("Eng", SubjectColors.Eng.dot, "#0891B2"),
    )

    RailPrepTheme {
        LazyColumn(
            contentPadding = PaddingValues(Spacing.Md),
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(Spacing.Lg),
        ) {
            item { Section("Neutrals", neutrals) }
            item { Section("Brand", brand) }
            item { Section("Semantic", semantic) }
            item { Section("Subjects", subjects) }
        }
    }
}

@Composable
private fun Section(title: String, swatches: List<Swatch>) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.Xs)) {
        Text(title, style = MaterialTheme.typography.headlineSmall)
        swatches.forEach { sw ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(sw.color),
                )
                Spacer(Modifier.size(Spacing.Sm))
                Column {
                    Text(sw.name, style = MaterialTheme.typography.titleMedium)
                    Text(sw.hex, style = MaterialTheme.typography.labelMedium, color = Muted)
                }
            }
        }
    }
}

@Preview(name = "Type scale", widthDp = 360, heightDp = 820, showBackground = true, backgroundColor = 0xFFFAFAFB)
@Composable
private fun TypeScalePreview() {
    RailPrepTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(Spacing.Md),
            verticalArrangement = Arrangement.spacedBy(Spacing.Sm),
        ) {
            Text("Display Large 34/ExtraBold", style = MaterialTheme.typography.displayLarge)
            Text("Display Medium 28/ExtraBold", style = MaterialTheme.typography.displayMedium)
            Text("Headline Large 22/ExtraBold", style = MaterialTheme.typography.headlineLarge)
            Text("Headline Medium 18/ExtraBold", style = MaterialTheme.typography.headlineMedium)
            Text("Headline Small 15/Bold", style = MaterialTheme.typography.headlineSmall)
            Text("Title Medium 14/SemiBold", style = MaterialTheme.typography.titleMedium)
            Text("Body Large 16/Normal — the quick brown fox.", style = MaterialTheme.typography.bodyLarge)
            Text("Body Medium 14/Medium — lorem ipsum dolor sit amet.", style = MaterialTheme.typography.bodyMedium)
            Text("Body Small 13/Medium — consectetur adipiscing.", style = MaterialTheme.typography.bodySmall)
            Text("Label Medium 11/SemiBold", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(Spacing.Xs))
            Text("रेलप्रेप — हिंदी रेंडरिंग जाँच", style = MaterialTheme.typography.headlineLarge)
            Text("हमारा लक्ष्य रेलवे परीक्षाओं की तैयारी को सरल बनाना है।", style = MaterialTheme.typography.bodyLarge)
        }
    }
}
