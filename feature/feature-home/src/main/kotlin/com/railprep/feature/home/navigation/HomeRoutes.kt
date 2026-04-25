package com.railprep.feature.home.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.railprep.feature.home.HomeRootScreen
import com.railprep.feature.home.profile.about.AboutScreen
import com.railprep.feature.home.profile.bookmarks.BookmarksScreen
import com.railprep.feature.home.profile.diag.DiagScreen
import com.railprep.feature.home.profile.edit.ProfileEditScreen
import kotlinx.serialization.Serializable

sealed interface HomeRoute {
    @Serializable data object Home : HomeRoute
    @Serializable data object ProfileEdit : HomeRoute
    @Serializable data object Bookmarks : HomeRoute
    @Serializable data object About : HomeRoute
    @Serializable data object Diag : HomeRoute
}

/**
 * [onNavigateToLearn] — called when the Home dashboard's "Start learning" CTA is tapped or the
 * user taps a bookmark. Hosted by the app module so it can cross into the feature-learn nav
 * graph (which feature-home doesn't depend on).
 */
fun NavGraphBuilder.homeGraph(
    navController: NavController,
    onSignedOut: () -> Unit,
    onNavigateToLearn: () -> Unit,
    onNavigateToTopic: (topicId: String) -> Unit,
    onNavigateToTestInstructions: (testId: String) -> Unit = {},
    onNavigateToPyqPaper: (testId: String) -> Unit = {},
    testsTabContent: @androidx.compose.runtime.Composable (
        onOpenInstructions: (String) -> Unit,
        onOpenPyqPaper: (String) -> Unit,
    ) -> Unit = { _, _ -> },
    dailyHomeCard: @androidx.compose.runtime.Composable () -> Unit = {},
) {
    composable<HomeRoute.Home> {
        HomeRootScreen(
            onSignedOut = onSignedOut,
            onNavigateToLearn = onNavigateToLearn,
            onOpenBookmarks = { navController.navigate(HomeRoute.Bookmarks) },
            onOpenProfileEdit = { navController.navigate(HomeRoute.ProfileEdit) },
            onOpenAbout = { navController.navigate(HomeRoute.About) },
            onOpenDiag = { navController.navigate(HomeRoute.Diag) },
            onOpenTestInstructions = onNavigateToTestInstructions,
            onOpenPyqPaper = onNavigateToPyqPaper,
            testsTabContent = testsTabContent,
            dailyHomeCard = dailyHomeCard,
        )
    }
    composable<HomeRoute.ProfileEdit> {
        ProfileEditScreen(onBack = { navController.popBackStack() })
    }
    composable<HomeRoute.Bookmarks> {
        BookmarksScreen(
            onTopicClick = onNavigateToTopic,
            onBack = { navController.popBackStack() },
        )
    }
    composable<HomeRoute.About> {
        AboutScreen(onBack = { navController.popBackStack() })
    }
    composable<HomeRoute.Diag> {
        DiagScreen(onBack = { navController.popBackStack() })
    }
}
