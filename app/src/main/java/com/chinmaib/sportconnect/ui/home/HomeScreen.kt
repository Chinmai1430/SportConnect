package com.chinmaib.sportconnect.ui.home

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.chinmaib.sportconnect.ui.theme.*
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToCreator: () -> Unit,
    onNavigateToRoster: () -> Unit,
    onNavigateToMatches: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(2) } // Default to 'HOME' (Index 2)
    val tabs = listOf("POSTS", "SPORTS", "HOME", "CHAT", "PROFILE")

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = SurfaceCards) {
                tabs.forEachIndexed { index, title ->
                    val icon = when (title) {
                        "POSTS" -> Icons.Default.Apps
                        "SPORTS" -> Icons.Default.EmojiEvents
                        "HOME" -> Icons.Default.Home
                        "CHAT" -> Icons.Default.ChatBubble
                        "PROFILE" -> Icons.Default.Person
                        else -> Icons.Default.Home
                    }
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        label = { Text(title, fontSize = 10.sp) },
                        icon = { Icon(icon, contentDescription = null) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GoldPrimary,
                            unselectedIconColor = TextMuted,
                            selectedTextColor = GoldPrimary,
                            unselectedTextColor = TextMuted,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreator,
                containerColor = GoldPrimary,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize().background(PrimaryBackground)) {
            when (selectedTab) {
                2 -> DashboardContent(viewModel, onNavigateToRoster, onNavigateToMatches)
                else -> WIPScreen(tabs[selectedTab])
            }
        }
    }
}

@Composable
fun WIPScreen(tabName: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(64.dp), tint = GoldPrimary)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "$tabName - Work in Progress", style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
        }
    }
}

@Composable
fun DashboardContent(
    viewModel: HomeViewModel,
    onNavigateToRoster: () -> Unit,
    onNavigateToMatches: () -> Unit
) {
    val selectedSport by viewModel.selectedSport.collectAsState()
    val events by viewModel.events.collectAsState()
    val activeMatch by viewModel.activeMatch.collectAsState()

    val sportsList = listOf("All Sports", "Cricket", "Football", "Basketball", "Tennis", "Kabaddi", "Badminton", "Volleyball", "MMA", "Boxing")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // 1. Contextual Header
        item {
            HeaderSection(activeMatch)
        }

        // 2. Sports Filtering System
        item {
            LazyRow(
                modifier = Modifier.padding(vertical = 16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sportsList) { sport ->
                    val isSelected = selectedSport == sport
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.fetchEvents(sport) },
                        label = { Text(sport) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldPrimary,
                            selectedLabelColor = Color.Black,
                            labelColor = TextPrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = ElevatedBorders,
                            selectedBorderColor = GoldPrimary
                        )
                    )
                }
            }
        }

        // 3. Main Content Feed Sections
        item { SectionHeader("UPCOMING ROSTER", onNavigateToRoster) }
        items(events) { event ->
            EventCard(event)
        }

        item { SectionHeader("MATCHES NEAR YOU", onNavigateToMatches) }
        // For prototype, we reuse events filtered logic or show hardcoded matches near you
        items(listOf(
            MatchData("7v7 Football", "Central Park Arena"),
            MatchData("T20 Cricket", "Downtown Stadium")
        )) { match ->
            MatchCard(match)
        }

        item { SectionHeader("SPORTS FILMS", { /* No-op or films list */ }) }
        item { SportsFilmsGrid() }
    }
}

@Composable
fun HeaderSection(activeMatch: MatchRecord?) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        FoodOrderBanner()
        LiveMatchBanner(activeMatch)
    }
}

@Composable
fun FoodOrderBanner() {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth().height(100.dp).clickable {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.swiggy.com/"))
            context.startActivity(intent)
        },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCards),
        border = androidx.compose.foundation.BorderStroke(1.dp, ElevatedBorders)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.LocalPizza, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Hungry during the match?", color = TextPrimary, fontWeight = FontWeight.Bold)
                Text("Order snacks via Swiggy", color = TextSecondary, fontSize = 12.sp)
            }
            Button(
                onClick = { /* Redundant as parent is clickable */ },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("ORDER", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun LiveMatchBanner(activeMatch: MatchRecord?) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                val url = if (activeMatch != null) "https://www.hotstar.com/sports/live" else "https://www.hotstar.com/sports/highlights"
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
    ) {
        // Blurred Stadium Background
        AsyncImage(
            model = activeMatch?.image_url ?: "https://images.unsplash.com/photo-1504450758481-7338eba7524a?q=80&w=1000",
            contentDescription = null,
            modifier = Modifier.fillMaxSize().blur(4.dp),
            contentScale = ContentScale.Crop
        )
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))

        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
            if (activeMatch != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FlashingDot()
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("LIVE", color = StatusLiveWin, fontWeight = FontWeight.Bold)
                }
                
                Column {
                    Text(activeMatch.title, color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                    Text("${activeMatch.description} • ${activeMatch.viewers} Viewers", color = GoldPrimary, fontSize = 14.sp)
                }

                Button(
                    onClick = { /* Trigger Deep Link */ },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("WATCH NOW", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            } else {
                Text("No Live Matches", color = TextPrimary, fontWeight = FontWeight.Bold)
                Text("See Highlights & Other Matches", color = GoldPrimary, fontSize = 14.sp)
                Button(
                    onClick = { /* Fallback link */ },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("SEE HIGHLIGHTS", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun FlashingDot() {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    Canvas(modifier = Modifier.size(10.dp).alpha(alpha)) {
        drawCircle(color = StatusLiveWin)
    }
}

@Composable
fun SectionHeader(title: String, onViewAll: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text("View All", color = GoldPrimary, fontSize = 12.sp, modifier = Modifier.clickable { onViewAll() })
    }
}

@Composable
fun EventCard(event: EventRecord) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCards),
        border = androidx.compose.foundation.BorderStroke(1.dp, ElevatedBorders)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            val icon = when(event.sport_type) {
                "Cricket" -> Icons.Default.SportsCricket
                "Football" -> Icons.Default.SportsFootball
                "Basketball" -> Icons.Default.SportsBasketball
                else -> Icons.Default.EmojiEvents
            }
            Icon(icon, contentDescription = null, tint = GoldPrimary)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(event.title, color = TextPrimary, fontWeight = FontWeight.Medium)
                if (event.location != null) {
                    Text(event.location, color = TextSecondary, fontSize = 12.sp)
                }
            }
            Text(event.date_label, color = GoldPrimary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MatchCard(match: MatchData) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCards),
        border = androidx.compose.foundation.BorderStroke(1.dp, ElevatedBorders)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Sports, contentDescription = null, tint = GoldPrimary)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(match.format, color = TextPrimary, fontWeight = FontWeight.Medium)
                Text(match.location, color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun SportsFilmsGrid() {
    val movies = listOf(
        Movie("Docu-Film 1", "https://via.placeholder.com/300x450/000000/FFD700?text=Movie+1"),
        Movie("Docu-Film 2", "https://via.placeholder.com/300x450/000000/FFD700?text=Movie+2"),
        Movie("Docu-Film 3", "https://via.placeholder.com/300x450/000000/FFD700?text=Movie+3")
    )
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(movies) { movie ->
            Column(modifier = Modifier.width(120.dp)) {
                AsyncImage(
                    model = movie.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.height(180.dp).fillMaxWidth().clip(RoundedCornerShape(8.dp)).border(1.dp, ElevatedBorders, RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(movie.title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

data class MatchData(val format: String, val location: String)
data class Movie(val title: String, val imageUrl: String)
