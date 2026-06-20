package com.chinmaib.sportconnect.ui.home

import android.content.Intent
import androidx.compose.animation.*
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
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.chinmaib.sportconnect.ui.theme.*
import com.chinmaib.sportconnect.auth.AuthViewModel
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt

@Immutable
data class MatchData(val format: String, val location: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    onNavigateToCreator: () -> Unit,
    onNavigateToRoster: () -> Unit,
    onNavigateToMatches: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToFilms: () -> Unit,
    onNavigateToFilmDetail: (FilmRecord) -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(2) } 
    val tabs = remember { listOf("EXPLORE", "STATS", "HOME", "SQUAD", "PROFILE") }

    val snackbarHostState = remember { SnackbarHostState() }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val hasSeenTutorialFlow = remember {
        context.dataStore.data.map { it[OnboardingPrefs.HAS_SEEN_NAV_TUTORIAL] ?: false }
    }
    val hasSeenTutorial by hasSeenTutorialFlow.collectAsState(initial = true)
    var showTutorial by remember { mutableStateOf(false) }
    var currentTutorialStep by remember { mutableIntStateOf(0) }
    
    var showSuccessNotification by remember { mutableStateOf(false) }

    LaunchedEffect(hasSeenTutorial) {
        if (!hasSeenTutorial) {
            showTutorial = true
        }
    }
    
    // DIRECTIVE: One-Time Login Success Message
    LaunchedEffect(Unit) {
        authViewModel.oneTimeEvents.collectLatest { message ->
            if (message == "Login Successful" || message == "Registration Successful") {
                showSuccessNotification = true
                kotlinx.coroutines.delay(3000L)
                showSuccessNotification = false
            }
        }
    }

    // PERFORMANCE: Bottom Bar Animation State
    val bottomBarHeight = 80.dp
    val bottomBarHeightPx = with(androidx.compose.ui.platform.LocalDensity.current) { bottomBarHeight.roundToPx().toFloat() }
    val bottomBarOffsetHeightPx = remember { mutableFloatStateOf(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newOffset = bottomBarOffsetHeightPx.floatValue + delta
                bottomBarOffsetHeightPx.floatValue = newOffset.coerceIn(-bottomBarHeightPx, 0f)
                return Offset.Zero
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refreshAll()
    }

    Scaffold(
        modifier = Modifier.nestedScroll(nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Box(
                modifier = Modifier
                    .offset { IntOffset(x = 0, y = -bottomBarOffsetHeightPx.floatValue.roundToInt()) }
            ) {
                HomeNavigationBar(
                    tabs = tabs,
                    selectedTab = selectedTab,
                    isTutorialActive = showTutorial,
                    onboardingStep = currentTutorialStep
                ) { index ->
                    if (!showTutorial) {
                        if (tabs[index] == "PROFILE") {
                            onNavigateToProfile()
                        } else {
                            selectedTab = index
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if (!showTutorial) {
                Box(
                    modifier = Modifier
                        .offset { IntOffset(x = 0, y = -bottomBarOffsetHeightPx.floatValue.roundToInt()) }
                ) {
                    HomeFAB(onClick = onNavigateToCreator)
                }
            }
        },
        containerColor = PrimaryBackground 
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when (selectedTab) {
                2 -> DashboardContent(
                    viewModel = viewModel, 
                    onNavigateToRoster = onNavigateToRoster, 
                    onNavigateToMatches = onNavigateToMatches,
                    onNavigateToFilms = onNavigateToFilms,
                    onNavigateToFilmDetail = onNavigateToFilmDetail,
                )
                else -> WIPScreen(tabs[selectedTab])
            }

            if (showTutorial) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f))
                        .clickable(enabled = false) {} // Absorb clicks
                )
            }
            
            AnimatedVisibility(
                visible = showSuccessNotification,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .zIndex(150f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF1E293B),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF078BDC)),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .padding(top = 48.dp)
                        .padding(horizontal = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = StatusLiveWin,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Login Successful",
                            color = TextPrimary,
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
        if (showTutorial) {
            TooltipOverlay(
                currentStep = currentTutorialStep,
                innerPadding = paddingValues,
                onNext = {
                    if (currentTutorialStep < 4) {
                        currentTutorialStep++
                    } else {
                        scope.launch {
                            context.dataStore.edit { settings ->
                                settings[OnboardingPrefs.HAS_SEEN_NAV_TUTORIAL] = true
                            }
                            showTutorial = false
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun HomeNavigationBar(
    tabs: List<String>,
    selectedTab: Int,
    isTutorialActive: Boolean = false,
    onboardingStep: Int = 0,
    onTabSelected: (Int) -> Unit,
) {
    NavigationBar(
        containerColor = SurfaceContainer.copy(alpha = 0.95f),
        tonalElevation = 0.dp,
        modifier = Modifier.clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
    ) {
        tabs.forEachIndexed { index, title ->
            val icon = remember(title) {
                when (title) {
                    "EXPLORE" -> Icons.Default.Explore
                    "STATS" -> Icons.Default.BarChart
                    "HOME" -> Icons.Default.Home
                    "SQUAD" -> Icons.Default.Groups
                    "PROFILE" -> Icons.Default.Person
                    else -> Icons.Default.Home
                }
            }

            val iconColor = if (isTutorialActive) {
                if (index == onboardingStep) AccentGold else TextSecondary.copy(alpha = 0.3f)
            } else {
                if (selectedTab == index) AccentGold else TextMuted
            }

            NavigationBarItem(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                icon = { 
                    Icon(
                        icon, 
                        contentDescription = null, 
                        modifier = Modifier.size(26.dp),
                        tint = iconColor
                    ) 
                },
                label = {
                    Text(
                        text = title,
                        color = iconColor,
                        fontSize = 10.sp,
                        fontWeight = if (selectedTab == index || (isTutorialActive && index == onboardingStep)) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = iconColor,
                    unselectedIconColor = iconColor,
                    selectedTextColor = iconColor,
                    unselectedTextColor = iconColor,
                    indicatorColor = Color.Transparent,
                ),
            )
        }
    }
}

@Composable
fun HomeFAB(onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        containerColor = AccentGold,
        contentColor = Color.Black,
        shape = RoundedCornerShape(24.dp),
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp, pressedElevation = 12.dp),
    ) {
        Icon(Icons.Default.Add, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("CREATE", fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DashboardContent(
    viewModel: HomeViewModel,
    onNavigateToRoster: () -> Unit,
    onNavigateToMatches: () -> Unit,
    onNavigateToFilms: () -> Unit,
    onNavigateToFilmDetail: (FilmRecord) -> Unit,
) {
    val selectedSport by viewModel.selectedSport.collectAsState()
    val matches by viewModel.matches.collectAsState() 
    val activeMatch by viewModel.activeMatch.collectAsState()
    val nearbyMatches by viewModel.nearbyMatches.collectAsState()
    val calendarMatches by viewModel.calendarMatches.collectAsState()
    val films by viewModel.films.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val allEventDates by viewModel.allEventDates.collectAsState()

    val sportsList = remember { listOf("All Sports", "Cricket", "Football", "Basketball", "Tennis", "MMA") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp), // DIRECTIVE: Reduce dead space
    ) {
        item(key = "ad_banner", contentType = "STATIC_BANNER") {
            AdMobBannerPlaceholder()
        }

        item(key = "calendar", contentType = "EXPANDABLE_SECTION") {
            ExpandableSportsCalendar(
                selectedDate = selectedDate,
                onDateSelected = { viewModel.onDateSelected(it) },
                events = matches,
                datesWithEvents = allEventDates
            )
        }

        item(key = "live_banner", contentType = "FEATURE_CARD") {
            LiveMatchBanner(activeMatch)
        }

        item(key = "sports_selector", contentType = "HORIZONTAL_SELECTOR") {
            SportsSelector(
                sportsList = sportsList,
                selectedSport = selectedSport,
            ) { viewModel.fetchEvents(it) }
        }

        item(key = "roster_header", contentType = "SECTION_HEADER") { 
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                SectionHeader("UPCOMING ROSTER", onNavigateToRoster) 
            }
        }
        
        if (matches.isEmpty()) {
            item(key = "empty_roster", contentType = "STATUS_MSG") { 
                Text(
                    text = "No matches found",
                    color = TextSecondary,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            items(
                items = matches.take(3),
                key = { "match_${it.title}_${it.teamName}_${it.dateLabel}" },
                contentType = { "VERTICAL_EVENT_CARD" },
            ) { match ->
                Box(modifier = Modifier.padding(horizontal = 16.dp).animateItem()) {
                    EventCard(match)
                }
            }
        }

        item(key = "nearby_header", contentType = "SECTION_HEADER") { 
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                SectionHeader("MATCHES NEAR YOU", onNavigateToMatches) 
            }
        }
        
        if (nearbyMatches.isEmpty()) {
            item(key = "empty_nearby", contentType = "STATUS_MSG") { EmptyStateMessage("No nearby matches found.") }
        } else {
            item(key = "nearby_row", contentType = "HORIZONTAL_ROW") {
                NearbyMatchesRow(nearbyMatches.take(4))
            }
        }

        item(key = "films_header", contentType = "SECTION_HEADER") { 
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                SectionHeader("SPORTS FILMS", onNavigateToFilms) 
            }
        }
        item(key = "films_row", contentType = "HORIZONTAL_GRID") { 
            SportsFilmsGrid(films) { film -> onNavigateToFilmDetail(film) } 
        }
    }
}

@Composable
fun SportsSelector(
    sportsList: List<String>,
    selectedSport: String,
    onSportSelected: (String) -> Unit,
) {
    LazyRow(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(sportsList, key = { it }) { sport ->
            val isSelected = selectedSport == sport
            Surface(
                onClick = { onSportSelected(sport) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) AppPrimaryBrand else SurfaceContainer,
                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, ElevatedBorders),
                modifier = Modifier.height(44.dp),
            ) {
                Text(
                    text = sport,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    color = if (isSelected) Color.White else TextPrimary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

@Composable
fun NearbyMatchesRow(matches: List<MatchRecord>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = Modifier.fillMaxWidth().height(120.dp),
    ) {
        items(matches, key = { it.id }) { match ->
            MatchCard(
                match = remember(match) { MatchData(match.title, match.location ?: "Unknown") },
                modifier = Modifier.width(200.dp).fillMaxHeight(),
            )
        }
    }
}

@Composable
fun ExpandableSportsCalendar(
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    events: List<EventRecord>,
    datesWithEvents: Set<String>,
) {
    val locale = androidx.compose.ui.platform.LocalConfiguration.current.locales[0]
    val sdf = remember(locale) { SimpleDateFormat("dd/MM/yyyy", locale) }
    val daySdf = remember(locale) { SimpleDateFormat("EEE", locale) }
    val dateSdf = remember(locale) { SimpleDateFormat("dd", locale) }
    
    val dates = remember(locale) {
        val calendar = Calendar.getInstance(locale)
        (0..14).map {
            val d = calendar.time
            val formatted = sdf.format(d)
            val day = daySdf.format(d).uppercase()
            val dateNum = dateSdf.format(d)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            Triple(formatted, day, dateNum)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(90.dp),
        ) {
            items(dates, key = { it.first }) { (formatted, day, dateNum) ->
                val isSelected = selectedDate == formatted
                val hasEvents = datesWithEvents.contains(formatted)
                
                CalendarDateItem(
                    day = day,
                    dateNum = dateNum,
                    isSelected = isSelected,
                    hasEvents = hasEvents,
                ) { if (isSelected) onDateSelected("") else onDateSelected(formatted) }
            }
        }

        AnimatedVisibility(
            visible = selectedDate.isNotEmpty(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            CalendarEventsList(selectedDate, events)
        }
    }
}

@Composable
fun CalendarDateItem(
    day: String,
    dateNum: String,
    isSelected: Boolean,
    hasEvents: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(60.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) AppPrimaryBrand else SurfaceContainer)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = day,
            color = if (isSelected) Color.White else TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = dateNum,
            color = if (isSelected) Color.White else TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        
        if (isSelected) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.size(4.dp).background(Color.White, CircleShape))
        } else if (hasEvents) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(AppPrimaryBrand))
        }
    }
}

@Composable
fun CalendarEventsList(selectedDate: String, events: List<EventRecord>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .background(SurfaceContainer.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            .border(1.dp, ElevatedBorders, RoundedCornerShape(24.dp))
            .padding(16.dp),
    ) {
        Text(
            text = "Events on $selectedDate",
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        
        if (events.isEmpty()) {
            Text(
                text = "No events scheduled for this date",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                textAlign = TextAlign.Center,
            )
        } else {
            events.forEach { event ->
                EventCard(event)
                Spacer(modifier = Modifier.height(8.dp))
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        textAlign = TextAlign.Center,
    )
}

@Composable
fun AdMobBannerPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SurfaceContainer, RoundedCornerShape(12.dp))
                .border(
                    width = 1.dp,
                    color = ElevatedBorders,
                    shape = RoundedCornerShape(12.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "ADVERTISEMENT",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                letterSpacing = 1.5.sp,
            )
        }
    }
}

@Composable
fun LiveMatchBanner(activeMatch: MatchRecord?) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(130.dp) // DIRECTIVE: Reduce height to 130.dp
            .clickable {
                val url = if (activeMatch != null) "https://www.hotstar.com/sports/live" else "https://www.hotstar.com/sports/highlights"
                context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
            },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val imageRequest = remember(activeMatch?.imageUrl) {
                ImageRequest.Builder(context)
                    .data(activeMatch?.imageUrl ?: "https://images.unsplash.com/photo-1504450758481-7338eba7524a?q=80&w=1000")
                    .crossfade(enable = true)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .build()
            }
            
            AsyncImage(
                model = imageRequest,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 50f,
                        ),
                    ),
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (activeMatch != null) {
                        Surface(
                            color = StatusLiveWin,
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                FlashingDot()
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("LIVE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                        }
                    }
                    
                    Surface(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(
                            "Featured",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = Color.White,
                            fontSize = 10.sp,
                        )
                    }
                }

                Column {
                    Text(
                        text = activeMatch?.title ?: "No Live Matches",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 24.sp,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (activeMatch != null) "WATCH NOW" else "HIGHLIGHTS", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        if (activeMatch != null) {
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("${activeMatch.viewers} watching", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
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
        border = androidx.compose.foundation.BorderStroke(1.dp, ElevatedBorders),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(AppPrimaryBrand.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Sports, contentDescription = null, tint = AppPrimaryBrand, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Column {
                Text(match.format, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                Text(match.location, color = TextSecondary, fontSize = 11.sp, maxLines = 1)
            }
        }
    }
}

@Composable
fun EventCard(event: EventRecord) {
    Card(
        modifier = Modifier.fillMaxWidth().height(96.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ElevatedBorders),
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val icon = remember(event.sportType) {
                when (event.sportType) {
                    "Cricket" -> Icons.Default.SportsCricket
                    "Football" -> Icons.Default.SportsFootball
                    "Basketball" -> Icons.Default.SportsBasketball
                    else -> Icons.Default.EmojiEvents
                }
            }
            
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(AppPrimaryBrand.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = AppPrimaryBrand, modifier = Modifier.size(22.dp))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(event.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1)
                event.teamName?.let {
                    Text(it, color = TextSecondary, fontSize = 12.sp, maxLines = 1)
                }
            }
            
            Surface(
                color = ElevatedBorders,
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    text = event.dateLabel ?: "",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = AccentGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                )
            }
        }
    }
}

@Composable
fun SportsFilmsGrid(films: List<FilmRecord>, onFilmClick: (FilmRecord) -> Unit) {
    val context = LocalContext.current
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = Modifier.height(240.dp)
    ) {
        items(films, key = { it.title }) { film ->
            FilmItem(film, context, onFilmClick)
        }
    }
}

@Composable
fun FilmItem(film: FilmRecord, context: android.content.Context, onFilmClick: (FilmRecord) -> Unit) {
    Column(modifier = Modifier.width(120.dp).fillMaxHeight().clickable { onFilmClick(film) }) {
        val imageRequest = remember(film.imageUrl) {
            ImageRequest.Builder(context)
                .data(film.imageUrl)
                .crossfade(enable = true)
                .placeholder(android.R.drawable.ic_menu_report_image)
                .error(android.R.drawable.ic_menu_report_image)
                .diskCachePolicy(CachePolicy.ENABLED)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .build()
        }
        AsyncImage(
            model = imageRequest,
            contentDescription = null,
            modifier = Modifier
                .width(120.dp)
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, ElevatedBorders, RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            film.title,
            color = TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            textAlign = TextAlign.Start,
        )
    }
}

@Composable
fun SectionHeader(title: String, onViewAll: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(32.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            color = TextPrimary,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            letterSpacing = 0.5.sp,
        )
        Text(
            "See All",
            color = AppPrimaryBrand,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { onViewAll() },
        )
    }
}

@Composable
fun FlashingDot() {
    val infiniteTransition = rememberInfiniteTransition(label = "flashing_dot")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "alpha",
    )
    Canvas(
        modifier = Modifier
            .size(8.dp)
            .alpha(alpha),
    ) {
        drawCircle(color = Color.White)
    }
}

@Composable
fun WIPScreen(tabName: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = AppPrimaryBrand.copy(alpha = 0.5f),
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Coming Soon",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "We're crafting the $tabName experience.",
                textAlign = TextAlign.Center,
                color = TextSecondary,
            )
        }
    }
}
