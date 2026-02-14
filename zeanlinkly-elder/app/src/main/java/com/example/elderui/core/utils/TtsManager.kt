package com.example.elderui.core.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * 文本转语音工具类，用于播报 AI 的回复
 */
class TtsManager(context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val pendingText =  mutableListOf<String>()

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.CHINA)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // 语言不支持，尝试使用英语或默认
                    tts?.setLanguage(Locale.getDefault())
                }
                isInitialized = true
                // 播放之前队列中的文本
                if (pendingText.isNotEmpty()) {
                    pendingText.forEach { speak(it) }
                    pendingText.clear()
                }
            }
        }
    }

    fun speak(text: String?) {
        if (text.isNullOrBlank()) return

        if (isInitialized) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            pendingText.add(text)
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}

