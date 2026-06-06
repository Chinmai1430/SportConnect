@file:Suppress("SpellCheckingInspection") // ALTRON: Silences all dictionary typo warnings

package com.chinmaib.sportconnect.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import com.chinmaib.sportconnect.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    // ALTRON INJECTION: Added userName to the output signal
    onAuthSuccess: (isLogin: Boolean, userName: String) -> Unit,
) {
    var isLoginMode by remember { mutableStateOf(value = true) }
    var isForgotPasswordMode by remember { mutableStateOf(value = false) }

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var verifiedEmails by remember { mutableStateOf(setOf<String>()) }
    var isOtpFieldVisible by remember { mutableStateOf(value = false) }
    var otpInput by remember { mutableStateOf("") }

    var timeLeft by remember { mutableIntStateOf(60) }
    var isTimerActive by remember { mutableStateOf(value = false) }

    val authState by viewModel.authState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val isCurrentEmailVerified = verifiedEmails.contains(email.trim())
    val isEmailReadyToVerify = email.trim().endsWith("@gmail.com")

    LaunchedEffect(isTimerActive, timeLeft) {
        if ((isTimerActive) && (timeLeft > 0)) {
            delay(1000)
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
    val recoveryTokenSentTemplate = stringResource(R.string.recovery_token_sent)

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                // ALTRON INJECTION: Routing the user's name forward
                onAuthSuccess(isLoginMode, fullName.trim())
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
                snackbarHostState.showSnackbar((authState as AuthState.Error).message)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(DeepForestNightStart, DeepForestNightEnd)))
                .padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 40.dp),
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.pitchup_logo),
                        contentDescription = stringResource(R.string.app_name),
                        modifier = Modifier.size(42.dp),
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "SPORTCONNECT",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x0F061710), RoundedCornerShape(16.dp))
                        .border(1.dp, TurfGreen.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = when {
                            isForgotPasswordMode -> stringResource(R.string.reset_password)
                            isLoginMode -> stringResource(R.string.welcome_back)
                            else -> stringResource(R.string.create_account)
                        },
                        color = Color.White, fontFamily = Montserrat, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp).align(Alignment.Start),
                    )

                    if (!isLoginMode && !isForgotPasswordMode) {
                        StyledTextField(
                            value = fullName, onValueChange = { fullName = it },
                            label = stringResource(R.string.full_name), placeholder = stringResource(R.string.name_placeholder),
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
                        label = stringResource(R.string.email_address),
                        trailingIcon = {
                            if (!isLoginMode && !isForgotPasswordMode) {
                                if (isCurrentEmailVerified) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = stringResource(R.string.verified_desc),
                                        tint = VerifiedBlue,
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
                                        colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.padding(end = 6.dp).height(34.dp),
                                    ) {
                                        Text(stringResource(R.string.verify), color = DeepForestNightEnd, fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                        },
                    )

                    if (isOtpFieldVisible && !isCurrentEmailVerified && !isLoginMode && !isForgotPasswordMode) {
                        Spacer(modifier = Modifier.height(12.dp))
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
                                        containerColor = Saffron,
                                        disabledContainerColor = Color.Gray.copy(alpha = 0.3f),
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.padding(end = 6.dp).height(34.dp),
                                ) {
                                    Text(stringResource(R.string.submit), color = if (otpInput.length >= 3) DeepForestNightEnd else Color.White, fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            },
                        )

                        Box(
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp, end = 2.dp),
                            contentAlignment = Alignment.CenterEnd,
                        ) {
                            if (timeLeft > 0) {
                                Text(
                                    text = stringResource(R.string.resend_otp_in, timeLeft), color = CoolTeal.copy(alpha = 0.8f),
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
                                    border = BorderStroke(1.dp, Saffron.copy(alpha = 0.5f)),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                                ) {
                                    Text(stringResource(R.string.resend_otp_button), color = Saffron, fontSize = 11.sp, fontFamily = Montserrat, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    if (!isForgotPasswordMode) {
                        Spacer(modifier = Modifier.height(12.dp))

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
                                    text = strength.label,
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
                            Spacer(modifier = Modifier.height(12.dp))
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
                            onClick = { isForgotPasswordMode = true },
                            modifier = Modifier.align(Alignment.End),
                        ) {
                            Text(stringResource(R.string.forgot_password), color = CoolTeal.copy(alpha = 0.8f), fontFamily = OpenSans, fontSize = 13.sp)
                        }
                    } else {
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    Button(
                        onClick = {
                            when {
                                isForgotPasswordMode -> {
                                    if (email.isBlank()) {
                                        coroutineScope.launch { snackbarHostState.showSnackbar(enterEmailError) }
                                    } else {
                                        coroutineScope.launch { snackbarHostState.showSnackbar(recoveryTokenSentTemplate.format(email)) }
                                        isForgotPasswordMode = false
                                        isLoginMode = true
                                    }
                                }
                                isLoginMode -> {
                                    if (email.isBlank() || password.isBlank()) {
                                        coroutineScope.launch { snackbarHostState.showSnackbar(emailPasswordRequired) }
                                    } else {
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
                                        viewModel.finalizeSignUp(password.trim())
                                    }
                                }
                            }
                        },
                        enabled = authState !is AuthState.Loading,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Saffron, disabledContainerColor = Saffron.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        if (authState is AuthState.Loading) {
                            CircularProgressIndicator(color = DeepForestNightEnd, modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
                        } else {
                            Text(
                                text = when {
                                    isForgotPasswordMode -> stringResource(R.string.send_recovery)
                                    isLoginMode -> stringResource(R.string.enter_system)
                                    else -> stringResource(R.string.get_on_field)
                                },
                                fontFamily = Montserrat, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = DeepForestNightEnd,
                            )
                        }
                    }

                    if (!isForgotPasswordMode) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = TurfGreen.copy(alpha = 0.2f))
                            Text(stringResource(R.string.or_divider), color = CoolTeal.copy(alpha = 0.5f), fontFamily = Montserrat, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp))
                            HorizontalDivider(modifier = Modifier.weight(1f), color = TurfGreen.copy(alpha = 0.2f))
                        }

                        Button(
                            onClick = { viewModel.loginWithGoogle() },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF1F1F1F)),
                            border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                                Image(painter = painterResource(id = R.drawable.ic_google_logo), contentDescription = stringResource(R.string.google_logo_desc), modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(stringResource(R.string.continue_with_google), fontFamily = OpenSans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, letterSpacing = 0.5.sp)
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
                                password = ""
                                confirmPassword = ""
                            }
                        },
                    ) {
                        Text(
                            text = when {
                                isForgotPasswordMode -> stringResource(R.string.back_to_signin)
                                isLoginMode -> stringResource(R.string.need_account)
                                else -> stringResource(R.string.already_on_roster)
                            },
                            color = CoolTeal, fontFamily = Montserrat, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, letterSpacing = 0.5.sp,
                        )
                    }
                }
            }
        }
    }
}
