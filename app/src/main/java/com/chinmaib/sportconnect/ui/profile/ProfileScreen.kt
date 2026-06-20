package com.chinmaib.sportconnect.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.chinmaib.sportconnect.auth.AuthState
import com.chinmaib.sportconnect.auth.AuthViewModel
import com.chinmaib.sportconnect.auth.StyledTextField
import com.chinmaib.sportconnect.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onLogout: () -> Unit,
) {
    val profile by profileViewModel.profileState.collectAsState()
    val uiState by profileViewModel.uiState.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current

    var isEditMode by remember { mutableStateOf(value = false) }
    var showLogoutDialog by remember { mutableStateOf(value = false) }
    
    var tempName by remember(profile) { mutableStateOf(profile?.fullName ?: "") }
    var tempBio by remember(profile) { mutableStateOf(profile?.bio ?: "") }
    var tempPhone by remember(profile) { mutableStateOf(profile?.phone ?: "") }
    var tempLocation by remember(profile) { mutableStateOf(profile?.location ?: "Global") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri -> selectedImageUri = uri }

    // PERFORMANCE: Centralized state observation
    LaunchedEffect(uiState) {
        if (uiState is AuthState.Success) {
            isEditMode = false
            profileViewModel.resetUiState()
        }
    }

    // ACCURACY: Respond to auth session death
    LaunchedEffect(authState) {
        if (authState is AuthState.SignedOut) {
            onLogout()
        }
    }

    Scaffold(
        containerColor = PrimaryBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("PROFILE", fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { 
                            if (isEditMode) {
                                profileViewModel.updateProfile(
                                    selectedImageUri, tempName, profile?.dob ?: "", 
                                    tempPhone, tempBio, tempLocation, context
                                )
                            } else {
                                isEditMode = true
                            }
                        },
                    ) {
                        Icon(
                            imageVector = if (isEditMode) Icons.Default.Check else Icons.Default.Edit, 
                            contentDescription = "Edit", 
                            tint = AccentGold,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            // Header Section
            Box(modifier = Modifier.height(200.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(Brush.horizontalGradient(listOf(AppPrimaryBrand, Color(0xFF0F172A)))),
                )
                
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 24.dp)
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainer)
                        .border(4.dp, PrimaryBackground, CircleShape)
                        .clickable(enabled = isEditMode) {
                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    val displayImage = selectedImageUri ?: profile?.avatarUrl
                    if (displayImage != null) {
                        AsyncImage(
                            model = displayImage,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(50.dp), tint = TextSecondary)
                    }
                    
                    if (isEditMode) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White)
                        }
                    }
                }
            }

            // User Info
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                if (isEditMode) {
                    StyledTextField(value = tempName, onValueChange = { tempName = it }, label = "Full Name")
                    Spacer(modifier = Modifier.height(12.dp))
                    StyledTextField(value = tempBio, onValueChange = { tempBio = it }, label = "Headline/Bio")
                    Spacer(modifier = Modifier.height(12.dp))
                    StyledTextField(value = tempLocation, onValueChange = { tempLocation = it }, label = "Location")
                } else {
                    Text(
                        text = profile?.fullName ?: "Athlete",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = Montserrat,
                    )
                    Text(
                        text = profile?.bio ?: "Professional Athlete",
                        color = TextSecondary,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                        Text(
                            text = profile?.location ?: "Global",
                            color = TextMuted,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                }
            }

            // Analytics
            BentoSection(title = "Analytics") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    AnalyticsItem("1.2k", "Profile views")
                    AnalyticsItem("450", "Post impressions")
                    AnalyticsItem("89", "Search appearances")
                }
            }

            // Contact
            BentoSection(title = "Contact Information") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ContactRow(Icons.Default.Email, profile?.email ?: "N/A")
                    if (isEditMode) {
                        StyledTextField(value = tempPhone, onValueChange = { tempPhone = it }, label = "Phone", isNumber = true)
                    } else {
                        ContactRow(Icons.Default.Phone, profile?.phone ?: "Not provided")
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Logout Button
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceContainer),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, StatusLossError.copy(alpha = 0.5f)),
            ) {
                Text("LOGOUT", color = StatusLossError, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }

            Spacer(modifier = Modifier.height(48.dp))
        }

        if (uiState is AuthState.Loading) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentGold)
            }
        }

        // LOGOUT CONFIRMATION DIALOG
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Logout?", fontFamily = Montserrat, fontWeight = FontWeight.Bold, color = Color.White) },
                text = { Text("Are you sure you want to log out of SportConnect?", color = TextSecondary) },
                confirmButton = {
                    TextButton(onClick = { 
                        showLogoutDialog = false
                        authViewModel.logout() // ONLY TRIGGER VIEWMODEL
                    }) {
                        Text("LOGOUT", color = StatusLossError, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("CANCEL", color = TextPrimary)
                    }
                },
                containerColor = SurfaceContainer,
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}

@Composable
fun BentoSection(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .fillMaxWidth()
            .background(SurfaceContainer, RoundedCornerShape(24.dp))
            .border(1.dp, ElevatedBorders, RoundedCornerShape(24.dp))
            .padding(24.dp),
    ) {
        Text(
            text = title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 16.dp),
        )
        content()
    }
}

@Composable
fun AnalyticsItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = AccentGold, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
        Text(text = label, color = TextSecondary, fontSize = 11.sp)
    }
}

@Composable
fun ContactRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = AppPrimaryBrand, modifier = Modifier.size(20.dp))
        Text(text = text, color = TextPrimary, modifier = Modifier.padding(start = 12.dp), fontSize = 14.sp)
    }
}
