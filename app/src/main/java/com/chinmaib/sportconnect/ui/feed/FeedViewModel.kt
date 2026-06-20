@file:Suppress("unused")
package com.chinmaib.sportconnect.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chinmaib.sportconnect.auth.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
data class PostRecord(
    val id: String,
    val username: String,
    @SerialName("user_avatar") val userAvatar: String,
    @SerialName("media_url") val mediaUrl: String,
    val caption: String,
    @SerialName("likes_count") val likesCount: String = "0",
    @SerialName("comments_count") val commentsCount: String = "0",
    @SerialName("shares_count") val sharesCount: String = "0",
    @SerialName("saves_count") val savesCount: String = "0",
    @SerialName("created_at") val createdAt: String,
    @SerialName("is_suggested") val isSuggested: Boolean = false,
)

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val postgrest: Postgrest,
) : ViewModel() {

    private val _posts = MutableStateFlow<List<PostRecord>>(emptyList())
    val posts: StateFlow<List<PostRecord>> = _posts.asStateFlow()

    private val _uiState = MutableStateFlow<AuthState>(AuthState.Idle)
    val uiState: StateFlow<AuthState> = _uiState.asStateFlow()

    init {
        fetchPosts()
    }

    fun fetchPosts() {
        viewModelScope.launch {
            _uiState.value = AuthState.Loading
            try {
                // Fetch posts from Supabase 'posts' table
                val result = postgrest["posts"]
                    .select()
                    .decodeList<PostRecord>()
                _posts.value = result
                _uiState.value = AuthState.Idle
            } catch (_: Exception) {
                // If table doesn't exist yet, provide sample data for immediate testing
                _posts.value = getSamplePosts()
                _uiState.value = AuthState.Idle
            }
        }
    }

    private fun getSamplePosts(): List<PostRecord> = listOf(
        PostRecord(
            id = "1",
            username = "alex_mercer",
            userAvatar = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?q=80&w=200",
            mediaUrl = "https://images.unsplash.com/photo-1579952363873-27f3bade9f55?q=80&w=800",
            caption = "Just finished an intense 5v5 pickup game! The energy was insane today. Anyone down for more?",
            likesCount = "37.7K",
            commentsCount = "284",
            sharesCount = "882",
            savesCount = "30K",
            createdAt = "April 22",
            isSuggested = true,
        ),
        PostRecord(
            id = "2",
            username = "sarah_j_tennis",
            userAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?q=80&w=200",
            mediaUrl = "https://images.unsplash.com/photo-1622279457486-62dcc4a431d6?q=80&w=800",
            caption = "Looking for a tennis partner around the downtown area. I'm at an intermediate level...",
            likesCount = "12.4K",
            commentsCount = "102",
            sharesCount = "45",
            savesCount = "1.2K",
            createdAt = "April 20",
            isSuggested = true,
        ),
        PostRecord(
            id = "3",
            username = "mj_hoops",
            userAvatar = "https://images.unsplash.com/photo-1530577197743-7adf14294584?q=80&w=200",
            mediaUrl = "https://images.unsplash.com/photo-1608245449230-4ac19066d2d0?q=80&w=800",
            caption = "The new Curry 11s perform beautifully on the indoor courts. Highly recommend them if you need ankle support and crazy traction. \uD83D\uDC5F\uD83C\uDFC0",
            likesCount = "89.2K",
            commentsCount = "1.1K",
            sharesCount = "5K",
            savesCount = "12K",
            createdAt = "April 18",
            isSuggested = false,
        ),
    )
}
