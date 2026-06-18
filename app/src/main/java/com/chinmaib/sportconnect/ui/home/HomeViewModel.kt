package com.chinmaib.sportconnect.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
data class MatchRecord(
    val id: String? = null,
    val title: String,
    val description: String,
    val viewers: String,
    val is_live: Boolean = false,
    val sport_type: String,
    val image_url: String? = null,
    val location: String? = null,
    val date_label: String? = null
)

@Serializable
data class EventRecord(
    val id: String? = null,
    val title: String,
    val sport_type: String,
    val date_label: String,
    val location: String? = null
)

@Serializable
data class FilmRecord(
    val id: String? = null,
    val title: String,
    val image_url: String
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val postgrest: Postgrest
) : ViewModel() {

    private val _activeMatch = MutableStateFlow<MatchRecord?>(null)
    val activeMatch = _activeMatch.asStateFlow()

    private val _events = MutableStateFlow<List<EventRecord>>(emptyList())
    val events = _events.asStateFlow()

    private val _nearbyMatches = MutableStateFlow<List<MatchRecord>>(emptyList())
    val nearbyMatches = _nearbyMatches.asStateFlow()

    private val _calendarMatches = MutableStateFlow<List<MatchRecord>>(emptyList())
    val calendarMatches = _calendarMatches.asStateFlow()

    private val _films = MutableStateFlow<List<FilmRecord>>(emptyList())
    val films = _films.asStateFlow()

    private val _selectedSport = MutableStateFlow("All Sports")
    val selectedSport = _selectedSport.asStateFlow()

    private val _selectedDate = MutableStateFlow("")
    val selectedDate = _selectedDate.asStateFlow()

    init {
        refreshAll()
    }

    fun refreshAll() {
        fetchActiveMatch()
        fetchEvents(_selectedSport.value)
        fetchNearbyMatches()
        fetchFilms()
    }

    fun onDateSelected(date: String) {
        _selectedDate.value = date
        if (date.isNotEmpty()) {
            fetchMatchesForDate(date)
        } else {
            _calendarMatches.value = emptyList()
        }
    }

    private fun fetchMatchesForDate(date: String) {
        viewModelScope.launch {
            try {
                val results = postgrest["matches"]
                    .select {
                        filter {
                            eq("date_label", date)
                        }
                    }
                    .decodeList<MatchRecord>()
                _calendarMatches.value = results
            } catch (e: Exception) {
                _calendarMatches.value = emptyList()
            }
        }
    }

    fun fetchActiveMatch() {
        viewModelScope.launch {
            try {
                val match = postgrest["matches"]
                    .select {
                        filter {
                            eq("is_live", true)
                        }
                        limit(1)
                    }
                    .decodeSingleOrNull<MatchRecord>()
                _activeMatch.value = match
            } catch (e: Exception) {
                _activeMatch.value = null
            }
        }
    }

    fun fetchNearbyMatches() {
        viewModelScope.launch {
            try {
                val results = postgrest["matches"]
                    .select {
                        filter {
                            eq("is_live", false)
                        }
                    }
                    .decodeList<MatchRecord>()
                _nearbyMatches.value = results
            } catch (e: Exception) {
                _nearbyMatches.value = emptyList()
            }
        }
    }

    fun fetchEvents(sport: String = "All Sports") {
        _selectedSport.value = sport
        viewModelScope.launch {
            try {
                val results = if (sport == "All Sports") {
                    postgrest["events"].select().decodeList<EventRecord>()
                } else {
                    postgrest["events"].select {
                        filter {
                            eq("sport_type", sport)
                        }
                    }.decodeList<EventRecord>()
                }
                _events.value = results
            } catch (e: Exception) {
                _events.value = emptyList()
            }
        }
    }

    fun fetchFilms() {
        viewModelScope.launch {
            try {
                val results = postgrest["films"].select().decodeList<FilmRecord>()
                _films.value = results
            } catch (e: Exception) {
                // Fallback for demo/schema safety
                _films.value = listOf(
                    FilmRecord(title = "The Last Dance", image_url = "https://images.unsplash.com/photo-1546519638-68e109498ffc?q=80&w=400"),
                    FilmRecord(title = "All or Nothing", image_url = "https://images.unsplash.com/photo-1574629810360-7efbbe195018?q=80&w=400"),
                    FilmRecord(title = "Formula 1", image_url = "https://images.unsplash.com/photo-1533130061792-64b345e4a833?q=80&w=400")
                )
            }
        }
    }
}