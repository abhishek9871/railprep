package com.railprep.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.railprep.BuildConfig
import com.railprep.feature.auth.navigation.AuthRoute
import com.railprep.feature.auth.navigation.authGraph
import com.railprep.feature.daily.home.DailyHomeCard
import com.railprep.feature.daily.navigation.DailyRoute
import com.railprep.feature.daily.navigation.dailyGraph
import com.railprep.feature.home.navigation.HomeRoute
import com.railprep.feature.home.navigation.homeGraph
import com.railprep.feature.learn.navigation.LearnRoute
import com.railprep.feature.learn.navigation.learnGraph
import com.railprep.feature.tests.list.TestsTabBody
import com.railprep.feature.tests.navigation.TestsRoute
import com.railprep.feature.tests.navigation.testsGraph

@Composable
fun RailPrepNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = AuthRoute.Splash,
    ) {
        authGraph(
            navController = navController,
            webClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID,
            supabaseUrl = BuildConfig.SUPABASE_URL,
            onAuthFlowComplete = {
                navController.navigate(HomeRoute.Home) {
                    popUpTo(AuthRoute.Splash) { inclusive = true }
                    launchSingleTop = true
                }
            },
        )
        homeGraph(
            navController = navController,
            onSignedOut = {
                navController.navigate(AuthRoute.Splash) {
                    popUpTo(HomeRoute.Home) { inclusive = true }
                    launchSingleTop = true
                }
            },
            onNavigateToLearn = { navController.navigate(LearnRoute.Subjects) },
            onNavigateToTopic = { topicId ->
                navController.navigate(LearnRoute.Topic(topicId))
            },
            onNavigateToTestInstructions = { testId ->
                navController.navigate(TestsRoute.Instructions(testId))
            },
            onNavigateToPyqPaper = { testId ->
                navController.navigate(TestsRoute.PyqPaper(testId))
            },
            testsTabContent = { onOpenInstructions, onOpenPyqPaper ->
                TestsTabBody(
                    onOpenInstructions = onOpenInstructions,
                    onOpenPyqPaper = onOpenPyqPaper,
                )
            },
            dailyHomeCard = {
                DailyHomeCard(
                    onOpenPlayer = { navController.navigate(DailyRoute.Digest) },
                    onOpenReview = { navController.navigate(DailyRoute.Review) },
                )
            },
        )
        learnGraph(navController)
        testsGraph(
            navController = navController,
            onOpenTopic = { topicId -> navController.navigate(LearnRoute.Topic(topicId)) },
        )
        dailyGraph(navController)
    }
}
