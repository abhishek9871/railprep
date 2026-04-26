package com.railprep.feature.auth.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.railprep.core.design.theme.Accent
import com.railprep.core.design.theme.AccentSoft
import com.railprep.core.design.theme.Ink
import com.railprep.core.design.theme.Line
import com.railprep.core.design.theme.Primary
import com.railprep.core.design.theme.PrimarySoft
import com.railprep.core.design.theme.SurfaceWhite
import com.railprep.core.design.theme.Teal
import com.railprep.core.design.theme.TealSoft
import com.railprep.core.design.tokens.Spacing
import com.railprep.feature.auth.R

@Composable
internal fun AuthEntryBackdrop(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val trackColor = Primary.copy(alpha = 0.06f)
        val accentColor = Accent.copy(alpha = 0.24f)
        val topRailY = size.height * 0.16f
        val lowerRailY = size.height * 0.77f

        drawLine(
            color = trackColor,
            start = Offset(-32.dp.toPx(), topRailY),
            end = Offset(size.width * 0.62f, topRailY + 72.dp.toPx()),
            strokeWidth = 5.dp.toPx(),
            cap = StrokeCap.Round,
        )
        drawLine(
            color = trackColor,
            start = Offset(size.width * 0.42f, lowerRailY),
            end = Offset(size.width + 32.dp.toPx(), lowerRailY - 80.dp.toPx()),
            strokeWidth = 5.dp.toPx(),
            cap = StrokeCap.Round,
        )
        repeat(5) { index ->
            val x = size.width * (0.08f + index * 0.17f)
            drawLine(
                color = accentColor,
                start = Offset(x, topRailY + 16.dp.toPx()),
                end = Offset(x + 44.dp.toPx(), topRailY + 56.dp.toPx()),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
        repeat(4) { index ->
            val x = size.width * (0.52f + index * 0.13f)
            drawLine(
                color = trackColor,
                start = Offset(x, lowerRailY - 46.dp.toPx()),
                end = Offset(x + 42.dp.toPx(), lowerRailY - 86.dp.toPx()),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
internal fun AuthBrandLockup(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.Sm),
    ) {
        Surface(
            color = Primary,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.size(52.dp),
        ) {
            RailMiniMark()
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = stringResource(R.string.app_wordmark),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.sp,
                ),
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Clip,
            )
            Text(
                text = stringResource(R.string.auth_brand_label),
                style = MaterialTheme.typography.labelLarge,
                color = Primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
internal fun AuthHeroPanel(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = SurfaceWhite,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Line),
        shadowElevation = 1.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(Spacing.Md),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val trackY = size.height * 0.68f
                drawLine(
                    color = Primary.copy(alpha = 0.12f),
                    start = Offset(size.width * 0.08f, trackY),
                    end = Offset(size.width * 0.94f, trackY - 28.dp.toPx()),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round,
                )
                repeat(6) { index ->
                    val x = size.width * (0.12f + index * 0.13f)
                    drawLine(
                        color = Primary.copy(alpha = 0.09f),
                        start = Offset(x, trackY + 16.dp.toPx()),
                        end = Offset(x + 34.dp.toPx(), trackY - 13.dp.toPx()),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                }
                drawRoundRect(
                    color = PrimarySoft,
                    topLeft = Offset(size.width * 0.55f, size.height * 0.12f),
                    size = Size(size.width * 0.34f, size.height * 0.28f),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                )
                drawRoundRect(
                    color = AccentSoft,
                    topLeft = Offset(size.width * 0.67f, size.height * 0.44f),
                    size = Size(size.width * 0.22f, size.height * 0.16f),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                )
            }

            Column(
                modifier = Modifier.align(Alignment.TopStart),
                verticalArrangement = Arrangement.spacedBy(Spacing.Sm),
            ) {
                Text(
                    text = stringResource(R.string.app_wordmark),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.sp,
                    ),
                    color = Primary,
                    maxLines = 1,
                )
                Text(
                    text = stringResource(R.string.app_tagline),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(0.72f),
                )
            }
            AuthStatRow(modifier = Modifier.align(Alignment.BottomStart))
        }
    }
}

@Composable
internal fun AuthStatRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.Xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AuthStatChip(
            text = stringResource(R.string.auth_stat_tests),
            color = Primary,
            background = PrimarySoft,
            modifier = Modifier.weight(1f),
        )
        AuthStatChip(
            text = stringResource(R.string.auth_stat_review),
            color = Teal,
            background = TealSoft,
            modifier = Modifier.weight(1f),
        )
        AuthStatChip(
            text = stringResource(R.string.auth_stat_bilingual),
            color = Accent,
            background = AccentSoft,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun AuthStatChip(
    text: String,
    color: Color,
    background: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = background,
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = Spacing.Xs, vertical = Spacing.Xs),
        )
    }
}

@Composable
private fun RailMiniMark() {
    Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
        val trainWidth = size.width * 0.72f
        val trainHeight = size.height * 0.44f
        val left = (size.width - trainWidth) / 2f
        val top = size.height * 0.14f
        drawRoundRect(
            color = SurfaceWhite,
            topLeft = Offset(left, top),
            size = Size(trainWidth, trainHeight),
            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
        )
        drawLine(
            color = Primary,
            start = Offset(left + trainWidth * 0.22f, top + trainHeight * 0.36f),
            end = Offset(left + trainWidth * 0.78f, top + trainHeight * 0.36f),
            strokeWidth = 2.5.dp.toPx(),
            cap = StrokeCap.Round,
        )
        drawCircle(
            color = SurfaceWhite,
            radius = 3.5.dp.toPx(),
            center = Offset(left + trainWidth * 0.26f, top + trainHeight + 9.dp.toPx()),
        )
        drawCircle(
            color = SurfaceWhite,
            radius = 3.5.dp.toPx(),
            center = Offset(left + trainWidth * 0.74f, top + trainHeight + 9.dp.toPx()),
        )
        drawLine(
            color = Accent,
            start = Offset(size.width * 0.14f, size.height * 0.84f),
            end = Offset(size.width * 0.86f, size.height * 0.84f),
            strokeWidth = 2.5.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}
