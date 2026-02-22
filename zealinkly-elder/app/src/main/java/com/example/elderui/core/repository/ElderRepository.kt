package com.example.elderui.core.repository

import com.example.elderui.core.api.*

/**
 * 老人端仓储
 */
class ElderRepository(private val elderApi: ElderApi) {
    // 任务相关
    suspend fun publishTask(title: String, description: String, pointsReward: Int): Result<Task> = try {
        val response = elderApi.publishTask(
            PublishTaskRequest(title, description, pointsReward)
        )
        if (response.code == 200 && response.data != null) {
            Result.success(response.data)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getMyTasks(): Result<List<Task>> = try {
        val response = elderApi.getMyTasks()
        if (response.code == 200 && response.data != null) {
            Result.success(response.data)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getTaskDetail(taskId: Long): Result<Task> = try {
        val response = elderApi.getTaskDetail(taskId)
        if (response.code == 200 && response.data != null) {
            Result.success(response.data)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun confirmTask(taskId: Long): Result<Task> = try {
        val response = elderApi.confirmTask(taskId)
        if (response.code == 200 && response.data != null) {
            Result.success(response.data)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun appealTask(taskId: Long, content: String): Result<Unit> = try {
        val response = elderApi.appealTask(taskId, AppealTaskRequest(content))
        if (response.code == 200) {
            Result.success(Unit)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    // 紧急报警
    suspend fun triggerEmergency(location: String? = null): Result<EmergencyAlert> = try {
        val response = elderApi.triggerEmergency(TriggerEmergencyRequest(location))
        if (response.code == 200 && response.data != null) {
            Result.success(response.data)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Agent处理
    suspend fun agentProcess(userInput: String): Result<AgentProcessResponse> = try {
        val response = elderApi.agentProcess(AgentProcessRequest(userInput))
        if (response.code == 200 && response.data != null) {
            Result.success(response.data)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun agentProcessVoice(audioBase64: String, format: String = "wav", rate: Int = 16000): Result<AgentProcessResponse> = try {
        val response = elderApi.agentProcessVoice(
            AgentVoiceRequest(audioBase64, format, rate)
        )
        if (response.code == 200 && response.data != null) {
            Result.success(response.data)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    // 意图识别
    suspend fun recognizeIntent(userInput: String): Result<IntentRecognitionResponse> = try {
        val response = elderApi.recognizeIntent(AgentProcessRequest(userInput))
        if (response.code == 200 && response.data != null) {
            Result.success(response.data)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    // 语音识别
    suspend fun recognizeAsr(audioBase64: String, format: String = "wav", rate: Int = 16000): Result<AsrResponse> = try {
        val response = elderApi.recognizeAsr(
            AsrRequest(audioBase64, format, rate)
        )
        if (response.code == 200 && response.data != null) {
            Result.success(response.data)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    // 积分
    suspend fun getPointsTotal(): Result<PointsInfo> = try {
        val response = elderApi.getPointsTotal()
        if (response.code == 200 && response.data != null) {
            Result.success(response.data)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getPointsHistory(): Result<List<PointsRecord>> = try {
        val response = elderApi.getPointsHistory()
        if (response.code == 200 && response.data != null) {
            Result.success(response.data)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    // 通知
    suspend fun getNotifications(): Result<List<Notification>> = try {
        val response = elderApi.getNotifications()
        if (response.code == 200 && response.data != null) {
            Result.success(response.data)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getUnreadCount(): Result<UnreadCount> = try {
        val response = elderApi.getUnreadCount()
        if (response.code == 200 && response.data != null) {
            Result.success(response.data)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun markNotificationAsRead(id: Long): Result<Unit> = try {
        val response = elderApi.markNotificationAsRead(id)
        if (response.code == 200) {
            Result.success(Unit)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun markAllNotificationsAsRead(): Result<Unit> = try {
        val response = elderApi.markAllNotificationsAsRead()
        if (response.code == 200) {
            Result.success(Unit)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    // AI聊天
    suspend fun ask(question: String): Result<String> = try {
        val response = elderApi.ask(AskRequest(question))
        if (response.code == 200 && response.data != null) {
            Result.success(response.data)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getChatHistory(): Result<List<ChatHistory>> = try {
        val response = elderApi.getChatHistory()
        if (response.code == 200 && response.data != null) {
            Result.success(response.data)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    // 文件
    suspend fun uploadFileBase64(base64Data: String, filename: String, contentType: String, relatedType: String? = null, relatedId: Long? = null): Result<FileUploadResponse> = try {
        val response = elderApi.uploadFileBase64(
            FileUploadBase64Request(base64Data, filename, contentType, relatedType, relatedId)
        )
        if (response.code == 200 && response.data != null) {
            Result.success(response.data)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getMyFiles(): Result<List<FileInfo>> = try {
        val response = elderApi.getMyFiles()
        if (response.code == 200 && response.data != null) {
            Result.success(response.data)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteFile(fileId: Long): Result<Unit> = try {
        val response = elderApi.deleteFile(fileId)
        if (response.code == 200) {
            Result.success(Unit)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

