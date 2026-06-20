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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chinmaib.sportconnect.R
import com.chinmaib.sportconnect.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    // Animation States
    val scale = remember { Animatable(0.5f) } 
    val textAlpha = remember { Animatable(0f) }

    LaunchedEffect(key1 = Unit) {
        delay(50.milliseconds)

        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 600,
                ) {
                    OvershootInterpolator(2f).getInterpolation(it)
                },
            )
        }

        launch {
            delay(200.milliseconds)
            textAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 400),
            )
        }

        delay(1400.milliseconds)

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
                contentDescription = null,
                modifier = Modifier
                    .size(160.dp)
                    .scale(scale.value),
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.app_name).uppercase(),
                color = TextPrimary,
                fontSize = 20.sp,
                fontFamily = Montserrat,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                letterSpacing = 4.sp,
                modifier = Modifier.alpha(textAlpha.value),
            )
        }
    }
}
