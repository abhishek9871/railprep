package com.railprep.feature.auth.snapshot

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import com.railprep.feature.auth.snapshot.PaparazziTheme
import com.railprep.feature.auth.goal.GoalContent
import com.railprep.feature.auth.goal.GoalUiState
import org.junit.Rule
import org.junit.Test

class GoalPaparazziTestEn {
    @get:Rule val paparazzi = paparazzi("en")
    @Test fun goal_en() {
        paparazzi.snapshot {
            PaparazziTheme {
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
                )
            }
        }
    }
}

class GoalPaparazziTestHi {
    @get:Rule val paparazzi = paparazzi("hi")
    @Test fun goal_hi() {
        paparazzi.snapshot {
            PaparazziTheme {
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
                )
            }
        }
    }
}
