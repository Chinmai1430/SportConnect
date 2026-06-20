package com.chinmaib.sportconnect.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chinmaib.sportconnect.BuildConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.OTP
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject
import java.util.regex.Pattern

sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data object Success : AuthState()
    data object OnboardingRequired : AuthState()
    data object Authenticated : AuthState()
    data object OtpSent : AuthState()
    data object OtpVerified : AuthState()
    data object ResetOtpSent : AuthState()
    data object ResetOtpVerified : AuthState()
    data object SignedOut : AuthState()
    data class Error(val message: String) : AuthState()
}

@Serializable
data class UserProfile(
    val id: String? = null,
    @SerialName("onboarding_completed")
    val onboardingCompleted: Boolean = false,
)

@Serializable
data class ProfileUpdate(
    val id: String,
    @SerialName("full_name")
    val fullName: String,
    val dob: String,
    val phone: String,
    @SerialName("onboarding_completed")
    val onboardingCompleted: Boolean,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    val auth: Auth,
    private val postgrest: Postgrest,
    private val storage: Storage,
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val redirectUrl = "sportconnect://login-callback"
    
    private val emailPattern = Pattern.compile(
        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
    )

    private fun isConfigValid(): Boolean {
        return (BuildConfig.SUPABASE_URL.isNotBlank() &&
                BuildConfig.SUPABASE_ANON_KEY.isNotBlank())
    }

    private fun emitConfigError() {
        _authState.value = AuthState.Error("Configuration Error: Security keys missing.")
    }

    init {
        // Initial state check
        val user = auth.currentUserOrNull()
        if (user != null) {
            checkProfileStatus()
        } else {
            // PRODUCTION: Proactively ensure Idle state if no user
            _authState.value = AuthState.Idle
        }
        observeSession()
    }

    private fun observeSession() {
        viewModelScope.launch {
            auth.sessionStatus.collect { status ->
                if (status is io.github.jan.supabase.auth.status.SessionStatus.Authenticated) {
                    if (_authState.value !is AuthState.Authenticated) {
                        checkProfileStatus()
                    }
                } else if (status is io.github.jan.supabase.auth.status.SessionStatus.NotAuthenticated) {
                    if (_authState.value !is AuthState.SignedOut) {
                        _authState.value = AuthState.Idle
                    }
                }
            }
        }
    }

    fun checkProfileStatus() {
        val user = auth.currentUserOrNull()
        if (user == null) {
            _authState.value = AuthState.Idle
            return
        }

        viewModelScope.launch {
            if (_authState.value !is AuthState.Loading) {
                _authState.value = AuthState.Loading
            }
            
            try {
                val response = postgrest["profiles"]
                    .select(columns = Columns.list("onboarding_completed")) {
                        filter {
                            eq("id", user.id)
                        }
                    }
                
                val profile = response.decodeSingleOrNull<UserProfile>()

                if (profile?.onboardingCompleted == true) {
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.OnboardingRequired
                }
            } catch (_: Exception) {
                _authState.value = AuthState.OnboardingRequired
            }
        }
    }

    private fun validateEmail(email: String): Boolean {
        return email.isNotBlank() && emailPattern.matcher(email).matches()
    }

    fun sendOtp(emailInput: String) {
        if (!isConfigValid()) { emitConfigError(); return }
        val sanitizedEmail = emailInput.trim()
        if (!validateEmail(sanitizedEmail)) {
            _authState.value = AuthState.Error("Invalid email format.")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.signInWith(OTP) {
                    email = sanitizedEmail
                    createUser = true
                }
                _authState.value = AuthState.OtpSent
            } catch (_: Exception) {
                _authState.value = AuthState.Error("Service error: Unable to dispatch OTP.")
            }
        }
    }

    fun verifyOtp(emailInput: String, otpInput: String) {
        val sanitizedEmail = emailInput.trim()
        val sanitizedOtp = otpInput.trim()
        
        if (sanitizedOtp.length < 6) {
            _authState.value = AuthState.Error("OTP must be 6 digits.")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.verifyEmailOtp(
                    type = OtpType.Email.EMAIL,
                    email = sanitizedEmail,
                    token = sanitizedOtp,
                )
                _authState.value = AuthState.OtpVerified
            } catch (_: Exception) {
                _authState.value = AuthState.Error("Verification failed: Invalid or expired OTP.")
            }
        }
    }

    fun finalizeSignUp(passwordInput: String) {
        val sanitizedPassword = passwordInput.trim()
        if (sanitizedPassword.length < 8) {
            _authState.value = AuthState.Error("Security Requirement: Password too short.")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.updateUser {
                    password = sanitizedPassword
                }
                _authState.value = AuthState.Success
            } catch (_: Exception) {
                _authState.value = AuthState.Error("Account Update Failed: Check connection.")
            }
        }
    }

    fun login(emailInput: String, passwordInput: String) {
        if (!isConfigValid()) { emitConfigError(); return }
        val sanitizedEmail = emailInput.trim()
        val sanitizedPassword = passwordInput.trim()

        if (!validateEmail(sanitizedEmail)) {
            _authState.value = AuthState.Error("Invalid email.")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.signInWith(Email) {
                    email = sanitizedEmail
                    password = sanitizedPassword
                }
                checkProfileStatus()
            } catch (_: Exception) {
                _authState.value = AuthState.Error("Access Denied: Invalid credentials.")
            }
        }
    }

    fun loginWithGoogle() {
        if (!isConfigValid()) { emitConfigError(); return }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.signInWith(Google, redirectUrl = redirectUrl)
            } catch (_: Exception) {
                _authState.value = AuthState.Error("Auth Provider Error: Google Sign-In failed.")
            }
        }
    }

    fun sendPasswordResetOtp(emailInput: String) {
        if (!isConfigValid()) { emitConfigError(); return }
        val sanitizedEmail = emailInput.trim()
        if (!validateEmail(sanitizedEmail)) {
            _authState.value = AuthState.Error("Invalid email.")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.signInWith(OTP) {
                    email = sanitizedEmail
                    createUser = false
                }
                _authState.value = AuthState.ResetOtpSent
            } catch (_: Exception) {
                _authState.value = AuthState.Error("Recovery Failed: Check if email is registered.")
            }
        }
    }

    fun verifyResetOtp(emailInput: String, otpInput: String) {
        val sanitizedEmail = emailInput.trim()
        val sanitizedOtp = otpInput.trim()

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.verifyEmailOtp(
                    type = OtpType.Email.EMAIL,
                    email = sanitizedEmail,
                    token = sanitizedOtp,
                )
                _authState.value = AuthState.ResetOtpVerified
            } catch (_: Exception) {
                _authState.value = AuthState.Error("Verification Failed: Code incorrect or expired.")
            }
        }
    }

    fun updatePassword(newPasswordInput: String) {
        val sanitizedPassword = newPasswordInput.trim()
        if (sanitizedPassword.length < 8) {
            _authState.value = AuthState.Error("Security Requirement: 8+ characters required.")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.updateUser {
                    password = sanitizedPassword
                }
                _authState.value = AuthState.Success
            } catch (_: Exception) {
                _authState.value = AuthState.Error("System Error: Failed to commit new password.")
            }
        }
    }

    fun saveProfile(
        imageUri: android.net.Uri?,
        fullName: String,
        dob: String,
        phone: String,
        context: android.content.Context,
    ) {
        val user = auth.currentUserOrNull() ?: return
        val sanitizedName = fullName.trim()
        val sanitizedPhone = phone.trim()

        if ((sanitizedName.isBlank()) || (sanitizedName.length < 2)) {
             _authState.value = AuthState.Error("Input Error: Full name is required.")
             return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                var avatarUrl: String? = null

                imageUri?.let { uri ->
                    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    if (bytes != null) {
                        val fileName = "${user.id}/avatar_${System.currentTimeMillis()}.jpg"
                        storage["avatars"].upload(fileName, bytes) {
                            upsert = true
                        }
                        avatarUrl = storage["avatars"].publicUrl(fileName)
                    }
                }

                val profileUpdate = ProfileUpdate(
                    id = user.id,
                    fullName = sanitizedName,
                    dob = dob,
                    phone = sanitizedPhone,
                    onboardingCompleted = true,
                    avatarUrl = avatarUrl,
                )

                postgrest["profiles"].upsert(profileUpdate)

                _authState.value = AuthState.Authenticated
            } catch (_: Exception) {
                _authState.value = AuthState.Error("DB Error: Profile persistence failed.")
            }
        }
    }

    fun resetState() {
        if ((_authState.value is AuthState.Error) || 
            (_authState.value is AuthState.OtpSent) || 
            (_authState.value is AuthState.Success) ||
            (_authState.value is AuthState.ResetOtpSent) ||
            (_authState.value is AuthState.SignedOut)
        ) {
            _authState.value = AuthState.Idle
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                auth.signOut()
                _authState.value = AuthState.SignedOut
            } catch (_: Exception) {
                _authState.value = AuthState.SignedOut
            }
        }
    }
}
