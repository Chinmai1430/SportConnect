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
import androidx.compose.ui.draw.clip
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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = GoldPrimary)
            }
            Text(
                "Select Favorite Teams",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            "Follow your favorite teams to get updates.",
            color = TextSecondary,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 24.dp, start = 48.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(teams) { team ->
                val isSelected = selectedTeams.contains(team.name)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (isSelected) selectedTeams.remove(team.name) else selectedTeams.add(team.name)
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) GoldPrimary.copy(alpha = 0.1f) else SurfaceCards
                    ),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary) else androidx.compose.foundation.BorderStroke(1.dp, ElevatedBorders)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(team.name, color = TextPrimary, fontWeight = FontWeight.Bold)
                            Text(team.sport, color = TextSecondary, fontSize = 12.sp)
                        }
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = {
                                if (isSelected) selectedTeams.remove(team.name) else selectedTeams.add(team.name)
                            },
                            colors = CheckboxDefaults.colors(checkedColor = GoldPrimary, checkmarkColor = Color.Black, uncheckedColor = TextMuted)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("FINISH", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        }
    }
}
