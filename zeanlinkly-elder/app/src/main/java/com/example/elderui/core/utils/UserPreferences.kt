package com.example.elderui.core.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class LoginMethod {
    ACCOUNT,
    CARD
}

class UserPreferences(private val context: Context) {
    companion object {
        private val LOGIN_METHOD_KEY = stringPreferencesKey("login_method")
    }

    val loginMethod: Flow<LoginMethod> = context.settingsDataStore.data.map { preferences ->
        try {
            val methodStr = preferences[LOGIN_METHOD_KEY]
            if (methodStr != null) {
                LoginMethod.valueOf(methodStr)
            } else {
                LoginMethod.ACCOUNT // 默认账号密码登录
            }
        } catch (e: Exception) {
            LoginMethod.ACCOUNT
        }
    }

    suspend fun saveLoginMethod(method: LoginMethod) {
        context.settingsDataStore.edit { preferences ->
            preferences[LOGIN_METHOD_KEY] = method.name
        }
    }
}

