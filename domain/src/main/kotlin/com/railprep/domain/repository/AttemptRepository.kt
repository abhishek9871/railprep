package com.railprep.domain.repository

import com.railprep.domain.model.Attempt
import com.railprep.domain.model.AttemptAnswer
import com.railprep.domain.util.DomainResult

interface AttemptRepository {
    /** Calls the server `start_attempt` RPC. Idempotent: returns the current IN_PROGRESS
     *  attempt for this (user, test) if one exists and isn't past its deadline. */
    suspend fun start(testId: String): DomainResult<Attempt>

    /** Inserts or updates a single answer row. selectedOptionId=null = explicit skip. */
    suspend fun upsertAnswer(
        attemptId: String,
        questionId: String,
        selectedOptionId: String?,
        flagged: Boolean,
    ): DomainResult<Unit>

    /** Convenience — toggles only the flagged bit. */
    suspend fun flag(attemptId: String, questionId: String, flagged: Boolean): DomainResult<Unit>

    /** Calls the server `submit_attempt` RPC. Scoring is computed server-side. */
    suspend fun submit(attemptId: String): DomainResult<Attempt>

    /** Snapshot of every answer row for the attempt (used to hydrate the player on cold start). */
    suspend fun listAnswers(attemptId: String): DomainResult<List<AttemptAnswer>>

    /** All of this user's attempts, most recent first. RLS filters to the current user. */
    suspend fun listMine(): DomainResult<List<Attempt>>

    /** The single most recent IN_PROGRESS attempt for this user (if any). */
    suspend fun resumeInProgress(): DomainResult<Attempt?>

    /** Fetches a specific attempt (owned by this user; enforced by RLS). */
    suspend fun get(attemptId: String): DomainResult<Attempt>
}
