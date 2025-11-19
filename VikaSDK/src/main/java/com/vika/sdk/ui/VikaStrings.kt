package com.vika.sdk.ui

import com.vika.sdk.models.VikaLanguage

/**
 * Localized strings for the VIKA SDK UI.
 */
internal object VikaStrings {
    fun listening(language: VikaLanguage): String = when (language) {
        VikaLanguage.ENGLISH -> "Listening..."
        VikaLanguage.INDONESIAN -> "Mendengarkan..."
    }

    fun processing(language: VikaLanguage): String = when (language) {
        VikaLanguage.ENGLISH -> "Processing..."
        VikaLanguage.INDONESIAN -> "Memproses..."
    }

    fun sending(language: VikaLanguage): String = when (language) {
        VikaLanguage.ENGLISH -> "Sending..."
        VikaLanguage.INDONESIAN -> "Mengirim..."
    }

    fun speaking(language: VikaLanguage): String = when (language) {
        VikaLanguage.ENGLISH -> "Speaking..."
        VikaLanguage.INDONESIAN -> "Berbicara..."
    }

    fun navigatingTo(language: VikaLanguage, screenName: String): String = when (language) {
        VikaLanguage.ENGLISH -> "Navigating to $screenName..."
        VikaLanguage.INDONESIAN -> "Menuju ke $screenName..."
    }

    fun navigating(language: VikaLanguage): String = when (language) {
        VikaLanguage.ENGLISH -> "Navigating..."
        VikaLanguage.INDONESIAN -> "Menuju..."
    }

    fun microphoneAccessRequired(language: VikaLanguage): String = when (language) {
        VikaLanguage.ENGLISH -> "Microphone Access Required"
        VikaLanguage.INDONESIAN -> "Akses Mikrofon Diperlukan"
    }

    fun microphonePermissionMessage(language: VikaLanguage): String = when (language) {
        VikaLanguage.ENGLISH -> "Please grant microphone permission to record audio messages."
        VikaLanguage.INDONESIAN -> "Mohon izinkan akses mikrofon untuk merekam pesan suara."
    }

    fun grantPermission(language: VikaLanguage): String = when (language) {
        VikaLanguage.ENGLISH -> "Grant Permission"
        VikaLanguage.INDONESIAN -> "Izinkan"
    }

    fun error(language: VikaLanguage, message: String): String = when (language) {
        VikaLanguage.ENGLISH -> "Error: $message"
        VikaLanguage.INDONESIAN -> "Kesalahan: $message"
    }
}
