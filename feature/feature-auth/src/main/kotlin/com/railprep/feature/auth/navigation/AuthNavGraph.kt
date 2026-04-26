package com.railprep.feature.auth.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.railprep.feature.auth.auth.AuthScreen
import com.railprep.feature.auth.diag.AuthDiagnosticsScreen
import com.railprep.feature.auth.email.EmailAuthScreen
import com.railprep.feature.auth.goal.GoalScreen
import com.railprep.feature.auth.language.LanguageScreen
import com.railprep.feature.auth.onboarding.OnboardingScreen
import com.railprep.feature.auth.passwordreset.NewPasswordScreen
import com.railprep.feature.auth.passwordreset.PasswordResetOtpScreen
import com.railprep.feature.auth.splash.SplashDestination
import com.railprep.feature.auth.splash.SplashScreen
import androidx.navigation.toRoute

/**
 * Registers every pre-Home destination under this extension. [webClientId] and [supabaseUrl]
 * come from the application's BuildConfig; they're used by Google Credential Manager and
 * surfaced (redacted) on the debug Auth Diagnostics screen.
 */
fun NavGraphBuilder.authGraph(
    navController: NavController,
    webClientId: String,
    supabaseUrl: String,
    onAuthFlowComplete: () -> Unit,
) {
    composable<AuthRoute.Splash> {
        SplashScreen(
            onRouteDecided = { dest ->
                val target: Any = when (dest) {
                    SplashDestination.Language -> AuthRoute.Language
                    SplashDestination.Onboarding -> AuthRoute.Onboarding
                    SplashDestination.Auth -> AuthRoute.Auth
                    SplashDestination.Goal -> AuthRoute.Goal
                    SplashDestination.Home -> {
                        onAuthFlowComplete()
                        return@SplashScreen
                    }
                }
                navController.navigate(target) {
                    popUpTo(AuthRoute.Splash) { inclusive = true }
                }
            },
        )
    }
    composable<AuthRoute.Language> {
        LanguageScreen(
            onContinue = {
                navController.navigate(AuthRoute.Onboarding) {
                    popUpTo(AuthRoute.Language) { inclusive = true }
                }
            },
        )
    }
    composable<AuthRoute.Onboarding> {
        OnboardingScreen(
            onCompleted = {
                navController.navigate(AuthRoute.Auth) {
                    popUpTo(AuthRoute.Onboarding) { inclusive = true }
                }
            },
        )
    }
    composable<AuthRoute.Auth> {
        AuthScreen(
            webClientId = webClientId,
            onAuthenticated = {
                navController.navigate(AuthRoute.Splash) {
                    popUpTo(AuthRoute.Auth) { inclusive = true }
                }
            },
            onEmailClicked = { navController.navigate(AuthRoute.EmailAuth) },
            onDiagnosticsRequested = { navController.navigate(AuthRoute.Diagnostics) },
        )
    }
    composable<AuthRoute.EmailAuth> {
        EmailAuthScreen(
            onAuthenticated = {
                navController.navigate(AuthRoute.Splash) {
                    popUpTo(AuthRoute.Auth) { inclusive = true }
                }
            },
            onForgotPasswordRequested = { email ->
                navController.navigate(AuthRoute.PasswordResetOtp(email))
            },
            onBack = { navController.popBackStack() },
        )
    }
    composable<AuthRoute.PasswordResetOtp> { backStackEntry ->
        val args = backStackEntry.toRoute<AuthRoute.PasswordResetOtp>()
        PasswordResetOtpScreen(
            email = args.email,
            onVerified = {
                navController.navigate(AuthRoute.PasswordResetNewPassword) {
                    popUpTo(AuthRoute.PasswordResetOtp(args.email)) { inclusive = true }
                }
            },
            onBack = { navController.popBackStack() },
        )
    }
    composable<AuthRoute.PasswordResetNewPassword> {
        NewPasswordScreen(
            onSuccess = {
                navController.navigate(AuthRoute.Splash) {
                    popUpTo(AuthRoute.Auth) { inclusive = true }
                }
            },
        )
    }
    composable<AuthRoute.Goal> {
        GoalScreen(
            onCompleted = onAuthFlowComplete,
        )
    }
    composable<AuthRoute.Diagnostics> {
        AuthDiagnosticsScreen(
            webClientId = webClientId,
            supabaseUrl = supabaseUrl,
            onBack = { navController.popBackStack() },
        )
    }
}
