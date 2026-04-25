package com.railprep.core.design.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// TODO Phase 1: avatar with Coil image URL, name-initial fallback, optional status dot.
@Composable
fun RPAvatar(
    imageUrl: String? = null,
    name: String = "",
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier)
}
