package com.chinmaib.sportconnect.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chinmaib.sportconnect.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RosterListScreen(
    viewModel: HomeViewModel,
    onBack: () -> Unit
) {
    val matches by viewModel.matches.collectAsState()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.fetchMatches()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("UPCOMING ROSTER", fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBackground,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = PrimaryBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(matches) { match ->
                EventCard(match)
            }
        }
    }
}