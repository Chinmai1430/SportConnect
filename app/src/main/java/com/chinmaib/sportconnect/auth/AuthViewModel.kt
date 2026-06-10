package com.chinmaib.sportconnect.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.OTP // ALTRON: Supabase OTP Engine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data object Success : AuthState()
    data object OtpSent : AuthState()       // UI Trigger: OTP Email dispatched
    data object OtpVerified : AuthState()   // UI Trigger: Blue tick unlocked
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: Auth,
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // ALTRON INJECTION: Triggers Supabase to send the real-time email
    fun sendOtp(emailInput: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.signInWith(OTP) {
                    email = emailInput
                }
                _authState.value = AuthState.OtpSent
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Failed to send OTP. Check email format.")
            }
        }
    }

    // ALTRON INJECTION: Checks the 6-digit code against the Supabase server
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
                _authState.value = AuthState.Error(e.localizedMessage ?: "Invalid OTP. Please check the code and try again.")
            }
        }
    }

    // Since the OTP already authenticated them, we just attach their new password
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
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Login failed. Check your credentials.")
            }
        }
    }

    fun loginWithGoogle() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.signInWith(Google)
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Google Sign-In canceled or failed.")
            }
        }
    }

    fun resetState() {
        if ((_authState.value is AuthState.Error) || (_authState.value is AuthState.OtpSent) || (_authState.value is AuthState.Success)) {
            _authState.value = AuthState.Idle
        }
    }
}