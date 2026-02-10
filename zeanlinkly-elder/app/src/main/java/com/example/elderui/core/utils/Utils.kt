package com.example.elderui.core.utils

import android.util.Base64
import java.io.File

/**
 * 文件工具 - 三端共用
 */
object FileUtils {
    /**
     * 将文件转换为Base64字符串
     */
    fun fileToBase64(file: File): String {
        val bytes = file.readBytes()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    /**
     * 从Base64字符串恢复文件
     */
    fun base64ToFile(base64String: String, outputFile: File): Boolean {
        return try {
            val bytes = Base64.decode(base64String, Base64.DEFAULT)
            outputFile.writeBytes(bytes)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 获取文件MIME类型
     */
    fun getMimeType(filename: String): String {
        return when {
            filename.endsWith(".jpg") || filename.endsWith(".jpeg") -> "image/jpeg"
            filename.endsWith(".png") -> "image/png"
            filename.endsWith(".gif") -> "image/gif"
            filename.endsWith(".wav") || filename.endsWith(".mp3") -> "audio/mpeg"
            filename.endsWith(".pdf") -> "application/pdf"
            filename.endsWith(".txt") -> "text/plain"
            else -> "application/octet-stream"
        }
    }
}

/**
 * 日期工具 - 三端共用
 */
object DateUtils {
    /**
     * 格式化日期时间
     */
    fun formatDateTime(timestamp: Long): String {
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.CHINA)
        return format.format(date)
    }

    /**
     * 格式化日期
     */
    fun formatDate(timestamp: Long): String {
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.CHINA)
        return format.format(date)
    }

    /**
     * 获取相对时间描述
     */
    fun getRelativeTimeDescription(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60 * 1000 -> "刚刚"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}分钟前"
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}小时前"
            diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)}天前"
            else -> formatDate(timestamp)
        }
    }
}

/**
 * 音频工具 - 三端共用
 */
object AudioUtils {
    /**
     * PCM转WAV
     */
    fun pcmToWav(pcmData: ByteArray, sampleRate: Int, channels: Int, sampleBits: Int): ByteArray {
        val wavHeader = createWavHeader(pcmData.size, sampleRate, channels, sampleBits)
        return wavHeader + pcmData
    }

    private fun createWavHeader(dataSize: Int, sampleRate: Int, channels: Int, sampleBits: Int): ByteArray {
        val header = ByteArray(44)
        val byteRate = sampleRate * channels * sampleBits / 8

        // RIFF header
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()

        // File size - 8
        val fileSize = dataSize + 36
        header[4] = (fileSize and 0xFF).toByte()
        header[5] = ((fileSize shr 8) and 0xFF).toByte()
        header[6] = ((fileSize shr 16) and 0xFF).toByte()
        header[7] = ((fileSize shr 24) and 0xFF).toByte()

        // WAVE header
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()

        // fmt sub-chunk
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()

        // Sub-chunk size
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0

        // Audio format (PCM)
        header[20] = 1
        header[21] = 0

        // Channels
        header[22] = channels.toByte()
        header[23] = 0

        // Sample rate
        header[24] = (sampleRate and 0xFF).toByte()
        header[25] = ((sampleRate shr 8) and 0xFF).toByte()
        header[26] = ((sampleRate shr 16) and 0xFF).toByte()
        header[27] = ((sampleRate shr 24) and 0xFF).toByte()

        // Byte rate
        header[28] = (byteRate and 0xFF).toByte()
        header[29] = ((byteRate shr 8) and 0xFF).toByte()
        header[30] = ((byteRate shr 16) and 0xFF).toByte()
        header[31] = ((byteRate shr 24) and 0xFF).toByte()

        // Block align
        val blockAlign = channels * sampleBits / 8
        header[32] = blockAlign.toByte()
        header[33] = 0

        // Bits per sample
        header[34] = sampleBits.toByte()
        header[35] = 0

        // data sub-chunk
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()

        // Data size
        header[40] = (dataSize and 0xFF).toByte()
        header[41] = ((dataSize shr 8) and 0xFF).toByte()
        header[42] = ((dataSize shr 16) and 0xFF).toByte()
        header[43] = ((dataSize shr 24) and 0xFF).toByte()

        return header
    }
}

/**
 * 验证工具 - 三端共用
 */
object ValidationUtils {
    /**
     * 验证手机号
     */
    fun isValidPhone(phone: String): Boolean {
        return phone.matches(Regex("^1[3-9]\\d{9}$"))
    }

    /**
     * 验证邮箱
     */
    fun isValidEmail(email: String): Boolean {
        return email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"))
    }

    /**
     * 验证用户名
     */
    fun isValidUsername(username: String): Boolean {
        return username.length >= 3 && username.length <= 20 && username.matches(Regex("^[a-zA-Z0-9_]*$"))
    }

    /**
     * 验证密码强度
     */
    fun isStrongPassword(password: String): Boolean {
        return password.length >= 6 && password.matches(Regex("^(?=.*[a-zA-Z])(?=.*\\d).{6,}$"))
    }
}

