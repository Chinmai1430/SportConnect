package com.chinmaib.sportconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chinmaib.sportconnect.auth.AuthScreen
import com.chinmaib.sportconnect.auth.SplashScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "splash"
                    ) {

                        // Phase 1: Boot Sequence
                        composable("splash") {
                            SplashScreen(
                                onSplashComplete = {
                                    navController.navigate("auth") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // Phase 2: Secure Gateway
                        composable("auth") {
                            AuthScreen(
                                onAuthSuccess = { isLogin ->
                                    if (isLogin) {
                                        println("SYSTEM: Returning user identified. Routing directly to Main Dashboard.")
                                        // navController.navigate("dashboard")
                                    } else {
                                        println("SYSTEM: New user identified. Routing to Profile Setup.")
                                        // navController.navigate("profile_setup")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}