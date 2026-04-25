package com.railprep.feature.tests.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.railprep.feature.tests.instructions.InstructionsScreen
import com.railprep.feature.tests.player.TestPlayerScreen
import com.railprep.feature.tests.pyq.PyqPaperScreen
import com.railprep.feature.tests.results.ResultsScreen
import com.railprep.feature.tests.review.ReviewScreen
import kotlinx.serialization.Serializable

sealed interface TestsRoute {
    @Serializable data class Instructions(val testId: String) : TestsRoute
    @Serializable data class Player(val attemptId: String) : TestsRoute
    @Serializable data class Results(val attemptId: String) : TestsRoute
    @Serializable data class Review(val attemptId: String) : TestsRoute
    @Serializable data class PyqPaper(val testId: String) : TestsRoute
}

fun NavGraphBuilder.testsGraph(
    navController: NavController,
    onOpenTopic: (topicId: String) -> Unit = {},
) {
    composable<TestsRoute.Instructions> { backStackEntry ->
        val args = backStackEntry.toRoute<TestsRoute.Instructions>()
        InstructionsScreen(
            testId = args.testId,
            onStartAttempt = { attemptId ->
                navController.navigate(TestsRoute.Player(attemptId)) {
                    popUpTo(TestsRoute.Instructions(args.testId)) { inclusive = true }
                    launchSingleTop = true
                }
            },
            onBack = { navController.popBackStack() },
        )
    }
    composable<TestsRoute.Player> { backStackEntry ->
        val args = backStackEntry.toRoute<TestsRoute.Player>()
        TestPlayerScreen(
            attemptId = args.attemptId,
            onSubmitted = {
                navController.navigate(TestsRoute.Results(args.attemptId)) {
                    popUpTo(TestsRoute.Player(args.attemptId)) { inclusive = true }
                    launchSingleTop = true
                }
            },
            onBack = { navController.popBackStack() },
        )
    }
    composable<TestsRoute.Results> { backStackEntry ->
        val args = backStackEntry.toRoute<TestsRoute.Results>()
        ResultsScreen(
            attemptId = args.attemptId,
            onReview = { navController.navigate(TestsRoute.Review(args.attemptId)) },
            onDone = { navController.popBackStack() },
            onOpenTopic = onOpenTopic,
        )
    }
    composable<TestsRoute.Review> { backStackEntry ->
        val args = backStackEntry.toRoute<TestsRoute.Review>()
        ReviewScreen(
            attemptId = args.attemptId,
            onBack = { navController.popBackStack() },
        )
    }
    composable<TestsRoute.PyqPaper> { backStackEntry ->
        val args = backStackEntry.toRoute<TestsRoute.PyqPaper>()
        PyqPaperScreen(
            testId = args.testId,
            onBack = { navController.popBackStack() },
        )
    }
}
