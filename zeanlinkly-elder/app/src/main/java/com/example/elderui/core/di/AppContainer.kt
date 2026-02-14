package com.example.elderui.core.di

import android.content.Context
import com.example.elderui.core.api.ApiClientFactory
import com.example.elderui.core.repository.AuthRepository
import com.example.elderui.core.repository.ElderRepository
import com.example.elderui.core.repository.EmergencyContactRepository
import com.example.elderui.core.repository.UserRepository
import com.example.elderui.core.utils.LocationProvider
import com.example.elderui.core.utils.UserPreferences

class AppContainer(context: Context) {
    companion object {
        // 配置说明：
        // Android 模拟器访问本机 → http://10.0.2.2:8080
        // 本地开发电脑 → http://localhost:8080
        // 局域网其他设备 → http://192.168.1.x:8080（根据实际IP修改）
        // 远程服务器 → http://api.example.com

        // 当前配置：Android 模拟器模式
        // 如需修改，在下面选择合适的地址并取消注释

        const val BASE_URL = "http://10.0.2.2:8080"  // ✅ 模拟器连接本机

        // const val BASE_URL = "http://localhost:8080"  // 本地开发电脑
        // const val BASE_URL = "http://192.168.1.100:8080"  // 局域网（改为你的电脑IP）
//         const val BASE_URL = "https://43.143.226.28:8080"  // 远程服务器
    }

    val locationProvider = LocationProvider(context)
    val userPreferences = UserPreferences(context)

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
