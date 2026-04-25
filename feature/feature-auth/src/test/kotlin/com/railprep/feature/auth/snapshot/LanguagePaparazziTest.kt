package com.railprep.feature.auth.snapshot

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import com.railprep.feature.auth.snapshot.PaparazziTheme
import com.railprep.domain.model.SupportedLanguage
import com.railprep.feature.auth.language.LanguageContent
import org.junit.Rule
import org.junit.Test

class LanguagePaparazziTestEn {
    @get:Rule val paparazzi = paparazzi("en")
    @Test fun language_en() {
        paparazzi.snapshot {
            PaparazziTheme {
                LanguageContent(
                    selected = SupportedLanguage.En,
                    onLanguageClick = {},
                    onContinueClick = {},
                    snackbarHostState = remember { SnackbarHostState() },
                )
            }
        }
    }
}

class LanguagePaparazziTestHi {
    @get:Rule val paparazzi = paparazzi("hi")
    @Test fun language_hi() {
        paparazzi.snapshot {
            PaparazziTheme {
                LanguageContent(
                    selected = SupportedLanguage.Hi,
                    onLanguageClick = {},
                    onContinueClick = {},
                    snackbarHostState = remember { SnackbarHostState() },
                )
            }
        }
    }
}
