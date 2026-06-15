package com.chinmaib.sportconnect.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.chinmaib.sportconnect.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchesListScreen(
    onBack: () -> Unit
) {
    val matches = listOf(
        MatchData("7v7 Football", "Central Park Arena"),
        MatchData("T20 Cricket", "Downtown Stadium"),
        MatchData("Basketball 3v3", "Community Center"),
        MatchData("Tennis Singles", "City Courts")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Matches Near You", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = GoldPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBackground)
            )
        },
        containerColor = PrimaryBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues).fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(matches) { match ->
                MatchCard(match)
            }
        }
    }
}