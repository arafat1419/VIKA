package com.vika.sdk.utils

import com.vika.sdk.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

/**
 * Helper utilities for handling audio files from the VIKA backend.
 *
 * Provides methods for constructing full audio URLs and downloading audio files.
 */
object AudioHelper {

    /**
     * Construct full URL for an audio file path.
     *
     * Converts relative paths (e.g., "/audio/file.wav") to full URLs.
     *
     * @param relativePath Relative path from API response
     * @return Full URL to the audio file
     */
    fun getFullAudioUrl(relativePath: String): String {
        if (relativePath.startsWith("http://") || relativePath.startsWith("https://")) {
            return relativePath
        }

        val baseUrl = BuildConfig.BASE_URL.trimEnd('/')
        val path = if (relativePath.startsWith("/")) relativePath else "/$relativePath"
        return "$baseUrl$path"
    }

    /**
     * Download audio file to the specified directory.
     *
     * @param audioUrl Full URL or relative path to the audio file
     * @param outputDir Directory to save the downloaded file
     * @param fileName Optional custom file name (uses URL filename if not provided)
     * @return Downloaded file
     * @throws Exception if download fails
     */
    suspend fun downloadAudio(
        audioUrl: String,
        outputDir: File,
        fileName: String? = null
    ): File = withContext(Dispatchers.IO) {
        val fullUrl = getFullAudioUrl(audioUrl)
        val url = URL(fullUrl)

        val outputFileName = fileName ?: url.path.substringAfterLast("/")
        val outputFile = File(outputDir, outputFileName)

        url.openStream().use { input ->
            FileOutputStream(outputFile).use { output ->
                input.copyTo(output)
            }
        }

        outputFile
    }

    /**
     * Extract filename from audio URL.
     *
     * @param audioUrl URL or path to extract filename from
     * @return Filename from the URL
     */
    fun extractFileName(audioUrl: String): String {
        return audioUrl.substringAfterLast("/")
    }
}
