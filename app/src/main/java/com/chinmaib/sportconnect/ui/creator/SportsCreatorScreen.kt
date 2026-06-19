package com.chinmaib.sportconnect.ui.creator

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chinmaib.sportconnect.auth.StyledTextField
import com.chinmaib.sportconnect.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SportsCreatorScreen(
    onBack: () -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    val sportType by remember { mutableStateOf("Cricket") }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    
    val locale = androidx.compose.ui.platform.LocalConfiguration.current.locales[0]
    val sdf = remember(locale) { SimpleDateFormat("dd/MM/yyyy", locale) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("CREATE MATCH", fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBackground,
                    titleContentColor = Color.White,
                ),
            )
        },
        containerColor = PrimaryBackground,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceContainer, RoundedCornerShape(24.dp))
                    .border(1.dp, ElevatedBorders, RoundedCornerShape(24.dp))
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                StyledTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = "Match Title",
                    placeholder = "e.g. Sunday Morning Friendly",
                )

                StyledTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = "Location",
                    placeholder = "e.g. Central Park Ground",
                    trailingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextMuted) },
                )

                StyledTextField(
                    value = date,
                    onValueChange = { },
                    label = "Date",
                    placeholder = "Select Date",
                    readOnly = true,
                    onClick = {
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val cal = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, year)
                                    set(Calendar.MONTH, month)
                                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                }
                                date = sdf.format(cal.time)
                            },
                            calendar[Calendar.YEAR],
                            calendar[Calendar.MONTH],
                            calendar[Calendar.DAY_OF_MONTH],
                        ).show()
                    },
                    trailingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = AccentGold) },
                )

                StyledTextField(
                    value = time,
                    onValueChange = { },
                    label = "Time",
                    placeholder = "Select Time",
                    readOnly = true,
                    onClick = {
                        TimePickerDialog(
                            context,
                            { _, hourOfDay, minute ->
                                time = String.format(locale, "%02d:%02d", hourOfDay, minute)
                            },
                            calendar[Calendar.HOUR_OF_DAY],
                            calendar[Calendar.MINUTE],
                            true,
                        ).show()
                    },
                    trailingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = AccentGold) },
                )
            }

            Button(
                onClick = { /* Logic to save to Supabase will go here with sportType */ println(sportType) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = Color.Black),
                shape = RoundedCornerShape(18.dp),
                enabled = title.isNotBlank() && location.isNotBlank() && date.isNotBlank() && time.isNotBlank(),
            ) {
                Text("PUBLISH MATCH", fontFamily = Montserrat, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
            }
        }
    }
}
