package com.chinmaib.sportconnect.auth.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.chinmaib.sportconnect.auth.FamousPlayer
import com.chinmaib.sportconnect.auth.Montserrat
import com.chinmaib.sportconnect.ui.theme.*

@Composable
fun PlayerSelectionStep(
    players: List<FamousPlayer>,
    selectedPlayers: MutableList<String>,
    onBack: () -> Unit,
    onFinish: () -> Unit,
    onSkip: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val displayedPlayers = remember(searchQuery, players) {
        if (searchQuery.length >= 2) {
            players.filter { it.name.contains(searchQuery, ignoreCase = true) }
        } else {
            players
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "FOLLOW PLAYERS",
            color = Color.White,
            fontSize = 28.sp,
            fontFamily = Montserrat,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center
        )

        Text(
            text = "GET THE LATEST NEWS FROM YOUR FAVOURITES",
            color = CoolTeal,
            fontSize = 10.sp,
            fontFamily = Montserrat,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(top = 6.dp, bottom = 20.dp),
            textAlign = TextAlign.Center
        )

        // ── Search bar ────────────────────────────────────────────────────
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            placeholder = {
                Text(
                    text = "Search players...",
                    color = CoolTeal.copy(alpha = 0.5f),
                    fontFamily = Montserrat,
                    fontSize = 14.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = CoolTeal
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Clear",
                            tint = CoolTeal.copy(alpha = 0.7f)
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = CoolTeal,
                unfocusedBorderColor = TurfGreen.copy(alpha = 0.4f),
                cursorColor = Saffron,
                focusedContainerColor = Color(0x0F74C2BD),
                unfocusedContainerColor = Color(0x0F74C2BD)
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        // ── Selected count indicator ──────────────────────────────────────
        if (selectedPlayers.isNotEmpty()) {
            Text(
                text = "${selectedPlayers.size} player${if (selectedPlayers.size > 1) "s" else ""} selected",
                color = Saffron,
                fontFamily = Montserrat,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            )
        }

        // ── Empty state ───────────────────────────────────────────────────
        if (players.isEmpty()) {
            Text(
                text = "No players found for your selected sports.\nYou can skip this step.",
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                fontFamily = Montserrat,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 32.dp, bottom = 32.dp)
            )
        } else if (displayedPlayers.isEmpty() && searchQuery.length >= 2) {
            Text(
                text = "No players match \"$searchQuery\"",
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                fontFamily = Montserrat,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 32.dp, bottom = 32.dp)
            )
        } else {
            // ── Player list ───────────────────────────────────────────────
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(displayedPlayers) { player ->
                    val isSelected = selectedPlayers.contains(player.name)
                    PlayerListItem(
                        player = player,
                        isSelected = isSelected,
                        onClick = {
                            if (isSelected) selectedPlayers.remove(player.name)
                            else selectedPlayers.add(player.name)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Bottom buttons ────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onSkip,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Saffron.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "SKIP",
                    color = Saffron,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = onFinish,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Saffron,
                    contentColor = DeepForestNightEnd
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = "DONE",
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        TextButton(
            onClick = onBack,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Text(
                text = "BACK TO SPORTS",
                color = CoolTeal,
                fontFamily = Montserrat,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun PlayerListItem(
    player: FamousPlayer,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(
                color = if (isSelected) Color(0x1FFFA500) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = if (isSelected) 1.5.dp else 0.5.dp,
                color = if (isSelected) Saffron else TurfGreen.copy(alpha = 0.25f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circular player photo
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) Saffron else TurfGreen.copy(alpha = 0.4f),
                    shape = CircleShape
                )
                .background(Color(0x1F74C2BD)),
            contentAlignment = Alignment.Center
        ) {
            SubcomposeAsyncImage(
                model = player.imageUrl,
                contentDescription = player.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            ) {
                when (painter.state) {
                    is AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent()
                    else -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = player.name.take(1).uppercase(),
                                color = if (isSelected) Saffron else CoolTeal,
                                fontFamily = Montserrat,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Player name
        Text(
            text = player.name,
            color = if (isSelected) Saffron else Color.White,
            fontFamily = Montserrat,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )

        // Checkmark when selected
        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Selected",
                tint = Saffron,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
