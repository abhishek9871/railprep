package com.railprep.core.design.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// Phase 0 stub. Renders the themed M3 Button so the theme can be verified end-to-end.
// TODO Phase 1: full variant system (primary/secondary/ghost/destructive), loading state, icon slots.
@Composable
fun RPButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(),
        content = content,
    )
}
