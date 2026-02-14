package com.example.elderui.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.elderui.ui.screen.LoginScreen
import com.example.elderui.ui.screen.HomeScreen

/**
 * 老人端导航
 */
@Composable
fun ElderNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("home") {
            HomeScreen(onLogout = {
                navController.navigate("login") {
                    popUpTo("home") { inclusive = true }
                }
            })
        }
    }
}

object ElderRoute {
    const val Login = "login"
    const val Register = "register"
    const val Home = "home"
    const val Tasks = "tasks"
    const val TaskDetail = "task_detail"
    const val Profile = "profile"
    const val Points = "points"
    const val Notifications = "notifications"
    const val Chat = "chat"
    const val EmergencyContacts = "emergency_contacts"
}



