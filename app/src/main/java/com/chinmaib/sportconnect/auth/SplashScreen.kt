package com.chinmaib.sportconnect.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chinmaib.sportconnect.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {

    // --- Sequence State Managers ---
    val logoScale = remember { Animatable(1.6f) }
    val textAlpha = remember { Animatable(0f) }

    // --- The Cinematic Boot Sequence Engine ---
    LaunchedEffect(Unit) {
        // Phase 1: Logo glides back into place (2.5 seconds)
        logoScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 2500,
                easing = FastOutSlowInEasing
            )
        )

        // Phase 2: The App Name slowly, deliberately fades in (1.2 seconds)
        textAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1200,
                easing = LinearEasing
            )
        )

        // Phase 3: Hold for a tiny fraction of a second so the typography visually settles, then route
        delay(200)
        onSplashComplete()
    }

    // --- UI Rendering ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(DeepForestNightStart, DeepForestNightEnd)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        val logoSizePx = 250.dp

        // --- The Logo Layer ---
        Image(
            painter = painterResource(id = R.drawable.pitchup_logo),
            contentDescription = "PITCHUP System Logo",
            modifier = Modifier
                .size(logoSizePx)
                .scale(logoScale.value)
        )

        // --- The App Name Typography Layer ---
        Text(
            text = "SPORTCONNECT",
            color = Color.White,
            fontSize = 22.sp,
            fontFamily = Montserrat, // Inherited from AuthScreen typography definitions
            fontWeight = FontWeight.Bold,
            letterSpacing = 6.sp,
            modifier = Modifier
                .offset(y = 140.dp) // Calibrated offset to sit perfectly beneath the rings
                .alpha(textAlpha.value)
        )
    }
}