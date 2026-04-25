package com.railprep.domain.model

import kotlinx.serialization.Serializable

/**
 * Server-stored content in multiple languages. Pick by language code; fall back to English.
 */
@Serializable
data class LocalizedString(
    val en: String,
    val hi: String? = null,
    val other: Map<String, String> = emptyMap(),
) {
    fun forLanguage(code: String): String = when (code) {
        "en" -> en
        "hi" -> hi ?: en
        else -> other[code] ?: en
    }
}
