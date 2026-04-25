package com.railprep.core.design.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// TODO Phase 1: card surfaces per prototype — elevation Card/Lifted, outlined/flat variants, onClick ripple.
@Composable
fun RPCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier) { content() }
}
