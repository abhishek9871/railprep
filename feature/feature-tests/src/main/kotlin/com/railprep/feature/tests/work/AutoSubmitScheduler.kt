package com.railprep.feature.tests.work

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "RailPrepTests"
/** 30-second grace added to the scheduled delay — covers clock skew and
 *  lets the in-VM ticker win the race when the app is alive. */
private const val GRACE_MS = 30_000L

/**
 * Schedules / cancels the process-death auto-submit safety net.
 *
 * Contract:
 *  - [schedule] — called from InstructionsViewModel.start() after the server
 *    creates/returns an IN_PROGRESS attempt. Replaces any existing work for
 *    this attemptId (idempotent across repeated calls).
 *  - [ensureScheduled] — called from TestPlayerViewModel.load() as
 *    belt-and-braces: if the user cold-started the app with an IN_PROGRESS
 *    attempt outstanding, re-enqueue the worker if it's missing. Keeps
 *    existing work if still present.
 *  - [cancel] — called on normal submit success. Best-effort — a late
 *    worker firing is harmless because submit_attempt rejects non-IN_PROGRESS.
 */
@Singleton
class AutoSubmitScheduler @Inject constructor(
    @ApplicationContext private val appContext: Context,
) {

    fun schedule(attemptId: String, deadlineEpochMs: Long) {
        enqueue(attemptId, deadlineEpochMs, ExistingWorkPolicy.REPLACE)
    }

    fun ensureScheduled(attemptId: String, deadlineEpochMs: Long) {
        // KEEP: if an existing worker is already scheduled for this attempt
        // (common — we scheduled it at start), don't disturb it. Otherwise
        // enqueue a fresh one.
        enqueue(attemptId, deadlineEpochMs, ExistingWorkPolicy.KEEP)
    }

    fun cancel(attemptId: String) {
        try {
            WorkManager.getInstance(appContext)
                .cancelUniqueWork(AutoSubmitWorker.uniqueNameFor(attemptId))
            Log.i(TAG, "auto-submit-cancel aid=${attemptId.takeLast(8)}")
        } catch (t: Throwable) {
            Log.w(TAG, "auto-submit-cancel FAILED for aid=${attemptId.takeLast(8)}: ${t.message}")
        }
    }

    private fun enqueue(attemptId: String, deadlineEpochMs: Long, policy: ExistingWorkPolicy) {
        // NOTE: `setExpedited(...)` is incompatible with `setInitialDelay(...)` —
        // WorkManager throws IllegalArgumentException("Expedited jobs cannot be
        // delayed") at build time. We keep the delay (we need to fire at
        // deadline, not now) and drop expedited. Running at standard priority
        // is fine for the safety net: even a few minutes of OS delay past the
        // deadline is harmless because submit_attempt() accepts late submits
        // (it scores whatever's in attempt_answers). The in-VM ticker remains
        // the primary auto-submit path while the UI is alive.
        try {
            val now = System.currentTimeMillis()
            val delayMs = (deadlineEpochMs + GRACE_MS - now).coerceAtLeast(0L)

            val input = Data.Builder().putString(AutoSubmitWorker.KEY_ATTEMPT_ID, attemptId).build()

            val request = OneTimeWorkRequestBuilder<AutoSubmitWorker>()
                .setInputData(input)
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .addTag(AutoSubmitWorker.tagFor(attemptId))
                .setConstraints(
                    Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build(),
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(appContext)
                .enqueueUniqueWork(AutoSubmitWorker.uniqueNameFor(attemptId), policy, request)

            Log.i(
                TAG,
                "auto-submit-scheduled aid=${attemptId.takeLast(8)} delay=${delayMs / 1000}s policy=$policy",
            )
        } catch (t: Throwable) {
            // Never let a safety-net scheduling failure crash the user's test start.
            // The in-VM ticker still handles auto-submit while the UI is alive; the
            // only regression is that a process-death mid-test may leave an
            // IN_PROGRESS attempt that start_attempt() will EXPIRE on next call.
            Log.e(TAG, "auto-submit-schedule FAILED for aid=${attemptId.takeLast(8)}: ${t.message}")
        }
    }

    /** True if there is currently a pending or running worker for this attempt. */
    suspend fun hasPendingWork(attemptId: String): Boolean = runCatching {
        val infos = WorkManager.getInstance(appContext)
            .getWorkInfosByTag(AutoSubmitWorker.tagFor(attemptId))
            .get()
        infos.any { it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.BLOCKED }
    }.getOrDefault(false)
}
