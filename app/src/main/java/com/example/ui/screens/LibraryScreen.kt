package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BookmarkEntity
import com.example.data.local.UnlockedEpisodeEntity
import com.example.data.local.UserAccountEntity
import com.example.data.local.WatchHistoryEntity
import com.example.data.model.Drama
import com.example.ui.components.DramaPosterCard
import com.example.ui.theme.ReelBackgroundDark
import com.example.ui.theme.ReelGoldLight
import com.example.ui.theme.ReelGoldPrimary
import com.example.ui.theme.ReelRedDark
import com.example.ui.theme.ReelRedPrimary
import com.example.ui.theme.ReelSurfaceDark
import com.example.ui.theme.ReelSurfaceVariantDark
import com.example.ui.theme.ReelTextSecondary
import com.example.ui.theme.ReelTextTertiary

import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Login
import com.example.data.firebase.UserProfile
import com.google.firebase.auth.FirebaseUser

@Composable
fun LibraryScreen(
    userAccount: UserAccountEntity,
    allDramas: List<Drama>,
    bookmarks: List<BookmarkEntity>,
    watchHistory: List<WatchHistoryEntity>,
    unlockedList: List<UnlockedEpisodeEntity>,
    firebaseUser: FirebaseUser? = null,
    userProfile: UserProfile? = null,
    onOpenAuth: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onSelectDrama: (Drama, Int) -> Unit,
    onOpenDramaDetail: (Drama) -> Unit,
    onClearHistory: () -> Unit,
    onNavigateToRewards: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIdx by remember { mutableStateOf(0) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val displayName = userProfile?.displayName?.ifBlank { null }
        ?: firebaseUser?.displayName
        ?: firebaseUser?.email?.substringBefore("@")
        ?: "Guest Viewer"

    val userInitial = displayName.take(1).uppercase()
    val isLoggedIn = firebaseUser != null || userProfile != null

    val bookmarkedDramas = allDramas.filter { drama ->
        bookmarks.any { it.dramaId == drama.id }
    }

    val historyItems = watchHistory.mapNotNull { hist ->
        allDramas.find { it.id == hist.dramaId }?.let { drama ->
            Pair(drama, hist)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("library_screen")
            .background(ReelBackgroundDark)
    ) {
        // --- 1. User Profile Header Card ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(ReelSurfaceDark)
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(ReelRedPrimary, ReelGoldPrimary)
                                    )
                                )
                                .clickable { onOpenAuth() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userInitial,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = displayName,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.widthIn(max = 150.dp)
                                )
                                if (userAccount.isVip || userProfile?.isVip == true) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(ReelGoldPrimary)
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "VIP",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color.Black,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 9.sp
                                            )
                                        )
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.clickable(onClick = onNavigateToRewards)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MonetizationOn,
                                    contentDescription = "Coins",
                                    tint = ReelGoldPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "${userAccount.coins} Coins",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ReelGoldLight
                                    )
                                )
                                Text(
                                    text = "• Top Up >",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = ReelRedPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!isLoggedIn) {
                            TextButton(
                                onClick = onOpenAuth,
                                modifier = Modifier.testTag("library_sign_in_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Login,
                                    contentDescription = null,
                                    tint = ReelGoldPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Sign In",
                                    color = ReelGoldPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        IconButton(onClick = { showSettingsDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = ReelTextSecondary
                            )
                        }
                    }
                }

                if (isLoggedIn) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ReelSurfaceVariantDark.copy(alpha = 0.5f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = ReelGoldLight,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = firebaseUser?.email ?: "Cloud Sync Active",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = ReelTextSecondary,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Text(
                            text = "Sign Out",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = ReelRedPrimary,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier
                                .clickable { onSignOut() }
                                .testTag("library_sign_out_button")
                        )
                    }
                }
            }
        }

        // --- 2. Tab Navigation ---
        val tabs = listOf("My List (${bookmarkedDramas.size})", "History (${historyItems.size})", "Unlocked (${unlockedList.size})")

        TabRow(
            selectedTabIndex = selectedTabIdx,
            containerColor = ReelSurfaceDark,
            contentColor = Color.White,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIdx]),
                    color = ReelRedPrimary
                )
            }
        ) {
            tabs.forEachIndexed { idx, title ->
                Tab(
                    selected = selectedTabIdx == idx,
                    onClick = { selectedTabIdx = idx },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selectedTabIdx == idx) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIdx == idx) Color.White else ReelTextSecondary
                            )
                        )
                    }
                )
            }
        }

        // --- 3. Tab Content ---
        when (selectedTabIdx) {
            0 -> {
                // My List Grid
                if (bookmarkedDramas.isEmpty()) {
                    EmptyState(
                        icon = Icons.Default.Bookmark,
                        message = "Your list is empty",
                        subtext = "Bookmark your favorite micro-dramas to watch anytime"
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(bookmarkedDramas) { drama ->
                            DramaPosterCard(
                                drama = drama,
                                onClick = { onOpenDramaDetail(drama) },
                                cardWidth = 110.dp
                            )
                        }
                    }
                }
            }
            1 -> {
                // Watch History List
                if (historyItems.isEmpty()) {
                    EmptyState(
                        icon = Icons.Default.History,
                        message = "No watch history",
                        subtext = "Series you watch will appear here with your playback progress"
                    )
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recently Watched",
                                style = MaterialTheme.typography.labelSmall.copy(color = ReelTextSecondary)
                            )
                            Text(
                                text = "Clear All",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ReelRedPrimary
                                ),
                                modifier = Modifier.clickable(onClick = onClearHistory)
                            )
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(historyItems) { (drama, hist) ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelectDrama(drama, hist.episodeNumber) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = ReelSurfaceDark)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    Brush.verticalGradient(
                                                        listOf(Color(drama.coverGradientStart), Color(drama.coverGradientEnd))
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "Resume",
                                                tint = Color.White
                                            )
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = drama.title,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "Ep ${hist.episodeNumber} / ${drama.totalEpisodes}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = ReelGoldLight,
                                                    fontSize = 11.sp
                                                )
                                            )
                                            Text(
                                                text = drama.genre.displayName,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = ReelTextTertiary,
                                                    fontSize = 10.sp
                                                )
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(ReelRedPrimary)
                                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                        ) {
                                            Text(
                                                text = "PLAY",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Black,
                                                    color = Color.White,
                                                    fontSize = 11.sp
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                // Unlocked Episodes List
                if (unlockedList.isEmpty()) {
                    EmptyState(
                        icon = Icons.Default.LockOpen,
                        message = "No purchased episodes yet",
                        subtext = "Episodes unlocked with coins or rewards will be stored permanently here"
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(unlockedList) { unlocked ->
                            val drama = allDramas.find { it.id == unlocked.dramaId }
                            if (drama != null) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelectDrama(drama, unlocked.episodeNumber) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = ReelSurfaceDark)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = drama.title,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            )
                                            Text(
                                                text = "Episode ${unlocked.episodeNumber} (Permanent Access)",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = Color(0xFF4CAF50),
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            )
                                        }

                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Play",
                                            tint = ReelRedPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = {
                Text(
                    text = "App Settings",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "• Streaming Quality: 1080p Ultra HD (VIP)",
                        color = ReelTextSecondary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "• Subtitles Language: English (Default)",
                        color = ReelTextSecondary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "• Auto-Play Next Episode: ON",
                        color = ReelTextSecondary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "• Version: 2.5.0 (Build 36)",
                        color = ReelTextTertiary,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("OK", color = ReelRedPrimary, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = ReelSurfaceDark
        )
    }
}

@Composable
private fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String,
    subtext: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(ReelSurfaceVariantDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ReelTextTertiary,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtext,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = ReelTextSecondary,
                    fontSize = 12.sp
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
