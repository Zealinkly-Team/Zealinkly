package com.example.elderui.core.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.elderui.ElderApplication

@Composable
fun rememberAppViewModelFactory(): AppViewModelFactory {
    val app = LocalContext.current.applicationContext as ElderApplication
    return remember { AppViewModelFactory(app.appContainer) }
}

