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

    private fun isConfigValid(): Boolean {
        return (BuildConfig.SUPABASE_URL.isNotBlank() &&
                BuildConfig.SUPABASE_ANON_KEY.isNotBlank())
    }

    private fun emitConfigError() {
        _authState.value = AuthState.Error("Supabase keys missing! Please add them to local.properties and rebuild.")
    }

    init {
        val user = auth.currentUserOrNull()
        if (user != null) {
            checkProfileStatus()
        }
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
                // Fetch only the boolean first to avoid serialization issues with complex types
                val response = postgrest["profiles"]
                    .select(columns = Columns.list("onboarding_completed")) {
                        filter {
                            eq("id", user.id)
                        }
                    }
                
                // Log the response or check manually
                val profile = response.decodeSingleOrNull<UserProfile>()

                if (profile?.onboardingCompleted == true) {
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.OnboardingRequired
                }
            } catch (_: Exception) {
                // If the error is exactly about decoding, we fallback to onboarding
                _authState.value = AuthState.OnboardingRequired
            }
        }
    }

    fun sendOtp(emailInput: String) {
        if (!isConfigValid()) { emitConfigError(); return }
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
        if (!isConfigValid()) { emitConfigError(); return }
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
        if (!isConfigValid()) { emitConfigError(); return }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.signInWith(Google, redirectUrl = redirectUrl)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Google Sign-In failed.")
            }
        }
    }

    // Native Google Sign-In is reserved for future implementation using Credential Manager
    /*
    fun loginWithGoogleNative(idToken: String, nonce: String?) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.signInWith(IDToken) {
                    this.idToken = idToken
                    this.nonce = nonce
                }
                checkProfileStatus()
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Native Google Sign-In failed.")
            }
        }
    }
    */

    fun sendPasswordResetOtp(emailInput: String) {
        if (!isConfigValid()) { emitConfigError(); return }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                // We use OTP sign in as a bridge to verify the email and get a session, 
                // allowing the user to then update their password.
                auth.signInWith(OTP) {
                    email = emailInput
                    createUser = false
                }
                _authState.value = AuthState.ResetOtpSent
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Failed to send reset code.")
            }
        }
    }

    fun verifyResetOtp(emailInput: String, otpInput: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.verifyEmailOtp(
                    type = OtpType.Email.EMAIL,
                    email = emailInput,
                    token = otpInput,
                )
                _authState.value = AuthState.ResetOtpVerified
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Invalid reset code.")
            }
        }
    }

    fun updatePassword(newPasswordInput: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.updateUser {
                    password = newPasswordInput
                }
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Failed to update password.")
            }
        }
    }

    fun saveProfile(
        imageUri: android.net.Uri?,
        fullName: String,
        dob: String,
        phone: String,
        context: android.content.Context
    ) {
        val user = auth.currentUserOrNull() ?: return

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                var avatarUrl: String? = null

                // 1. Upload image if available
                imageUri?.let { uri ->
                    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    if (bytes != null) {
                        val fileName = "${user.id}/avatar.jpg"
                        storage["avatars"].upload(fileName, bytes) {
                            upsert = true
                        }
                        avatarUrl = storage["avatars"].publicUrl(fileName)
                    }
                }

                // 2. Update profile in database
                val profileUpdate = ProfileUpdate(
                    id = user.id,
                    fullName = fullName,
                    dob = dob,
                    phone = phone,
                    onboardingCompleted = true,
                    avatarUrl = avatarUrl
                )

                postgrest["profiles"].upsert(profileUpdate)

                _authState.value = AuthState.Authenticated
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Failed to save profile")
            }
        }
    }

    fun resetState() {
        if ((_authState.value is AuthState.Error) || 
            (_authState.value is AuthState.OtpSent) || 
            (_authState.value is AuthState.Success) ||
            (_authState.value is AuthState.ResetOtpSent)
        ) {
            _authState.value = AuthState.Idle
        }
    }
}
