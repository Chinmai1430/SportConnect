package com.chinmaib.sportconnect.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.OtpType
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.providers.builtin.OTP // ALTRON: Supabase OTP Engine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    object OtpSent : AuthState()       // UI Trigger: OTP Email dispatched
    object OtpVerified : AuthState()   // UI Trigger: Blue tick unlocked
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: Auth
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
                _authState.value = AuthState.Error(e.message ?: "Failed to send OTP. Check email format.")
            }
        }
    }

    // ALTRON INJECTION: Checks the 6-digit code against the Supabase server
    fun verifyOtp(emailInput: String, otpInput: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.verifyEmailOtp(
                    type = OtpType.Email.MAGIC_LINK,
                    email = emailInput,
                    token = otpInput
                )
                _authState.value = AuthState.OtpVerified
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Invalid OTP. Please check the code and try again.")
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
                _authState.value = AuthState.Error(e.message ?: "Failed to finalize account.")
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
                _authState.value = AuthState.Error("Login failed. Check your credentials.")
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
                _authState.value = AuthState.Error(e.message ?: "Google Sign-In canceled or failed.")
            }
        }
    }

    fun resetState() {
        if (_authState.value is AuthState.Error || _authState.value is AuthState.OtpSent) {
            _authState.value = AuthState.Idle
        }
    }
}