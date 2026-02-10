package com.example.elderui.core.di

import android.content.Context
import com.example.elderui.core.api.ApiClientFactory
import com.example.elderui.core.repository.AuthRepository
import com.example.elderui.core.repository.ElderRepository
import com.example.elderui.core.repository.EmergencyContactRepository
import com.example.elderui.core.repository.UserRepository

class AppContainer(context: Context) {
    companion object {
        // Emulator uses 10.0.2.2 for host loopback.
        const val BASE_URL = "http://10.0.2.2:8080"
    }

    private val apiClientFactory = ApiClientFactory(context)
    private val retrofit = apiClientFactory.createRetrofit(BASE_URL)

    private val authApi = apiClientFactory.createAuthApi(retrofit)
    private val userApi = apiClientFactory.createUserApi(retrofit)
    private val emergencyContactApi = apiClientFactory.createEmergencyContactApi(retrofit)
    private val elderApi = apiClientFactory.createElderApi(retrofit)

    val authRepository = AuthRepository(authApi, apiClientFactory.getTokenStore())
    val userRepository = UserRepository(userApi)
    val emergencyContactRepository = EmergencyContactRepository(emergencyContactApi)
    val elderRepository = ElderRepository(elderApi)
}

