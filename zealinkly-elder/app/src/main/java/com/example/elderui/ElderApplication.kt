package com.example.elderui

import android.app.Application
import com.example.elderui.core.api.ApiClientFactory

/**
 * 应用程序入口点
 */
class ElderApplication : Application() {
    companion object {
        private lateinit var instance: ElderApplication

        fun getInstance(): ElderApplication = instance
    }

    private lateinit var apiClientFactory: ApiClientFactory

    override fun onCreate() {
        super.onCreate()
        instance = this
        initializeApiClient()
    }

    private fun initializeApiClient() {
        apiClientFactory = ApiClientFactory(this)
    }

    fun getApiClientFactory(): ApiClientFactory = apiClientFactory

    val appContainer: com.example.elderui.core.di.AppContainer by lazy {
        com.example.elderui.core.di.AppContainer(this)
    }
}
