@file:Suppress("DEPRECATION")
package com.chinmaib.sportconnect.auth

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.chinmaib.sportconnect.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

enum class SetupStep {
    PROFILE_INFO,
    PLAYER_SELECTION
}

data class FamousPlayer(val name: String, val sport: String, val imageUrl: String? = null)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileSetupScreen(
    userName: String,
    onComplete: (Uri?, String, List<String>) -> Unit,
) {
    var currentStep by remember { mutableStateOf(SetupStep.PROFILE_INFO) }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedDob by remember { mutableStateOf("") }
    val selectedSports = remember { mutableStateListOf<String>() }
    val selectedPlayers = remember { mutableStateListOf<String>() }

    val availableSports = listOf("Football", "Basketball", "Cricket", "Volleyball", "Tennis", "Badminton", "Hockey", "Rugby")

    // Mock Data for Famous Players
    @Suppress("SpellCheckingInspection")
    val allPlayers = listOf(
        FamousPlayer("Lionel Messi", "Football"),
        FamousPlayer("Cristiano Ronaldo", "Football"),
        FamousPlayer("LeBron James", "Basketball"),
        FamousPlayer("Stephen Curry", "Basketball"),
        FamousPlayer("Virat Kohli", "Cricket"),
        FamousPlayer("MS Dhoni", "Cricket"),
        FamousPlayer("Roger Federer", "Tennis"),
        FamousPlayer("Rafael Nadal", "Tennis"),
        FamousPlayer("Serena Williams", "Tennis"),
        FamousPlayer("P.V. Sindhu", "Badminton"),
        FamousPlayer("Lin Dan", "Badminton"),
    )

    val filteredPlayers = allPlayers.filter { selectedSports.contains(it.sport) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DeepForestNightStart, DeepForestNightEnd)))
    ) {
        when (currentStep) {
            SetupStep.PROFILE_INFO -> {
                ProfileInfoStep(
                    userName = userName,
                    selectedImageUri = selectedImageUri,
                    onImageSelected = { selectedImageUri = it },
                    selectedDob = selectedDob,
                    onDobSelected = { selectedDob = it },
                    availableSports = availableSports,
                    selectedSports = selectedSports,
                    onNext = { currentStep = SetupStep.PLAYER_SELECTION }
                )
            }
            SetupStep.PLAYER_SELECTION -> {
                PlayerSelectionStep(
                    players = filteredPlayers,
                    selectedPlayers = selectedPlayers,
                    onBack = { currentStep = SetupStep.PROFILE_INFO },
                    onFinish = { onComplete(selectedImageUri, selectedDob, selectedSports.toList()) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileInfoStep(
    userName: String,
    selectedImageUri: Uri?,
    onImageSelected: (Uri?) -> Unit,
    selectedDob: String,
    onDobSelected: (String) -> Unit,
    availableSports: List<String>,
    selectedSports: MutableList<String>,
    onNext: () -> Unit
) {
    val showDatePicker = remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val adjustProfileTitle = stringResource(R.string.adjust_profile_title)

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
                    toolbarColor = "#061710".toColorInt(),
                    activityTitle = adjustProfileTitle,
                    activityMenuIconColor = android.graphics.Color.WHITE,
                    cropMenuCropButtonTitle = "SAVE",
                    backgroundColor = "#061710".toColorInt()
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
            color = CoolTeal,
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
                .background(Color(0x1F74C2BD))
                .border(2.dp, if (selectedImageUri == null) TurfGreen else Saffron, CircleShape)
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

        Text(
            text = userName.ifBlank { stringResource(R.string.new_athlete_default) },
            color = Color.White,
            fontSize = 24.sp,
            fontFamily = Montserrat,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 24.dp, bottom = 32.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0x0F061710), RoundedCornerShape(16.dp))
                .border(1.dp, TurfGreen.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Box(modifier = Modifier.fillMaxWidth()) {
                StyledTextField(
                    value = selectedDob,
                    onValueChange = {},
                    label = stringResource(R.string.dob_label),
                    placeholder = stringResource(R.string.dob_placeholder),
                    trailingIcon = { Icon(imageVector = Icons.Filled.CalendarMonth, contentDescription = stringResource(R.string.select_date_desc), tint = CoolTeal.copy(alpha = 0.7f)) }
                )
                Box(modifier = Modifier.matchParentSize().clickable { showDatePicker.value = true }.background(Color.Transparent))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.sports_label),
                color = CoolTeal,
                fontFamily = Montserrat,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 16.dp, start = 4.dp)
            )

            // Spotify-style Visual Grid for Sports
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                availableSports.forEach { sport ->
                    val isSelected = selectedSports.contains(sport)

                    VisualSelectionCard(
                        name = sport,
                        isSelected = isSelected,
                        onClick = {
                            if (isSelected) {
                                selectedSports.remove(sport)
                            } else {
                                selectedSports.add(sport)
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onNext,
            enabled = selectedSports.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Saffron, contentColor = DeepForestNightEnd),
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
                    Text(text = stringResource(R.string.confirm), color = Saffron, fontFamily = Montserrat, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker.value = false }) {
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlayerSelectionStep(
    players: List<FamousPlayer>,
    selectedPlayers: MutableList<String>,
    onBack: () -> Unit,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "FOLLOW PLAYERS",
            color = Color.White,
            fontSize = 32.sp,
            fontFamily = Montserrat,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center
        )

        Text(
            text = "GET THE LATEST NEWS FROM YOUR FAVORITES",
            color = CoolTeal,
            fontSize = 10.sp,
            fontFamily = Montserrat,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(top = 8.dp, bottom = 40.dp)
        )

        if (players.isEmpty()) {
            Text(
                text = "No players found for your selected sports. You can skip this step.",
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )
        } else {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                players.forEach { player ->
                    val isSelected = selectedPlayers.contains(player.name)

                    VisualSelectionCard(
                        name = player.name,
                        isSelected = isSelected,
                        onClick = {
                            if (isSelected) {
                                selectedPlayers.remove(player.name)
                            } else {
                                selectedPlayers.add(player.name)
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(40.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onFinish, // Skip
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Saffron.copy(alpha = 0.5f))
            ) {
                Text(text = "SKIP", color = Saffron, fontFamily = Montserrat, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onFinish,
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Saffron, contentColor = DeepForestNightEnd),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(text = stringResource(R.string.finalize_registration), fontFamily = Montserrat, fontWeight = FontWeight.Bold)
            }
        }

        TextButton(
            onClick = onBack,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = "BACK TO SPORTS", color = CoolTeal, fontFamily = Montserrat, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun VisualSelectionCard(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(100.dp)
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) Saffron else TurfGreen.copy(alpha = 0.3f),
                    shape = CircleShape
                )
                .background(if (isSelected) DeepForestNightEnd else Color(0x1F74C2BD)),
            contentAlignment = Alignment.Center
        ) {
            // Placeholder for Image
            Text(
                text = name.take(1).uppercase(),
                color = if (isSelected) Saffron else CoolTeal,
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Selected",
                        tint = Saffron,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = name,
            color = if (isSelected) Saffron else Color.White,
            fontFamily = Montserrat,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 16.sp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}