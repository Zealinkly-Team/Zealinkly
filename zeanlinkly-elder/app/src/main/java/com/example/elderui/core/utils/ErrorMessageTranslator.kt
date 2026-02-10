package com.example.elderui.core.utils

/**
 * 错误消息翻译器 - 将 HTTP 错误和网络错误转换为中文提示
 */
object ErrorMessageTranslator {

    fun translateError(error: Throwable?): String {
        if (error == null) {
            return "未知错误，请重试"
        }

        val errorMessage = error.message ?: error.toString()

        return when {
            // 网络连接错误
            errorMessage.contains("Connection refused", ignoreCase = true) ->
                "连接失败，请检查服务器是否正常运行"

            errorMessage.contains("Connection timeout", ignoreCase = true) ||
            errorMessage.contains("timeout", ignoreCase = true) ->
                "连接超时，请检查网络连接"

            errorMessage.contains("Connection reset", ignoreCase = true) ->
                "连接被重置，请重试"

            errorMessage.contains("No network", ignoreCase = true) ||
            errorMessage.contains("network unavailable", ignoreCase = true) ->
                "网络不可用，请检查网络连接"

            // HTTP 错误代码
            errorMessage.contains("400", ignoreCase = true) ->
//                "请求参数错误，请检查输入内容"
                "用户名或密码错误"

            errorMessage.contains("401", ignoreCase = true) ||
            errorMessage.contains("Unauthorized", ignoreCase = true) ->
                "用户名或密码错误"

            errorMessage.contains("403", ignoreCase = true) ||
            errorMessage.contains("Forbidden", ignoreCase = true) ->
                "您没有权限访问此资源"

            errorMessage.contains("404", ignoreCase = true) ||
            errorMessage.contains("Not Found", ignoreCase = true) ->
                "请求的资源不存在"

            errorMessage.contains("500", ignoreCase = true) ||
            errorMessage.contains("Internal Server Error", ignoreCase = true) ->
                "服务器错误，请稍后重试"

            errorMessage.contains("502", ignoreCase = true) ||
            errorMessage.contains("Bad Gateway", ignoreCase = true) ->
                "网关错误，请稍后重试"

            errorMessage.contains("503", ignoreCase = true) ||
            errorMessage.contains("Service Unavailable", ignoreCase = true) ->
                "服务暂时不可用，请稍后重试"

            // 特定的业务错误
            errorMessage.contains("Invalid credentials", ignoreCase = true) ->
                "用户名或密码错误"

            errorMessage.contains("User not found", ignoreCase = true) ->
                "用户不存在，请检查用户名"

            errorMessage.contains("already exists", ignoreCase = true) ->
                "该用户已经存在，请使用其他用户名"

            errorMessage.contains("password", ignoreCase = true) ->
                "密码不符合要求，请使用8-16位字符"

            // 其他错误
            errorMessage.contains("JSON", ignoreCase = true) ->
                "数据格式错误，请重试"

            errorMessage.contains("SSL", ignoreCase = true) ||
            errorMessage.contains("Certificate", ignoreCase = true) ->
                "安全连接错误，请更新应用"

            else -> {
                // 如果包含中文，直接返回
                if (errorMessage.matches(Regex(".*\\p{InCJKUnifiedIdeographs}.*"))) {
                    errorMessage
                } else {
                    // 否则返回通用错误信息
                    "操作失败：${errorMessage.take(50)}"
                }
            }
        }
    }

    // 特定业务逻辑的错误翻译
    fun translateAuthError(message: String?): String {
        return when {
            message == null -> "登录失败，请重试"
            message.contains("username", ignoreCase = true) &&
            message.contains("empty", ignoreCase = true) ->
                "用户名不能为空"

            message.contains("password", ignoreCase = true) &&
            message.contains("empty", ignoreCase = true) ->
                "密码不能为空"

            message.contains("invalid", ignoreCase = true) ->
                "用户名或密码错误"

            message.contains("not found", ignoreCase = true) ->
                "用户不存在"

            message.contains("already", ignoreCase = true) ->
                "用户已存在，请使用其他用户名"

            else -> translateError(Exception(message))
        }
    }
}

