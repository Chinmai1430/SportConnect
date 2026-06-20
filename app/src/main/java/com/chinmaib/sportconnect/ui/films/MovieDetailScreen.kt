package com.chinmaib.sportconnect.ui.films

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.chinmaib.sportconnect.ui.home.FilmRecord
import com.chinmaib.sportconnect.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    film: FilmRecord,
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = PrimaryBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(film.title, fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                ),
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(450.dp)
            ) {
                AsyncImage(
                    model = film.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, PrimaryBackground),
                                startY = 300f
                            )
                        )
                )
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .offset(y = (-40).dp)
            ) {
                Text(
                    text = film.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Montserrat
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${film.releaseYear ?: "2023"} • Sports / Documentary",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        fontFamily = OpenSans
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = { /* Placeholder for trailer */ },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("WATCH TRAILER", fontWeight = FontWeight.Bold, fontFamily = Montserrat)
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = "SYNOPSIS",
                    color = AccentGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    fontFamily = Montserrat
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = film.synopsis ?: "A compelling look into the world of professional sports, exploring the highs and lows of the competitive spirit through the eyes of legendary athletes and rising stars.",
                    color = TextSecondary,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    fontFamily = OpenSans
                )
            }
        }
    }
}
