package com.railprep

import android.app.Application
import android.app.ApplicationExitInfo
import android.os.Build
import com.railprep.core.i18n.LanguageManager
import com.railprep.domain.repository.ErrorLogRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.datetime.Clock
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.PrintWriter
import java.io.StringWriter
import javax.inject.Inject

@HiltAndroidApp
class RailPrepApplication : Application() {

    @Inject lateinit var languageManager: LanguageManager
    @Inject lateinit var errorLogRepository: ErrorLogRepository

    override fun onCreate() {
        super.onCreate()
        // Apply the stored per-app locale before the first activity is displayed so initial
        // strings already reflect the user's choice. No-op if the user hasn't picked yet.
        languageManager.applyStoredLocaleOnStart()
        installErrorLogging()
        recordPreviousAnrIfAny()
    }

    private fun installErrorLogging() {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runBlocking(Dispatchers.IO) {
                withTimeoutOrNull(1_500L) {
                    errorLogRepository.logClientError(
                        appVersion = BuildConfig.VERSION_NAME,
                        kotlinClass = throwable::class.java.name,
                        message = scrub(throwable.message ?: "Uncaught exception"),
                        stacktrace = scrub(throwable.stackTraceString()),
                        breadcrumbs = buildJsonObject {
                            put("thread", scrub(thread.name))
                            put("fatal", true)
                        },
                        occurredAt = Clock.System.now(),
                    )
                }
            }
            previous?.uncaughtException(thread, throwable)
        }
    }

    private fun recordPreviousAnrIfAny() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return
        val activityManager = getSystemService(android.app.ActivityManager::class.java)
        val exit = activityManager.getHistoricalProcessExitReasons(packageName, 0, 5)
            .firstOrNull { it.reason == ApplicationExitInfo.REASON_ANR }
            ?: return
        runBlocking(Dispatchers.IO) {
            withTimeoutOrNull(1_500L) {
                errorLogRepository.logClientError(
                    appVersion = BuildConfig.VERSION_NAME,
                    kotlinClass = "ApplicationExitInfo",
                    message = scrub(exit.description ?: "Previous process exited after ANR"),
                    stacktrace = null,
                    breadcrumbs = buildJsonObject {
                        put("reason", "ANR")
                        put("importance", exit.importance)
                        put("timestamp", exit.timestamp)
                    },
                    occurredAt = Clock.System.now(),
                )
            }
        }
    }

    private fun Throwable.stackTraceString(): String {
        val sw = StringWriter()
        PrintWriter(sw).use { printStackTrace(it) }
        return sw.toString()
    }

    private fun scrub(raw: String): String = raw
        .replace(Regex("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}", RegexOption.IGNORE_CASE), "[email]")
        .replace(Regex("\\b(?:\\+?91[-\\s]?)?[6-9]\\d{9}\\b"), "[phone]")
        .replace(Regex("(?i)(note|notes)\\s*[:=]\\s*[^,;\\n]+"), "$1=[redacted]")
        .take(12_000)
}
