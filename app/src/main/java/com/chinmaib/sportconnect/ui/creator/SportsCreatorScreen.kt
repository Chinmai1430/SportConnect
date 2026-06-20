package com.chinmaib.sportconnect.ui.creator

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chinmaib.sportconnect.auth.StyledTextField
import com.chinmaib.sportconnect.ui.theme.*
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.chinmaib.sportconnect.ui.home.HomeViewModel
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SportsCreatorScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var sportType by remember { mutableStateOf("") }
    var teamName by remember { mutableStateOf("") }
    var dateLabel by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }
    
    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                // Allow today and future dates only
                return utcTimeMillis >= System.currentTimeMillis() - 86400000
            }
        }
    )

    val locale = androidx.compose.ui.platform.LocalConfiguration.current.locales[0]
    val sdf = remember(locale) { SimpleDateFormat("dd/MM/yyyy", locale) }
    
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.submitSuccess.collectLatest { success ->
            if (success) {
                onBack()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiErrorState.collectLatest { error ->
            snackbarHostState.showSnackbar(error)
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        dateLabel = sdf.format(Date(it))
                    }
                    showDatePicker = false
                }) {
                    Text("OK", color = AccentGold, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("CANCEL", color = TextMuted)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = SurfaceContainer)
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = AppPrimaryBrand,
                    todayDateBorderColor = AppPrimaryBrand,
                    todayContentColor = AppPrimaryBrand
                )
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
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
                    value = sportType,
                    onValueChange = { sportType = it },
                    label = "Sport Type",
                    placeholder = "e.g. Football",
                    trailingIcon = { Icon(Icons.Default.SportsScore, contentDescription = null, tint = TextMuted) },
                )

                StyledTextField(
                    value = teamName,
                    onValueChange = { teamName = it },
                    label = "Team Name",
                    placeholder = "e.g. Red Warriors",
                    trailingIcon = { Icon(Icons.Default.Group, contentDescription = null, tint = TextMuted) },
                )

                StyledTextField(
                    value = dateLabel,
                    onValueChange = { },
                    label = "Date",
                    placeholder = "Select Date",
                    readOnly = true,
                    onClick = { showDatePicker = true },
                    trailingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = AccentGold) },
                )
            }

            Button(
                onClick = { viewModel.createEvent(title, sportType, teamName, dateLabel) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = Color.Black),
                shape = RoundedCornerShape(18.dp),
                enabled = title.isNotBlank() && sportType.isNotBlank() && teamName.isNotBlank() && dateLabel.isNotBlank() && !isSubmitting,
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                } else {
                    Text("PUBLISH MATCH", fontFamily = Montserrat, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                }
            }
        }
    }
}
