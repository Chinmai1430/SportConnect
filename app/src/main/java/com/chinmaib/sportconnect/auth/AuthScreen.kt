@file:Suppress("SpellCheckingInspection")

package com.chinmaib.sportconnect.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import com.chinmaib.sportconnect.R
import com.chinmaib.sportconnect.ui.theme.*
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onAuthSuccess: (isLogin: Boolean, userName: String) -> Unit,
) {
    var isLoginMode by remember { mutableStateOf(value = true) }
    var isForgotPasswordMode by remember { mutableStateOf(value = false) }
    var forgotPasswordStep by remember { mutableStateOf(ForgotPasswordStep.EMAIL_ENTRY) }

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var resetOtpInput by remember { mutableStateOf("") }

    var verifiedEmails by remember { mutableStateOf(setOf<String>()) }
    var isOtpFieldVisible by remember { mutableStateOf(value = false) }
    var otpInput by remember { mutableStateOf("") }

    var timeLeft by remember { mutableIntStateOf(60) }
    var isTimerActive by remember { mutableStateOf(value = false) }

    val authState by viewModel.authState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showSuccessDialog by remember { mutableStateOf(value = false) }

    val isCurrentEmailVerified = verifiedEmails.contains(email.trim())
    val isEmailReadyToVerify = android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()

    LaunchedEffect(isTimerActive, timeLeft) {
        if ((isTimerActive) && (timeLeft > 0)) {
            delay(1.seconds)
            timeLeft--
        } else if (timeLeft == 0) {
            isTimerActive = false
        }
    }

    val otpDispatchedMsg = stringResource(R.string.otp_dispatched)
    val emailVerifiedMsg = stringResource(R.string.email_verified)
    val enterEmailError = stringResource(R.string.enter_email_error)
    val emailPasswordRequired = stringResource(R.string.email_password_required)
    val invalidNameError = stringResource(R.string.invalid_name_error)
    val verifyEmailFirst = stringResource(R.string.verify_email_first)
    val passwordTooWeak = stringResource(R.string.password_too_weak)
    val passwordsDoNotMatch = stringResource(R.string.passwords_do_not_match)
    val fieldsCompulsory = stringResource(R.string.fields_compulsory)

    var hasAttemptedAuth by remember { mutableStateOf(value = false) }

    LaunchedEffect(authState) {
        when (val currentAuthState = authState) {
            is AuthState.Authenticated -> {
                if (hasAttemptedAuth || (viewModel.auth.currentUserOrNull() != null)) {
                    onAuthSuccess(true, fullName.ifBlank { "Athlete" })
                    viewModel.resetState()
                }
            }
            is AuthState.OnboardingRequired -> {
                if (hasAttemptedAuth) {
                    onAuthSuccess(false, fullName.ifBlank { "Athlete" })
                    viewModel.resetState()
                }
            }
            is AuthState.Success -> {
                if (isForgotPasswordMode) {
                    showSuccessDialog = true
                    isForgotPasswordMode = false
                    isLoginMode = true
                    forgotPasswordStep = ForgotPasswordStep.EMAIL_ENTRY
                    password = ""
                    confirmPassword = ""
                    resetOtpInput = ""
                    viewModel.resetState()
                } else {
                    viewModel.checkProfileStatus()
                }
            }
            is AuthState.ResetOtpSent -> {
                forgotPasswordStep = ForgotPasswordStep.OTP_ENTRY
                snackbarHostState.showSnackbar("6-digit code sent to your email")
                viewModel.resetState()
            }
            is AuthState.ResetOtpVerified -> {
                forgotPasswordStep = ForgotPasswordStep.NEW_PASSWORD
                viewModel.resetState()
            }
            is AuthState.OtpSent -> {
                snackbarHostState.showSnackbar(otpDispatchedMsg)
                viewModel.resetState()
            }
            is AuthState.OtpVerified -> {
                verifiedEmails += email.trim()
                isOtpFieldVisible = false
                isTimerActive = false
                snackbarHostState.showSnackbar(emailVerifiedMsg)
                viewModel.resetState()
            }
            is AuthState.Error -> {
                snackbarHostState.showSnackbar(currentAuthState.message)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = PrimaryBackground,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .systemBarsPadding(), // DIRECTIVE: Safe Drawing Padding
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 48.dp),
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.pitchup_logo),
                        contentDescription = stringResource(R.string.app_name),
                        modifier = Modifier.size(48.dp),
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = stringResource(R.string.sportconnect_caps),
                        color = Color.White,
                        fontSize = 28.sp,
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceContainer, RoundedCornerShape(24.dp)) // DIRECTIVE: Bento Shape
                        .border(1.dp, ElevatedBorders, RoundedCornerShape(24.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = when {
                            isForgotPasswordMode -> {
                                when (forgotPasswordStep) {
                                    ForgotPasswordStep.EMAIL_ENTRY -> "RESET PASSWORD"
                                    ForgotPasswordStep.OTP_ENTRY -> "VERIFY CODE"
                                    ForgotPasswordStep.NEW_PASSWORD -> "SET NEW PASSWORD"
                                }
                            }
                            isLoginMode -> stringResource(R.string.welcome_back)
                            else -> stringResource(R.string.create_account)
                        },
                        color = Color.White, 
                        fontFamily = Montserrat, 
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(bottom = 24.dp).align(Alignment.Start),
                    )

                    if (isForgotPasswordMode) {
                        when (forgotPasswordStep) {
                            ForgotPasswordStep.EMAIL_ENTRY -> {
                                StyledTextField(
                                    value = email,
                                    onValueChange = { email = it },
                                    label = stringResource(R.string.email_address),
                                    placeholder = "Enter your registered email",
                                )
                            }
                            ForgotPasswordStep.OTP_ENTRY -> {
                                StyledTextField(
                                    value = resetOtpInput,
                                    onValueChange = { if (it.length <= 6) resetOtpInput = it },
                                    label = "6-Digit Code",
                                    placeholder = "Enter the code from your email",
                                    isNumber = true,
                                )
                            }
                            ForgotPasswordStep.NEW_PASSWORD -> {
                                StyledTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    label = "New Password",
                                    isPassword = true,
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                StyledTextField(
                                    value = confirmPassword,
                                    onValueChange = { confirmPassword = it },
                                    label = "Confirm Password",
                                    isPassword = true,
                                )
                            }
                        }
                    } else {
                        if (!isLoginMode) {
                            StyledTextField(
                                value = fullName, onValueChange = { fullName = it },
                                label = stringResource(R.string.full_name), placeholder = stringResource(R.string.name_placeholder),
                            )
                            Spacer(modifier = Modifier.height(16.dp))
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
                            label = stringResource(R.string.email_address),
                            trailingIcon = {
                                if (!isLoginMode) {
                                    if (isCurrentEmailVerified) {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = stringResource(R.string.verified_desc),
                                            tint = StatusLiveWin,
                                            modifier = Modifier.padding(end = 12.dp),
                                        )
                                    } else if (isEmailReadyToVerify) {
                                        Button(
                                            onClick = {
                                                isOtpFieldVisible = true
                                                timeLeft = 60
                                                isTimerActive = true
                                                viewModel.sendOtp(email.trim())
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = AppPrimaryBrand),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.padding(end = 6.dp).height(34.dp),
                                        ) {
                                            Text(stringResource(R.string.verify), color = Color.White, fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }
                                    }
                                }
                            },
                        )

                        if (isOtpFieldVisible && !isCurrentEmailVerified && !isLoginMode) {
                            Spacer(modifier = Modifier.height(16.dp))
                            StyledTextField(
                                value = otpInput,
                                onValueChange = { otpInput = it },
                                label = stringResource(R.string.otp_label),
                                isNumber = true,
                                trailingIcon = {
                                    Button(
                                        onClick = { viewModel.verifyOtp(email.trim(), otpInput.trim()) },
                                        enabled = otpInput.length >= 3,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = AccentGold,
                                            disabledContainerColor = ElevatedBorders,
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.padding(end = 6.dp).height(34.dp),
                                    ) {
                                        Text(stringResource(R.string.submit), color = if (otpInput.length >= 3) Color.Black else TextMuted, fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                },
                            )

                            Box(
                                modifier = Modifier.fillMaxWidth().padding(top = 12.dp, end = 2.dp),
                                contentAlignment = Alignment.CenterEnd,
                            ) {
                                if (timeLeft > 0) {
                                    Text(
                                        text = stringResource(R.string.resend_otp_in, timeLeft), color = TextSecondary,
                                        fontSize = 13.sp, fontFamily = OpenSans,
                                    )
                                } else {
                                    OutlinedButton(
                                        onClick = {
                                            timeLeft = 60
                                            isTimerActive = true
                                            viewModel.sendOtp(email.trim())
                                        },
                                        modifier = Modifier.height(36.dp), shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, AppPrimaryBrand.copy(alpha = 0.5f)),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                                    ) {
                                        Text(stringResource(R.string.resend_otp_button), color = AppPrimaryBrand, fontSize = 11.sp, fontFamily = Montserrat, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        StyledTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = if (isLoginMode) stringResource(R.string.password) else stringResource(R.string.create_password),
                            isPassword = true,
                        )

                        if (!isLoginMode && password.isNotEmpty()) {
                            val strength = calculateStrength(password)
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, start = 4.dp, end = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = strength.labelResId?.let { stringResource(it) } ?: "",
                                    color = strength.color,
                                    fontSize = 11.sp,
                                    fontFamily = Montserrat,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(64.dp),
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(4.dp)
                                        .background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(2.dp)),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(strength.progress)
                                            .height(4.dp)
                                            .background(strength.color, RoundedCornerShape(2.dp)),
                                    )
                                }
                            }
                        }

                        if (!isLoginMode) {
                            Spacer(modifier = Modifier.height(16.dp))
                            StyledTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = stringResource(R.string.confirm_password),
                                isPassword = true,
                            )
                        }
                    }

                    if (isLoginMode && !isForgotPasswordMode) {
                        TextButton(
                            onClick = { 
                                isForgotPasswordMode = true 
                                forgotPasswordStep = ForgotPasswordStep.EMAIL_ENTRY
                            },
                            modifier = Modifier.align(Alignment.End),
                        ) {
                            Text(stringResource(R.string.forgot_password), color = TextSecondary, fontFamily = OpenSans, fontSize = 13.sp)
                        }
                    } else {
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    Button(
                        onClick = {
                            when {
                                isForgotPasswordMode -> {
                                    when (forgotPasswordStep) {
                                        ForgotPasswordStep.EMAIL_ENTRY -> {
                                            if (email.isBlank()) {
                                                coroutineScope.launch { snackbarHostState.showSnackbar(enterEmailError) }
                                            } else {
                                                viewModel.sendPasswordResetOtp(email.trim())
                                            }
                                        }
                                        ForgotPasswordStep.OTP_ENTRY -> {
                                            if (resetOtpInput.length < 6) {
                                                coroutineScope.launch { snackbarHostState.showSnackbar("Enter the 6-digit code") }
                                            } else {
                                                viewModel.verifyResetOtp(email.trim(), resetOtpInput.trim())
                                            }
                                        }
                                        ForgotPasswordStep.NEW_PASSWORD -> {
                                            val currentStrength = calculateStrength(password)
                                            if (password.isBlank() || confirmPassword.isBlank()) {
                                                coroutineScope.launch { snackbarHostState.showSnackbar(fieldsCompulsory) }
                                            } else if (currentStrength != PasswordStrength.STRONG) {
                                                coroutineScope.launch { snackbarHostState.showSnackbar(passwordTooWeak) }
                                            } else if (password != confirmPassword) {
                                                coroutineScope.launch { snackbarHostState.showSnackbar(passwordsDoNotMatch) }
                                            } else {
                                                viewModel.updatePassword(password.trim())
                                            }
                                        }
                                    }
                                }
                                isLoginMode -> {
                                    if (email.isBlank() || password.isBlank()) {
                                        coroutineScope.launch { snackbarHostState.showSnackbar(emailPasswordRequired) }
                                    } else {
                                        hasAttemptedAuth = true
                                        viewModel.login(email.trim(), password.trim())
                                    }
                                }
                                else -> {
                                    val nameRegex = Regex("""^[a-zA-Z\s]+$""")
                                    val currentStrength = calculateStrength(password)

                                    if (fullName.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                                        coroutineScope.launch { snackbarHostState.showSnackbar(fieldsCompulsory) }
                                    } else if (!fullName.trim().matches(nameRegex)) {
                                        coroutineScope.launch { snackbarHostState.showSnackbar(invalidNameError) }
                                    } else if (!isCurrentEmailVerified) {
                                        coroutineScope.launch { snackbarHostState.showSnackbar(verifyEmailFirst) }
                                    } else if (currentStrength != PasswordStrength.STRONG) {
                                        coroutineScope.launch { snackbarHostState.showSnackbar(passwordTooWeak) }
                                    } else if (password != confirmPassword) {
                                        coroutineScope.launch { snackbarHostState.showSnackbar(passwordsDoNotMatch) }
                                    } else {
                                        hasAttemptedAuth = true
                                        viewModel.finalizeSignUp(password.trim())
                                    }
                                }
                            }
                        },
                        enabled = authState !is AuthState.Loading,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGold, disabledContainerColor = ElevatedBorders),
                        shape = RoundedCornerShape(18.dp),
                    ) {
                        if (authState is AuthState.Loading) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
                        } else {
                            Text(
                                text = when {
                                    isForgotPasswordMode -> {
                                        when (forgotPasswordStep) {
                                            ForgotPasswordStep.EMAIL_ENTRY -> "VERIFY"
                                            ForgotPasswordStep.OTP_ENTRY -> "SUBMIT"
                                            ForgotPasswordStep.NEW_PASSWORD -> "UPDATE"
                                        }
                                    }
                                    isLoginMode -> stringResource(R.string.enter_system)
                                    else -> stringResource(R.string.get_on_field)
                                },
                                fontFamily = Montserrat, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Color.Black,
                            )
                        }
                    }

                    if (isLoginMode && !isForgotPasswordMode) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = ElevatedBorders)
                            Text(stringResource(R.string.or_divider), color = TextMuted, fontFamily = Montserrat, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp))
                            HorizontalDivider(modifier = Modifier.weight(1f), color = ElevatedBorders)
                        }

                        Button(
                            onClick = { 
                                hasAttemptedAuth = true
                                viewModel.loginWithGoogle() 
                            },
                            enabled = authState !is AuthState.Loading,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF1F1F1F)),
                            border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                        ) {
                            if (authState is AuthState.Loading) {
                                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                                    Image(painter = painterResource(id = R.drawable.ic_google_logo), contentDescription = stringResource(R.string.google_logo_desc), modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(stringResource(R.string.continue_with_google), fontFamily = OpenSans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, letterSpacing = 0.5.sp)
                                }
                            }
                        }
                    }

                    if (!isForgotPasswordMode || (forgotPasswordStep != ForgotPasswordStep.NEW_PASSWORD)) {
                        Spacer(modifier = Modifier.height(24.dp))

                        TextButton(
                            onClick = {
                                if (isForgotPasswordMode) {
                                    if (forgotPasswordStep == ForgotPasswordStep.EMAIL_ENTRY) {
                                        isForgotPasswordMode = false
                                        isLoginMode = true
                                    } else {
                                        forgotPasswordStep = ForgotPasswordStep.EMAIL_ENTRY
                                    }
                                } else {
                                    isLoginMode = !isLoginMode
                                    password = ""
                                    confirmPassword = ""
                                }
                            },
                        ) {
                            Text(
                                text = when {
                                    isForgotPasswordMode -> {
                                        if (forgotPasswordStep == ForgotPasswordStep.EMAIL_ENTRY) stringResource(R.string.back_to_signin)
                                        else "Back to Email Entry"
                                    }
                                    isLoginMode -> stringResource(R.string.need_account)
                                    else -> stringResource(R.string.already_on_roster)
                                },
                                color = AccentGold, fontFamily = Montserrat, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, letterSpacing = 0.5.sp,
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            confirmButton = {
                TextButton(onClick = { showSuccessDialog = false }) {
                    Text("OK", color = AccentGold, fontWeight = FontWeight.Bold)
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = StatusLiveWin,
                    modifier = Modifier.size(48.dp),
                )
            },
            title = {
                Text(
                    "Password Changed",
                    color = Color.White,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    "Your password has been changed successfully. You can now login with your new password.",
                    color = TextSecondary,
                    fontFamily = OpenSans,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            },
            containerColor = SurfaceContainer,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

enum class ForgotPasswordStep {
    EMAIL_ENTRY,
    OTP_ENTRY,
    NEW_PASSWORD
}
