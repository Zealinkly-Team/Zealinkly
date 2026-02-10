package com.example.elderui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import com.example.elderui.ui.navigation.ElderNavigation
import com.example.elderui.ui.theme.ElderUITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ElderUITheme {
                ElderNavigation()
            }
        }
    }
}
