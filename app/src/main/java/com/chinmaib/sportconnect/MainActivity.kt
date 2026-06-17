@file:Suppress("SpellCheckingInspection")
package com.chinmaib.sportconnect

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
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
import androidx.hilt.navigation.compose.hiltViewModel
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
        super.onCreate(savedInstanceState)
        
        // ALTRON OAUTH CATCHER: Intercept deep links from Supabase
        intent?.let {
            supabaseClient.handleDeeplinks(it)
        }

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
        supabaseClient.handleDeeplinks(intent)
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
            val authViewModel: AuthViewModel = hiltViewModel()
            val encodedName = backStackEntry.arguments?.getString("userName") ?: "New Athlete"
            val userName = URLDecoder.decode(encodedName, StandardCharsets.UTF_8.toString())
            val context = androidx.compose.ui.platform.LocalContext.current

            ProfileSetupScreen(userName = userName) { imageUri, fullName, dob, phone, _, _, _ ->
                authViewModel.saveProfile(imageUri, fullName, dob, phone, context)
            }
            
            val authState by authViewModel.authState.collectAsState()
            LaunchedEffect(authState) {
                if (authState is com.chinmaib.sportconnect.auth.AuthState.Authenticated) {
                    navController.navigate("home") {
                        popUpTo("auth") { inclusive = true }
                        popUpTo("profile_setup/{userName}") { inclusive = true }
                    }
                }
            }
        }

        composable("home") {
            val homeViewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToCreator = { navController.navigate("sports_creator") },
                onNavigateToRoster = { navController.navigate("roster_list") },
                onNavigateToMatches = { navController.navigate("matches_list") }
            )
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
