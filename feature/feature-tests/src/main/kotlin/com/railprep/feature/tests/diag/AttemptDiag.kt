package com.railprep.feature.tests.diag

/**
 * Process-level diagnostic snapshot for the active attempt. Mirrors the `PdfDiag` pattern
 * established in Phase 2.5. Written by TestPlayerViewModel on every sync / tick; read by
 * the long-press-timer overlay.
 */
object AttemptDiag {
    data class Snapshot(
        val attemptId: String?,
        val deadlineEpochMs: Long?,
        val localNowMs: Long,
        val lastServerSyncMs: Long,
        val pendingLocalWrites: Int,
    )

    @Volatile var last: Snapshot? = null
        private set

    fun record(snapshot: Snapshot) {
        last = snapshot
    }
}
