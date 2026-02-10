package com.example.elderui.core.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.elderui.core.viewmodel.*

class AppViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) ->
                AuthViewModel(container.authRepository)
            modelClass.isAssignableFrom(UserViewModel::class.java) ->
                UserViewModel(container.userRepository)
            modelClass.isAssignableFrom(EmergencyContactViewModel::class.java) ->
                EmergencyContactViewModel(container.emergencyContactRepository)
            modelClass.isAssignableFrom(TaskViewModel::class.java) ->
                TaskViewModel(container.elderRepository)
            modelClass.isAssignableFrom(EmergencyViewModel::class.java) ->
                EmergencyViewModel(container.elderRepository)
            modelClass.isAssignableFrom(AgentViewModel::class.java) ->
                AgentViewModel(container.elderRepository)
            modelClass.isAssignableFrom(IntentViewModel::class.java) ->
                IntentViewModel(container.elderRepository)
            modelClass.isAssignableFrom(AsrViewModel::class.java) ->
                AsrViewModel(container.elderRepository)
            modelClass.isAssignableFrom(PointsViewModel::class.java) ->
                PointsViewModel(container.elderRepository)
            modelClass.isAssignableFrom(NotificationViewModel::class.java) ->
                NotificationViewModel(container.elderRepository)
            modelClass.isAssignableFrom(ChatViewModel::class.java) ->
                ChatViewModel(container.elderRepository)
            modelClass.isAssignableFrom(FileViewModel::class.java) ->
                FileViewModel(container.elderRepository)
            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        } as T
    }
}

