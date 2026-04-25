package com.railprep.feature.daily.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.railprep.core.design.tokens.Radius
import com.railprep.core.design.tokens.Spacing
import com.railprep.domain.model.DigestAnswer
import com.railprep.domain.model.Option
import com.railprep.domain.model.Question
import com.railprep.feature.daily.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DigestReviewScreen(
    onBack: () -> Unit,
    viewModel: DigestReviewViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.daily_review_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleBilingual() }) {
                        Text(
                            text = if (state.showHi) "EN" else "हि",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        when {
            state.loading -> Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }
            state.digest == null -> Box(
                Modifier.fillMaxSize().padding(innerPadding).padding(Spacing.Lg),
                contentAlignment = Alignment.Center,
            ) { Text(stringResource(R.string.daily_review_load_error)) }
            else -> {
                val digest = state.digest!!
                val answersByQ = state.attempt?.answers.orEmpty().associateBy { it.questionId }
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentPadding = PaddingValues(
                        start = Spacing.Lg, end = Spacing.Lg,
                        top = Spacing.Sm, bottom = Spacing.Xl,
                    ),
                    verticalArrangement = Arrangement.spacedBy(Spacing.Md),
                ) {
                    items(digest.questions, key = { it.id }) { q ->
                        ReviewCard(
                            question = q,
                            answer = answersByQ[q.id],
                            showHi = state.showHi,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewCard(question: Question, answer: DigestAnswer?, showHi: Boolean) {
    val correct = question.options.firstOrNull { it.isCorrect }
    val selected = answer?.selectedOptionId?.let { id -> question.options.firstOrNull { it.id == id } }

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(Radius.Md),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(Spacing.Md),
            verticalArrangement = Arrangement.spacedBy(Spacing.Xs),
        ) {
            Text(
                text = if (showHi && !question.stemHi.isNullOrBlank()) question.stemHi!! else question.stemEn,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.size(Spacing.Xs))
            question.options.forEach { opt ->
                OptionRow(
                    option = opt,
                    isCorrect = opt.id == correct?.id,
                    isUserSelected = opt.id == selected?.id,
                    showHi = showHi,
                )
                val trap = if (showHi && !opt.trapReasonHi.isNullOrBlank()) opt.trapReasonHi
                           else opt.trapReasonEn
                if (!opt.isCorrect && !trap.isNullOrBlank()) {
                    TrapReason(trap)
                }
            }
            if (answer == null || answer.selectedOptionId == null) {
                Text(
                    stringResource(R.string.daily_review_not_answered),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            ExplanationSection(question = question, showHi = showHi)
        }
    }
}

@Composable
private fun OptionRow(
    option: Option,
    isCorrect: Boolean,
    isUserSelected: Boolean,
    showHi: Boolean,
) {
    val bg = when {
        isCorrect -> Color(0xFFD7EED7)
        isUserSelected && !isCorrect -> Color(0xFFF8D7D7)
        else -> Color.Transparent
    }
    val label = buildString {
        append(option.label).append(".  ")
        append(if (showHi && !option.textHi.isNullOrBlank()) option.textHi else option.textEn)
        if (isCorrect) append("  ✓") else if (isUserSelected) append("  ✗")
    }
    Surface(
        color = bg,
        shape = RoundedCornerShape(Radius.Sm),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = Spacing.Sm, vertical = Spacing.Xs),
        )
    }
}

@Composable
private fun TrapReason(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
        shape = RoundedCornerShape(Radius.Sm),
        modifier = Modifier.fillMaxWidth().padding(start = Spacing.Md),
    ) {
        Column(modifier = Modifier.padding(Spacing.Sm)) {
            Text(
                stringResource(R.string.daily_review_trap_label),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
    }
}

@Composable
private fun ExplanationSection(question: Question, showHi: Boolean) {
    val method = if (showHi && !question.explanationMethodHi.isNullOrBlank()) question.explanationMethodHi
                 else question.explanationMethodEn
    val concept = if (showHi && !question.explanationConceptHi.isNullOrBlank()) question.explanationConceptHi
                  else question.explanationConceptEn
    val legacy = if (showHi && !question.explanationHi.isNullOrBlank()) question.explanationHi
                 else question.explanationEn
    val hasLayered = !method.isNullOrBlank() || !concept.isNullOrBlank()

    if (hasLayered) {
        Spacer(Modifier.size(Spacing.Sm))
        if (!method.isNullOrBlank()) {
            Text(
                stringResource(R.string.daily_review_explanation_method),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary,
            )
            Text(text = method, style = MaterialTheme.typography.bodyMedium)
        }
        if (!concept.isNullOrBlank()) {
            Spacer(Modifier.size(Spacing.Xs))
            Text(
                stringResource(R.string.daily_review_explanation_concept),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.tertiary,
            )
            Text(text = concept, style = MaterialTheme.typography.bodyMedium)
        }
    } else if (!legacy.isNullOrBlank()) {
        Spacer(Modifier.size(Spacing.Sm))
        Text(
            stringResource(R.string.daily_review_explanation_legacy),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
        )
        Text(text = legacy, style = MaterialTheme.typography.bodyMedium)
    }
}
