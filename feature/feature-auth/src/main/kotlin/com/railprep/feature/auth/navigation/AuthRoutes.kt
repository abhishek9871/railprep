package com.railprep.feature.auth.navigation

import kotlinx.serialization.Serializable

/** Type-safe destinations for the pre-Home flow. */
sealed interface AuthRoute {
    @Serializable data object Splash : AuthRoute
    @Serializable data object Language : AuthRoute
    @Serializable data object Onboarding : AuthRoute
    @Serializable data object Auth : AuthRoute
    @Serializable data object EmailAuth : AuthRoute
    @Serializable data class PasswordResetOtp(val email: String) : AuthRoute
    @Serializable data object PasswordResetNewPassword : AuthRoute
    @Serializable data object Goal : AuthRoute
    @Serializable data object Diagnostics : AuthRoute
}
