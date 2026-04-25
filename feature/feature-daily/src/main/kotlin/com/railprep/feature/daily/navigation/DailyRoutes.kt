package com.railprep.feature.daily.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.railprep.feature.daily.digest.DailyDigestScreen
import com.railprep.feature.daily.results.DailyResultsScreen
import com.railprep.feature.daily.review.DigestReviewScreen
import kotlinx.serialization.Serializable

sealed interface DailyRoute {
    @Serializable data object Digest : DailyRoute
    @Serializable data object Results : DailyRoute
    @Serializable data object Review : DailyRoute
}

fun NavGraphBuilder.dailyGraph(navController: NavController) {
    composable<DailyRoute.Digest> {
        DailyDigestScreen(
            onSubmitted = {
                navController.navigate(DailyRoute.Results) {
                    popUpTo(DailyRoute.Digest) { inclusive = true }
                    launchSingleTop = true
                }
            },
            onBack = { navController.popBackStack() },
        )
    }
    composable<DailyRoute.Results> {
        DailyResultsScreen(
            onReview = { navController.navigate(DailyRoute.Review) },
            onDone = { navController.popBackStack() },
        )
    }
    composable<DailyRoute.Review> {
        DigestReviewScreen(
            onBack = { navController.popBackStack() },
        )
    }
}
