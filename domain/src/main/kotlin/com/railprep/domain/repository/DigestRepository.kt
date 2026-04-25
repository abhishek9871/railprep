package com.railprep.domain.repository

import com.railprep.domain.model.Digest
import com.railprep.domain.model.DigestAttempt
import com.railprep.domain.model.Profile
import com.railprep.domain.util.DomainResult
import kotlinx.datetime.LocalDate

/**
 * Daily Digest repo (Phase 4 D1). The server picks the 10 questions; the client
 * fetches them, lets the user answer, then submits. Streak state is canonical
 * on `profiles` and is advanced server-side by `submit_digest()`.
 */
interface DigestRepository {
    /** Idempotent — calls `ensure_today_digest(date)` then resolves question_ids. */
    suspend fun loadForDate(date: LocalDate): DomainResult<Digest>

    /** Server-scored submission. Returns the updated profile (carries streak fields). */
    suspend fun submit(
        date: LocalDate,
        answers: List<Pair<String, String?>>,
    ): DomainResult<Profile>

    /** Returns the user's submitted attempt for [date], or null if not yet submitted. */
    suspend fun getMyAttempt(date: LocalDate): DomainResult<DigestAttempt?>
}
