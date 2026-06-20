package com.chinmaib.sportconnect.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
data class EventRecord(
    val title: String,
    @SerialName("sport_type") val sportType: String,
    @SerialName("team_name") val teamName: String? = null,
    @SerialName("date_label") val dateLabel: String? = null,
    @SerialName("is_live") val isLive: Boolean = false,
    @SerialName("image_url") val imageUrl: String? = null,
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
    val synopsis: String? = null,
    @SerialName("release_year") val releaseYear: String? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val postgrest: Postgrest,
) : ViewModel() {

    private val _selectedSport = MutableStateFlow("All Sports")
    val selectedSport: StateFlow<String> = _selectedSport.asStateFlow()

    private val _matches = MutableStateFlow<List<EventRecord>>(emptyList())
    val matches: StateFlow<List<EventRecord>> = _matches.asStateFlow()

    private val _activeMatch = MutableStateFlow<MatchRecord?>(null)
    val activeMatch: StateFlow<MatchRecord?> = _activeMatch.asStateFlow()

    private val _nearbyMatches = MutableStateFlow<List<MatchRecord>>(emptyList())
    val nearbyMatches: StateFlow<List<MatchRecord>> = _nearbyMatches.asStateFlow()

    private val _calendarMatches = MutableStateFlow<List<MatchRecord>>(emptyList())
    val calendarMatches: StateFlow<List<MatchRecord>> = _calendarMatches.asStateFlow()

    private val _films = MutableStateFlow<List<FilmRecord>>(emptyList())
    val films: StateFlow<List<FilmRecord>> = _films.asStateFlow()

    private val _allEventDates = MutableStateFlow<Set<String>>(emptySet())
    val allEventDates: StateFlow<Set<String>> = _allEventDates.asStateFlow()

    private val _selectedDate = MutableStateFlow("")
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _submitSuccess = MutableSharedFlow<Boolean>()
    val submitSuccess = _submitSuccess.asSharedFlow()

    private val _uiErrorState = MutableSharedFlow<String>()
    val uiErrorState = _uiErrorState.asSharedFlow()

    init {
        // refreshAll() - Rely on HomeScreen to trigger this once to avoid double fetch
    }

    fun refreshAll() {
        fetchMatches()
        fetchActiveMatch()
        fetchNearbyMatches()
        fetchFilms()
        fetchAllEventDates()
    }

    private fun fetchAllEventDates() {
        viewModelScope.launch {
            try {
                val events = postgrest["events"].select(columns = Columns.list("date_label"))
                    .decodeList<EventRecord>()
                _allEventDates.value = events.mapNotNull { it.dateLabel }.toSet()
            } catch (_: Exception) { }
        }
    }

    fun fetchMatches() {
        val sport = _selectedSport.value
        val date = _selectedDate.value
        viewModelScope.launch {
            try {
                _matches.value = postgrest["events"].select {
                    filter {
                        if (sport != "All Sports") {
                            eq("sport_type", sport)
                        }
                        if (date.isNotEmpty()) {
                            eq("date_label", date)
                        }
                    }
                }.decodeList<EventRecord>()
            } catch (_: Exception) {
                _matches.value = emptyList()
            }
        }
    }

    fun fetchEvents(sport: String) {
        _selectedSport.value = sport
        fetchMatches()
    }

    fun onDateSelected(date: String) {
        _selectedDate.value = date
        fetchMatches()
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
                
                // DATA CORRECTION: Ensure fallback URL is valid and HTTPS
                val correctedMatch = match?.copy(
                    imageUrl = match.imageUrl?.takeIf { it.startsWith("https") } 
                        ?: "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?q=80&w=1000"
                )
                _activeMatch.value = correctedMatch
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

    fun createEvent(title: String, sportType: String, teamName: String, date: String) {
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val newEvent = EventRecord(
                    title = title,
                    sportType = sportType,
                    teamName = teamName,
                    dateLabel = date
                )
                postgrest["events"].insert(newEvent)
                fetchMatches() // DIRECTIVE: Refresh UI state
                _submitSuccess.emit(true)
            } catch (e: Exception) {
                _uiErrorState.emit(e.localizedMessage ?: "Failed to publish match")
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    private fun fetchFilms() {
        // Sample real data for films - in production, this should come from Supabase
        _films.value = listOf(
            FilmRecord(
                title = "The Last Dance", 
                imageUrl = "https://www.themoviedb.org/t/p/w600_and_h900_bestv2/8695v9378oUonuYClX6p9C6S8pS.jpg",
                synopsis = "A 10-part documentary series providing an all-access look at Michael Jordan and the 1990s Chicago Bulls.",
                releaseYear = "2020"
            ),
            FilmRecord(
                title = "Goal!", 
                imageUrl = "https://www.themoviedb.org/t/p/w600_and_h900_bestv2/6pYVj6pA8aK7mX2U6o5z0qFq3qW.jpg",
                synopsis = "The story of Santiago Muñez, a young man from Los Angeles who gets the chance to play professional football for Newcastle United.",
                releaseYear = "2005"
            ),
            FilmRecord(
                title = "83", 
                imageUrl = "https://www.themoviedb.org/t/p/w600_and_h900_bestv2/6pD2XoD6Q2kXU0N7m9U7u6n9O3v.jpg",
                synopsis = "The true story of the Indian cricket team's incredible victory at the 1983 World Cup.",
                releaseYear = "2021"
            ),
            FilmRecord(
                title = "Senna",
                imageUrl = "https://www.themoviedb.org/t/p/w600_and_h900_bestv2/xX1hS5M0fG9R1u8u6P6eX1N7a8k.jpg",
                synopsis = "A documentary on Brazilian Formula One racing driver Ayrton Senna, who won the F1 world championship three times before his death at age 34.",
                releaseYear = "2010"
            ),
            FilmRecord(
                title = "Moneyball",
                imageUrl = "https://www.themoviedb.org/t/p/w600_and_h900_bestv2/vS96R2u5nE9aH1K7qX9n8S7W6aL.jpg",
                synopsis = "Oakland A's general manager Billy Beane's successful attempt to assemble a baseball team on a lean budget by employing computer-generated analysis to acquire new players.",
                releaseYear = "2011"
            )
        )
    }
}
