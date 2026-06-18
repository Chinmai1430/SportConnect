@file:Suppress("SpellCheckingInspection")
package com.chinmaib.sportconnect.auth

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.chinmaib.sportconnect.R
import com.chinmaib.sportconnect.ui.theme.*

// Shared Utilities
enum class PasswordStrength(val labelResId: Int?, val color: Color, val progress: Float) {
    NONE(null, Color.Transparent, 0f),
    WEAK(R.string.password_strength_weak, StatusLossError, 0.33f),
    MODERATE(R.string.password_strength_moderate, AppPrimaryBrand, 0.66f),
    STRONG(R.string.password_strength_strong, StatusLiveWin, 1f)
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

@Composable
fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false,
    isNumber: Boolean = false,
    readOnly: Boolean = false,
    placeholder: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    var isPasswordVisible by remember { mutableStateOf(value = false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label, fontFamily = OpenSans) },
        placeholder = placeholder?.let { { Text(text = it, fontFamily = OpenSans) } },
        visualTransformation = if (isPassword && !isPasswordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = if (isNumber) KeyboardType.Number else KeyboardType.Text),
        readOnly = readOnly,
        trailingIcon = {
            if (isPassword) {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (isPasswordVisible) stringResource(R.string.hide_password_desc) else stringResource(R.string.show_password_desc),
                        tint = AppPrimaryBrand.copy(alpha = 0.7f),
                    )
                }
            } else {
                trailingIcon?.invoke()
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        enabled = onClick == null, 
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AppPrimaryBrand,
            unfocusedBorderColor = ElevatedBorders,
            disabledBorderColor = ElevatedBorders,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            disabledTextColor = TextPrimary,
            cursorColor = AppPrimaryBrand,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedLabelColor = AppPrimaryBrand,
            unfocusedLabelColor = TextSecondary,
            disabledLabelColor = TextSecondary,
            focusedPlaceholderColor = TextMuted,
            unfocusedPlaceholderColor = TextMuted,
            disabledPlaceholderColor = TextMuted
        ),
        shape = RoundedCornerShape(18.dp), // DIRECTIVE: Standardized rounded corners
        singleLine = true,
    )
}
