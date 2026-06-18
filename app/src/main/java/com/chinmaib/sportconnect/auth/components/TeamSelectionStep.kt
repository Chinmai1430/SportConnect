package com.chinmaib.sportconnect.auth.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chinmaib.sportconnect.ui.theme.*

data class FamousTeam(val name: String, val sport: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamSelectionStep(
    teams: List<FamousTeam>,
    selectedTeams: MutableList<String>,
    onBack: () -> Unit,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AppPrimaryBrand)
            }
            Text(
                "SELECT TEAMS",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            "Follow your favorite teams to get updates.",
            color = TextSecondary,
            fontSize = 14.sp,
            fontFamily = Montserrat,
            modifier = Modifier.padding(bottom = 24.dp, start = 48.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(teams) { team ->
                val isSelected = selectedTeams.contains(team.name)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (isSelected) selectedTeams.remove(team.name) else selectedTeams.add(team.name)
                        },
                    shape = RoundedCornerShape(24.dp), // Bento Shape
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) AppPrimaryBrand.copy(alpha = 0.1f) else SurfaceContainer
                    ),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, AppPrimaryBrand) else androidx.compose.foundation.BorderStroke(1.dp, ElevatedBorders)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(team.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(team.sport, color = TextSecondary, fontSize = 12.sp)
                        }
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = {
                                if (isSelected) selectedTeams.remove(team.name) else selectedTeams.add(team.name)
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = AppPrimaryBrand,
                                checkmarkColor = Color.White,
                                uncheckedColor = TextMuted
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = Color.Black),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text("FINISH SETUP", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, fontFamily = Montserrat)
        }
    }
}
