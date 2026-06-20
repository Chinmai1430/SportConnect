package com.chinmaib.sportconnect.ui.home

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.chinmaib.sportconnect.ui.theme.AccentGold
import com.chinmaib.sportconnect.ui.theme.Montserrat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object OnboardingPrefs {
    val HAS_SEEN_NAV_TUTORIAL = booleanPreferencesKey("has_seen_nav_tutorial")
}

class SpeechBubbleShape(private val tailOffset: Dp) : androidx.compose.ui.graphics.Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
        density: androidx.compose.ui.unit.Density,
    ): androidx.compose.ui.graphics.Outline {
        val rectHeight = size.height - with(density) { 12.dp.toPx() }
        val triangleWidth = with(density) { 24.dp.toPx() }
        val cornerRadius = with(density) { 16.dp.toPx() }
        val tailCenter = with(density) { tailOffset.toPx() }

        val path = Path().apply {
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    left = 0f,
                    top = 0f,
                    right = size.width,
                    bottom = rectHeight,
                    radiusX = cornerRadius,
                    radiusY = cornerRadius,
                )
            )

            // Triangle tail pointing down at tailOffset
            moveTo(tailCenter - (triangleWidth / 2), rectHeight)
            lineTo(tailCenter, size.height)
            lineTo(tailCenter + (triangleWidth / 2), rectHeight)
            close()
        }
        return androidx.compose.ui.graphics.Outline.Generic(path)
    }
}

@Composable
fun TooltipOverlay(
    currentStep: Int,
    innerPadding: PaddingValues,
    onNext: () -> Unit
) {
    val tooltipText = when (currentStep) {
        0 -> "Explore: Discover news & feeds."
        1 -> "Stats: View performance metrics."
        2 -> "Home: Central dashboard."
        3 -> "Squad: Manage teams & chat."
        4 -> "Profile: Update settings."
        else -> ""
    }

    if (currentStep <= 4) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(100f)
                .clickable { onNext() }
        ) {
            val segmentWidth = maxWidth / 5
            val targetIconCenterX = (segmentWidth * currentStep) + (segmentWidth / 2)
            val tooltipWidth = 160.dp
            
            // Clamps the X position to prevent edge overflow
            val tooltipLeftOffset = (targetIconCenterX - (tooltipWidth / 2)).coerceIn(
                minimumValue = 8.dp,
                maximumValue = maxWidth - tooltipWidth - 8.dp
            )
            
            val relativeTailOffset = targetIconCenterX - tooltipLeftOffset

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = innerPadding.calculateBottomPadding() + 8.dp)
                    .offset(x = tooltipLeftOffset)
                    .width(tooltipWidth)
            ) {
                Surface(
                    color = AccentGold,
                    shape = SpeechBubbleShape(relativeTailOffset),
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = tooltipText,
                        modifier = Modifier.padding(12.dp).padding(bottom = 8.dp),
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        fontFamily = Montserrat,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            }
            
            Text(
                text = "Tap to continue (${currentStep + 1}/5)",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
                fontFamily = Montserrat,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            )
        }
    }
}
