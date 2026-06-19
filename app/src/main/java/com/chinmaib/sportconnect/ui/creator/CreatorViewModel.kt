@file:Suppress("unused")
package com.chinmaib.sportconnect.ui.creator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
data class NewEvent(
    val title: String,
    @SerialName("sport_type") val sportType: String,
    @SerialName("team_name") val teamName: String,
    @SerialName("date_label") val dateLabel: String,
)

@HiltViewModel
class CreatorViewModel @Inject constructor(
    private val postgrest: Postgrest,
) : ViewModel() {

    private val _isSubmitting = MutableStateFlow(value = false)
    val isSubmitting = _isSubmitting.asStateFlow()

    private val _submitSuccess = MutableStateFlow(value = false)
    val submitSuccess = _submitSuccess.asStateFlow()

    private val _uiErrorState = MutableSharedFlow<String>()
    val uiErrorState = _uiErrorState.asSharedFlow()

    fun createEvent(title: String, sport: String, team: String, date: String) {
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                // DIRECTIVE: Data Write with Supabase
                val newEvent = NewEvent(
                    title = title,
                    sportType = sport,
                    teamName = team,
                    dateLabel = date,
                )
                postgrest["events"].insert(newEvent)
                _submitSuccess.value = true
            } catch (e: Exception) {
                // DIRECTIVE: Strict try-catch and error emission
                _uiErrorState.emit(e.localizedMessage ?: "Failed to create event. Please try again.")
            } finally {
                _isSubmitting.value = false
            }
        }
    }
}
