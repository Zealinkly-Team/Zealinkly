package com.example.elderui.core.api

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")

/**
 * 令牌存储 - 三端共用
 */
class TokenStore(private val context: Context) {
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_TYPE_KEY = stringPreferencesKey("user_type")
    }

    val token: Flow<String?> = context.dataStore.data.map { it[TOKEN_KEY] }
    val userId: Flow<String?> = context.dataStore.data.map { it[USER_ID_KEY] }
    val userType: Flow<String?> = context.dataStore.data.map { it[USER_TYPE_KEY] }

    suspend fun saveToken(token: String, userId: String, userType: String) {
        context.dataStore.edit {
            it[TOKEN_KEY] = token
            it[USER_ID_KEY] = userId
            it[USER_TYPE_KEY] = userType
        }
    }

    suspend fun clearToken() {
        context.dataStore.edit {
            it.remove(TOKEN_KEY)
            it.remove(USER_ID_KEY)
            it.remove(USER_TYPE_KEY)
        }
    }
}

/**
 * 授权拦截器 - 三端共用
 */
class AuthInterceptor(private val tokenStore: TokenStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val originalRequest = chain.request()

        // 检查是否需要授权（登录和注册请求不需要token）
        if (originalRequest.url.encodedPath.contains("/auth/login") ||
            originalRequest.url.encodedPath.contains("/auth/register")) {
            return chain.proceed(originalRequest)
        }

        // 同步获取token（使用runBlocking）
        val token = runBlocking {
            tokenStore.token.first()
        }

        // 如果有token，添加到请求头
        val requestWithAuth = if (token != null) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(requestWithAuth)
    }
}

/**
 * API客户端工厂 - 三端共用
 */
class ApiClientFactory(private val context: Context) {
    private val tokenStore = TokenStore(context)

    fun createOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(AuthInterceptor(tokenStore))
            .build()
    }

    fun createRetrofit(baseUrl: String): Retrofit {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(createOkHttpClient())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    fun createAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    fun createUserApi(retrofit: Retrofit): UserApi {
        return retrofit.create(UserApi::class.java)
    }

    fun createEmergencyContactApi(retrofit: Retrofit): EmergencyContactApi {
        return retrofit.create(EmergencyContactApi::class.java)
    }

    fun createElderApi(retrofit: Retrofit): ElderApi {
        return retrofit.create(ElderApi::class.java)
    }

    fun getTokenStore(): TokenStore = tokenStore
}

