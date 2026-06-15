package com.chinmaib.sportconnect.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.OTP
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import javax.inject.Inject

sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data object Success : AuthState()
    data object OnboardingRequired : AuthState()
    data object Authenticated : AuthState()
    data object OtpSent : AuthState()
    data object OtpVerified : AuthState()
    data class Error(val message: String) : AuthState()
}

@Serializable
data class UserProfile(
    val id: String? = null,
    val onboarding_completed: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: Auth,
    private val postgrest: Postgrest
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Supabase Configuration Instruction:
    // Update your Supabase Web Dashboard -> Authentication -> URL Configuration
    // Set both "Site URL" and "Redirect URLs" to: sportconnect://login-callback

    private val redirectUrl = "sportconnect://login-callback"

    init {
        observeSession()
    }

    private fun observeSession() {
        viewModelScope.launch {
            auth.sessionStatus.collect { status ->
                if (status is io.github.jan.supabase.auth.status.SessionStatus.Authenticated) {
                    checkProfileStatus()
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
            _authState.value = AuthState.Loading
            try {
                val profile = postgrest["profiles"]
                    .select(columns = Columns.list("onboarding_completed")) {
                        filter {
                            eq("id", user.id)
                        }
                    }
                    .decodeSingleOrNull<UserProfile>()

                if (profile?.onboarding_completed == true) {
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.OnboardingRequired
                }
            } catch (e: Exception) {
                _authState.value = AuthState.OnboardingRequired
            }
        }
    }

    fun sendOtp(emailInput: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.signInWith(OTP) {
                    email = emailInput
                    createUser = true
                }
                _authState.value = AuthState.OtpSent
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Failed to send OTP.")
            }
        }
    }

    fun verifyOtp(emailInput: String, otpInput: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.verifyEmailOtp(
                    type = OtpType.Email.EMAIL,
                    email = emailInput,
                    token = otpInput,
                )
                _authState.value = AuthState.OtpVerified
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Invalid OTP.")
            }
        }
    }

    fun finalizeSignUp(passwordInput: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.updateUser {
                    password = passwordInput
                }
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Failed to finalize account.")
            }
        }
    }

    fun login(emailInput: String, passwordInput: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.signInWith(Email) {
                    email = emailInput
                    password = passwordInput
                }
                checkProfileStatus()
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Login failed.")
            }
        }
    }

    fun loginWithGoogle() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.signInWith(Google, redirectUrl = redirectUrl)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Google Sign-In failed.")
            }
        }
    }

    fun resetState() {
        if ((_authState.value is AuthState.Error) || (_authState.value is AuthState.OtpSent) || (_authState.value is AuthState.Success)) {
            _authState.value = AuthState.Idle
        }
    }
}