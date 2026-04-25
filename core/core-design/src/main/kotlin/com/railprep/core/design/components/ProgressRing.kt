package com.railprep.core.design.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// TODO Phase 1: circular progress arc with centre label; used on Home dashboard + Results.
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier)
}
