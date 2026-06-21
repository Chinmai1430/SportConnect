@file:Suppress("SpellCheckingInspection")
package com.chinmaib.sportconnect

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chinmaib.sportconnect.auth.AuthScreen
import com.chinmaib.sportconnect.auth.AuthState
import com.chinmaib.sportconnect.auth.AuthViewModel
import com.chinmaib.sportconnect.auth.ProfileSetupScreen
import com.chinmaib.sportconnect.auth.SplashScreen
import com.chinmaib.sportconnect.ui.creator.SportsCreatorScreen
import com.chinmaib.sportconnect.ui.feed.FeedScreen
import com.chinmaib.sportconnect.ui.home.HomeScreen
import com.chinmaib.sportconnect.ui.home.HomeViewModel
import com.chinmaib.sportconnect.ui.home.MatchesListScreen
import com.chinmaib.sportconnect.ui.home.RosterListScreen
import com.chinmaib.sportconnect.ui.profile.ProfileScreen
import com.chinmaib.sportconnect.ui.theme.*
import com.chinmaib.sportconnect.ui.films.MovieDetailScreen
import com.chinmaib.sportconnect.ui.films.SportsFilmCatalogueScreen
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.handleDeeplinks
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.delay
import javax.inject.Inject
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlin.time.Duration.Companion.seconds

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var supabaseClient: SupabaseClient

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
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
            if ((data?.scheme == "sportconnect") && (data.host == "login-callback")) {
                supabaseClient.handleDeeplinks(it)
            }
        }
    }
}

@Composable
fun SportConnectNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()
    
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var showToast by remember { mutableStateOf(value = false) }

    fun triggerToast(message: String) {
        toastMessage = message
        showToast = true
    }

    LaunchedEffect(showToast) {
        if (showToast) {
            delay(2.seconds)
            showToast = false
        }
    }

    // ACCURACY GUARD: Force resolution check for cold starts
    var isAuthResolved by remember { mutableStateOf(value = false) }
    
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated, 
            is AuthState.OnboardingRequired, 
            is AuthState.Idle -> {
                if (authState is AuthState.Idle) {
                    if (authViewModel.auth.currentUserOrNull() == null) {
                        isAuthResolved = true
                    }
                } else {
                    isAuthResolved = true
                }
            }
            is AuthState.Error -> {
                isAuthResolved = true
            }
            else -> {} 
        }
    }

    val startRoute = "splash"

    Box(modifier = Modifier.fillMaxSize()) {
        if (isAuthResolved) {
            NavHost(navController = navController, startDestination = startRoute) {
                composable("splash") {
                    SplashScreen {
                        // FIX: Redirect to HOME immediately as requested
                        val target = when (authState) {
                            is AuthState.Authenticated -> "home"
                            is AuthState.OnboardingRequired -> "profile_setup/Athlete"
                            else -> "auth"
                        }
                        navController.navigate(target) {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                }

                composable("auth") {
                    LaunchedEffect(Unit) {
                        if (authState is AuthState.SignedOut) {
                            triggerToast("✅ Logged out successfully!")
                            authViewModel.resetState()
                        }
                    }

                    AuthScreen(viewModel = authViewModel) { isLoginMode, userName ->
                        if (isLoginMode) {
                            navController.navigate("home") {
                                popUpTo("auth") { inclusive = true }
                            }
                        } else {
                            val sanitized = userName.trim().take(50).replace(Regex("[^a-zA-Z\\s]"), "")
                            val safeName = if (sanitized.isNotBlank()) URLEncoder.encode(sanitized, StandardCharsets.UTF_8.toString()) else "Athlete"
                            navController.navigate("profile_setup/$safeName")
                        }
                    }
                }

                composable("profile_setup/{userName}") { backStackEntry ->
                    val encodedName = backStackEntry.arguments?.getString("userName") ?: "New Athlete"
                    val userName = URLDecoder.decode(encodedName, StandardCharsets.UTF_8.toString())
                    val context = LocalContext.current

                    Box {
                        ProfileSetupScreen(userName = userName) { imageUri, fullName, dob, phone, _, _, _ ->
                            authViewModel.saveProfile(imageUri, fullName, dob, phone, context)
                        }

                        if (authState is AuthState.Loading) {
                            Box(
                                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                androidx.compose.material3.CircularProgressIndicator(color = AccentGold)
                            }
                        }
                    }

                    LaunchedEffect(authState) {
                        when (authState) {
                            is AuthState.Authenticated -> {
                                navController.navigate("home") {
                                    popUpTo("auth") { inclusive = true }
                                    popUpTo("profile_setup/{userName}") { inclusive = true }
                                }
                            }
                            is AuthState.Error -> {
                                Toast.makeText(context, (authState as AuthState.Error).message, Toast.LENGTH_LONG).show()
                                authViewModel.resetState()
                            }
                            else -> {}
                        }
                    }
                }

                composable(route = "home") {
                    // Show login toaster after landing on home
                    LaunchedEffect(Unit) {
                        if (authState is AuthState.Authenticated) {
                            triggerToast("✅ Logged in successfully!")
                        }
                    }

                    val homeViewModel: HomeViewModel = hiltViewModel()
                    HomeScreen(
                        viewModel = homeViewModel,
                        onNavigateToCreator = { navController.navigate("sports_creator") },
                        onNavigateToRoster = { navController.navigate("roster_list") },
                        onNavigateToMatches = { navController.navigate("matches_list") },
                        onNavigateToProfile = { navController.navigate("profile") },
                        onNavigateToFeed = { navController.navigate("feed") },
                        onNavigateToFilms = { navController.navigate("film_catalogue") },
                        onNavigateToFilmDetail = { film ->
                            navController.navigate("film_detail/${URLEncoder.encode(film.title, StandardCharsets.UTF_8.toString())}")
                        }
                    )
                }

                composable("feed") {
                    FeedScreen(
                        onNavigateToHome = {
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                        },
                        onNavigateToProfile = { navController.navigate("profile") }
                    )
                }

                composable("film_catalogue") {
                    val homeViewModel: HomeViewModel = hiltViewModel()
                    SportsFilmCatalogueScreen(
                        viewModel = homeViewModel,
                        onBack = { navController.popBackStack() },
                        onFilmClick = { film ->
                            navController.navigate("film_detail/${URLEncoder.encode(film.title, StandardCharsets.UTF_8.toString())}")
                        }
                    )
                }

                composable("film_detail/{filmTitle}") { backStackEntry ->
                    val filmTitle = backStackEntry.arguments?.getString("filmTitle") ?: ""
                    val homeViewModel: HomeViewModel = hiltViewModel()
                    val films by homeViewModel.films.collectAsState()
                    val decodedTitle = URLDecoder.decode(filmTitle, StandardCharsets.UTF_8.toString())
                    val film = films.find { it.title == decodedTitle }
                    
                    film?.let {
                        MovieDetailScreen(
                            film = it,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }

                composable("profile") {
                    ProfileScreen(
                        onBack = { navController.popBackStack() },
                        onLogout = {
                            authViewModel.logout()
                            navController.navigate("auth") {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
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
                    val homeViewModel: HomeViewModel = hiltViewModel()
                    SportsCreatorScreen(viewModel = homeViewModel) {
                        navController.popBackStack()
                    }
                }
            }
        }

        // PREMIUM TOP-RIGHT TOASTER Overlay
        AnimatedVisibility(
            visible = showToast,
            enter = slideInHorizontally { it } + fadeIn(),
            exit = slideOutHorizontally { it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 64.dp, end = 16.dp)
                .statusBarsPadding(),
        ) {
            Surface(
                color = SurfaceContainer,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElevatedBorders),
                shadowElevation = 8.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = toastMessage ?: "",
                        color = Color.White,
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                    )
                }
            }
        }
    }
}
