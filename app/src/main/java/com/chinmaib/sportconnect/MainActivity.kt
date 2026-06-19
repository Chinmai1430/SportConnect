@file:Suppress("SpellCheckingInspection")
package com.chinmaib.sportconnect

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import com.chinmaib.sportconnect.auth.AuthScreen
import com.chinmaib.sportconnect.auth.AuthViewModel
import com.chinmaib.sportconnect.auth.ProfileSetupScreen
import com.chinmaib.sportconnect.auth.SplashScreen
import com.chinmaib.sportconnect.ui.creator.SportsCreatorScreen
import com.chinmaib.sportconnect.ui.home.HomeScreen
import com.chinmaib.sportconnect.ui.home.HomeViewModel
import com.chinmaib.sportconnect.ui.home.MatchesListScreen
import com.chinmaib.sportconnect.ui.home.RosterListScreen
import com.chinmaib.sportconnect.ui.theme.SportConnectTheme
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.handleDeeplinks
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import javax.inject.Inject
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

// SUPABASE CONFIGURATION INSTRUCTION:
// Update your Supabase Web Dashboard -> Authentication -> URL Configuration
// Set both "Site URL" and "Redirect URLs" to: sportconnect://login-callback

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var supabaseClient: SupabaseClient

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge() // DIRECTIVE: Edge-to-Edge
        super.onCreate(savedInstanceState)
        
        // SECURITY: Validate Deep Link Intent
        handleDeepLink(intent)

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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        intent?.let {
            val data = it.data
            // SECURITY: Only handle links that match our specific scheme to prevent intent hijacking
            if ((data?.scheme == "sportconnect") && (data.host == "login-callback")) {
                supabaseClient.handleDeeplinks(it)
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
                    // SECURITY: Strictly sanitize and encode name for URL transit
                    val sanitized = userName.trim().take(50).replace(Regex("[^a-zA-Z\\s]"), "")
                    val safeName = if (sanitized.isNotBlank()) URLEncoder.encode(sanitized, StandardCharsets.UTF_8.toString()) else "Athlete"
                    navController.navigate("profile_setup/$safeName")
                }
            }
        }

        composable("profile_setup/{userName}") { backStackEntry ->
            val authViewModel: AuthViewModel = hiltViewModel()
            val encodedName = backStackEntry.arguments?.getString("userName") ?: "New Athlete"
            // SECURITY: Decode with explicit charset
            val userName = URLDecoder.decode(encodedName, StandardCharsets.UTF_8.toString())
            val context = androidx.compose.ui.platform.LocalContext.current
            val authState by authViewModel.authState.collectAsState()

            Box {
                ProfileSetupScreen(userName = userName) { imageUri, fullName, dob, phone, _, _, _ ->
                    authViewModel.saveProfile(imageUri, fullName, dob, phone, context)
                }

                if (authState is com.chinmaib.sportconnect.auth.AuthState.Loading) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(color = com.chinmaib.sportconnect.ui.theme.AccentGold)
                    }
                }
            }

            LaunchedEffect(authState) {
                when (authState) {
                    is com.chinmaib.sportconnect.auth.AuthState.Authenticated -> {
                        navController.navigate("home") {
                            popUpTo("auth") { inclusive = true }
                            popUpTo("profile_setup/{userName}") { inclusive = true }
                        }
                    }
                    is com.chinmaib.sportconnect.auth.AuthState.Error -> {
                        // PRODUCTION: Toast is fine for simple errors, but localized
                        android.widget.Toast.makeText(context, (authState as com.chinmaib.sportconnect.auth.AuthState.Error).message, android.widget.Toast.LENGTH_LONG).show()
                        authViewModel.resetState()
                    }
                    else -> {}
                }
            }
        }

        composable("home") {
            val homeViewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToCreator = { navController.navigate("sports_creator") },
                onNavigateToRoster = { navController.navigate("roster_list") },
            ) {
                navController.navigate("matches_list")
            }
        }

        composable("roster_list") {
            val homeViewModel: HomeViewModel = hiltViewModel()
            RosterListScreen(viewModel = homeViewModel) {
                navController.popBackStack()
            }
        }

        composable("matches_list") {
            MatchesListScreen {
                navController.popBackStack()
            }
        }

        composable("sports_creator") {
            SportsCreatorScreen {
                navController.popBackStack()
            }
        }
    }
}
