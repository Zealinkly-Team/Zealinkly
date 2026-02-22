package com.example.elderui.core.api

import com.squareup.moshi.JsonClass

/**
 * 统一API响应格式 - 三端共用
 */
@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T?
)

/**
 * 错误响应信息
 */
@JsonClass(generateAdapter = true)
data class ErrorResponse(
    val code: Int,
    val message: String
)

/**
 * 分页响应格式 - 三端共用
 */
@JsonClass(generateAdapter = true)
data class PaginatedResponse<T>(
    val content: List<T>,
    val totalElements: Int,
    val totalPages: Int,
    val currentPage: Int
)

