package com.example.elderui.core.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.elderui.core.api.*
import com.example.elderui.core.repository.AuthRepository
import com.example.elderui.core.repository.UserRepository
import com.example.elderui.core.repository.EmergencyContactRepository
import com.example.elderui.core.utils.ErrorMessageTranslator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 认证ViewModel - 三端共用基类
 */
open class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            authRepository.login(username, password).onSuccess {
                _loginState.value = LoginState.Success(it)
            }.onFailure { error ->
                // 使用中文错误消息
                val chineseErrorMessage = ErrorMessageTranslator.translateError(error)
                _loginState.value = LoginState.Error(chineseErrorMessage)
            }
        }
    }

    fun register(username: String, password: String, realName: String, phone: String) {
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading
            authRepository.register(username, password, realName, phone).onSuccess {
                _registerState.value = RegisterState.Success(it)
            }.onFailure { error ->
                // 使用中文错误消息
                val chineseErrorMessage = ErrorMessageTranslator.translateError(error)
                _registerState.value = RegisterState.Error(chineseErrorMessage)
            }
        }
    }

    fun loginByCard(cardImageBase64: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            authRepository.loginByCard(cardImageBase64).onSuccess {
                _loginState.value = LoginState.Success(it)
            }.onFailure { error ->
                // 使用中文错误消息
                val chineseErrorMessage = ErrorMessageTranslator.translateError(error)
                _loginState.value = LoginState.Error(chineseErrorMessage)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _loginState.value = LoginState.Idle
        }
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val data: LoginResponse) : LoginState()
    data class Error(val message: String) : LoginState()
}

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    data class Success(val data: RegisterResponse) : RegisterState()
    data class Error(val message: String) : RegisterState()
}

/**
 * 用户ViewModel - 三端共用基类
 */
open class UserViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val _userInfo = MutableStateFlow<UserInfo?>(null)
    val userInfo: StateFlow<UserInfo?> = _userInfo

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun getUserInfo() {
        viewModelScope.launch {
            _loading.value = true
            userRepository.getUserInfo().onSuccess {
                _userInfo.value = it
                _error.value = null
            }.onFailure {
                _error.value = it.message ?: "获取用户信息失败"
            }
            _loading.value = false
        }
    }

    fun updateUserInfo(realName: String?, phone: String?, address: String?) {
        viewModelScope.launch {
            _loading.value = true
            userRepository.updateUserInfo(realName, phone, address).onSuccess {
                _userInfo.value = it
                _error.value = null
            }.onFailure {
                _error.value = it.message ?: "更新用户信息失败"
            }
            _loading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}

/**
 * 紧急联系人ViewModel - 三端共用基类
 */
open class EmergencyContactViewModel(private val repository: EmergencyContactRepository) : ViewModel() {
    private val _contacts = MutableStateFlow<List<EmergencyContact>>(emptyList())
    val contacts: StateFlow<List<EmergencyContact>> = _contacts

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun getEmergencyContacts() {
        viewModelScope.launch {
            _loading.value = true
            repository.getEmergencyContacts().onSuccess {
                _contacts.value = it
                _error.value = null
            }.onFailure {
                _error.value = it.message ?: "获取联系人列表失败"
            }
            _loading.value = false
        }
    }

    fun addEmergencyContact(name: String, relation: String, phone: String, priority: Int) {
        viewModelScope.launch {
            _loading.value = true
            repository.addEmergencyContact(name, relation, phone, priority).onSuccess {
                _contacts.value = _contacts.value + it
                _error.value = null
            }.onFailure {
                _error.value = it.message ?: "添加联系人失败"
            }
            _loading.value = false
        }
    }

    fun updateEmergencyContact(id: Long, name: String, relation: String, phone: String, priority: Int) {
        viewModelScope.launch {
            _loading.value = true
            repository.updateEmergencyContact(id, name, relation, phone, priority).onSuccess { updated ->
                _contacts.value = _contacts.value.map { if (it.id == id) updated else it }
                _error.value = null
            }.onFailure {
                _error.value = it.message ?: "更新联系人失败"
            }
            _loading.value = false
        }
    }

    fun deleteEmergencyContact(id: Long) {
        viewModelScope.launch {
            _loading.value = true
            repository.deleteEmergencyContact(id).onSuccess {
                _contacts.value = _contacts.value.filter { it.id != id }
                _error.value = null
            }.onFailure {
                _error.value = it.message ?: "删除联系人失败"
            }
            _loading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}

