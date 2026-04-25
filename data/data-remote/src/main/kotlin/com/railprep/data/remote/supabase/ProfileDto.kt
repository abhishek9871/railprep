package com.railprep.data.remote.supabase

import com.railprep.domain.model.Category
import com.railprep.domain.model.ExamTarget
import com.railprep.domain.model.Profile
import com.railprep.domain.model.Qualification
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Mirrors the `public.profiles` table defined in supabase/migrations/0001_profiles.sql.
 * Column names use snake_case (via @SerialName) to match Postgres.
 */
@Serializable
data class ProfileDto(
    @SerialName("id") val id: String,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("language") val language: String = "en",
    @SerialName("exam_target") val examTarget: String? = null,
    @SerialName("exam_target_date") val examTargetDate: LocalDate? = null,
    @SerialName("daily_minutes") val dailyMinutes: Int = 60,
    @SerialName("qualification") val qualification: String? = null,
    @SerialName("category") val category: String? = null,
    @SerialName("dob") val dob: LocalDate? = null,
    @SerialName("onboarding_complete") val onboardingComplete: Boolean = false,
    @SerialName("xp") val xp: Int = 0,
    @SerialName("level") val level: Int = 1,
    @SerialName("streak_current") val streakCurrent: Int = 0,
    @SerialName("streak_best") val streakBest: Int = 0,
    @SerialName("notifications_enabled") val notificationsEnabled: Boolean = false,
)

/**
 * Partial update payload. Every field nullable so `null` means "leave unchanged" — we only send
 * keys the user actually filled in.
 */
@Serializable
data class ProfileUpdateDto(
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("language") val language: String? = null,
    @SerialName("exam_target") val examTarget: String? = null,
    @SerialName("exam_target_date") val examTargetDate: LocalDate? = null,
    @SerialName("daily_minutes") val dailyMinutes: Int? = null,
    @SerialName("qualification") val qualification: String? = null,
    @SerialName("category") val category: String? = null,
    @SerialName("dob") val dob: LocalDate? = null,
    @SerialName("onboarding_complete") val onboardingComplete: Boolean? = null,
)

fun ProfileDto.toDomain(): Profile = Profile(
    id = id,
    displayName = displayName,
    avatarUrl = avatarUrl,
    language = language,
    examTarget = ExamTarget.fromWire(examTarget),
    examTargetDate = examTargetDate,
    dailyMinutes = dailyMinutes,
    qualification = Qualification.fromWire(qualification),
    category = Category.fromWire(category),
    dob = dob,
    onboardingComplete = onboardingComplete,
    xp = xp,
    level = level,
    streakCurrent = streakCurrent,
    streakBest = streakBest,
    notificationsEnabled = notificationsEnabled,
)
