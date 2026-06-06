@file:Suppress("DEPRECATION")
package com.chinmaib.sportconnect.auth

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import androidx.compose.ui.res.stringResource
import androidx.core.graphics.toColorInt
import com.chinmaib.sportconnect.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileSetupScreen(
    userName: String,
    onComplete: (Uri?, String, List<String>) -> Unit,
) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // ALTRON INJECTION: DOB Logic States
    var showDatePicker by remember { mutableStateOf(value = false) }
    val datePickerState = rememberDatePickerState()
    var selectedDob by remember { mutableStateOf("") }

    // ALTRON INJECTION: Sports Engine States
    val availableSports = listOf("Football", "Basketball", "Cricket", "Volleyball", "Throwball", "Tennis", "Badminton", "Hockey", "Rugby", "Athletics", "Baseball")
    var sportsSearchQuery by remember { mutableStateOf("") }
    val selectedSports = remember { mutableStateListOf<String>() }

    val adjustProfileTitle = stringResource(R.string.adjust_profile_title)

    // ALTRON INJECTION: Tier-1 Native Cropper Matrix
    val cropImageLauncher = rememberLauncherForActivityResult(contract = CropImageContract()) { result ->
        if (result.isSuccessful) {
            selectedImageUri = result.uriContent
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            // Instantly transition picked photo to the Native WhatsApp-style cropper
            val cropOptions = CropImageContractOptions(
                uri,
                CropImageOptions(
                    cropShape = CropImageView.CropShape.OVAL,
                    fixAspectRatio = true,
                    aspectRatioX = 1,
                    aspectRatioY = 1,
                    toolbarColor = "#061710".toColorInt(),
                    activityTitle = adjustProfileTitle,
                    // ALTRON INJECTION: Forces the "OK" checkmark and back arrow to be pure white
                    activityMenuIconColor = android.graphics.Color.WHITE
                )
            )
            cropImageLauncher.launch(cropOptions)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DeepForestNightStart, DeepForestNightEnd)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // Permits scrolling for extended form data
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
                color = CoolTeal,
                fontSize = 12.sp,
                fontFamily = Montserrat,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 40.dp)
            )

            // The Profile Avatar Core
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(Color(0x1F74C2BD))
                    .border(2.dp, if (selectedImageUri == null) TurfGreen else Saffron, CircleShape)
                    .clickable {
                        photoPickerLauncher.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                if (selectedImageUri == null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = stringResource(R.string.add_photo_desc), tint = CoolTeal, modifier = Modifier.size(40.dp))
                        Text(text = stringResource(R.string.upload), color = CoolTeal, fontFamily = Montserrat, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
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

            // ALTRON INJECTION: Dynamic Username parsed from Auth Screen
            Text(
                text = userName.ifBlank { stringResource(R.string.new_athlete_default) },
                color = Color.White,
                fontSize = 24.sp,
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 24.dp, bottom = 32.dp)
            )

            // Extended Detail Form Matrix
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x0F061710), RoundedCornerShape(16.dp))
                    .border(1.dp, TurfGreen.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                // DOB Field Trigger
                Box(modifier = Modifier.fillMaxWidth()) {
                    StyledTextField(
                        value = selectedDob,
                        onValueChange = {},
                        label = stringResource(R.string.dob_label),
                        placeholder = stringResource(R.string.dob_placeholder),
                        trailingIcon = { Icon(imageVector = Icons.Filled.CalendarMonth, contentDescription = stringResource(R.string.select_date_desc), tint = CoolTeal.copy(alpha = 0.7f)) }
                    )
                    // Invisible interception layer to pop the calendar instead of the keyboard
                    Box(modifier = Modifier.matchParentSize().clickable { showDatePicker = true }.background(Color.Transparent))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sports Autocomplete Field
                StyledTextField(
                    value = sportsSearchQuery,
                    onValueChange = { sportsSearchQuery = it },
                    label = stringResource(R.string.sports_label),
                    placeholder = stringResource(R.string.sports_placeholder)
                )

                // Sports Autocomplete Suggestions Dropdown
                if (sportsSearchQuery.isNotEmpty()) {
                    val filtered = availableSports.filter { (it.contains(other = sportsSearchQuery, ignoreCase = true)) && (!selectedSports.contains(it)) }
                    if (filtered.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = DeepForestNightStart),
                            border = BorderStroke(1.dp, TurfGreen.copy(alpha = 0.3f)),
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                filtered.forEach { sport ->
                                    Text(
                                        text = sport,
                                        color = Color.White,
                                        fontFamily = OpenSans,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedSports.add(sport)
                                                sportsSearchQuery = "" // Reset field after selection
                                            }
                                            .padding(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // LinkedIn Style Activity Chips
                if (selectedSports.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        selectedSports.forEach { sport ->
                            AssistChip(
                                onClick = { selectedSports.remove(sport) },
                                label = { Text(text = sport, color = DeepForestNightEnd, fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                                trailingIcon = { Icon(imageVector = Icons.Filled.Close, contentDescription = stringResource(R.string.remove_desc), modifier = Modifier.size(14.dp), tint = DeepForestNightEnd) },
                                colors = AssistChipDefaults.assistChipColors(containerColor = Saffron),
                                shape = RoundedCornerShape(16.dp),
                                border = null,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Database Submission Vector
            Button(
                onClick = { onComplete(selectedImageUri, selectedDob, selectedSports.toList()) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Saffron, contentColor = DeepForestNightEnd),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(text = stringResource(R.string.finalize_registration), fontFamily = Montserrat, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        // Material 3 Calendar Overlay Matrix
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        showDatePicker = false
                        val utcTimeMillis = datePickerState.selectedDateMillis
                        if (utcTimeMillis != null) {
                            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            formatter.timeZone = TimeZone.getTimeZone("UTC")
                            selectedDob = formatter.format(Date(utcTimeMillis))
                        }
                    }) {
                        Text(text = stringResource(R.string.confirm), color = Saffron, fontFamily = Montserrat, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text(text = stringResource(R.string.cancel), color = Color.Gray, fontFamily = Montserrat, fontWeight = FontWeight.Bold)
                    }
                },
                colors = DatePickerDefaults.colors(containerColor = DeepForestNightStart),
            ) {
                DatePicker(
                    state = datePickerState,
                    colors = DatePickerDefaults.colors(
                        titleContentColor = CoolTeal,
                        headlineContentColor = Color.White,
                        weekdayContentColor = CoolTeal,
                        dayContentColor = Color.White,
                        todayContentColor = Saffron,
                        todayDateBorderColor = Saffron,
                        selectedDayContainerColor = Saffron,
                        selectedDayContentColor = DeepForestNightEnd,
                    ),
                )
            }
        }
    }
}