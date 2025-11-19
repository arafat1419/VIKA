package com.vika.sdk.models

/**
 * Supported languages for the VIKA SDK UI.
 *
 * @property code The language code (ISO 639-1)
 */
enum class VikaLanguage(val code: String) {
    /**
     * English language.
     */
    ENGLISH("en"),

    /**
     * Indonesian language.
     */
    INDONESIAN("id")
}
