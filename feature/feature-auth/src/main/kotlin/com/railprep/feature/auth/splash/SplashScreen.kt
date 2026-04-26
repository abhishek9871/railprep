package com.railprep.feature.auth.splash

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.railprep.core.design.RailPrepTheme
import com.railprep.core.design.theme.Accent
import com.railprep.core.design.theme.Primary
import com.railprep.core.design.theme.PrimaryDark
import com.railprep.core.design.theme.SurfaceWhite
import com.railprep.core.design.theme.Teal
import com.railprep.core.design.tokens.Spacing
import com.railprep.feature.auth.R

@Composable
fun SplashScreen(
    onRouteDecided: (SplashDestination) -> Unit,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val destination by viewModel.destination.collectAsStateWithLifecycle()

    LaunchedEffect(destination) {
        destination?.let(onRouteDecided)
    }

    SplashContent()
}

@Composable
internal fun SplashContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryDark),
        contentAlignment = Alignment.Center,
    ) {
        RailBackdrop(modifier = Modifier.fillMaxSize())
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.Md),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.Xl),
        ) {
            Surface(
                color = SurfaceWhite.copy(alpha = 0.12f),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.size(104.dp),
            ) {
                RailBadge()
            }
            Text(
                text = stringResource(R.string.app_wordmark),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.sp,
                ),
                color = SurfaceWhite,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Clip,
            )
            Text(
                text = stringResource(R.string.app_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = SurfaceWhite.copy(alpha = 0.84f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(0.9f),
            )
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = Spacing.Xs),
            ) {
                SplashChip(stringResource(R.string.splash_badge_cbt), Accent)
                Spacer(Modifier.width(Spacing.Xs))
                SplashChip(stringResource(R.string.splash_badge_pyq), Teal)
                Spacer(Modifier.width(Spacing.Xs))
                SplashChip(stringResource(R.string.splash_badge_daily), SurfaceWhite)
            }
        }
    }
}

@Composable
private fun RailBackdrop(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val trackColor = SurfaceWhite.copy(alpha = 0.09f)
        val accentColor = Accent.copy(alpha = 0.88f)
        val lower = size.height * 0.69f
        drawLine(
            color = trackColor,
            start = Offset(size.width * 0.1f, lower),
            end = Offset(size.width * 0.9f, lower - size.height * 0.06f),
            strokeWidth = 6.dp.toPx(),
            cap = StrokeCap.Round,
        )
        drawLine(
            color = trackColor,
            start = Offset(size.width * 0.16f, lower + 46.dp.toPx()),
            end = Offset(size.width * 0.94f, lower - size.height * 0.06f + 46.dp.toPx()),
            strokeWidth = 6.dp.toPx(),
            cap = StrokeCap.Round,
        )
        repeat(7) { i ->
            val x = size.width * (0.18f + i * 0.1f)
            drawLine(
                color = trackColor,
                start = Offset(x, lower + 34.dp.toPx()),
                end = Offset(x + 52.dp.toPx(), lower - 12.dp.toPx()),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
        drawLine(
            color = accentColor,
            start = Offset(size.width * 0.24f, size.height * 0.28f),
            end = Offset(size.width * 0.76f, size.height * 0.28f),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun RailBadge() {
    Canvas(modifier = Modifier.fillMaxSize().padding(18.dp)) {
        val w = size.width
        val h = size.height
        drawRoundRect(
            color = SurfaceWhite,
            topLeft = Offset(w * 0.2f, h * 0.18f),
            size = androidx.compose.ui.geometry.Size(w * 0.6f, h * 0.42f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx(), 12.dp.toPx()),
        )
        drawCircle(color = Primary, radius = 4.5.dp.toPx(), center = Offset(w * 0.34f, h * 0.7f))
        drawCircle(color = Primary, radius = 4.5.dp.toPx(), center = Offset(w * 0.66f, h * 0.7f))
        drawLine(
            color = Accent,
            start = Offset(w * 0.18f, h * 0.84f),
            end = Offset(w * 0.82f, h * 0.84f),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round,
        )
        drawLine(
            color = Primary,
            start = Offset(w * 0.32f, h * 0.32f),
            end = Offset(w * 0.68f, h * 0.32f),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun SplashChip(label: String, color: Color) {
    Surface(
        color = SurfaceWhite.copy(alpha = 0.12f),
        shape = RoundedCornerShape(999.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = color,
            modifier = Modifier.padding(horizontal = Spacing.Sm, vertical = Spacing.Xs),
        )
    }
}

@Preview(widthDp = 360, heightDp = 780, showBackground = false)
@Composable
private fun SplashPreview() {
    RailPrepTheme { SplashContent() }
}
