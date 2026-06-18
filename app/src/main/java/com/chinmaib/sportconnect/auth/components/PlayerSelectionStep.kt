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
import coil.compose.AsyncImage
import com.chinmaib.sportconnect.auth.FamousPlayer
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
            color = TextSecondary,
            fontSize = 10.sp,
            fontFamily = Montserrat,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(top = 6.dp, bottom = 20.dp),
            textAlign = TextAlign.Center
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            placeholder = {
                Text(
                    text = "Search players...",
                    color = TextMuted,
                    fontFamily = Montserrat,
                    fontSize = 14.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = TextSecondary
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Clear",
                            tint = TextSecondary
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = AppPrimaryBrand,
                unfocusedBorderColor = ElevatedBorders,
                cursorColor = AppPrimaryBrand,
                focusedContainerColor = SurfaceContainer,
                unfocusedContainerColor = SurfaceContainer
            ),
            shape = RoundedCornerShape(18.dp),
            singleLine = true
        )

        if (selectedPlayers.isNotEmpty()) {
            Text(
                text = "${selectedPlayers.size} player${if (selectedPlayers.size > 1) "s" else ""} selected",
                color = AppPrimaryBrand,
                fontFamily = Montserrat,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            )
        }

        if (players.isEmpty()) {
            Text(
                text = "No players found for your selected sports.\nYou can skip this step.",
                color = TextSecondary,
                textAlign = TextAlign.Center,
                fontFamily = Montserrat,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 32.dp, bottom = 32.dp)
            )
        } else if (displayedPlayers.isEmpty() && searchQuery.length >= 2) {
            Text(
                text = "No players match \"$searchQuery\"",
                color = TextSecondary,
                textAlign = TextAlign.Center,
                fontFamily = Montserrat,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 32.dp, bottom = 32.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
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
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AppPrimaryBrand.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "SKIP",
                    color = AppPrimaryBrand,
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
                    containerColor = AccentGold,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(18.dp),
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
                color = AccentGold,
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
                color = if (isSelected) AppPrimaryBrand.copy(alpha = 0.1f) else Color.Transparent,
                shape = RoundedCornerShape(24.dp) // Bento Shape
            )
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) AppPrimaryBrand else ElevatedBorders,
                shape = RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) AppPrimaryBrand else ElevatedBorders,
                    shape = CircleShape
                )
                .background(SurfaceContainer),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = player.imageUrl,
                contentDescription = player.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                alpha = if (isSelected) 1f else 0.8f
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = player.name,
            color = if (isSelected) AppPrimaryBrand else TextPrimary,
            fontFamily = Montserrat,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )

        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Selected",
                tint = AppPrimaryBrand,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
