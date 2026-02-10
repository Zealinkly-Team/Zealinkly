package com.example.elderui.core.api

import com.squareup.moshi.JsonClass
import java.time.LocalDateTime

/**
 * 认证相关数据模型 - 三端共用
 */

@JsonClass(generateAdapter = true)
data class LoginRequest(
    val username: String,
    val password: String,
    val userType: String = "elder"  // 老人端默认为 elder
)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    val token: String,
    val userType: String,
    val userId: Long
)

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    val username: String,
    val password: String,
    val realName: String,
    val phone: String,
    val userType: String = "elder"  // 老人端默认为 elder
)

@JsonClass(generateAdapter = true)
data class RegisterResponse(
    val id: Long,
    val username: String,
    val userType: String
)

@JsonClass(generateAdapter = true)
data class CardLoginRequest(
    val cardImageBase64: String,
    val userType: String = "elder"  // 老人端默认为 elder
)

/**
 * 用户相关数据模型 - 三端共用
 */
@JsonClass(generateAdapter = true)
data class UserInfo(
    val id: Long,
    val username: String,
    val realName: String,
    val phone: String,
    val address: String?,
    val points: Int,
    val userType: String
)

@JsonClass(generateAdapter = true)
data class UpdateUserRequest(
    val realName: String? = null,
    val phone: String? = null,
    val address: String? = null
)

/**
 * 紧急联系人 - 三端共用
 */
@JsonClass(generateAdapter = true)
data class EmergencyContact(
    val id: Long,
    val name: String,
    val relation: String,
    val phone: String,
    val priority: Int
)

@JsonClass(generateAdapter = true)
data class CreateEmergencyContactRequest(
    val name: String,
    val relation: String,
    val phone: String,
    val priority: Int
)

