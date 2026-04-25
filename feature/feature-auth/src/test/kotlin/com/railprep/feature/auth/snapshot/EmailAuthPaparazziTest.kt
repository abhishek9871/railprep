package com.railprep.feature.auth.snapshot

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import com.railprep.feature.auth.snapshot.PaparazziTheme
import com.railprep.feature.auth.email.EmailAuthContent
import com.railprep.feature.auth.email.EmailAuthUiState
import org.junit.Rule
import org.junit.Test

class EmailAuthPaparazziTestEn {
    @get:Rule val paparazzi = paparazzi("en")
    @Test fun email_en() {
        paparazzi.snapshot {
            PaparazziTheme {
                EmailAuthContent(
                    state = EmailAuthUiState(),
                    onEmailChange = {},
                    onPasswordChange = {},
                    onModeChange = {},
                    onSubmit = {},
                    onReset = {},
                    onBack = {},
                    snackbarHostState = remember { SnackbarHostState() },
                )
            }
        }
    }
}

class EmailAuthPaparazziTestHi {
    @get:Rule val paparazzi = paparazzi("hi")
    @Test fun email_hi() {
        paparazzi.snapshot {
            PaparazziTheme {
                EmailAuthContent(
                    state = EmailAuthUiState(),
                    onEmailChange = {},
                    onPasswordChange = {},
                    onModeChange = {},
                    onSubmit = {},
                    onReset = {},
                    onBack = {},
                    snackbarHostState = remember { SnackbarHostState() },
                )
            }
        }
    }
}
