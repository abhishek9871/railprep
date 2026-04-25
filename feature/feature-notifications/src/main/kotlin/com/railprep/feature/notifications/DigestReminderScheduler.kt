package com.railprep.feature.notifications

import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.days
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

private const val TAG = "RailPrepNotif"
private const val IST_ZONE_ID = "Asia/Kolkata"
private const val UNIQUE_NAME = "digest-reminder"
private const val FIRE_HOUR_IST = 20   // 8:00 PM IST

/**
 * AlarmManager+FCM were evaluated and rejected — see docs/PHASE_4_D2_NOTIFICATIONS.md.
 * We use a WorkManager [PeriodicWorkRequest] (24-hour interval) because:
 *   - survives device reboot (WM's JobScheduler persists)
 *   - respects Doze / battery optimization gracefully
 *   - no server infrastructure (no pg_cron/pg_net on Supabase, no FCM project)
 *   - the digest reminder is Best-effort by design — a few minutes of OS delay is fine.
 *
 * Known OEM risk: Oppo / Xiaomi occasionally drop WM jobs under aggressive battery settings.
 * Mitigation: Profile screen toggle lets the user turn it off; the in-VM HomeCard already
 * surfaces the streak so the reminder isn't the only channel.
 */
@Singleton
class DigestReminderScheduler @Inject constructor(
    @ApplicationContext private val appContext: Context,
) {

    fun enable() {
        val delayMs = computeInitialDelayMs(Clock.System.now(), FIRE_HOUR_IST)
        val request = PeriodicWorkRequestBuilder<DigestReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .addTag("digest-reminder")
            .build()
        WorkManager.getInstance(appContext)
            .enqueueUniquePeriodicWork(UNIQUE_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        Log.i(TAG, "digest-reminder-scheduler: enabled, first fire in ~${delayMs / 60_000}min")
    }

    fun disable() {
        WorkManager.getInstance(appContext).cancelUniqueWork(UNIQUE_NAME)
        Log.i(TAG, "digest-reminder-scheduler: disabled")
    }

    companion object {
        /**
         * Compute delay to the next 20:00 IST instant from [now]. If already past 20:00 today,
         * fire tomorrow.
         * Exposed internal for unit-testing the corner cases (UTC-midnight, DST-none-in-IST etc.).
         */
        internal fun computeInitialDelayMs(
            now: kotlinx.datetime.Instant,
            fireHourIst: Int,
        ): Long {
            val zone = TimeZone.of(IST_ZONE_ID)
            val istNow = now.toLocalDateTime(zone)
            val fireToday = LocalDateTime(
                year = istNow.year, monthNumber = istNow.monthNumber, dayOfMonth = istNow.dayOfMonth,
                hour = fireHourIst, minute = 0, second = 0, nanosecond = 0,
            )
            val fireInstantToday = fireToday.toInstant(zone)
            val target = if (fireInstantToday > now) fireInstantToday
                         else fireInstantToday.plus(1.days)
            return target.toEpochMilliseconds() - now.toEpochMilliseconds()
        }
    }
}
