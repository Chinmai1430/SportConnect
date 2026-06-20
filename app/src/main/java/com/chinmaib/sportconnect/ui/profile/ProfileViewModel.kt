package com.chinmaib.sportconnect.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chinmaib.sportconnect.auth.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
data class FullProfile(
    val id: String,
    @SerialName("full_name") val fullName: String,
    val email: String? = null,
    val dob: String? = null,
    val phone: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("onboarding_completed") val onboardingCompleted: Boolean = false,
    val bio: String? = "Professional Athlete",
    val location: String? = "Global",
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val auth: Auth,
    private val postgrest: Postgrest,
    private val storage: Storage,
) : ViewModel() {

    private val _profileState = MutableStateFlow<FullProfile?>(null)
    val profileState: StateFlow<FullProfile?> = _profileState.asStateFlow()

    private val _uiState = MutableStateFlow<AuthState>(AuthState.Idle)
    val uiState: StateFlow<AuthState> = _uiState.asStateFlow()

    init {
        fetchProfile()
    }

    fun fetchProfile() {
        val user = auth.currentUserOrNull() ?: return
        viewModelScope.launch {
            _uiState.value = AuthState.Loading
            try {
                val profile = postgrest["profiles"]
                    .select {
                        filter {
                            eq("id", user.id)
                        }
                    }
                    .decodeSingle<FullProfile>()
                
                // Merge with email from Auth
                _profileState.value = profile.copy(email = user.email)
                _uiState.value = AuthState.Idle
            } catch (_: Exception) {
                _uiState.value = AuthState.Error("Failed to fetch profile")
            }
        }
    }

    fun updateProfile(
        imageUri: Uri?,
        fullName: String,
        dob: String,
        phone: String,
        bio: String,
        location: String,
        context: android.content.Context,
    ) {
        val user = auth.currentUserOrNull() ?: return
        viewModelScope.launch {
            _uiState.value = AuthState.Loading
            try {
                var avatarUrl = _profileState.value?.avatarUrl

                // 1. Image Upload
                imageUri?.let { uri ->
                    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    if (bytes != null) {
                        val fileName = "${user.id}/avatar_${System.currentTimeMillis()}.jpg"
                        storage["avatars"].upload(fileName, bytes) { upsert = true }
                        avatarUrl = storage["avatars"].publicUrl(fileName)
                    }
                }

                // 2. Database Update
                val update = FullProfile(
                    id = user.id,
                    fullName = fullName,
                    dob = dob,
                    phone = phone,
                    bio = bio,
                    location = location,
                    avatarUrl = avatarUrl,
                    onboardingCompleted = true,
                )

                postgrest["profiles"].upsert(update)
                fetchProfile() // Refresh local state
                _uiState.value = AuthState.Success
            } catch (_: Exception) {
                _uiState.value = AuthState.Error("Failed to update profile")
            }
        }
    }

    fun resetUiState() {
        _uiState.value = AuthState.Idle
    }
}
