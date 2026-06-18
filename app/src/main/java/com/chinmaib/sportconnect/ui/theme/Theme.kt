package com.chinmaib.sportconnect.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val SportConnectColorScheme = darkColorScheme(
    primary = AppPrimaryBrand,
    onPrimary = Color.White,
    primaryContainer = SurfaceContainer,
    onPrimaryContainer = AppPrimaryBrand,
    
    secondary = AccentGold,
    onSecondary = Color.Black,
    
    background = PrimaryBackground,
    onBackground = TextPrimary,
    
    surface = SurfaceContainer,
    onSurface = TextPrimary,
    
    surfaceVariant = ElevatedBorders,
    onSurfaceVariant = TextSecondary,
    
    outline = ElevatedBorders,
    error = StatusLossError,
    onError = Color.White,
    
    tertiary = StatusInfo,
    onTertiary = Color.White
)

val BentoShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp) // Premium Bento Box UI
)

@Composable
fun SportConnectTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = SportConnectColorScheme,
        typography = Typography,
        shapes = BentoShapes,
        content = content,
    )
}
