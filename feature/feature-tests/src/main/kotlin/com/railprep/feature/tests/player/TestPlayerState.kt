package com.railprep.feature.tests.player

import com.railprep.domain.model.Attempt
import com.railprep.domain.model.Question
import com.railprep.domain.model.Test
import com.railprep.domain.model.TestSection

data class PlayerAnswer(
    val selectedOptionId: String?,
    val flagged: Boolean,
    /** Best-effort: true if this answer has been confirmed-written to the server. */
    val synced: Boolean,
)

data class TestPlayerState(
    val loading: Boolean = true,
    val error: String? = null,
    val test: Test? = null,
    val sections: List<TestSection> = emptyList(),
    /** Flat, ordered by section.display_order, then question.display_order. */
    val questions: List<Question> = emptyList(),
    /** Keyed by question.id. Missing key = never visited. */
    val answers: Map<String, PlayerAnswer> = emptyMap(),

    val attempt: Attempt? = null,
    val attemptId: String = "",
    val deadlineEpochMs: Long = 0L,
    val remainingMs: Long = 0L,
    val timeUp: Boolean = false,

    val currentIndex: Int = 0,
    val showHi: Boolean = false,

    val paletteOpen: Boolean = false,
    val confirmSubmit: Boolean = false,

    val submitting: Boolean = false,
    val submitted: Boolean = false,
    val pendingUnsynced: Int = 0,
) {
    val currentQuestion: Question? get() = questions.getOrNull(currentIndex)
    val currentSection: TestSection? get() {
        val q = currentQuestion ?: return null
        return sections.firstOrNull { it.id == q.sectionId }
    }
    val answeredCount: Int get() = answers.values.count { it.selectedOptionId != null }
    val totalCount: Int get() = questions.size
}
