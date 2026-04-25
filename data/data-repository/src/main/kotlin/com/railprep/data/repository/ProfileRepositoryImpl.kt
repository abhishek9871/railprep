package com.railprep.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.railprep.core.common.coroutines.DispatcherProvider
import com.railprep.domain.model.ExamGoal
import com.railprep.domain.model.Profile
import com.railprep.domain.repository.ProfileRepository
import com.railprep.domain.util.DomainError
import com.railprep.domain.util.DomainResult
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import com.railprep.data.remote.supabase.ProfileDto
import com.railprep.data.remote.supabase.ProfileUpdateDto
import com.railprep.data.remote.supabase.toDomain
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private val Context.profileCachePrefs: DataStore<Preferences> by preferencesDataStore(
    name = "railprep_profile_cache",
)

private object ProfileCacheKeys {
    val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
}

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val supabase: SupabaseClient,
    private val dispatchers: DispatcherProvider,
) : ProfileRepository {

    private val _cached = MutableStateFlow<Profile?>(null)

    override suspend fun getCurrentProfile(): DomainResult<Profile> = withContext(dispatchers.io) {
        val userId = supabase.auth.currentSessionOrNull()?.user?.id
            ?: return@withContext DomainResult.Failure(DomainError.Auth("No active session"))

        runCatching {
            val dto = supabase.postgrest.from("profiles")
                .select(columns = Columns.ALL) {
                    filter { eq("id", userId) }
                    limit(1)
                    single()
                }
                .decodeAs<ProfileDto>()
            dto.toDomain()
        }.fold(
            onSuccess = { profile ->
                _cached.value = profile
                cacheOnboardingComplete(profile.onboardingComplete)
                DomainResult.Success(profile)
            },
            onFailure = { t ->
                DomainResult.Failure(DomainError.Network(t.message ?: "Profile fetch failed", t))
            },
        )
    }

    override fun observeCurrentProfile(): Flow<Profile?> = _cached.asStateFlow()

    override suspend fun completeOnboarding(goal: ExamGoal): DomainResult<Profile> =
        withContext(dispatchers.io) {
            val userId = supabase.auth.currentSessionOrNull()?.user?.id
                ?: return@withContext DomainResult.Failure(DomainError.Auth("No active session"))

            val update = ProfileUpdateDto(
                displayName = goal.displayName?.takeIf { it.isNotBlank() },
                examTarget = goal.examTarget.wire,
                examTargetDate = goal.examTargetDate,
                dailyMinutes = goal.dailyMinutes,
                qualification = goal.qualification.wire,
                category = goal.category.wire,
                dob = goal.dob,
                onboardingComplete = true,
            )

            runCatching {
                supabase.postgrest.from("profiles")
                    .update(update) {
                        filter { eq("id", userId) }
                        select()
                        single()
                    }
                    .decodeAs<ProfileDto>()
                    .toDomain()
            }.fold(
                onSuccess = { profile ->
                    _cached.value = profile
                    cacheOnboardingComplete(true)
                    DomainResult.Success(profile)
                },
                onFailure = { t ->
                    DomainResult.Failure(DomainError.Network(t.message ?: "Profile update failed", t))
                },
            )
        }

    override fun cachedOnboardingComplete(): Boolean = runBlocking {
        appContext.profileCachePrefs.data.first()[ProfileCacheKeys.ONBOARDING_COMPLETE] == true
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean): DomainResult<Profile> =
        withContext(dispatchers.io) {
            val userId = supabase.auth.currentSessionOrNull()?.user?.id
                ?: return@withContext DomainResult.Failure(DomainError.Auth("No active session"))

            runCatching {
                supabase.postgrest.from("profiles")
                    .update(mapOf("notifications_enabled" to enabled)) {
                        filter { eq("id", userId) }
                        select()
                        single()
                    }
                    .decodeAs<ProfileDto>()
                    .toDomain()
            }.fold(
                onSuccess = { profile ->
                    _cached.value = profile
                    DomainResult.Success(profile)
                },
                onFailure = { t ->
                    DomainResult.Failure(DomainError.Network(t.message ?: "toggle failed", t))
                },
            )
        }

    private suspend fun cacheOnboardingComplete(value: Boolean) {
        appContext.profileCachePrefs.edit { prefs ->
            prefs[ProfileCacheKeys.ONBOARDING_COMPLETE] = value
        }
    }
}
