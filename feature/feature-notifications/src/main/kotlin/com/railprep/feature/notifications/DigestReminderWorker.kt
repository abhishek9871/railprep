package com.railprep.feature.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.railprep.domain.repository.DigestRepository
import com.railprep.domain.repository.ProfileRepository
import com.railprep.domain.util.DomainResult
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private const val TAG = "RailPrepNotif"
internal const val CHANNEL_ID = "digest_reminders"
internal const val NOTIFICATION_ID = 0xDA17

/**
 * Fires at 20:00 IST daily (approx; WorkManager's periodic scheduling is coarse by design).
 * Silent no-op when:
 *   - profiles.notifications_enabled = false (user opted out server-side — flag is canonical)
 *   - digest_attempts row already exists for today's IST calendar date (user already studied)
 *   - POST_NOTIFICATIONS permission not granted (Android 13+)
 * Never pulls the app into a background-data hotpath: one RPC + one SELECT max.
 *
 * Hilt wired via [EntryPointAccessors] — no custom WorkerFactory.
 */
class DigestReminderWorker(
    ctx: Context,
    params: WorkerParameters,
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val deps = EntryPointAccessors.fromApplication(applicationContext, Deps::class.java)
        val profileRepo = deps.profileRepository()
        val digestRepo = deps.digestRepository()

        // Canonical opt-in check — server is source of truth, so a user who turned the toggle
        // off on another device (or via Profile screen) won't get nagged here either.
        val profileRes = profileRepo.getCurrentProfile()
        val profile = (profileRes as? DomainResult.Success)?.value
        if (profile == null) {
            Log.w(TAG, "digest-reminder: no profile — will retry next delivery")
            return Result.retry()
        }
        if (!profile.notificationsEnabled) {
            Log.i(TAG, "digest-reminder: opted-out, no-op")
            return Result.success()
        }

        // Already studied today? Silent no-op.
        val today = todayInIst()
        val attemptRes = digestRepo.getMyAttempt(today)
        if (attemptRes is DomainResult.Success && attemptRes.value != null) {
            Log.i(TAG, "digest-reminder: attempt exists for $today, no-op")
            return Result.success()
        }

        // Permission check (Android 13+). If not granted, silent no-op — the permission prompt
        // is fired from the post-submit flow, not here.
        if (!NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()) {
            Log.i(TAG, "digest-reminder: POST_NOTIFICATIONS not granted, no-op")
            return Result.success()
        }

        postReminder(applicationContext, profile.streakCurrent)
        Log.i(TAG, "digest-reminder: posted for $today streak=${profile.streakCurrent}")
        return Result.success()
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface Deps {
        fun profileRepository(): ProfileRepository
        fun digestRepository(): DigestRepository
    }
}

internal fun ensureChannel(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (nm.getNotificationChannel(CHANNEL_ID) != null) return
    val channel = NotificationChannel(
        CHANNEL_ID,
        context.getString(R.string.notif_channel_digest_name),
        NotificationManager.IMPORTANCE_DEFAULT,
    ).apply {
        description = context.getString(R.string.notif_channel_digest_desc)
        setShowBadge(true)
    }
    nm.createNotificationChannel(channel)
}

@Suppress("MissingPermission")  // the worker checks areNotificationsEnabled() before calling
internal fun postReminder(context: Context, streak: Int) {
    ensureChannel(context)
    val title = context.getString(R.string.notif_digest_title)
    val body = if (streak > 0)
        context.getString(R.string.notif_digest_body_streak_fmt, streak)
    else context.getString(R.string.notif_digest_body_no_streak)

    val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
    }
    val pending = launch?.let {
        PendingIntent.getActivity(
            context, 0, it,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_dialog_info)   // swap for branded icon in Phase 5
        .setContentTitle(title)
        .setContentText(body)
        .setAutoCancel(true)
        .setContentIntent(pending)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .build()
    NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
}

private const val IST_ZONE_ID = "Asia/Kolkata"
private fun todayInIst(): LocalDate =
    Clock.System.now().toLocalDateTime(TimeZone.of(IST_ZONE_ID)).date
