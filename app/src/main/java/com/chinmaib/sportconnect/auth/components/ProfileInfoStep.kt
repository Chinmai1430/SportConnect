@file:Suppress("DEPRECATION")
package com.chinmaib.sportconnect.auth.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import coil.compose.AsyncImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.chinmaib.sportconnect.R
import com.chinmaib.sportconnect.auth.Montserrat
import com.chinmaib.sportconnect.auth.StyledTextField
import com.chinmaib.sportconnect.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileInfoStep(
    userName: String,
    onNameChanged: (String) -> Unit,
    selectedImageUri: Uri?,
    onImageSelected: (Uri?) -> Unit,
    selectedDob: String,
    onDobSelected: (String) -> Unit,
    phoneNumber: String,
    onPhoneChanged: (String) -> Unit,
    onNext: () -> Unit
) {
    val showDatePicker = remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val adjustProfileTitle = stringResource(R.string.adjust_profile_title)

    // Automatic Country Code Detection
    LaunchedEffect(Unit) {
        if (phoneNumber.isEmpty()) {
            val countryCode = Locale.getDefault().country
            val dialCode = when (countryCode) {
                "IN" -> "+91 "
                "US" -> "+1 "
                "GB" -> "+44 "
                "AU" -> "+61 "
                "CA" -> "+1 "
                else -> ""
            }
            onPhoneChanged(dialCode)
        }
    }

    val cropImageLauncher = rememberLauncherForActivityResult(contract = CropImageContract()) { result ->
        if (result.isSuccessful) {
            onImageSelected(result.uriContent)
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            val cropOptions = CropImageContractOptions(
                uri,
                CropImageOptions(
                    cropShape = CropImageView.CropShape.OVAL,
                    fixAspectRatio = true,
                    aspectRatioX = 1,
                    aspectRatioY = 1,
                    toolbarColor = "#111111".toColorInt(),
                    activityTitle = adjustProfileTitle,
                    activityMenuIconColor = android.graphics.Color.WHITE,
                    cropMenuCropButtonTitle = "SAVE",
                    backgroundColor = "#111111".toColorInt()
                )
            )
            cropImageLauncher.launch(cropOptions)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.gear_up),
            color = Color.White,
            fontSize = 32.sp,
            fontFamily = Montserrat,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center
        )

        Text(
            text = stringResource(R.string.establish_identity),
            color = TextSecondary,
            fontSize = 12.sp,
            fontFamily = Montserrat,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(top = 8.dp, bottom = 40.dp)
        )

        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(SurfaceCards)
                .border(2.dp, if (selectedImageUri == null) ElevatedBorders else GoldPrimary, CircleShape)
                .clickable {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            if (selectedImageUri == null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = stringResource(R.string.add_photo_desc), tint = TextSecondary, modifier = Modifier.size(40.dp))
                    Text(text = stringResource(R.string.upload), color = TextSecondary, fontFamily = Montserrat, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                }
            } else {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = stringResource(R.string.profile_photo_desc),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceCards, RoundedCornerShape(16.dp))
                .border(1.dp, ElevatedBorders, RoundedCornerShape(16.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            StyledTextField(
                value = userName,
                onValueChange = onNameChanged,
                label = "Full Name",
                placeholder = "Enter your full name"
            )

            Spacer(modifier = Modifier.height(16.dp))

            StyledTextField(
                value = selectedDob,
                onValueChange = {},
                label = stringResource(R.string.dob_label),
                placeholder = stringResource(R.string.dob_placeholder),
                readOnly = true,
                onClick = { showDatePicker.value = true },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = stringResource(R.string.select_date_desc),
                        tint = if (selectedDob.isEmpty()) TextMuted else GoldPrimary
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            StyledTextField(
                value = phoneNumber,
                onValueChange = onPhoneChanged,
                label = "Phone Number",
                placeholder = "e.g. +91 9876543210",
                isNumber = true,
                trailingIcon = { Icon(imageVector = Icons.Filled.Phone, contentDescription = "Phone", tint = TextMuted) }
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onNext,
            enabled = userName.isNotBlank() && selectedDob.isNotBlank() && phoneNumber.length >= 8,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(text = "CONTINUE", fontFamily = Montserrat, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

    if (showDatePicker.value) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker.value = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker.value = false
                    val utcTimeMillis = datePickerState.selectedDateMillis
                    if (utcTimeMillis != null) {
                        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.ROOT).apply {
                            timeZone = TimeZone.getTimeZone("UTC")
                        }
                        onDobSelected(formatter.format(Date(utcTimeMillis)))
                    }
                }) {
                    Text(text = stringResource(R.string.confirm), color = GoldPrimary, fontFamily = Montserrat, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker.value = false }) {
                    Text(text = stringResource(R.string.cancel), color = TextMuted, fontFamily = Montserrat, fontWeight = FontWeight.Bold)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = SurfaceCards),
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    titleContentColor = TextSecondary,
                    headlineContentColor = TextPrimary,
                    weekdayContentColor = TextSecondary,
                    dayContentColor = TextPrimary,
                    todayContentColor = GoldPrimary,
                    todayDateBorderColor = GoldPrimary,
                    selectedDayContainerColor = GoldPrimary,
                    selectedDayContentColor = Color.Black,
                ),
            )
        }
    }
}
