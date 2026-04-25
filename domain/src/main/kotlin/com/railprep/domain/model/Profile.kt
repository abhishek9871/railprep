package com.railprep.domain.model

import kotlinx.datetime.LocalDate

data class Profile(
    val id: String,
    val displayName: String?,
    val avatarUrl: String?,
    val language: String,
    val examTarget: ExamTarget?,
    val examTargetDate: LocalDate?,
    val dailyMinutes: Int,
    val qualification: Qualification?,
    val category: Category?,
    val dob: LocalDate?,
    val onboardingComplete: Boolean,
    val xp: Int,
    val level: Int,
    val streakCurrent: Int,
    val streakBest: Int,
    /** Opt-in for the 20:00 IST on-device reminder. Default false; flipped via the
     *  first-digest-submit prompt OR the Profile > Daily reminder toggle. */
    val notificationsEnabled: Boolean = false,
)

enum class ExamTarget(val wire: String) {
    NtpcCbt1("NTPC_CBT1"),
    NtpcCbt2("NTPC_CBT2");

    companion object {
        fun fromWire(value: String?): ExamTarget? = value?.let { v -> entries.find { it.wire == v } }
    }
}

enum class Qualification(val wire: String) {
    Twelfth("12TH"),
    Graduate("GRADUATE");

    companion object {
        fun fromWire(value: String?): Qualification? = value?.let { v -> entries.find { it.wire == v } }
    }
}

enum class Category(val wire: String) {
    UR("UR"), OBC("OBC"), SC("SC"), ST("ST"), EWS("EWS");

    companion object {
        fun fromWire(value: String?): Category? = value?.let { v -> entries.find { it.wire == v } }
    }
}
