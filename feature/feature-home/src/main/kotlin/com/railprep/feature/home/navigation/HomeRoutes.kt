package com.railprep.feature.home.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.railprep.feature.home.HomeRootScreen
import com.railprep.feature.home.profile.about.AboutScreen
import com.railprep.feature.home.profile.account.AccountSettingsScreen
import com.railprep.feature.home.profile.bookmarks.BookmarksScreen
import com.railprep.feature.home.profile.diag.DiagScreen
import com.railprep.feature.home.profile.edit.ProfileEditScreen
import com.railprep.feature.home.profile.performance.PerformanceScreen
import com.railprep.feature.home.profile.savedquestions.SavedQuestionDetailScreen
import com.railprep.feature.home.profile.savedquestions.SavedQuestionsScreen
import kotlinx.serialization.Serializable

sealed interface HomeRoute {
    @Serializable data object Home : HomeRoute
    @Serializable data object ProfileEdit : HomeRoute
    @Serializable data object Bookmarks : HomeRoute
    @Serializable data object SavedQuestions : HomeRoute
    @Serializable data class SavedQuestionDetail(val questionId: String) : HomeRoute
    @Serializable data object Performance : HomeRoute
    @Serializable data object AccountSettings : HomeRoute
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
    privacyPolicyUrl: String = "",
    termsUrl: String = "",
    supportEmail: String = "",
    onOpenPro: () -> Unit = {},
    onNavigateToTestInstructions: (testId: String) -> Unit = {},
    onNavigateToPyqPaper: (testId: String) -> Unit = {},
    testsTabContent: @androidx.compose.runtime.Composable (
        onOpenInstructions: (String) -> Unit,
        onOpenPyqPaper: (String) -> Unit,
        onOpenPro: () -> Unit,
    ) -> Unit = { _, _, _ -> },
    dailyHomeCard: @androidx.compose.runtime.Composable () -> Unit = {},
) {
    composable<HomeRoute.Home> {
        HomeRootScreen(
            onSignedOut = onSignedOut,
            onNavigateToLearn = onNavigateToLearn,
            onOpenBookmarks = { navController.navigate(HomeRoute.Bookmarks) },
            onOpenSavedQuestions = { navController.navigate(HomeRoute.SavedQuestions) },
            onOpenPerformance = { navController.navigate(HomeRoute.Performance) },
            onOpenAccountSettings = { navController.navigate(HomeRoute.AccountSettings) },
            onOpenPro = onOpenPro,
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
    composable<HomeRoute.SavedQuestions> {
        SavedQuestionsScreen(
            onQuestionClick = { questionId ->
                navController.navigate(HomeRoute.SavedQuestionDetail(questionId))
            },
            onBack = { navController.popBackStack() },
        )
    }
    composable<HomeRoute.SavedQuestionDetail> { backStackEntry ->
        val args = backStackEntry.toRoute<HomeRoute.SavedQuestionDetail>()
        SavedQuestionDetailScreen(
            questionId = args.questionId,
            onBack = { navController.popBackStack() },
            onRemoved = { navController.popBackStack() },
        )
    }
    composable<HomeRoute.Performance> {
        PerformanceScreen(
            onBack = { navController.popBackStack() },
            onOpenTopic = onNavigateToTopic,
        )
    }
    composable<HomeRoute.AccountSettings> {
        AccountSettingsScreen(
            onBack = { navController.popBackStack() },
            onDeleted = onSignedOut,
        )
    }
    composable<HomeRoute.About> {
        AboutScreen(
            onBack = { navController.popBackStack() },
            privacyPolicyUrl = privacyPolicyUrl,
            termsUrl = termsUrl,
            supportEmail = supportEmail,
        )
    }
    composable<HomeRoute.Diag> {
        DiagScreen(onBack = { navController.popBackStack() })
    }
}
