package com.railprep.core.i18n

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.railprep.core.i18n.datastore.railprepPreferences
import com.railprep.domain.model.SupportedLanguage
import com.railprep.domain.repository.LanguageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

private object PrefKeys {
    val LANGUAGE = stringPreferencesKey("language_code")
    val ONBOARDING_SEEN = booleanPreferencesKey("onboarding_seen")
}

@Singleton
class LanguageManager @Inject constructor(
    @ApplicationContext private val appContext: Context,
) : LanguageRepository {

    override fun observeCurrent(): Flow<SupportedLanguage?> =
        appContext.railprepPreferences.data.map { prefs ->
            SupportedLanguage.fromCode(prefs[PrefKeys.LANGUAGE])
        }

    override fun currentSync(): SupportedLanguage? = runBlocking {
        val code = appContext.railprepPreferences.data.first()[PrefKeys.LANGUAGE]
        SupportedLanguage.fromCode(code)
    }

    override suspend fun setLanguage(language: SupportedLanguage) = withContext(Dispatchers.IO) {
        appContext.railprepPreferences.edit { prefs ->
            prefs[PrefKeys.LANGUAGE] = language.code
        }
        applyLocale(language)
        Unit
    }

    override fun hasSeenOnboardingSync(): Boolean = runBlocking {
        appContext.railprepPreferences.data.first()[PrefKeys.ONBOARDING_SEEN] == true
    }

    override suspend fun markOnboardingSeen() = withContext(Dispatchers.IO) {
        appContext.railprepPreferences.edit { prefs ->
            prefs[PrefKeys.ONBOARDING_SEEN] = true
        }
        Unit
    }

    /**
     * Reapplies the stored locale on application start — call once from Application.onCreate
     * before the first activity is shown. After that, [setLanguage] keeps it in sync.
     */
    fun applyStoredLocaleOnStart() {
        val current = currentSync() ?: return
        applyLocale(current)
    }

    private fun applyLocale(language: SupportedLanguage) {
        // Android 13+ exposes first-class per-app languages through LocaleManager.
        // RailPrep's Activity is pure ComponentActivity/Compose, so using the
        // framework API avoids relying on AppCompatActivity recreation semantics.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            appContext.getSystemService(LocaleManager::class.java)
                .applicationLocales = LocaleList.forLanguageTags(language.code)
        } else {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.code))
        }
    }
}
