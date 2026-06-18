package com.chinmaib.sportconnect.ui.creator

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chinmaib.sportconnect.ui.theme.*
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SportsCreatorScreen(
    viewModel: CreatorViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var matchTitle by remember { mutableStateOf("") }
    var sportType by remember { mutableStateOf("") }
    var teamName by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
    var dateError by remember { mutableStateOf<String?>(null) }

    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val submitSuccess by viewModel.submitSuccess.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(submitSuccess) {
        if (submitSuccess) {
            onBack()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiErrorState.collectLatest { errorMessage ->
            snackbarHostState.showSnackbar(errorMessage)
        }
    }

    val calendar = Calendar.getInstance()
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val pickedDate = Calendar.getInstance().apply {
                set(year, month, dayOfMonth, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (pickedDate.before(today)) {
                dateError = "Please select today or a future date"
                selectedDate = ""
            } else {
                dateError = null
                selectedDate = sdf.format(pickedDate.time)
            }
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        datePicker.minDate = System.currentTimeMillis() - 1000
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Create Event", 
                        color = Color.White, 
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AppPrimaryBrand)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBackground)
            )
        },
        containerColor = PrimaryBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(24.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                "New Sport Event", 
                style = MaterialTheme.typography.headlineSmall, 
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = matchTitle,
                onValueChange = { matchTitle = it },
                label = { Text("Match Title") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppPrimaryBrand,
                    focusedLabelColor = AppPrimaryBrand,
                    unfocusedBorderColor = ElevatedBorders,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    unfocusedLabelColor = TextSecondary
                )
            )

            OutlinedTextField(
                value = sportType,
                onValueChange = { sportType = it },
                label = { Text("Sport Type") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppPrimaryBrand,
                    focusedLabelColor = AppPrimaryBrand,
                    unfocusedBorderColor = ElevatedBorders,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    unfocusedLabelColor = TextSecondary
                )
            )

            OutlinedTextField(
                value = teamName,
                onValueChange = { teamName = it },
                label = { Text("Team/Venue") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppPrimaryBrand,
                    focusedLabelColor = AppPrimaryBrand,
                    unfocusedBorderColor = ElevatedBorders,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    unfocusedLabelColor = TextSecondary
                )
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedDate,
                    onValueChange = { },
                    label = { Text("Date & Time") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    isError = dateError != null,
                    shape = RoundedCornerShape(18.dp),
                    trailingIcon = {
                        IconButton(onClick = { datePickerDialog.show() }) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "Pick Date", tint = AppPrimaryBrand)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppPrimaryBrand,
                        focusedLabelColor = AppPrimaryBrand,
                        unfocusedBorderColor = ElevatedBorders,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        unfocusedLabelColor = TextSecondary,
                        errorBorderColor = StatusLossError
                    )
                )
            }
            if (dateError != null) {
                Text(dateError!!, color = StatusLossError, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { 
                    viewModel.createEvent(matchTitle, sportType, teamName, selectedDate)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = Color.Black),
                shape = RoundedCornerShape(18.dp),
                enabled = selectedDate.isNotBlank() && matchTitle.isNotBlank() && !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                } else {
                    Text("CREATE EVENT", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }
        }
    }
}