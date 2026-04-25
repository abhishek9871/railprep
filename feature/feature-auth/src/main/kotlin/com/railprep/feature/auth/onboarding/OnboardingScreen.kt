package com.railprep.feature.auth.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.railprep.core.design.RailPrepTheme
import com.railprep.core.design.theme.Line
import com.railprep.core.design.theme.Primary
import com.railprep.core.design.tokens.Spacing
import com.railprep.core.design.tokens.TouchTarget
import com.railprep.feature.auth.R
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onCompleted: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    OnboardingContent(onDone = { viewModel.onCompleted(onCompleted) })
}

private data class Slide(val titleRes: Int, val bodyRes: Int, val illustrationRes: Int)

private val slides = listOf(
    Slide(R.string.onboarding_slide1_title, R.string.onboarding_slide1_body, R.drawable.ic_onboarding_1),
    Slide(R.string.onboarding_slide2_title, R.string.onboarding_slide2_body, R.drawable.ic_onboarding_2),
    Slide(R.string.onboarding_slide3_title, R.string.onboarding_slide3_body, R.drawable.ic_onboarding_3),
)

@Composable
internal fun OnboardingContent(onDone: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { slides.size })
    val scope = rememberCoroutineScope()

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(Spacing.Md),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onDone) {
                    Text(stringResource(R.string.onboarding_skip), style = MaterialTheme.typography.titleMedium)
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth().weight(1f),
            ) { page ->
                val slide = slides[page]
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.Xl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Image(
                        painter = painterResource(slide.illustrationRes),
                        contentDescription = null,
                        modifier = Modifier.size(220.dp),
                    )
                    Spacer(Modifier.size(Spacing.Xl))
                    Text(
                        stringResource(slide.titleRes),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.size(Spacing.Sm))
                    Text(
                        stringResource(slide.bodyRes),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.Md),
                horizontalArrangement = Arrangement.Center,
            ) {
                repeat(slides.size) { i ->
                    val isActive = pagerState.currentPage == i
                    Box(
                        modifier = Modifier
                            .size(if (isActive) 10.dp else 8.dp)
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(if (isActive) Primary else Line),
                    )
                }
            }

            Button(
                onClick = {
                    if (pagerState.currentPage == slides.lastIndex) {
                        onDone()
                    } else {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.Lg)
                    .padding(bottom = Spacing.Xl)
                    .heightIn(min = TouchTarget.Min),
            ) {
                Text(
                    if (pagerState.currentPage == slides.lastIndex) stringResource(R.string.onboarding_get_started)
                    else stringResource(R.string.onboarding_next),
                )
            }
        }
    }
}

@Preview(widthDp = 360, heightDp = 780)
@Composable
private fun OnboardingPreview() {
    RailPrepTheme { OnboardingContent(onDone = {}) }
}
