package com.example.elderui.core.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.elderui.core.api.*
import com.example.elderui.core.repository.ElderRepository
import com.example.elderui.core.utils.ErrorMessageTranslator
import com.example.elderui.core.utils.LocationProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 老人端ViewModel
 */

// 任务ViewModel
class TaskViewModel(private val repository: ElderRepository) : ViewModel() {
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    private val _currentTask = MutableStateFlow<Task?>(null)
    val currentTask: StateFlow<Task?> = _currentTask

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun getMyTasks() {
        viewModelScope.launch {
            _loading.value = true
            repository.getMyTasks().onSuccess {
                _tasks.value = it
                _error.value = null
            }.onFailure {
                _error.value = ErrorMessageTranslator.translateError(it)
            }
            _loading.value = false
        }
    }

    fun getTaskDetail(taskId: Long) {
        viewModelScope.launch {
            _loading.value = true
            repository.getTaskDetail(taskId).onSuccess {
                _currentTask.value = it
                _error.value = null
            }.onFailure {
                _error.value = ErrorMessageTranslator.translateError(it)
            }
            _loading.value = false
        }
    }

    fun publishTask(title: String, description: String, pointsReward: Int) {
        viewModelScope.launch {
            _loading.value = true
            repository.publishTask(title, description, pointsReward).onSuccess {
                _tasks.value = listOf(it) + _tasks.value
                _error.value = null
            }.onFailure {
                _error.value = ErrorMessageTranslator.translateError(it)
            }
            _loading.value = false
        }
    }

    fun confirmTask(taskId: Long) {
        viewModelScope.launch {
            _loading.value = true
            repository.confirmTask(taskId).onSuccess { updatedTask ->
                _tasks.value = _tasks.value.map { if (it.id == taskId) updatedTask else it }
                _currentTask.value = updatedTask
                _error.value = null
            }.onFailure {
                _error.value = ErrorMessageTranslator.translateError(it)
            }
            _loading.value = false
        }
    }

    fun appealTask(taskId: Long, content: String) {
        viewModelScope.launch {
            _loading.value = true
            repository.appealTask(taskId, content).onSuccess {
                _error.value = null
            }.onFailure {
                _error.value = ErrorMessageTranslator.translateError(it)
            }
            _loading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}

// 紧急报警ViewModel
class EmergencyViewModel(
    private val repository: ElderRepository,
    private val locationProvider: LocationProvider
) : ViewModel() {
    private val _alert = MutableStateFlow<EmergencyAlert?>(null)
    val alert: StateFlow<EmergencyAlert?> = _alert

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun triggerEmergency(location: String? = null) {
        viewModelScope.launch {
            _loading.value = true
            repository.triggerEmergency(location).onSuccess {
                _alert.value = it
                _error.value = null
            }.onFailure {
                _error.value = ErrorMessageTranslator.translateError(it)
            }
            _loading.value = false
        }
    }

    fun triggerEmergencyWithLocation() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val location = locationProvider.getLastKnownLocation()
                val locationString = location?.let { "${it.latitude},${it.longitude}" }
                triggerEmergency(locationString)
            } catch (e: Exception) {
                _error.value = "获取位置失败: ${e.message}"
                triggerEmergency(null) // 即使位置失败，也继续报警
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}

// Agent处理ViewModel
class AgentViewModel(private val repository: ElderRepository) : ViewModel() {
    private val _result = MutableStateFlow<AgentProcessResponse?>(null)
    val result: StateFlow<AgentProcessResponse?> = _result

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun processText(userInput: String) {
        viewModelScope.launch {
            _loading.value = true
            repository.agentProcess(userInput).onSuccess {
                _result.value = it
                _error.value = null
            }.onFailure {
                _error.value = ErrorMessageTranslator.translateError(it)
            }
            _loading.value = false
        }
    }

    fun processVoice(audioBase64: String, format: String = "wav", rate: Int = 16000) {
        viewModelScope.launch {
            _loading.value = true
            repository.agentProcessVoice(audioBase64, format, rate).onSuccess {
                _result.value = it
                _error.value = null
            }.onFailure {
                _error.value = ErrorMessageTranslator.translateError(it)
            }
            _loading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}

// 意图识别ViewModel
class IntentViewModel(private val repository: ElderRepository) : ViewModel() {
    private val _intent = MutableStateFlow<IntentRecognitionResponse?>(null)
    val intent: StateFlow<IntentRecognitionResponse?> = _intent

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun recognizeIntent(userInput: String) {
        viewModelScope.launch {
            _loading.value = true
            repository.recognizeIntent(userInput).onSuccess {
                _intent.value = it
                _error.value = null
            }.onFailure {
                _error.value = ErrorMessageTranslator.translateError(it)
            }
            _loading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}

// 语音识别ViewModel
class AsrViewModel(private val repository: ElderRepository) : ViewModel() {
    private val _text = MutableStateFlow<String?>(null)
    val text: StateFlow<String?> = _text

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun recognizeAsr(audioBase64: String, format: String = "wav", rate: Int = 16000) {
        viewModelScope.launch {
            _loading.value = true
            repository.recognizeAsr(audioBase64, format, rate).onSuccess {
                _text.value = it.text
                _error.value = null
            }.onFailure {
                _error.value = ErrorMessageTranslator.translateError(it)
            }
            _loading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}

// 积分ViewModel
class PointsViewModel(private val repository: ElderRepository) : ViewModel() {
    private val _total = MutableStateFlow<Int>(0)
    val total: StateFlow<Int> = _total

    private val _history = MutableStateFlow<List<PointsRecord>>(emptyList())
    val history: StateFlow<List<PointsRecord>> = _history

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun getPointsTotal() {
        viewModelScope.launch {
            _loading.value = true
            repository.getPointsTotal().onSuccess {
                _total.value = it.total
                _error.value = null
            }.onFailure {
                _error.value = ErrorMessageTranslator.translateError(it)
            }
            _loading.value = false
        }
    }

    fun getPointsHistory() {
        viewModelScope.launch {
            _loading.value = true
            repository.getPointsHistory().onSuccess {
                _history.value = it
                _error.value = null
            }.onFailure {
                _error.value = ErrorMessageTranslator.translateError(it)
            }
            _loading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}

// 通知ViewModel
class NotificationViewModel(private val repository: ElderRepository) : ViewModel() {
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun getNotifications() {
        viewModelScope.launch {
            _loading.value = true
            repository.getNotifications().onSuccess {
                _notifications.value = it
                _error.value = null
            }.onFailure {
                _error.value = ErrorMessageTranslator.translateError(it)
            }
            _loading.value = false
        }
    }

    fun getUnreadCount() {
        viewModelScope.launch {
            repository.getUnreadCount().onSuccess {
                _unreadCount.value = it.unreadCount
            }.onFailure {
                _error.value = ErrorMessageTranslator.translateError(it)
            }
        }
    }

    fun markAsRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id).onSuccess {
                _notifications.value = _notifications.value.map {
                    if (it.id == id) it.copy(isRead = true) else it
                }
            }.onFailure {
                _error.value = ErrorMessageTranslator.translateError(it)
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead().onSuccess {
                _notifications.value = _notifications.value.map { it.copy(isRead = true) }
            }.onFailure {
                _error.value = ErrorMessageTranslator.translateError(it)
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}

// AI聊天ViewModel
class ChatViewModel(private val repository: ElderRepository) : ViewModel() {
    private val _history = MutableStateFlow<List<ChatHistory>>(emptyList())
    val history: StateFlow<List<ChatHistory>> = _history

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun ask(question: String) {
        viewModelScope.launch {
            _loading.value = true
            repository.ask(question).onSuccess { answer ->
                val chatEntry = ChatHistory(
                    id = 0,
                    question = question,
                    answer = answer,
                    createdAt = System.currentTimeMillis().toString()
                )
                _history.value = listOf(chatEntry) + _history.value
                _error.value = null
            }.onFailure {
                _error.value = ErrorMessageTranslator.translateError(it)
            }
            _loading.value = false
        }
    }

    fun getChatHistory() {
        viewModelScope.launch {
            _loading.value = true
            repository.getChatHistory().onSuccess {
                _history.value = it
                _error.value = null
            }.onFailure {
                _error.value = ErrorMessageTranslator.translateError(it)
            }
            _loading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}

// 文件ViewModel
class FileViewModel(private val repository: ElderRepository) : ViewModel() {
    private val _files = MutableStateFlow<List<FileInfo>>(emptyList())
    val files: StateFlow<List<FileInfo>> = _files

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun getMyFiles() {
        viewModelScope.launch {
            _loading.value = true
            repository.getMyFiles().onSuccess {
                _files.value = it
                _error.value = null
            }.onFailure {
                _error.value = ErrorMessageTranslator.translateError(it)
            }
            _loading.value = false
        }
    }

    fun uploadFile(base64Data: String, filename: String, contentType: String, relatedType: String? = null, relatedId: Long? = null) {
        viewModelScope.launch {
            _loading.value = true
            repository.uploadFileBase64(base64Data, filename, contentType, relatedType, relatedId).onSuccess { fileUploadResponse ->
                val fileInfo = FileInfo(
                    id = fileUploadResponse.id,
                    fileUrl = fileUploadResponse.fileUrl,
                    originalFilename = fileUploadResponse.originalFilename,
                    fileSize = fileUploadResponse.fileSize,
                    contentType = fileUploadResponse.contentType,
                    fileType = "UNKNOWN",
                    createdAt = System.currentTimeMillis().toString()
                )
                _files.value = listOf(fileInfo) + _files.value
                _error.value = null
            }.onFailure {
                _error.value = ErrorMessageTranslator.translateError(it)
            }
            _loading.value = false
        }
    }

    fun deleteFile(fileId: Long) {
        viewModelScope.launch {
            _loading.value = true
            repository.deleteFile(fileId).onSuccess {
                _files.value = _files.value.filter { it.id != fileId }
                _error.value = null
            }.onFailure {
                _error.value = ErrorMessageTranslator.translateError(it)
            }
            _loading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}
