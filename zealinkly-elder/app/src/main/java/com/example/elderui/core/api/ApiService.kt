package com.example.elderui.core.api

import retrofit2.http.*
import com.squareup.moshi.JsonClass
import okhttp3.MultipartBody

/**
 * 认证API - 三端共用
 */
interface AuthApi {
    @POST("/api/auth/register/elder")
    suspend fun registerElder(@Body request: RegisterRequest): ApiResponse<RegisterResponse>

    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<LoginResponse>

    @POST("/api/auth/login-by-card")
    suspend fun loginByCard(@Body request: CardLoginRequest): ApiResponse<LoginResponse>
}

/**
 * 用户相关API - 三端共用
 */
interface UserApi {
    @GET("/api/user/info")
    suspend fun getUserInfo(): ApiResponse<UserInfo>

    @PUT("/api/user/info")
    suspend fun updateUserInfo(@Body request: UpdateUserRequest): ApiResponse<UserInfo>
}

/**
 * 紧急联系人API - 三端共用
 */
interface EmergencyContactApi {
    @GET("/api/emergency-contacts")
    suspend fun getEmergencyContacts(): ApiResponse<List<EmergencyContact>>

    @POST("/api/emergency-contacts")
    suspend fun addEmergencyContact(@Body request: CreateEmergencyContactRequest): ApiResponse<EmergencyContact>

    @PUT("/api/emergency-contacts/{id}")
    suspend fun updateEmergencyContact(
        @Path("id") id: Long,
        @Body request: CreateEmergencyContactRequest
    ): ApiResponse<EmergencyContact>

    @DELETE("/api/emergency-contacts/{id}")
    suspend fun deleteEmergencyContact(@Path("id") id: Long): ApiResponse<Unit>
}

/**
 * 老人端专用API
 */
interface ElderApi {
    // 任务相关
    @POST("/api/tasks/cooperation/publish")
    suspend fun publishTask(@Body request: PublishTaskRequest): ApiResponse<Task>

    @GET("/api/tasks/cooperation/my-as-elder")
    suspend fun getMyTasks(): ApiResponse<List<Task>>

    @GET("/api/tasks/cooperation/{taskId}")
    suspend fun getTaskDetail(@Path("taskId") taskId: Long): ApiResponse<Task>

    @POST("/api/tasks/cooperation/{taskId}/confirm")
    suspend fun confirmTask(@Path("taskId") taskId: Long): ApiResponse<Task>

    @POST("/api/tasks/cooperation/{taskId}/appeal")
    suspend fun appealTask(@Path("taskId") taskId: Long, @Body request: AppealTaskRequest): ApiResponse<Unit>

    // 紧急报警
    @POST("/api/emergency/trigger")
    suspend fun triggerEmergency(@Body request: TriggerEmergencyRequest): ApiResponse<EmergencyAlert>

    // Agent处理
    @POST("/api/agent/process")
    suspend fun agentProcess(@Body request: AgentProcessRequest): ApiResponse<AgentProcessResponse>

    @POST("/api/agent/process-voice")
    suspend fun agentProcessVoice(@Body request: AgentVoiceRequest): ApiResponse<AgentProcessResponse>

    // 意图识别
    @POST("/api/intent/recognize")
    suspend fun recognizeIntent(@Body request: AgentProcessRequest): ApiResponse<IntentRecognitionResponse>

    // 语音识别
    @POST("/api/asr/recognize")
    suspend fun recognizeAsr(@Body request: AsrRequest): ApiResponse<AsrResponse>

    // 积分
    @GET("/api/points/total")
    suspend fun getPointsTotal(): ApiResponse<PointsInfo>

    @GET("/api/points/history")
    suspend fun getPointsHistory(): ApiResponse<List<PointsRecord>>

    // 通知
    @GET("/api/notifications")
    suspend fun getNotifications(): ApiResponse<List<Notification>>

    @GET("/api/notifications/unread-count")
    suspend fun getUnreadCount(): ApiResponse<UnreadCount>

    @PATCH("/api/notifications/{id}/read")
    suspend fun markNotificationAsRead(@Path("id") id: Long): ApiResponse<Unit>

    @PATCH("/api/notifications/read-all")
    suspend fun markAllNotificationsAsRead(): ApiResponse<Unit>

    // AI聊天
    @POST("/api/ai/ask")
    suspend fun ask(@Body request: AskRequest): ApiResponse<String>

    @GET("/api/ai/history")
    suspend fun getChatHistory(): ApiResponse<List<ChatHistory>>

    // 文件
    @Multipart
    @POST("/api/files/upload")
    suspend fun uploadFile(
        @Part file: okhttp3.MultipartBody.Part,
        @Part("relatedType") relatedType: String? = null,
        @Part("relatedId") relatedId: Long? = null
    ): ApiResponse<FileUploadResponse>

    @POST("/api/files/upload-base64")
    suspend fun uploadFileBase64(
        @Body request: FileUploadBase64Request
    ): ApiResponse<FileUploadResponse>

    @GET("/api/files/my")
    suspend fun getMyFiles(): ApiResponse<List<FileInfo>>

    @DELETE("/api/files/{fileId}")
    suspend fun deleteFile(@Path("fileId") fileId: Long): ApiResponse<Unit>
}

@JsonClass(generateAdapter = true)
data class FileUploadBase64Request(
    val base64Data: String,
    val filename: String,
    val contentType: String,
    val relatedType: String? = null,
    val relatedId: Long? = null
)

