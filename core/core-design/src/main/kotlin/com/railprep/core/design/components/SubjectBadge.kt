package com.railprep.core.design.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// TODO Phase 1: pill with subject icon + name in the subject's bg/fg pair from SubjectColors.
enum class Subject { Math, Reason, Ga, Gs, Ca, Eng }

@Composable
fun SubjectBadge(
    subject: Subject,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier)
}
