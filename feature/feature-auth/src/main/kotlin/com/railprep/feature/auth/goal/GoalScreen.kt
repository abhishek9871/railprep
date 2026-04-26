package com.railprep.feature.auth.goal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.railprep.core.design.RailPrepTheme
import com.railprep.core.design.tokens.Spacing
import com.railprep.core.design.tokens.TouchTarget
import com.railprep.domain.model.Category
import com.railprep.domain.model.ExamTarget
import com.railprep.domain.model.Qualification
import com.railprep.feature.auth.R
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

@Composable
fun GoalScreen(
    onCompleted: () -> Unit,
    viewModel: GoalViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errors["_general"]) {
        state.errors["_general"]?.let { snackbarHostState.showSnackbar(it) }
    }

    GoalContent(
        state = state,
        onDisplayNameChange = viewModel::onDisplayNameChange,
        onExamTargetChange = viewModel::onExamTargetChange,
        onExamTargetDateChange = viewModel::onExamTargetDateChange,
        onDailyMinutesChange = viewModel::onDailyMinutesChange,
        onQualificationChange = viewModel::onQualificationChange,
        onCategoryChange = viewModel::onCategoryChange,
        onDobChange = viewModel::onDobChange,
        onSubmit = { viewModel.onSubmit(onCompleted) },
        snackbarHostState = snackbarHostState,
        targetDateMin = viewModel.targetDateMin,
        targetDateMax = viewModel.targetDateMax,
        dobMaxAllowed = viewModel.dobMaxAllowed,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GoalContent(
    state: GoalUiState,
    onDisplayNameChange: (String) -> Unit,
    onExamTargetChange: (ExamTarget) -> Unit,
    onExamTargetDateChange: (LocalDate) -> Unit,
    onDailyMinutesChange: (Int) -> Unit,
    onQualificationChange: (Qualification) -> Unit,
    onCategoryChange: (Category) -> Unit,
    onDobChange: (LocalDate) -> Unit,
    onSubmit: () -> Unit,
    snackbarHostState: SnackbarHostState,
    targetDateMin: LocalDate,
    targetDateMax: LocalDate,
    dobMaxAllowed: LocalDate,
) {
    var examDatePickerOpen by remember { mutableStateOf(false) }
    var dobPickerOpen by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.Lg, vertical = Spacing.Xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.Md),
        ) {
            Text(
                stringResource(R.string.goal_title),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                stringResource(R.string.goal_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Display name
            OutlinedTextField(
                value = state.displayName,
                onValueChange = onDisplayNameChange,
                label = { Text(stringResource(R.string.goal_display_name)) },
                isError = state.errors.containsKey("displayName"),
                supportingText = state.errors["displayName"]?.let {
                    { Text(stringResource(R.string.goal_err_display_name)) }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            // Exam target
            FormSectionLabel(stringResource(R.string.goal_exam_target))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.Xs)) {
                ExamTarget.entries.forEach { target ->
                    FilterChip(
                        selected = state.examTarget == target,
                        onClick = { onExamTargetChange(target) },
                        label = {
                            Text(
                                when (target) {
                                    ExamTarget.NtpcCbt1 -> stringResource(R.string.goal_exam_cbt1)
                                    ExamTarget.NtpcCbt2 -> stringResource(R.string.goal_exam_cbt2)
                                },
                            )
                        },
                    )
                }
            }
            state.errors["examTarget"]?.let { ErrorText(stringResource(R.string.goal_err_required)) }

            // Exam target date
            FormSectionLabel(stringResource(R.string.goal_target_date))
            OutlinedTextField(
                value = state.examTargetDate?.toString() ?: "",
                onValueChange = {},
                readOnly = true,
                placeholder = { Text(stringResource(R.string.goal_pick_date)) },
                modifier = Modifier.fillMaxWidth().heightIn(min = TouchTarget.Min),
                trailingIcon = {
                    TextButton(onClick = { examDatePickerOpen = true }) {
                        Text(stringResource(R.string.goal_pick))
                    }
                },
                isError = state.errors.containsKey("examTargetDate"),
                supportingText = {
                    Text(
                        if (state.errors.containsKey("examTargetDate")) {
                            examDateErrorText(state.errors["examTargetDate"])
                        } else {
                            stringResource(R.string.goal_target_date_helper)
                        },
                    )
                },
            )

            // Daily minutes
            FormSectionLabel(stringResource(R.string.goal_daily_minutes, state.dailyMinutes))
            Slider(
                value = state.dailyMinutes.toFloat(),
                onValueChange = { onDailyMinutesChange(it.toInt()) },
                valueRange = 30f..240f,
                steps = ((240 - 30) / 15) - 1,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                stringResource(R.string.goal_daily_minutes_helper),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Qualification
            FormSectionLabel(stringResource(R.string.goal_qualification))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.Xs)) {
                Qualification.entries.forEach { q ->
                    FilterChip(
                        selected = state.qualification == q,
                        onClick = { onQualificationChange(q) },
                        label = {
                            Text(
                                when (q) {
                                    Qualification.Twelfth -> stringResource(R.string.goal_qual_12th)
                                    Qualification.Graduate -> stringResource(R.string.goal_qual_graduate)
                                },
                            )
                        },
                    )
                }
            }
            state.errors["qualification"]?.let { ErrorText(stringResource(R.string.goal_err_required)) }

            // Category
            FormSectionLabel(stringResource(R.string.goal_category))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.Xxs)) {
                Category.entries.forEach { c ->
                    FilterChip(
                        selected = state.category == c,
                        onClick = { onCategoryChange(c) },
                        label = { Text(c.wire) },
                    )
                }
            }
            state.errors["category"]?.let { ErrorText(stringResource(R.string.goal_err_required)) }

            // DOB
            FormSectionLabel(stringResource(R.string.goal_dob))
            OutlinedTextField(
                value = state.dob?.toString() ?: "",
                onValueChange = {},
                readOnly = true,
                placeholder = { Text(stringResource(R.string.goal_pick_date)) },
                modifier = Modifier.fillMaxWidth().heightIn(min = TouchTarget.Min),
                trailingIcon = {
                    TextButton(onClick = { dobPickerOpen = true }) {
                        Text(stringResource(R.string.goal_pick))
                    }
                },
                isError = state.errors.containsKey("dob"),
                supportingText = {
                    Text(
                        if (state.errors.containsKey("dob")) {
                            dobErrorText(state.errors["dob"])
                        } else {
                            stringResource(R.string.goal_dob_helper)
                        },
                    )
                },
            )

            Spacer(Modifier.size(Spacing.Md))

            Button(
                onClick = onSubmit,
                enabled = !state.loading,
                modifier = Modifier.fillMaxWidth().heightIn(min = TouchTarget.Min),
            ) {
                if (state.loading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.size(Spacing.Xs))
                }
                Text(stringResource(R.string.goal_finish))
            }
        }
    }

    if (examDatePickerOpen) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.examTargetDate?.toUtcMillis(),
            selectableDates = selectableDateRange(targetDateMin, targetDateMax),
        )
        DatePickerDialog(
            onDismissRequest = { examDatePickerOpen = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        onExamTargetDateChange(millis.toPickerDate())
                    }
                    examDatePickerOpen = false
                }) { Text(stringResource(R.string.goal_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { examDatePickerOpen = false }) {
                    Text(stringResource(R.string.goal_cancel))
                }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }

    if (dobPickerOpen) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.dob?.toUtcMillis(),
            selectableDates = selectableDateRange(null, dobMaxAllowed),
        )
        DatePickerDialog(
            onDismissRequest = { dobPickerOpen = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        onDobChange(millis.toPickerDate())
                    }
                    dobPickerOpen = false
                }) { Text(stringResource(R.string.goal_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { dobPickerOpen = false }) {
                    Text(stringResource(R.string.goal_cancel))
                }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun FormSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = Spacing.Xs),
    )
}

@Composable
private fun ErrorText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error,
    )
}

@Composable
private fun examDateErrorText(code: String?): String = when (code) {
    "REQUIRED" -> stringResource(R.string.goal_err_required)
    "OUT_OF_RANGE" -> stringResource(R.string.goal_err_date_range)
    else -> stringResource(R.string.goal_err_date_range)
}

@Composable
private fun dobErrorText(code: String?): String = when (code) {
    "UNDER_18" -> stringResource(R.string.goal_err_under_18)
    "REQUIRED" -> stringResource(R.string.goal_err_required)
    else -> stringResource(R.string.goal_err_required)
}

@OptIn(ExperimentalMaterial3Api::class)
private fun selectableDateRange(
    min: LocalDate?,
    max: LocalDate?,
): SelectableDates = object : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
        val date = utcTimeMillis.toPickerDate()
        return (min == null || date >= min) && (max == null || date <= max)
    }
}

private fun LocalDate.toUtcMillis(): Long =
    atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

private fun Long.toPickerDate(): LocalDate =
    Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.UTC).date

@Preview(widthDp = 360, heightDp = 780)
@Composable
private fun GoalPreview() {
    RailPrepTheme {
        GoalContent(
            state = GoalUiState(),
            onDisplayNameChange = {},
            onExamTargetChange = {},
            onExamTargetDateChange = {},
            onDailyMinutesChange = {},
            onQualificationChange = {},
            onCategoryChange = {},
            onDobChange = {},
            onSubmit = {},
            snackbarHostState = remember { SnackbarHostState() },
            targetDateMin = LocalDate(2026, 5, 26),
            targetDateMax = LocalDate(2028, 4, 26),
            dobMaxAllowed = LocalDate(2008, 4, 26),
        )
    }
}
