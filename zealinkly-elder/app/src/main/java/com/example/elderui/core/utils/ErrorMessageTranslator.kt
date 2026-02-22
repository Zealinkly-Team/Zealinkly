package com.example.elderui.core.utils

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName
import retrofit2.HttpException
import java.io.IOException

/**
 * 错误消息翻译器 - 将 HTTP 错误和网络错误转换为中文提示
 */
object ErrorMessageTranslator {

    // 用于解析后端可能返回的JSON错误结构
    private data class ErrorResponse(
        @SerializedName("message") val message: String?
    )

    fun translateError(error: Throwable?): String {
        if (error == null) {
            return "未知错误，请重试"
        }

        // 优先处理 HttpException，因为业务异常通常封装在里面
        if (error is HttpException) {
            val response = error.response()
            // errorBody().string() 只能被成功读取一次，所以我们先读出来保存
            val errorBodyString = try {
                response?.errorBody()?.string()
            } catch (e: IOException) {
                null
            }

            if (!errorBodyString.isNullOrBlank()) {
                // 1. 尝试按JSON格式解析，提取 "message" 字段
                try {
                    val errorResponse = Gson().fromJson(errorBodyString, ErrorResponse::class.java)
                    if (!errorResponse.message.isNullOrBlank()) {
                        return errorResponse.message
                    }
                } catch (e: JsonSyntaxException) {
                    // 如果不是标准JSON，则继续往下走
                }

                // 2. 如果JSON解析失败，但内容包含中文，则尝试作为纯文本处理
                //    这能处理 "业务异常: 您的积分余额不足..." 和 "code:400,message:用户名或密码错误..." 这两种情况
                if (errorBodyString.matches(Regex(".*\\p{InCJKUnifiedIdeographs}.*"))) {
                    return errorBodyString
                        .replace(Regex("""^.*业务异常:\s*"""), "") // 移除 "业务异常: " 前缀
                        .replace(Regex("""^.*message:\s*"""), "") // 移除 "message: " 前缀
                        .replace(Regex(""",data:null.*$"""), "") // 移除末尾的 ",data:null"
                        .replace(Regex("""["'{}]"""), "") // 移除各种括号
                        .trim()
                }
            }
        }

        // 如果以上都失败，则回退到基于 Exception message 和通用代码的翻译
        val errorMessage = error.message ?: error.toString()

        return when {
            // 网络连接错误
            errorMessage.contains("Connection refused", ignoreCase = true) -> "连接失败，请检查服务器是否正常运行"
            errorMessage.contains("Connection timeout", ignoreCase = true) || errorMessage.contains("timeout", ignoreCase = true) -> "连接超时，请检查网络连接"
            errorMessage.contains("Connection reset", ignoreCase = true) -> "连接被重置，请重试"
            errorMessage.contains("No network", ignoreCase = true) || errorMessage.contains("network unavailable", ignoreCase = true) -> "网络不可用，请检查网络连接"

            // HTTP 错误代码
            errorMessage.contains("400", ignoreCase = true) -> "请求无效，请检查您的操作或输入"
            errorMessage.contains("401", ignoreCase = true) || errorMessage.contains("Unauthorized", ignoreCase = true) -> "认证失败，请重新登录"
            errorMessage.contains("403", ignoreCase = true) || errorMessage.contains("Forbidden", ignoreCase = true) -> "您没有权限执行此操作"
            errorMessage.contains("404", ignoreCase = true) || errorMessage.contains("Not Found", ignoreCase = true) -> "请求的资源不存在"
            errorMessage.contains("500", ignoreCase = true) || errorMessage.contains("Internal Server Error", ignoreCase = true) -> "服务器内部错误，请稍后重试"
            errorMessage.contains("502", ignoreCase = true) || errorMessage.contains("Bad Gateway", ignoreCase = true) -> "网关错误，请稍后重试"
            errorMessage.contains("503", ignoreCase = true) || errorMessage.contains("Service Unavailable", ignoreCase = true) -> "服务暂时不可用，请稍后重试"

            // 其他错误
            errorMessage.contains("JSON", ignoreCase = true) -> "数据格式错误，请重试"
            errorMessage.contains("SSL", ignoreCase = true) || errorMessage.contains("Certificate", ignoreCase = true) -> "安全连接错误，请更新应用"

            else -> {
                // 如果 errorMessage 本身就包含中文，直接返回
                if (errorMessage.matches(Regex(".*\\p{InCJKUnifiedIdeographs}.*"))) {
                    errorMessage
                } else {
                    // 否则返回最终的通用错误信息
                    "操作失败，请稍后重试"
                }
            }
        }
    }

    // 特定业务逻辑的错误翻译 (保持不变)
    fun translateAuthError(message: String?): String {
        return when {
            message == null -> "登录失败，请重试"
            message.contains("username", ignoreCase = true) && message.contains("empty", ignoreCase = true) -> "用户名不能为空"
            message.contains("password", ignoreCase = true) && message.contains("empty", ignoreCase = true) -> "密码不能为空"
            message.contains("invalid", ignoreCase = true) -> "用户名或密码错误"
            message.contains("not found", ignoreCase = true) -> "用户不存在"
            message.contains("already", ignoreCase = true) -> "用户已存在，请使用其他用户名"
            else -> translateError(Exception(message))
        }
    }
}

