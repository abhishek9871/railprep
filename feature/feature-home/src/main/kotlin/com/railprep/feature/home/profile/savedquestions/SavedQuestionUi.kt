package com.railprep.feature.home.profile.savedquestions

import com.railprep.domain.model.Option
import com.railprep.domain.model.Question
import com.railprep.domain.model.QuestionBookmark

internal fun Question.localizedStem(useHi: Boolean): String =
    if (useHi && !stemHi.isNullOrBlank()) stemHi!! else stemEn

internal fun Option.localizedText(useHi: Boolean): String =
    if (useHi && !textHi.isNullOrBlank()) textHi!! else textEn

internal fun Option.localizedTrapReason(useHi: Boolean): String? =
    if (useHi && !trapReasonHi.isNullOrBlank()) trapReasonHi else trapReasonEn

internal fun Question.localizedMethod(useHi: Boolean): String? =
    if (useHi && !explanationMethodHi.isNullOrBlank()) {
        explanationMethodHi
    } else {
        explanationMethodEn ?: if (useHi && !explanationHi.isNullOrBlank()) explanationHi else explanationEn
    }

internal fun Question.localizedConcept(useHi: Boolean): String? =
    if (useHi && !explanationConceptHi.isNullOrBlank()) {
        explanationConceptHi
    } else {
        explanationConceptEn ?: if (useHi && !explanationHi.isNullOrBlank()) explanationHi else explanationEn
    }

internal fun QuestionBookmark.sourceLabel(useHi: Boolean): String {
    val title = if (useHi && !test.titleHi.isNullOrBlank()) test.titleHi!! else test.titleEn
    return "$title · Q${question.displayOrder + 1}"
}
