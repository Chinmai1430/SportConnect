package com.chinmaib.sportconnect.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
data class EventRecord(
    val title: String,
    @SerialName("is_live") val isLive: Boolean = false,
    @SerialName("sport_type") val sportType: String,
    @SerialName("image_url") val imageUrl: String? = null,
    val location: String? = null,
    @SerialName("date_label") val dateLabel: String? = null,
)

@Serializable
data class MatchRecord(
    val id: String,
    val title: String,
    val location: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    val viewers: Int = 0,
)

@Serializable
data class FilmRecord(
    val title: String,
    @SerialName("image_url") val imageUrl: String,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val postgrest: Postgrest,
) : ViewModel() {

    private val _selectedSport = MutableStateFlow("All Sports")
    val selectedSport: StateFlow<String> = _selectedSport.asStateFlow()

    private val _events = MutableStateFlow<List<EventRecord>>(emptyList())
    val events: StateFlow<List<EventRecord>> = _events.asStateFlow()

    private val _activeMatch = MutableStateFlow<MatchRecord?>(null)
    val activeMatch: StateFlow<MatchRecord?> = _activeMatch.asStateFlow()

    private val _nearbyMatches = MutableStateFlow<List<MatchRecord>>(emptyList())
    val nearbyMatches: StateFlow<List<MatchRecord>> = _nearbyMatches.asStateFlow()

    private val _calendarMatches = MutableStateFlow<List<MatchRecord>>(emptyList())
    val calendarMatches: StateFlow<List<MatchRecord>> = _calendarMatches.asStateFlow()

    private val _films = MutableStateFlow<List<FilmRecord>>(emptyList())
    val films: StateFlow<List<FilmRecord>> = _films.asStateFlow()

    private val _selectedDate = MutableStateFlow("")
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    init {
        // refreshAll() - Rely on HomeScreen to trigger this once to avoid double fetch
    }

    fun refreshAll() {
        fetchEvents("All Sports")
        fetchActiveMatch()
        fetchNearbyMatches()
        fetchFilms()
    }

    fun fetchEvents(sport: String) {
        _selectedSport.value = sport
        viewModelScope.launch {
            try {
                _events.value = postgrest["events"].select {
                    if (sport != "All Sports") {
                        filter {
                            eq("sport_type", sport)
                        }
                    }
                }.decodeList<EventRecord>()
            } catch (_: Exception) {
                // Silently handle for now
            }
        }
    }

    private fun fetchActiveMatch() {
        viewModelScope.launch {
            try {
                val match = postgrest["matches"]
                    .select(columns = Columns.ALL) {
                        filter {
                            eq("is_live", value = true)
                        }
                    }
                    .decodeSingleOrNull<MatchRecord>()
                _activeMatch.value = match
            } catch (_: Exception) { }
        }
    }

    private fun fetchNearbyMatches() {
        viewModelScope.launch {
            try {
                _nearbyMatches.value = postgrest["matches"]
                    .select()
                    .decodeList<MatchRecord>()
            } catch (_: Exception) { }
        }
    }

    fun onDateSelected(date: String) {
        _selectedDate.value = date
        if (date.isEmpty()) {
            _calendarMatches.value = emptyList()
            return
        }
        viewModelScope.launch {
            try {
                _calendarMatches.value = postgrest["matches"]
                    .select {
                        filter {
                            eq("match_date", date)
                        }
                    }
                    .decodeList<MatchRecord>()
            } catch (_: Exception) { }
        }
    }

    private fun fetchFilms() {
        // Sample static data for films as it might not be a DB table yet
        _films.value = listOf(
            FilmRecord(title = "The Last Dance", imageUrl = "https://images.unsplash.com/photo-1546519638-68e109498ffc?q=80&w=400"),
            FilmRecord(title = "Goal!", imageUrl = "https://images.unsplash.com/photo-1574629810360-7efbbe195018?q=80&w=400"),
            FilmRecord(title = "83", imageUrl = "https://images.unsplash.com/photo-1531415074968-036ba1b575da?q=80&w=400"),
        )
    }
}
