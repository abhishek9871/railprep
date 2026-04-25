package com.railprep

import android.app.Application
import com.railprep.core.i18n.LanguageManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class RailPrepApplication : Application() {

    @Inject lateinit var languageManager: LanguageManager

    override fun onCreate() {
        super.onCreate()
        // Apply the stored per-app locale before the first activity is displayed so initial
        // strings already reflect the user's choice. No-op if the user hasn't picked yet.
        languageManager.applyStoredLocaleOnStart()
    }
}
