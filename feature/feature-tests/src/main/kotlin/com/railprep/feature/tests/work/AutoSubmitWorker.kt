package com.railprep.feature.tests.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.railprep.domain.model.AttemptStatus
import com.railprep.domain.repository.AttemptRepository
import com.railprep.domain.util.DomainResult
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

private const val TAG = "RailPrepTests"

/**
 * Process-death auto-submit safety net. Scheduled by [AutoSubmitScheduler]
 * at test Start with `initialDelay = total_minutes + 30s grace`. If the app
 * is alive, the in-VM ticker in TestPlayerViewModel will hit the deadline
 * first and submit — the RPC rejects late submits for IN_PROGRESS attempts
 * that are already submitted, so this worker is idempotent.
 *
 * Hilt DI is wired via [EntryPointAccessors] so we don't have to register a
 * custom [WorkerFactory] in the app module.
 */
class AutoSubmitWorker(
    ctx: Context,
    params: WorkerParameters,
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val attemptId = inputData.getString(KEY_ATTEMPT_ID)
        if (attemptId.isNullOrEmpty()) {
            Log.w(TAG, "auto-submit-worker: missing attempt_id input; giving up")
            return Result.failure()
        }

        val repo: AttemptRepository = EntryPointAccessors
            .fromApplication(applicationContext, Deps::class.java)
            .attemptRepository()

        // Status check — if the user already submitted from the UI, or a
        // prior worker already submitted, the attempt won't be IN_PROGRESS
        // and we should just exit cleanly.
        val currentRes = repo.get(attemptId)
        if (currentRes !is DomainResult.Success) {
            Log.w(TAG, "auto-submit-worker: get(attempt) failed; will retry on next delivery")
            return Result.retry()
        }
        val status = currentRes.value.status
        if (status != AttemptStatus.IN_PROGRESS) {
            Log.i(
                TAG,
                "auto-submit-worker: aid=${attemptId.takeLast(8)} status=$status — already terminal, no-op",
            )
            return Result.success()
        }

        return when (val r = repo.submit(attemptId)) {
            is DomainResult.Success -> {
                Log.i(
                    TAG,
                    "auto-submit-worker: aid=${attemptId.takeLast(8)} submit-success " +
                        "status=${r.value.status} score=${r.value.score}",
                )
                Result.success()
            }
            is DomainResult.Failure -> {
                Log.w(
                    TAG,
                    "auto-submit-worker: aid=${attemptId.takeLast(8)} submit-failed " +
                        "(${r.error.message}) — will retry",
                )
                Result.retry()
            }
        }
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface Deps {
        fun attemptRepository(): AttemptRepository
    }

    companion object {
        const val KEY_ATTEMPT_ID = "attempt_id"
        fun tagFor(attemptId: String): String = "auto-submit:$attemptId"
        fun uniqueNameFor(attemptId: String): String = "auto-submit:$attemptId"
    }
}
