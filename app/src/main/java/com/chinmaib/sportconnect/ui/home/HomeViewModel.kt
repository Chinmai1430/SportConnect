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
    val id: String,
    val title: String,
    val description: String,
    val viewers: String,
    val is_live: Boolean = false,
    val sport_type: String,
    val image_url: String? = null
)

@Serializable
data class EventRecord(
    val id: String,
    val title: String,
    val sport_type: String,
    val date_label: String,
    val location: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val postgrest: Postgrest
) : ViewModel() {

    private val _activeMatch = MutableStateFlow<MatchRecord?>(null)
    val activeMatch = _activeMatch.asStateFlow()

    private val _events = MutableStateFlow<List<EventRecord>>(emptyList())
    val events = _events.asStateFlow()

    private val _selectedSport = MutableStateFlow("All Sports")
    val selectedSport = _selectedSport.asStateFlow()

    init {
        fetchActiveMatch()
        fetchEvents()
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
}