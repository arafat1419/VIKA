package com.vika.sdk.network.api

import com.vika.sdk.network.models.ConversationResponse
import com.vika.sdk.network.models.InitializeData
import com.vika.sdk.network.models.InitializeRequest
import com.vika.sdk.network.models.ScreenData
import com.vika.sdk.network.models.ScreenRequest
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * Retrofit API interface for VIKA backend communication.
 *
 * Defines endpoints for SDK initialization, screen registration,
 * and audio conversation submission.
 */
internal interface NavigationApi {
    /**
     * Initialize SDK and validate credentials.
     *
     * @param request Initialization request with API key and signature
     * @return Response containing session ID on success
     */
    @POST("auth/initialize")
    suspend fun initialize(
        @Body request: InitializeRequest
    ): Response<InitializeData>

    /**
     * Save screens for voice navigation.
     *
     * Requires Bearer token authentication with session ID.
     *
     * @param request Screen request with screen list
     * @return Response containing updated screen count
     */
    @POST("screen/")
    suspend fun saveScreens(
        @Body request: ScreenRequest
    ): Response<ScreenData>

    /**
     * Send audio for conversation processing.
     *
     * Requires Bearer token authentication with session ID.
     * Results are delivered through Socket.IO 'conversation_processed' event.
     *
     * @param audio Audio file as multipart (mp3, wav, m4a, ogg, webm)
     * @return Response with conversation ID for tracking
     */
    @Multipart
    @POST("conversation/")
    suspend fun sendConversation(
        @Part audio: MultipartBody.Part
    ): Response<ConversationResponse>
}
