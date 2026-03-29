package com.example.mobildrontesztprojekt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mobildrontesztprojekt.ui.screen.*
import com.example.mobildrontesztprojekt.ui.theme.DroneTechTheme
import com.example.mobildrontesztprojekt.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DroneTechTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel()

                NavHost(navController = navController, startDestination = "splash") {

                    composable("splash") {
                        SplashScreen(onNavigateToLogin = {
                            navController.navigate("login") {
                                popUpTo("splash") { inclusive = true }
                            }
                        })
                    }

                    composable("login") {
                        LoginScreen(
                            viewModel = authViewModel,
                            onLoginSuccess = {
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("home") {
                        HomeScreen(
                            viewModel = authViewModel,
                            onLogout = {
                                navController.navigate("login") {
                                    popUpTo("home") { inclusive = true }
                                }
                            },
                            onNavigate = { route -> navController.navigate(route) }
                        )
                    }

                    composable("admin") {
                        AdminScreen(onBack = { navController.popBackStack() })
                    }

                    composable("manager") {
                        ManagerScreen(onBack = { navController.popBackStack() })
                    }

                    composable("technical") {
                        TechnicalScreen(onBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}
