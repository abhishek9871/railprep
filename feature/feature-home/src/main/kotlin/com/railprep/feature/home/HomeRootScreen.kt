package com.railprep.feature.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.railprep.feature.home.dashboard.DashboardTab
import com.railprep.feature.home.placeholder.FeedTab
import com.railprep.feature.home.placeholder.TestsTab
import com.railprep.feature.home.profile.ProfileTab

private enum class Tab { HOME, TESTS, PYQ, PROFILE }

@Composable
fun HomeRootScreen(
    onSignedOut: () -> Unit,
    onNavigateToLearn: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenSavedQuestions: () -> Unit,
    onOpenPerformance: () -> Unit,
    onOpenAccountSettings: () -> Unit,
    onOpenPro: () -> Unit,
    onOpenProfileEdit: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenDiag: () -> Unit,
    onOpenTestInstructions: (testId: String) -> Unit = {},
    onOpenPyqPaper: (testId: String) -> Unit = {},
    /** Slot filled by the app nav graph with [feature-tests]'s TestsTabBody composable.
     *  Keeps feature-home independent of feature-tests at the module level. */
    testsTabContent: @Composable (
        onOpenInstructions: (String) -> Unit,
        onOpenPyqPaper: (String) -> Unit,
        onOpenPro: () -> Unit,
    ) -> Unit = { _, _, _ -> TestsTab() },
    pyqTabContent: @Composable (
        onOpenPyqPaper: (String) -> Unit,
    ) -> Unit = { _ -> FeedTab() },
    /** Slot filled by the app nav graph with feature-daily's DailyHomeCard. Default empty. */
    dailyHomeCard: @Composable () -> Unit = {},
) {
    var tab by rememberSaveable { mutableStateOf(Tab.HOME) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == Tab.HOME,
                    onClick = { tab = Tab.HOME },
                    icon = { Icon(Icons.Filled.Home, null) },
                    label = { Text(stringResource(R.string.nav_home)) },
                )
                NavigationBarItem(
                    selected = tab == Tab.TESTS,
                    onClick = { tab = Tab.TESTS },
                    icon = { Icon(Icons.Filled.Quiz, null) },
                    label = { Text(stringResource(R.string.nav_tests)) },
                )
                NavigationBarItem(
                    selected = tab == Tab.PYQ,
                    onClick = { tab = Tab.PYQ },
                    icon = { Icon(Icons.AutoMirrored.Filled.Article, null) },
                    label = { Text(stringResource(R.string.nav_pyq)) },
                )
                NavigationBarItem(
                    selected = tab == Tab.PROFILE,
                    onClick = { tab = Tab.PROFILE },
                    icon = { Icon(Icons.Filled.Person, null) },
                    label = { Text(stringResource(R.string.nav_profile)) },
                )
            }
        },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            when (tab) {
                Tab.HOME -> DashboardTab(
                    onStartLearning = onNavigateToLearn,
                    onOpenBookmarks = onOpenBookmarks,
                    dailyHomeCard = dailyHomeCard,
                )
                Tab.TESTS -> testsTabContent(onOpenTestInstructions, onOpenPyqPaper, onOpenPro)
                Tab.PYQ -> pyqTabContent(onOpenPyqPaper)
                Tab.PROFILE -> ProfileTab(
                    onSignedOut = onSignedOut,
                    onOpenEdit = onOpenProfileEdit,
                    onOpenBookmarks = onOpenBookmarks,
                    onOpenSavedQuestions = onOpenSavedQuestions,
                    onOpenPerformance = onOpenPerformance,
                    onOpenAccountSettings = onOpenAccountSettings,
                    onOpenPro = onOpenPro,
                    onOpenAbout = onOpenAbout,
                    onOpenDiag = onOpenDiag,
                )
            }
        }
    }
}
