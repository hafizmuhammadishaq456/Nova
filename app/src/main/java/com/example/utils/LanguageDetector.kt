package com.example.utils

import java.util.Locale

object LanguageDetector {
    fun detectLanguage(text: String): String {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return "en"

        // Arabic / Urdu script range
        if (trimmed.any { it in '\u0600'..'\u06FF' || it in '\u0750'..'\u077F' }) {
            return "ur"
        }

        // Devanagari / Hindi script range
        if (trimmed.any { it in '\u0900'..'\u097F' }) {
            return "hi"
        }

        // Roman Urdu common heuristic
        val lower = trimmed.lowercase(Locale.ROOT)
        val romanUrduWords = setOf(
            "kya", "hai", "hain", "kaise", "mera", "meri", "mere", "mujhe", "batao",
            "karo", "shukriya", "theek", "acha", "aap", "tum", "hum", "kaun", "kab",
            "kaha", "kyun", "kuch", "suno", "bolo", "sahih", "zaroor"
        )
        val tokens = lower.split(Regex("\\s+"))
        val matchCount = tokens.count { it in romanUrduWords }
        if (matchCount >= 2 || (tokens.size <= 3 && matchCount >= 1)) {
            return "roman_ur"
        }

        return "en"
    }

    fun getLocaleForLanguage(languageCode: String, fallbackText: String = ""): Locale {
        val resolved = if (languageCode == "auto") detectLanguage(fallbackText) else languageCode
        return when (resolved) {
            "ur" -> Locale.forLanguageTag("ur-PK")
            "hi" -> Locale.forLanguageTag("hi-IN")
            "roman_ur" -> Locale.forLanguageTag("ur-PK") // Try Urdu or fallback to Indian/US English
            else -> Locale.US
        }
    }
}
