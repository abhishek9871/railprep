package com.railprep.feature.auth.snapshot

import com.railprep.feature.auth.snapshot.PaparazziTheme
import com.railprep.feature.auth.splash.SplashContent
import org.junit.Rule
import org.junit.Test

class SplashPaparazziTestEn {
    @get:Rule val paparazzi = paparazzi("en")
    @Test fun splash_en() {
        paparazzi.snapshot { PaparazziTheme { SplashContent() } }
    }
}

class SplashPaparazziTestHi {
    @get:Rule val paparazzi = paparazzi("hi")
    @Test fun splash_hi() {
        paparazzi.snapshot { PaparazziTheme { SplashContent() } }
    }
}
