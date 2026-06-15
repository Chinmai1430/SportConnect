@file:Suppress("SpellCheckingInspection")
package com.chinmaib.sportconnect.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SportConnectColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = Color.Black,
    primaryContainer = GoldDark,
    onPrimaryContainer = Color.White,
    secondary = GoldLight,
    onSecondary = Color.Black,
    background = PrimaryBackground,
    onBackground = TextPrimary,
    surface = SurfaceCards,
    onSurface = TextPrimary,
    surfaceVariant = ElevatedBorders,
    onSurfaceVariant = TextSecondary,
    outline = ElevatedBorders,
    error = StatusLossError,
    onError = Color.Black,
    tertiary = StatusInfo,
    onTertiary = Color.White
)

@Composable
fun SportConnectTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = SportConnectColorScheme,
        typography = Typography,
        content = content,
    )
}
