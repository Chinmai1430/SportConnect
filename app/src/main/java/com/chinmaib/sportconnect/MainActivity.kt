@file:Suppress("SpellCheckingInspection")
package com.chinmaib.sportconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chinmaib.sportconnect.auth.AuthScreen
import com.chinmaib.sportconnect.auth.ProfileSetupScreen
import com.chinmaib.sportconnect.auth.SplashScreen
import com.chinmaib.sportconnect.ui.theme.SportConnectTheme
import dagger.hilt.android.AndroidEntryPoint
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            SportConnectTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    SportConnectNavigation()
                }
            }
        }
    }
}

@Composable
fun SportConnectNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {

        composable("splash") {
            SplashScreen {
                navController.navigate("auth") {
                    popUpTo("splash") { inclusive = true }
                }
            }
        }

        composable("auth") {
            AuthScreen { isLoginMode, userName ->
                if (isLoginMode) {
                    navController.navigate("home") {
                        popUpTo("auth") { inclusive = true }
                    }
                } else {
                    // Safely encode the name for URL transit
                    val safeName = if (userName.isNotBlank()) URLEncoder.encode(userName, StandardCharsets.UTF_8.toString()) else "Athlete"
                    navController.navigate("profile_setup/$safeName")
                }
            }
        }

        composable("profile_setup/{userName}") { backStackEntry ->
            val encodedName = backStackEntry.arguments?.getString("userName") ?: "New Athlete"
            val userName = URLDecoder.decode(encodedName, StandardCharsets.UTF_8.toString())

            ProfileSetupScreen(userName = userName) { _, _, _, _, _ ->
                navController.navigate("home") {
                    popUpTo("auth") { inclusive = true }
                    popUpTo("profile_setup/{userName}") { inclusive = true }
                }
            }
        }

        composable("home") {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.welcome_home),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}