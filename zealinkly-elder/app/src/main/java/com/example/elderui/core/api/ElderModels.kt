package com.example.elderui.core.api

import com.squareup.moshi.JsonClass

/**
 * 老人端特定数据模型
 */

// 任务相关
@JsonClass(generateAdapter = true)
data class Task(
    val id: Long,
    val taskType: String,
    val status: String,
    val elder: UserBasic? = null,
    val volunteer: UserBasic? = null,
    val content: String,
    val pointsReward: Int,
    val evidences: List<Evidence>? = null,
    val createdAt: String
)

@JsonClass(generateAdapter = true)
data class UserBasic(
    val id: Long,
    val realName: String
)

@JsonClass(generateAdapter = true)
data class Evidence(
    val id: Long,
    val evidenceType: String,
    val fileUrl: String,
    val createdAt: String
)

@JsonClass(generateAdapter = true)
data class PublishTaskRequest(
    val title: String,
    val description: String,
    val pointsReward: Int
)

@JsonClass(generateAdapter = true)
data class ConfirmTaskRequest(
    val taskId: Long
)

@JsonClass(generateAdapter = true)
data class AppealTaskRequest(
    val content: String
)

// 紧急报警
@JsonClass(generateAdapter = true)
data class TriggerEmergencyRequest(
    val location: String? = null
)

@JsonClass(generateAdapter = true)
data class EmergencyAlert(
    val id: Long,
    val taskType: String,
    val status: String,
    val content: String,
    val createdAt: String
)

// Agent处理
@JsonClass(generateAdapter = true)
data class AgentProcessRequest(
    val userInput: String
)

@JsonClass(generateAdapter = true)
data class IntentTask(
    val type: String,
    val typeDescription: String,
    val description: String,
    val priority: String
)

@JsonClass(generateAdapter = true)
data class AgentProcessResponse(
    val intentType: String,
    val intentDescription: String,
    val userInput: String,
    val tasks: List<IntentTask>,
    val message: String,
    val createdTasks: List<Task>? = null,
    val aiResponse: String? = null
)

@JsonClass(generateAdapter = true)
data class AgentVoiceRequest(
    val audioBase64: String,
    val format: String = "wav",
    val rate: Int = 16000
)

// 意图识别
@JsonClass(generateAdapter = true)
data class IntentRecognitionResponse(
    val intentType: String,
    val intentDescription: String,
    val userInput: String,
    val tasks: List<IntentTask>
)

// 语音识别
@JsonClass(generateAdapter = true)
data class AsrRequest(
    val audioBase64: String,
    val format: String = "wav",
    val rate: Int = 16000
)

@JsonClass(generateAdapter = true)
data class AsrResponse(
    val text: String
)

// 积分
@JsonClass(generateAdapter = true)
data class PointsInfo(
    val total: Int
)

@JsonClass(generateAdapter = true)
data class PointsRecord(
    val id: Long,
    val amount: Int,
    val balanceAfter: Int,
    val reason: String,
    val reasonDescription: String,
    val taskId: Long? = null,
    val exchangeId: Long? = null,
    val createdAt: String
)

// 通知
@JsonClass(generateAdapter = true)
data class Notification(
    val id: Long,
    val title: String,
    val message: String,
    val isRead: Boolean,
    val createdAt: String
)

@JsonClass(generateAdapter = true)
data class UnreadCount(
    val unreadCount: Int
)

// AI聊天
@JsonClass(generateAdapter = true)
data class AskRequest(
    val question: String
)

@JsonClass(generateAdapter = true)
data class ChatHistory(
    val id: Long,
    val question: String,
    val answer: String,
    val createdAt: String
)

// 文件
@JsonClass(generateAdapter = true)
data class FileUploadResponse(
    val id: Long,
    val fileUrl: String,
    val originalFilename: String,
    val fileSize: Long,
    val contentType: String
)

@JsonClass(generateAdapter = true)
data class FileInfo(
    val id: Long,
    val fileUrl: String,
    val originalFilename: String,
    val fileSize: Long,
    val contentType: String,
    val fileType: String,
    val createdAt: String
)

