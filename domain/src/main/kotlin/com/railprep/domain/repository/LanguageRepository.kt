package com.railprep.domain.repository

import com.railprep.domain.model.SupportedLanguage
import kotlinx.coroutines.flow.Flow

interface LanguageRepository {
    /** Current language code, null if never set. */
    fun observeCurrent(): Flow<SupportedLanguage?>

    /** Synchronous read for Splash routing. Blocks briefly for DataStore first-read. */
    fun currentSync(): SupportedLanguage?

    /**
     * Persists the chosen language and applies it as the app-level locale so subsequent
     * screens pick up the new strings. Triggers activity recreation on the current activity.
     */
    suspend fun setLanguage(language: SupportedLanguage)

    // --- Onboarding seen flag — also lives in LanguageRepository for Phase 1; could split later. ---
    fun hasSeenOnboardingSync(): Boolean
    suspend fun markOnboardingSeen()
}
