package com.chinmaib.sportconnect.ui.feed

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.chinmaib.sportconnect.ui.theme.*
import kotlin.math.roundToInt

// DESIGN TOKENS FROM HTML
val ForestEnd = Color(0xFF061710)
val CoolTeal = Color(0xFF74C2BD)
val Saffron = Color(0xFFFF9B54)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: FeedViewModel = hiltViewModel(),
    @Suppress("UNUSED_PARAMETER") onBack: () -> Unit,
) {
    val posts by viewModel.posts.collectAsState()
    var isFabMenuOpen by remember { mutableStateOf(value = false) }

    // PERFORMANCE: YouTube-style Hide-on-Scroll Logic
    val bottomBarHeight = 80.dp
    val density = androidx.compose.ui.platform.LocalDensity.current
    val bottomBarHeightPx = with(density) { bottomBarHeight.roundToPx().toFloat() }
    val barOffsetHeightPx = remember { mutableFloatStateOf(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newOffset = barOffsetHeightPx.floatValue + delta
                barOffsetHeightPx.floatValue = newOffset.coerceIn(-bottomBarHeightPx, 0f)
                return Offset.Zero
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ForestEnd)
            .nestedScroll(nestedScrollConnection),
    ) {
        // SCROLLING FEED
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 100.dp, bottom = 100.dp),
        ) {
            items(posts, key = { it.id }) { post ->
                PostCard(post)
            }
        }

        // TOP NAVIGATION (Sticky/Blur)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(ForestEnd.copy(alpha = 0.9f))
                .statusBarsPadding()
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "SportConnect",
                color = Color.White,
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                letterSpacing = (-0.5).sp,
            )
            
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                IconButton(onClick = { }) {
                    Box {
                        Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(Color.Red, CircleShape)
                                .border(2.dp, ForestEnd, CircleShape)
                                .align(Alignment.TopEnd),
                        )
                    }
                }
            }
        }

        // FAB OVERLAY (Blur/Dim)
        AnimatedVisibility(
            visible = isFabMenuOpen,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ForestEnd.copy(alpha = 0.8f))
                    .blur(10.dp)
                    .clickable { isFabMenuOpen = false },
            )
        }

        // EXPANDABLE FAB MENU
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 110.dp, end = 16.dp)
                .offset { IntOffset(0, -barOffsetHeightPx.floatValue.roundToInt()) },
        ) {
            Column(horizontalAlignment = Alignment.End) {
                AnimatedVisibility(
                    visible = isFabMenuOpen,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut(),
                ) {
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        FabMenuItem(label = "Go Live", icon = Icons.Default.Videocam)
                        FabMenuItem(label = "Post", icon = Icons.Default.Edit, modifier = Modifier.padding(bottom = 16.dp))
                    }
                }

                FloatingActionButton(
                    onClick = { isFabMenuOpen = !isFabMenuOpen },
                    containerColor = if (isFabMenuOpen) Color.White else Saffron,
                    contentColor = ForestEnd,
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp),
                ) {
                    val rotation by animateFloatAsState(if (isFabMenuOpen) 45f else 0f, label = "fabRotation")
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp).graphicsLayer { rotationZ = rotation },
                    )
                }
            }
        }

        // BOTTOM NAVIGATION
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset { IntOffset(0, -barOffsetHeightPx.floatValue.roundToInt()) }
                .fillMaxWidth()
                .height(bottomBarHeight)
                .background(ForestEnd)
                .border(width = 1.dp, color = CoolTeal.copy(alpha = 0.15f), shape = RoundedCornerShape(0.dp))
                .navigationBarsPadding(),
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NavIcon(Icons.Default.Home, isSelected = true)
                NavIcon(Icons.Default.Search)
                LogoTab()
                NavIcon(Icons.Default.Notifications)
                NavIcon(Icons.Default.Mail)
            }
        }
    }
}

@Composable
fun PostCard(post: PostRecord) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .background(ForestEnd.copy(alpha = 0.3f)),
    ) {
        // HEADER
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                AsyncImage(
                    model = post.userAvatar,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(post.username, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, fontFamily = Montserrat)
                    if (post.isSuggested) {
                        Text("Suggested for you", color = CoolTeal.copy(alpha = 0.6f), fontSize = 11.sp)
                    } else {
                        Text("Following", color = CoolTeal, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (post.isSuggested) {
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp),
                    ) {
                        Text("Follow", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.White.copy(alpha = 0.8f))
                }
            }
        }

        // MEDIA
        AsyncImage(
            model = post.mediaUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().aspectRatio(0.8f),
            contentScale = ContentScale.Crop,
        )

        // ACTION BAR
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                ActionIcon(Icons.Default.FavoriteBorder, post.likesCount)
                ActionIcon(Icons.Outlined.ModeComment, post.commentsCount)
                ActionIcon(Icons.Default.Repeat, post.sharesCount)
                ActionIcon(Icons.AutoMirrored.Filled.Send, post.savesCount)
            }
            Icon(Icons.Default.BookmarkBorder, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
        }

        // CAPTION
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            RichTextCaption(post.username, post.caption)
            Text(
                text = "${post.createdAt} • See translation",
                color = CoolTeal.copy(alpha = 0.6f),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = CoolTeal.copy(alpha = 0.15f))
    }
}

@Composable
fun RichTextCaption(username: String, text: String) {
    var isExpanded by remember { mutableStateOf(value = false) }
    
    Column {
        Text(
            text = buildString { append(username); append(" "); append(text) },
            color = Color.White,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            maxLines = if (isExpanded) Int.MAX_VALUE else 2,
            overflow = TextOverflow.Ellipsis,
        )
        if ((!isExpanded) && (text.length > 60)) {
            Text(
                "more",
                color = CoolTeal.copy(alpha = 0.6f),
                fontSize = 14.sp,
                modifier = Modifier.clickable { isExpanded = true },
            )
        }
    }
}

@Composable
fun FabMenuItem(label: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Text(
            text = label,
            color = Color.White,
            fontFamily = Montserrat,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            modifier = Modifier.padding(end = 12.dp),
        )
        Surface(
            color = Color.White,
            shape = CircleShape,
            modifier = Modifier.size(48.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = ForestEnd, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun ActionIcon(icon: ImageVector, count: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
        Text(count, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun NavIcon(icon: ImageVector, isSelected: Boolean = false) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (isSelected) Color.White else CoolTeal.copy(alpha = 0.6f),
        modifier = Modifier.size(28.dp),
    )
}

@Composable
fun LogoTab() {
    Box(
        modifier = Modifier
            .size(28.dp)
            .border(2.dp, CoolTeal.copy(alpha = 0.6f), RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text("SC", color = CoolTeal.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = Montserrat)
    }
}
