package com.railprep.feature.home.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.railprep.core.design.theme.Accent
import com.railprep.core.design.theme.AccentSoft
import com.railprep.core.design.theme.Ink
import com.railprep.core.design.theme.Line
import com.railprep.core.design.theme.Primary
import com.railprep.core.design.theme.PrimarySoft
import com.railprep.core.design.theme.Success
import com.railprep.core.design.theme.SuccessSoft
import com.railprep.core.design.theme.SurfaceWhite
import com.railprep.core.design.theme.Teal
import com.railprep.core.design.theme.TealSoft
import com.railprep.core.design.tokens.Spacing
import com.railprep.core.design.tokens.TouchTarget
import com.railprep.feature.home.HomeViewModel
import com.railprep.feature.home.R

@Composable
fun DashboardTab(
    onStartTests: () -> Unit,
    onStartLearning: () -> Unit,
    onOpenBookmarks: () -> Unit,
    /** Optional slot filled by the app nav graph with feature-daily's DailyHomeCard.
     *  Keeps feature-home independent of feature-daily at module level. */
    dailyHomeCard: @Composable () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val name = state.displayName ?: state.email?.substringBefore('@') ?: stringResource(R.string.home_friend)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.Lg, vertical = Spacing.Lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.Md),
    ) {
        HomeHero(name = name)
        dailyHomeCard()
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.Sm), modifier = Modifier.fillMaxWidth()) {
            HomeMetric(
                label = stringResource(R.string.home_metric_sectionals),
                value = stringResource(R.string.home_metric_sectionals_value),
                color = Primary,
                background = PrimarySoft,
                modifier = Modifier.weight(1f),
            )
            HomeMetric(
                label = stringResource(R.string.home_metric_pyq),
                value = stringResource(R.string.home_metric_pyq_value),
                color = Teal,
                background = TealSoft,
                modifier = Modifier.weight(1f),
            )
        }
        DashboardCta(
            icon = Icons.Filled.Quiz,
            title = stringResource(R.string.home_cta_tests_title),
            subtitle = stringResource(R.string.home_cta_tests_subtitle),
            tint = Success,
            background = SuccessSoft,
            onClick = onStartTests,
        )
        DashboardCta(
            icon = Icons.Filled.MenuBook,
            title = stringResource(R.string.home_cta_start_learning),
            subtitle = stringResource(R.string.home_cta_learn_subtitle),
            tint = Primary,
            background = PrimarySoft,
            onClick = onStartLearning,
        )
        DashboardCta(
            icon = Icons.Filled.Bookmark,
            title = stringResource(R.string.home_cta_browse_bookmarks),
            subtitle = stringResource(R.string.home_cta_bookmarks_subtitle),
            tint = Accent,
            background = AccentSoft,
            onClick = onOpenBookmarks,
        )
    }
}

@Composable
private fun HomeHero(name: String) {
    Surface(
        color = SurfaceWhite,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Line),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(Spacing.Md)) {
            HomeRailPattern(modifier = Modifier.matchParentSize())
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.Sm)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.Sm)) {
                    Surface(color = Primary, shape = RoundedCornerShape(8.dp), modifier = Modifier.size(52.dp)) {
                        RailPrepMark()
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.home_welcome_fmt, name),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                            color = Ink,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = stringResource(R.string.home_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.home_hero_title),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.sp,
                    ),
                    color = Ink,
                )
                Text(
                    text = stringResource(R.string.home_hero_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.Xs), modifier = Modifier.fillMaxWidth()) {
                    HeroChip(Icons.Filled.CheckCircle, stringResource(R.string.home_chip_cbt2), Primary, PrimarySoft)
                    HeroChip(Icons.Filled.Article, stringResource(R.string.home_chip_pyq), Teal, TealSoft)
                }
            }
        }
    }
}

@Composable
private fun HomeRailPattern(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val railY = size.height * 0.78f
        drawLine(
            color = Primary.copy(alpha = 0.07f),
            start = Offset(size.width * 0.08f, railY),
            end = Offset(size.width * 0.96f, railY - 42.dp.toPx()),
            strokeWidth = 5.dp.toPx(),
            cap = StrokeCap.Round,
        )
        repeat(7) { index ->
            val x = size.width * (0.13f + index * 0.11f)
            drawLine(
                color = Accent.copy(alpha = 0.16f),
                start = Offset(x, railY + 18.dp.toPx()),
                end = Offset(x + 34.dp.toPx(), railY - 12.dp.toPx()),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
private fun RailPrepMark() {
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

@Composable
private fun HeroChip(
    icon: ImageVector,
    label: String,
    tint: Color,
    background: Color,
) {
    Surface(
        color = background,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.heightIn(min = TouchTarget.Min),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.Sm, vertical = Spacing.Xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.Xs),
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = tint,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun HomeMetric(
    label: String,
    value: String,
    color: Color,
    background: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = background,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.heightIn(min = 86.dp),
    ) {
        Column(modifier = Modifier.padding(Spacing.Md), verticalArrangement = Arrangement.spacedBy(Spacing.Xxs)) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = color,
                maxLines = 1,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DashboardCta(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color,
    background: Color,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, Line),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = TouchTarget.Min * 2),
    ) {
        Row(
            modifier = Modifier.padding(Spacing.Md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.Md),
        ) {
            Surface(color = background, shape = CircleShape, modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(26.dp))
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
