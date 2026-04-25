package com.railprep.core.design.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// TODO Phase 1: filter / selection / assist chip variants with subject-color theming.
@Composable
fun RPChip(
    selected: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    content: @Composable () -> Unit = {},
) {
    Box(modifier = modifier) { content() }
}
