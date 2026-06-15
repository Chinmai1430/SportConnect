@file:Suppress("SpellCheckingInspection")
package com.chinmaib.sportconnect.auth

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chinmaib.sportconnect.R
import com.chinmaib.sportconnect.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    // Animation States
    val scale = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        // ALTRON FIX 1: The Handover Delay.
        // Wait 300ms for the OS-level splash screen to finish its fade-out before moving.
        delay(300L)

        // ALTRON FIX 2: Concurrent Execution.
        // Using 'launch' makes the animations run at the same time for a fluid effect.
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 800,
                ) {
                    OvershootInterpolator(2f).getInterpolation(it)
                },
            )
        }

        launch {
            // Wait just a fraction of a second so the text fades in right as the logo impacts
            delay(400L)
            textAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 600),
            )
        }

        // 3. Hold the screen so the user can admire the branding
        delay(2200L)

        // 4. Trigger the warp to AuthScreen
        onSplashFinished()
    }

    // UI Rendering
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBackground),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(id = R.drawable.pitchup_logo),
                contentDescription = stringResource(R.string.add_photo_desc),
                modifier = Modifier
                    .size(180.dp)
                    .scale(scale.value),
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.app_name).uppercase(),
                color = TextPrimary,
                fontSize = 22.sp,
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                letterSpacing = 6.sp,
                modifier = Modifier.alpha(textAlpha.value),
            )
        }
    }
}
