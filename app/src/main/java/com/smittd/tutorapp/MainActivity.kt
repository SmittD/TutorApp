package com.smittd.tutorapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smittd.tutorapp.ui.AdminPanelScreen
import com.smittd.tutorapp.ui.CreateGlobalMessageScreen
import com.smittd.tutorapp.ui.CreateMeetingScreen
import com.smittd.tutorapp.ui.CreateUserScreen
import com.smittd.tutorapp.ui.HomeScreen
import com.smittd.tutorapp.ui.LoginScreen
import com.smittd.tutorapp.ui.SplashScreen
import com.smittd.tutorapp.ui.theme.TutorAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TutorAppTheme {
                Surface {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "splash"
                    ) {
                        composable("splash") { SplashScreen(navController) }
                        composable("login") { LoginScreen(navController) }
                        composable("home") { HomeScreen(navController) }
                        composable("adminPanel") { AdminPanelScreen(navController) }
                        composable("createUserScreen") { CreateUserScreen() }
                        composable("createMeetingScreen") { CreateMeetingScreen(navController) }
                        composable("createGlobalMessageScreen") {
                            CreateGlobalMessageScreen(
                                navController
                            )
                        }
                    }
                }
            }
        }
    }
}