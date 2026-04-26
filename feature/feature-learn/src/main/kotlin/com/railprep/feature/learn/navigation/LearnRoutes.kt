package com.railprep.feature.learn.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import androidx.compose.ui.platform.LocalConfiguration
import com.railprep.feature.learn.chapters.ChaptersScreen
import com.railprep.feature.learn.subjects.SubjectsScreen
import com.railprep.feature.learn.topic.TopicDetailScreen
import kotlinx.serialization.Serializable

sealed interface LearnRoute {
    @Serializable data object Subjects : LearnRoute
    @Serializable data class Chapters(val subjectId: String, val subjectTitle: String) : LearnRoute
    @Serializable data class Topic(val topicId: String) : LearnRoute
}

fun NavGraphBuilder.learnGraph(navController: NavController) {
    composable<LearnRoute.Subjects> {
        val useHi = LocalConfiguration.current.locales.get(0).language == "hi"
        SubjectsScreen(
            onSubjectClick = { subject ->
                navController.navigate(
                    LearnRoute.Chapters(
                        subject.id,
                        if (useHi) subject.titleHi else subject.titleEn,
                    ),
                )
            },
            onBack = { navController.popBackStack() },
        )
    }
    composable<LearnRoute.Chapters> { backStackEntry ->
        val args = backStackEntry.toRoute<LearnRoute.Chapters>()
        ChaptersScreen(
            subjectId = args.subjectId,
            subjectTitle = args.subjectTitle,
            onTopicClick = { topicId ->
                navController.navigate(LearnRoute.Topic(topicId))
            },
            onBack = { navController.popBackStack() },
        )
    }
    composable<LearnRoute.Topic> { backStackEntry ->
        val args = backStackEntry.toRoute<LearnRoute.Topic>()
        TopicDetailScreen(
            topicId = args.topicId,
            onBack = { navController.popBackStack() },
        )
    }
}
