package com.railprep.domain.repository

import com.railprep.domain.model.ExamGoal
import com.railprep.domain.model.Profile
import com.railprep.domain.util.DomainResult
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    /** Fetches once, cached. Refreshes on next successful fetch. */
    suspend fun getCurrentProfile(): DomainResult<Profile>

    /** Observes locally-cached profile, emits fresh copies on repository refresh. Null when no session. */
    fun observeCurrentProfile(): Flow<Profile?>

    /** Apply the goal-screen submission: writes fields + flips onboarding_complete to true. */
    suspend fun completeOnboarding(goal: ExamGoal): DomainResult<Profile>

    /** Synchronous cached view for Splash routing. */
    fun cachedOnboardingComplete(): Boolean

    /** Flip profiles.notifications_enabled. Client schedules/cancels the on-device worker
     *  separately; the server is the canonical state so the flag travels with the user's account. */
    suspend fun setNotificationsEnabled(enabled: Boolean): DomainResult<Profile>

    /** JSON export of profile, attempts, bookmarks, digest attempts, and entitlements. */
    suspend fun exportMyData(): DomainResult<String>

    /** Deletes the authenticated account. Server cascades user-owned rows. */
    suspend fun deleteMyAccount(): DomainResult<Unit>
}
