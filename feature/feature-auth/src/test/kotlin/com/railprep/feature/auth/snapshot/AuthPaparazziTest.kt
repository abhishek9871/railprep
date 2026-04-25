package com.railprep.feature.auth.snapshot

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import com.railprep.feature.auth.snapshot.PaparazziTheme
import com.railprep.feature.auth.auth.AuthContent
import org.junit.Rule
import org.junit.Test

class AuthPaparazziTestEn {
    @get:Rule val paparazzi = paparazzi("en")
    @Test fun auth_en() {
        paparazzi.snapshot {
            PaparazziTheme {
                AuthContent(
                    googleLoading = false,
                    onGoogleClick = {},
                    onEmailClick = {},
                    snackbarHostState = remember { SnackbarHostState() },
                )
            }
        }
    }
}

class AuthPaparazziTestHi {
    @get:Rule val paparazzi = paparazzi("hi")
    @Test fun auth_hi() {
        paparazzi.snapshot {
            PaparazziTheme {
                AuthContent(
                    googleLoading = false,
                    onGoogleClick = {},
                    onEmailClick = {},
                    snackbarHostState = remember { SnackbarHostState() },
                )
            }
        }
    }
}
