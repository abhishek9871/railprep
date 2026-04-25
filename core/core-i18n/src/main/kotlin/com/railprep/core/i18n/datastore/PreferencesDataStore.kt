package com.railprep.core.i18n.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

// Single DataStore shared across i18n + onboarding flags (simple enough for Phase 1).
internal val Context.railprepPreferences: DataStore<Preferences> by preferencesDataStore(
    name = "railprep_preferences",
)
