package com.chinmaib.sportconnect.ui.home

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.core.net.toUri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.chinmaib.sportconnect.ui.theme.*
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToCreator: () -> Unit,
    onNavigateToRoster: () -> Unit,
    onNavigateToMatches: () -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(2) } // Default to 'HOME' (Index 2)
    val tabs = listOf("EXPLORE", "STATS", "HOME", "SQUAD", "PROFILE")

    LaunchedEffect(Unit) {
        viewModel.refreshAll()
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceContainer.copy(alpha = 0.95f),
                tonalElevation = 0.dp,
                modifier = Modifier.clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            ) {
                tabs.forEachIndexed { index, title ->
                    val icon = when (title) {
                        "EXPLORE" -> Icons.Default.Explore
                        "STATS" -> Icons.Default.BarChart
                        "HOME" -> Icons.Default.Home
                        "SQUAD" -> Icons.Default.Groups
                        "PROFILE" -> Icons.Default.Person
                        else -> Icons.Default.Home
                    }
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(icon, contentDescription = null, modifier = Modifier.size(26.dp)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentGold,
                            unselectedIconColor = TextMuted,
                            indicatorColor = AppPrimaryBrand.copy(alpha = 0.1f),
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToCreator,
                containerColor = AccentGold,
                contentColor = Color.Black,
                shape = RoundedCornerShape(24.dp),
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("CREATE", fontWeight = FontWeight.Bold)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(PrimaryBackground)
                .systemBarsPadding() // DIRECTIVE: Safe Drawing Padding
        ) {
            when (selectedTab) {
                2 -> DashboardContent(viewModel, onNavigateToRoster, onNavigateToMatches)
                else -> WIPScreen(tabs[selectedTab])
            }
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
    val nearbyMatches by viewModel.nearbyMatches.collectAsState()
    val calendarMatches by viewModel.calendarMatches.collectAsState()
    val films by viewModel.films.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    val sportsList = listOf("All Sports", "Cricket", "Football", "Basketball", "Tennis", "MMA")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // DIRECTIVE: Ad Placeholder at absolute top
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                AdMobBannerPlaceholder()
            }
        }

        // DIRECTIVE: Calendar Integration (Replacing Welcome Text)
        item {
            ExpandableSportsCalendar(
                selectedDate = selectedDate,
                onDateSelected = { viewModel.onDateSelected(it) },
                calendarMatches = calendarMatches
            )
        }

        item {
            LiveMatchBanner(activeMatch)
        }

        item {
            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sportsList) { sport ->
                    val isSelected = selectedSport == sport
                    Surface(
                        onClick = { viewModel.fetchEvents(sport) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) AppPrimaryBrand else SurfaceContainer,
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, ElevatedBorders)
                    ) {
                        Text(
                            text = sport,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                            color = if (isSelected) Color.White else TextPrimary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        item { 
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                SectionHeader("UPCOMING ROSTER", onNavigateToRoster) 
            }
        }
        if (events.isEmpty()) {
            item { EmptyStateMessage("No upcoming events found.") }
        } else {
            items(events.take(3)) { event ->
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    EventCard(event)
                }
            }
        }

        item { 
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                SectionHeader("MATCHES NEAR YOU", onNavigateToMatches) 
            }
        }
        if (nearbyMatches.isEmpty()) {
            item { EmptyStateMessage("No nearby matches found.") }
        } else {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(nearbyMatches.take(4)) { match ->
                        MatchCard(
                            match = MatchData(match.title, match.location ?: "Unknown"),
                            modifier = Modifier.width(200.dp)
                        )
                    }
                }
            }
        }

        item { 
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                SectionHeader("SPORTS FILMS") { } 
            }
        }
        item { SportsFilmsGrid(films) }
    }
}

@Composable
fun ExpandableSportsCalendar(
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    calendarMatches: List<MatchRecord>
) {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val daySdf = SimpleDateFormat("EEE", Locale.getDefault())
    val dateSdf = SimpleDateFormat("dd", Locale.getDefault())
    
    val calendar = Calendar.getInstance()
    val dates = (0..14).map {
        val d = calendar.time
        val formatted = sdf.format(d)
        val day = daySdf.format(d).uppercase()
        val dateNum = dateSdf.format(d)
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        Triple(formatted, day, dateNum)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .animateContentSize()
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(dates) { (formatted, day, dateNum) ->
                val isSelected = selectedDate == formatted
                Column(
                    modifier = Modifier
                        .width(60.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) AppPrimaryBrand else SurfaceContainer)
                        .clickable { 
                            if (isSelected) onDateSelected("") else onDateSelected(formatted) 
                        }
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = day,
                        color = if (isSelected) Color.White else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dateNum,
                        color = if (isSelected) Color.White else TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.size(4.dp).background(Color.White, CircleShape))
                    }
                }
            }
        }

        AnimatedVisibility(visible = selectedDate.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .background(SurfaceContainer.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                    .border(1.dp, ElevatedBorders, RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Events on $selectedDate",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                if (calendarMatches.isEmpty()) {
                    Text(
                        text = "No events scheduled for this date",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        textAlign = TextAlign.Center
                    )
                } else {
                    calendarMatches.forEach { match ->
                        MatchCard(
                            match = MatchData(match.title, match.location ?: "Unknown"),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateMessage(message: String) {
    Text(
        text = message,
        color = TextMuted,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
fun AdMobBannerPlaceholder() {
    Box(
        modifier = Modifier
            .size(width = 320.dp, height = 60.dp) // DIRECTIVE: 60.dp height
            .background(SurfaceContainer, RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = ElevatedBorders,
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "ADVERTISEMENT",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            letterSpacing = 1.5.sp
        )
    }
}

@Composable
fun LiveMatchBanner(activeMatch: MatchRecord?) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(220.dp)
            .clickable {
                val url = if (activeMatch != null) "https://www.hotstar.com/sports/live" else "https://www.hotstar.com/sports/highlights"
                context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
            },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = activeMatch?.image_url ?: "https://images.unsplash.com/photo-1504450758481-7338eba7524a?q=80&w=1000",
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 100f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp), // Premium padding
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (activeMatch != null) {
                        Surface(
                            color = StatusLiveWin,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FlashingDot()
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("LIVE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                    
                    Surface(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Featured",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }

                Column {
                    Text(
                        text = activeMatch?.title ?: "No Live Matches",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 32.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (activeMatch != null) "WATCH NOW" else "HIGHLIGHTS", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                        if (activeMatch != null) {
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("${activeMatch.viewers} watching", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MatchCard(match: MatchData, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ElevatedBorders)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(AppPrimaryBrand.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Sports, contentDescription = null, tint = AppPrimaryBrand)
            }
            Column {
                Text(match.format, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(match.location, color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun EventCard(event: EventRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ElevatedBorders)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = when (event.sport_type) {
                "Cricket" -> Icons.Default.SportsCricket
                "Football" -> Icons.Default.SportsFootball
                "Basketball" -> Icons.Default.SportsBasketball
                else -> Icons.Default.EmojiEvents
            }
            
            Box(
                modifier = Modifier.size(48.dp).background(AppPrimaryBrand.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = AppPrimaryBrand, modifier = Modifier.size(24.dp))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(event.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                event.location?.let {
                    Text(it, color = TextSecondary, fontSize = 13.sp)
                }
            }
            
            Surface(
                color = ElevatedBorders,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = event.date_label,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = AccentGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun SportsFilmsGrid(films: List<FilmRecord>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(films) { film ->
            Column(modifier = Modifier.width(140.dp)) {
                AsyncImage(
                    model = film.image_url,
                    contentDescription = null,
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.dp, ElevatedBorders, RoundedCornerShape(24.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    film.title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, onViewAll: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            color = TextPrimary,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            letterSpacing = 0.5.sp
        )
        Text(
            "See All",
            color = AppPrimaryBrand,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { onViewAll() }
        )
    }
}

@Composable
fun FlashingDot() {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    Canvas(modifier = Modifier.size(8.dp).alpha(alpha)) {
        drawCircle(color = Color.White)
    }
}

@Composable
fun WIPScreen(tabName: String) {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = AppPrimaryBrand.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Coming Soon",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "We're crafting the $tabName experience.",
                textAlign = TextAlign.Center,
                color = TextSecondary
            )
        }
    }
}

data class MatchData(val format: String, val location: String)
