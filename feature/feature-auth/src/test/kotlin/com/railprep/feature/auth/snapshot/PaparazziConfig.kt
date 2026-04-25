package com.railprep.feature.auth.snapshot

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi

/** 360×780 dp canvas in mdpi — matches the prototype/spec screen size. */
internal fun paparazzi(locale: String): Paparazzi = Paparazzi(
    deviceConfig = DeviceConfig.PIXEL_5.copy(
        screenWidth = 360,
        screenHeight = 780,
        xdpi = 160,
        ydpi = 160,
        locale = locale,
    ),
    renderingMode = com.android.ide.common.rendering.api.SessionParams.RenderingMode.NORMAL,
)
