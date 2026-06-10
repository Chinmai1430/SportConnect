@file:Suppress("SpellCheckingInspection")
package com.chinmaib.sportconnect.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.chinmaib.sportconnect.R
import com.chinmaib.sportconnect.ui.theme.*

// Shared Fonts
val Montserrat = FontFamily(
    Font(R.font.montserrat_semi_bold, FontWeight.SemiBold),
    Font(R.font.montserrat_bold, FontWeight.Bold),
)

val OpenSans = FontFamily(
    Font(R.font.open_sans_regular, FontWeight.Normal),
    Font(R.font.open_sans_semi_bold, FontWeight.SemiBold),
)

// Shared Utilities
enum class PasswordStrength(val labelResId: Int?, val color: Color, val progress: Float) {
    NONE(null, Color.Transparent, 0f),
    WEAK(R.string.password_strength_weak, Color(0xFFE57373), 0.33f),
    MODERATE(R.string.password_strength_moderate, Saffron, 0.66f),
    STRONG(R.string.password_strength_strong, TurfGreen, 1f)
}

fun calculateStrength(password: String): PasswordStrength {
    if (password.isEmpty()) return PasswordStrength.NONE

    var score = 0
    if (password.length >= 8) score++
    if (password.any { it.isUpperCase() }) score++
    if (password.any { it.isLowerCase() }) score++
    if (password.any { it.isDigit() }) score++
    if (password.any { !it.isLetterOrDigit() }) score++

    return when {
        score <= 2 -> PasswordStrength.WEAK
        score in (3..4) -> PasswordStrength.MODERATE
        score == 5 -> PasswordStrength.STRONG
        else -> PasswordStrength.WEAK
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false,
    isNumber: Boolean = false,
    placeholder: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    var isPasswordVisible by remember { mutableStateOf(value = false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label, color = CoolTeal.copy(alpha = 0.7f), fontFamily = OpenSans) },
        placeholder = placeholder?.let { { Text(text = it, color = CoolTeal.copy(alpha = 0.4f), fontFamily = OpenSans) } },
        visualTransformation = if (isPassword && !isPasswordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = if (isNumber) KeyboardType.Number else KeyboardType.Text),
        trailingIcon = {
            if (isPassword) {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (isPasswordVisible) stringResource(R.string.hide_password_desc) else stringResource(R.string.show_password_desc),
                        tint = CoolTeal.copy(alpha = 0.7f),
                    )
                }
            } else {
                trailingIcon?.invoke()
            }
        },
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = TurfGreen,
            unfocusedBorderColor = TurfGreen.copy(alpha = 0.3f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Saffron,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
        ),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
    )
}
