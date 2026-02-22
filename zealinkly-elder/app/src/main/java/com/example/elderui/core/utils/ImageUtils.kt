package com.example.elderui.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.InputStream

object ImageUtils {
    fun uriToBase64(context: Context, uri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            bitmapToBase64(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        // 压缩图片，JPEG 格式，质量 80%
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val bytes = outputStream.toByteArray()
        // 这里添加 data prefix 还是 API 自动处理？
        // 根据文档：图片需要Base64编码，包含data URI前缀（如：`data:image/jpeg;base64,`）
        return "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}

