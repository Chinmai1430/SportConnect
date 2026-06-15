package com.chinmaib.sportconnect.ui.creator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
data class NewEvent(
    val title: String,
    val sport_type: String,
    val team_name: String,
    val date_label: String
)

@HiltViewModel
class CreatorViewModel @Inject constructor(
    private val postgrest: Postgrest
) : ViewModel() {

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting = _isSubmitting.asStateFlow()

    private val _submitSuccess = MutableStateFlow(false)
    val submitSuccess = _submitSuccess.asStateFlow()

    fun createEvent(title: String, sport: String, team: String, date: String) {
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val newEvent = NewEvent(
                    title = title,
                    sport_type = sport,
                    team_name = team,
                    date_label = date
                )
                postgrest["events"].insert(newEvent)
                _submitSuccess.value = true
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isSubmitting.value = false
            }
        }
    }
}