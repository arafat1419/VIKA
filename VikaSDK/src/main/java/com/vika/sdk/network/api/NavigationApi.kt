package com.vika.sdk.network.api

import com.vika.sdk.network.models.ApiResponse
import com.vika.sdk.network.models.InitializeData
import com.vika.sdk.network.models.InitializeRequest
import com.vika.sdk.network.models.RecordingData
import com.vika.sdk.network.models.RegisterScreensData
import com.vika.sdk.network.models.RegisterScreensRequest
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
 * and voice recording submission.
 */
internal interface NavigationApi {
    /**
     * Initialize SDK and validate credentials.
     *
     * @param request Initialization request with API key and app info
     * @return Response containing session ID on success
     */
    @POST("initialize")
    suspend fun initialize(
        @Body request: InitializeRequest
    ): Response<ApiResponse<InitializeData>>

    /**
     * Register screens for voice navigation.
     *
     * @param request Registration request with screen list
     * @return Response containing registered count
     */
    @POST("register-screens")
    suspend fun registerScreens(
        @Body request: RegisterScreensRequest
    ): Response<ApiResponse<RegisterScreensData>>

    /**
     * Send voice recording and get navigation response.
     *
     * @param audio Audio file as multipart
     * @param sessionId Current session ID
     * @return Response with transcription, AI reply, and navigation data
     */
    @Multipart
    @POST("send-recording")
    suspend fun sendRecording(
        @Part audio: MultipartBody.Part,
        @Part("session_id") sessionId: String
    ): Response<ApiResponse<RecordingData>>
}
