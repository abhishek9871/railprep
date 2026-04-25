package com.railprep.feature.auth.snapshot

import com.railprep.feature.auth.snapshot.PaparazziTheme
import com.railprep.feature.auth.onboarding.OnboardingContent
import org.junit.Rule
import org.junit.Test

class OnboardingPaparazziTestEn {
    @get:Rule val paparazzi = paparazzi("en")
    @Test fun onboarding_en() {
        paparazzi.snapshot { PaparazziTheme { OnboardingContent(onDone = {}) } }
    }
}

class OnboardingPaparazziTestHi {
    @get:Rule val paparazzi = paparazzi("hi")
    @Test fun onboarding_hi() {
        paparazzi.snapshot { PaparazziTheme { OnboardingContent(onDone = {}) } }
    }
}
