package com.vika.sdk.network.models

import com.google.gson.annotations.SerializedName

/**
 * Data payload for recording response.
 *
 * Contains transcription, AI reply, and navigation instructions.
 *
 * @property transcription Transcribed text from user's voice
 * @property replyText AI-generated text reply
 * @property replyAudioUrl URL to download AI voice reply audio
 * @property navigation Navigation instructions if a screen match was found
 */
data class RecordingData(
    @SerializedName("transcription")
    val transcription: String? = null,
    @SerializedName("reply_text")
    val replyText: String? = null,
    @SerializedName("reply_audio_url")
    val replyAudioUrl: String? = null,
    @SerializedName("navigation")
    val navigation: NavigationData? = null
)
