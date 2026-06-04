package com.chinmaib.sportconnect.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chinmaib.sportconnect.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

val DeepForestNightStart = Color(0xFF112E21)
val DeepForestNightEnd = Color(0xFF061710)
val Saffron = Color(0xFFF4A261)
val TurfGreen = Color(0xFF3C8C5A)
val CoolTeal = Color(0xFF74C2BD)
val VerifiedBlue = Color(0xFF1DA1F2)

val Montserrat = FontFamily(
    Font(R.font.montserrat_semi_bold, FontWeight.SemiBold),
    Font(R.font.montserrat_bold, FontWeight.Bold)
)

val OpenSans = FontFamily(
    Font(R.font.open_sans_regular, FontWeight.Normal),
    Font(R.font.open_sans_semi_bold, FontWeight.SemiBold)
)

// ALTRON INJECTION: Security State Matrix
enum class PasswordStrength(val label: String, val color: Color, val progress: Float) {
    NONE("", Color.Transparent, 0f),
    WEAK("Weak", Color(0xFFE57373), 0.33f),      // Red
    MODERATE("Moderate", Saffron, 0.66f),        // Orange/Yellow
    STRONG("Strong", TurfGreen, 1f)              // Green
}

fun calculateStrength(password: String): PasswordStrength {
    if (password.isEmpty()) return PasswordStrength.NONE

    var score = 0
    if (password.length >= 8) score++
    if (password.any { it.isUpperCase() }) score++
    if (password.any { it.isLowerCase() }) score++
    if (password.any { it.isDigit() }) score++
    if (password.any { !it.isLetterOrDigit() }) score++ // Special character check

    return when {
        score <= 2 -> PasswordStrength.WEAK
        score in 3..4 -> PasswordStrength.MODERATE
        score == 5 -> PasswordStrength.STRONG
        else -> PasswordStrength.WEAK
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onAuthSuccess: (isLogin: Boolean) -> Unit
) {
    var isLoginMode by remember { mutableStateOf(true) }
    var isForgotPasswordMode by remember { mutableStateOf(false) }

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    // Password States
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") } // ALTRON: New Confirm State

    var verifiedEmails by remember { mutableStateOf(setOf<String>()) }
    var isOtpFieldVisible by remember { mutableStateOf(false) }
    var otpInput by remember { mutableStateOf("") }

    var timeLeft by remember { mutableStateOf(60) }
    var isTimerActive by remember { mutableStateOf(false) }

    val authState by viewModel.authState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val isCurrentEmailVerified = verifiedEmails.contains(email.trim())
    val isEmailReadyToVerify = email.trim().endsWith("@gmail.com")

    LaunchedEffect(isTimerActive, timeLeft) {
        if (isTimerActive && timeLeft > 0) {
            delay(1000)
            timeLeft--
        } else if (timeLeft == 0) {
            isTimerActive = false
        }
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                onAuthSuccess(isLoginMode)
                viewModel.resetState()
            }
            is AuthState.OtpSent -> {
                snackbarHostState.showSnackbar("OTP dispatched to your email in real-time.")
                viewModel.resetState()
            }
            is AuthState.OtpVerified -> {
                verifiedEmails = verifiedEmails + email.trim()
                isOtpFieldVisible = false
                isTimerActive = false
                snackbarHostState.showSnackbar("Email officially verified!")
                viewModel.resetState()
            }
            is AuthState.Error -> {
                snackbarHostState.showSnackbar((authState as AuthState.Error).message)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(DeepForestNightStart, DeepForestNightEnd)))
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "PITCHUP", color = Color.White, fontSize = 42.sp, fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold, letterSpacing = 4.sp, textAlign = TextAlign.Center
                )

                Text(
                    text = "PREMIUM SPORTS COMMUNITY", color = CoolTeal, fontSize = 12.sp,
                    fontFamily = Montserrat, fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp,
                    modifier = Modifier.padding(bottom = 40.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x0F061710), RoundedCornerShape(16.dp))
                        .border(1.dp, TurfGreen.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = when {
                            isForgotPasswordMode -> "RESET PASSWORD"
                            isLoginMode -> "WELCOME BACK"
                            else -> "CREATE ACCOUNT"
                        },
                        color = Color.White, fontFamily = Montserrat, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp).align(Alignment.Start)
                    )

                    if (!isLoginMode && !isForgotPasswordMode) {
                        StyledTextField(
                            value = fullName, onValueChange = { fullName = it },
                            label = "Full Name", placeholder = "e.g. John Doe"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    StyledTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            if (isOtpFieldVisible) {
                                isOtpFieldVisible = false
                                isTimerActive = false
                                otpInput = ""
                            }
                        },
                        label = "Email Address",
                        trailingIcon = {
                            if (!isLoginMode && !isForgotPasswordMode) {
                                if (isCurrentEmailVerified) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = "Verified",
                                        tint = VerifiedBlue,
                                        modifier = Modifier.padding(end = 12.dp)
                                    )
                                } else if (isEmailReadyToVerify) {
                                    Button(
                                        onClick = {
                                            isOtpFieldVisible = true
                                            timeLeft = 60
                                            isTimerActive = true
                                            viewModel.sendOtp(email.trim())
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.padding(end = 6.dp).height(34.dp)
                                    ) {
                                        Text("VERIFY", color = DeepForestNightEnd, fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    )

                    if (isOtpFieldVisible && !isCurrentEmailVerified && !isLoginMode && !isForgotPasswordMode) {
                        Spacer(modifier = Modifier.height(12.dp))
                        StyledTextField(
                            value = otpInput,
                            onValueChange = { otpInput = it },
                            label = "Enter 6-Digit OTP",
                            isNumber = true,
                            trailingIcon = {
                                Button(
                                    onClick = { viewModel.verifyOtp(email.trim(), otpInput.trim()) },
                                    enabled = otpInput.length >= 3,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Saffron,
                                        disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.padding(end = 6.dp).height(34.dp)
                                ) {
                                    Text("SUBMIT", color = if (otpInput.length >= 3) DeepForestNightEnd else Color.White, fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        )

                        Box(
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp, end = 2.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            if (timeLeft > 0) {
                                Text(
                                    text = "Resend OTP in ${timeLeft}s", color = CoolTeal.copy(alpha = 0.8f),
                                    fontSize = 13.sp, fontFamily = OpenSans
                                )
                            } else {
                                OutlinedButton(
                                    onClick = {
                                        timeLeft = 60
                                        isTimerActive = true
                                        viewModel.sendOtp(email.trim())
                                    },
                                    modifier = Modifier.height(36.dp), shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Saffron.copy(alpha = 0.5f)),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                                ) {
                                    Text("RESEND OTP", color = Saffron, fontSize = 11.sp, fontFamily = Montserrat, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    if (!isForgotPasswordMode) {
                        Spacer(modifier = Modifier.height(12.dp))

                        // ALTRON INJECTION: Dynamic Password Fields
                        StyledTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = if (isLoginMode) "Password" else "Create Password",
                            isPassword = true
                        )

                        // Strength UI (Only visible when creating an account and typing)
                        if (!isLoginMode && password.isNotEmpty()) {
                            val strength = calculateStrength(password)
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, start = 4.dp, end = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = strength.label,
                                    color = strength.color,
                                    fontSize = 11.sp,
                                    fontFamily = Montserrat,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(64.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(4.dp)
                                        .background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(strength.progress)
                                            .height(4.dp)
                                            .background(strength.color, RoundedCornerShape(2.dp))
                                    )
                                }
                            }
                        }

                        // Confirm Password UI
                        if (!isLoginMode) {
                            Spacer(modifier = Modifier.height(12.dp))
                            StyledTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = "Confirm Password",
                                isPassword = true
                            )
                        }
                    }

                    if (isLoginMode && !isForgotPasswordMode) {
                        TextButton(
                            onClick = { isForgotPasswordMode = true },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Forgot Password?", color = CoolTeal.copy(alpha = 0.8f), fontFamily = OpenSans, fontSize = 13.sp)
                        }
                    } else {
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    Button(
                        onClick = {
                            when {
                                isForgotPasswordMode -> {
                                    if (email.isBlank()) {
                                        coroutineScope.launch { snackbarHostState.showSnackbar("Please enter your email address.") }
                                    } else {
                                        coroutineScope.launch { snackbarHostState.showSnackbar("Recovery token transmitted to $email") }
                                        isForgotPasswordMode = false
                                        isLoginMode = true
                                    }
                                }
                                isLoginMode -> {
                                    if (email.isBlank() || password.isBlank()) {
                                        coroutineScope.launch { snackbarHostState.showSnackbar("Both Email and Password are required.") }
                                    } else {
                                        viewModel.login(email.trim(), password.trim())
                                    }
                                }
                                else -> {
                                    val nameRegex = Regex("^[a-zA-Z\\s]+\$")
                                    val currentStrength = calculateStrength(password)

                                    // ALTRON INJECTION: Strict Validation Gates
                                    if (fullName.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                                        coroutineScope.launch { snackbarHostState.showSnackbar("All fields are compulsory.") }
                                    } else if (!fullName.trim().matches(nameRegex)) {
                                        coroutineScope.launch { snackbarHostState.showSnackbar("Invalid name. Only uppercase/lowercase letters and spaces are allowed.") }
                                    } else if (!isCurrentEmailVerified) {
                                        coroutineScope.launch { snackbarHostState.showSnackbar("Please verify your email address first.") }
                                    } else if (currentStrength != PasswordStrength.STRONG) {
                                        coroutineScope.launch { snackbarHostState.showSnackbar("Password is not strong enough. Require 8+ chars, upper, lower, number, & symbol.") }
                                    } else if (password != confirmPassword) {
                                        coroutineScope.launch { snackbarHostState.showSnackbar("Passwords do not match.") }
                                    } else {
                                        viewModel.finalizeSignUp(password.trim())
                                    }
                                }
                            }
                        },
                        enabled = authState !is AuthState.Loading,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Saffron, disabledContainerColor = Saffron.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (authState is AuthState.Loading) {
                            CircularProgressIndicator(color = DeepForestNightEnd, modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
                        } else {
                            Text(
                                text = when {
                                    isForgotPasswordMode -> "SEND RECOVERY LINK"
                                    isLoginMode -> "ENTER SYSTEM"
                                    else -> "GET ON THE FIELD"
                                },
                                fontFamily = Montserrat, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = DeepForestNightEnd
                            )
                        }
                    }

                    if (!isForgotPasswordMode) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = TurfGreen.copy(alpha = 0.2f))
                            Text("OR", color = CoolTeal.copy(alpha = 0.5f), fontFamily = Montserrat, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp))
                            HorizontalDivider(modifier = Modifier.weight(1f), color = TurfGreen.copy(alpha = 0.2f))
                        }

                        Button(
                            onClick = { viewModel.loginWithGoogle() },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF1F1F1F)),
                            border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                                Image(painter = painterResource(id = R.drawable.ic_google_logo), contentDescription = "Google Logo", modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Continue with Google", fontFamily = OpenSans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, letterSpacing = 0.5.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    TextButton(
                        onClick = {
                            if (isForgotPasswordMode) {
                                isForgotPasswordMode = false
                                isLoginMode = true
                            } else {
                                isLoginMode = !isLoginMode
                                // Reset fields when toggling modes
                                password = ""
                                confirmPassword = ""
                            }
                        }
                    ) {
                        Text(
                            text = when {
                                isForgotPasswordMode -> "BACK TO SIGN IN"
                                isLoginMode -> "NEED AN ACCOUNT? SIGN UP"
                                else -> "ALREADY ON THE ROSTER? LOGIN"
                            },
                            color = CoolTeal, fontFamily = Montserrat, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false,
    isNumber: Boolean = false,
    placeholder: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label, color = CoolTeal.copy(alpha = 0.7f), fontFamily = OpenSans) },
        placeholder = placeholder?.let { { Text(text = it, color = CoolTeal.copy(alpha = 0.4f), fontFamily = OpenSans) } },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = if (isNumber) KeyboardType.Number else KeyboardType.Text),
        trailingIcon = trailingIcon,
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = TurfGreen, unfocusedBorderColor = TurfGreen.copy(alpha = 0.3f),
            focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = Saffron
        ),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )
}