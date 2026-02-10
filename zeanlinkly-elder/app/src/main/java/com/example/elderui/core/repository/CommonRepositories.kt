package com.example.elderui.core.repository

import com.example.elderui.core.api.*

/**
 * 认证仓储 - 三端共用
 */
class AuthRepository(
    private val authApi: AuthApi,
    private val tokenStore: TokenStore
) {
    suspend fun login(username: String, password: String): Result<LoginResponse> = try {
        val response = authApi.login(LoginRequest(username, password))
        if (response.code == 200 && response.data != null) {
            tokenStore.saveToken(response.data.token, response.data.userId.toString(), response.data.userType)
            Result.success(response.data)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun register(username: String, password: String, realName: String, phone: String): Result<RegisterResponse> = try {
        val response = authApi.registerElder(
            RegisterRequest(username, password, realName, phone)
        )
        if (response.code == 200 && response.data != null) {
            Result.success(response.data)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun loginByCard(cardImageBase64: String): Result<LoginResponse> = try {
        val response = authApi.loginByCard(CardLoginRequest(cardImageBase64))
        if (response.code == 200 && response.data != null) {
            tokenStore.saveToken(response.data.token, response.data.userId.toString(), response.data.userType)
            Result.success(response.data)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun logout() {
        tokenStore.clearToken()
    }
}

/**
 * 用户仓储 - 三端共用
 */
class UserRepository(private val userApi: UserApi) {
    suspend fun getUserInfo(): Result<UserInfo> = try {
        val response = userApi.getUserInfo()
        if (response.code == 200 && response.data != null) {
            Result.success(response.data)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateUserInfo(realName: String?, phone: String?, address: String?): Result<UserInfo> = try {
        val response = userApi.updateUserInfo(
            UpdateUserRequest(realName, phone, address)
        )
        if (response.code == 200 && response.data != null) {
            Result.success(response.data)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/**
 * 紧急联系人仓储 - 三端共用
 */
class EmergencyContactRepository(private val api: EmergencyContactApi) {
    suspend fun getEmergencyContacts(): Result<List<EmergencyContact>> = try {
        val response = api.getEmergencyContacts()
        if (response.code == 200 && response.data != null) {
            Result.success(response.data)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun addEmergencyContact(name: String, relation: String, phone: String, priority: Int): Result<EmergencyContact> = try {
        val response = api.addEmergencyContact(
            CreateEmergencyContactRequest(name, relation, phone, priority)
        )
        if (response.code == 200 && response.data != null) {
            Result.success(response.data)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateEmergencyContact(id: Long, name: String, relation: String, phone: String, priority: Int): Result<EmergencyContact> = try {
        val response = api.updateEmergencyContact(
            id,
            CreateEmergencyContactRequest(name, relation, phone, priority)
        )
        if (response.code == 200 && response.data != null) {
            Result.success(response.data)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteEmergencyContact(id: Long): Result<Unit> = try {
        val response = api.deleteEmergencyContact(id)
        if (response.code == 200) {
            Result.success(Unit)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

