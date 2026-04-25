package com.railprep.domain.model

/**
 * Languages offered on the Language picker. Only [En] and [Hi] have full UI coverage in Phase 1;
 * others show a "Coming soon" hint that falls back to English.
 */
enum class SupportedLanguage(
    val code: String,
    val englishName: String,
    val nativeName: String,
    val phase1Supported: Boolean,
) {
    En(code = "en", englishName = "English", nativeName = "English", phase1Supported = true),
    Hi(code = "hi", englishName = "Hindi", nativeName = "हिन्दी", phase1Supported = true),
    Bn(code = "bn", englishName = "Bengali", nativeName = "বাংলা", phase1Supported = false),
    Ta(code = "ta", englishName = "Tamil", nativeName = "தமிழ்", phase1Supported = false),
    Te(code = "te", englishName = "Telugu", nativeName = "తెలుగు", phase1Supported = false),
    Mr(code = "mr", englishName = "Marathi", nativeName = "मराठी", phase1Supported = false),
    Gu(code = "gu", englishName = "Gujarati", nativeName = "ગુજરાતી", phase1Supported = false),
    Pa(code = "pa", englishName = "Punjabi", nativeName = "ਪੰਜਾਬੀ", phase1Supported = false);

    companion object {
        fun fromCode(code: String?): SupportedLanguage? = code?.let { c -> entries.find { it.code == c } }
    }
}
