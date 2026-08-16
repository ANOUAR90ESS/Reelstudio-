package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import com.example.data.local.UnlockedEpisodeEntity
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

@Composable
fun DramaDetailScreen(
    drama: Drama,
    allDramas: List<Drama>,
    isBookmarked: Boolean,
    isVip: Boolean,
    unlockedList: List<UnlockedEpisodeEntity>,
    onBack: () -> Unit,
    onPlayEpisode: (Int) -> Unit,
    onToggleBookmark: () -> Unit,
    onShare: () -> Unit,
    onSelectRecommendedDrama: (Drama) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpandedSynopsis by remember { mutableStateOf(false) }

    val recommended = allDramas.filter { it.id != drama.id }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("drama_detail_screen")
            .background(ReelBackgroundDark),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // --- 1. Hero Backdrop Header ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(drama.coverGradientStart),
                                Color(drama.coverGradientEnd),
                                ReelBackgroundDark
                            )
                        )
                    )
            ) {
                // Top Navigation Icons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x66000000))
                            .testTag("detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = onToggleBookmark,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0x66000000))
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (isBookmarked) ReelGoldPrimary else Color.White
                            )
                        }

                        IconButton(
                            onClick = onShare,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0x66000000))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Play Ep 1 Large FAB in center bottom
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(ReelRedPrimary)
                        .clickable { onPlayEpisode(1) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        // --- 2. Title, Stats & Badges ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = drama.title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 22.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Stats Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = ReelGoldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${drama.rating}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = ReelGoldLight
                            )
                        )
                    }

                    Text(
                        text = "•",
                        color = ReelTextTertiary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Views",
                            tint = ReelTextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = drama.viewCount,
                            style = MaterialTheme.typography.bodySmall.copy(color = ReelTextSecondary)
                        )
                    }

                    Text(
                        text = "•",
                        color = ReelTextTertiary
                    )

                    Text(
                        text = "${drama.totalEpisodes} Episodes",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ReelRedPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tags Row
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(drama.tags) { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(ReelSurfaceVariantDark)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = ReelTextSecondary,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Synopsis
                Text(
                    text = drama.description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xDDFFFFFF),
                        lineHeight = 20.sp,
                        fontSize = 13.sp
                    ),
                    maxLines = if (isExpandedSynopsis) 10 else 3,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = if (isExpandedSynopsis) "Show less" else "Read more...",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = ReelRedPrimary
                    ),
                    modifier = Modifier
                        .clickable { isExpandedSynopsis = !isExpandedSynopsis }
                        .padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Main Play CTA Button
                Button(
                    onClick = { onPlayEpisode(1) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("detail_play_cta"),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ReelRedPrimary)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Text(
                            text = "Play Episode 1 (Free)",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                        )
                    }
                }
            }
        }

        // --- 3. Episodes Grid Section ---
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "All Episodes (${drama.episodes.size})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(drama.episodes) { ep ->
                        val isUnlocked = ep.isFree || isVip || unlockedList.any {
                            it.dramaId == drama.id && it.episodeNumber == ep.episodeNumber
                        }

                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isUnlocked) ReelSurfaceVariantDark else Color(0xFF13141D))
                                .clickable { onPlayEpisode(ep.episodeNumber) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${ep.episodeNumber}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isUnlocked) Color.White else ReelTextTertiary
                                    )
                                )
                                if (!isUnlocked) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Locked",
                                        tint = ReelGoldPrimary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }

                            if (ep.isFree) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(Color(0xFF2E7D32))
                                        .padding(horizontal = 3.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "FREE",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 4. Cast & Crew ---
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Cast & Crew",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Director: ${drama.director}",
                    style = MaterialTheme.typography.bodySmall.copy(color = ReelTextSecondary)
                )
                Text(
                    text = "Lead Cast: " + drama.cast.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall.copy(color = ReelTextSecondary)
                )
            }
        }

        // --- 5. More Like This ---
        if (recommended.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "More Like This",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(recommended) { rec ->
                            DramaPosterCard(
                                drama = rec,
                                onClick = { onSelectRecommendedDrama(rec) },
                                cardWidth = 120.dp
                            )
                        }
                    }
                }
            }
        }
    }
}
