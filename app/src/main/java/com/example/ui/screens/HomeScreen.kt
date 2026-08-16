package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.local.WatchHistoryEntity
import com.example.data.model.Drama
import com.example.data.model.DramaGenre
import com.example.ui.components.DramaPosterCard
import com.example.ui.theme.ReelBackgroundDark
import com.example.ui.theme.ReelGoldDark
import com.example.ui.theme.ReelGoldLight
import com.example.ui.theme.ReelGoldPrimary
import com.example.ui.theme.ReelRedDark
import com.example.ui.theme.ReelRedLight
import com.example.ui.theme.ReelRedPrimary
import com.example.ui.theme.ReelSurfaceDark
import com.example.ui.theme.ReelSurfaceVariantDark
import com.example.ui.theme.ReelTextSecondary

@Composable
fun HomeScreen(
    dramas: List<Drama>,
    watchHistory: List<WatchHistoryEntity>,
    bookmarks: List<BookmarkEntity>,
    selectedGenre: DramaGenre,
    onSelectGenre: (DramaGenre) -> Unit,
    onSelectDrama: (Drama, Int) -> Unit,
    onOpenDramaDetail: (Drama) -> Unit,
    onToggleBookmark: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val featuredDrama = dramas.firstOrNull()
    val isFeaturedBookmarked = bookmarks.any { it.dramaId == featuredDrama?.id }

    val continueWatchingDrama = watchHistory.firstOrNull()?.let { hist ->
        dramas.find { it.id == hist.dramaId }?.let { drama ->
            Triple(drama, hist.episodeNumber, hist.progressSeconds)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen")
            .background(ReelBackgroundDark),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // --- 1. Featured Spotlight Hero Banner ---
        if (featuredDrama != null) {
            item {
                FeaturedHeroBanner(
                    drama = featuredDrama,
                    isBookmarked = isFeaturedBookmarked,
                    onPlayClick = { onSelectDrama(featuredDrama, 1) },
                    onDetailClick = { onOpenDramaDetail(featuredDrama) },
                    onBookmarkClick = { onToggleBookmark(featuredDrama.id) }
                )
            }
        }

        // --- 2. Genre Chips Bar ---
        item {
            GenreChipsBar(
                selectedGenre = selectedGenre,
                onSelectGenre = onSelectGenre
            )
        }

        // --- 3. Continue Watching Shelf (if any) ---
        if (continueWatchingDrama != null) {
            item {
                ContinueWatchingSection(
                    drama = continueWatchingDrama.first,
                    episodeNumber = continueWatchingDrama.second,
                    progressSeconds = continueWatchingDrama.third,
                    onResume = {
                        onSelectDrama(continueWatchingDrama.first, continueWatchingDrama.second)
                    }
                )
            }
        }

        // --- 4. Top 10 Trending Today ---
        item {
            DramaSectionHeader(
                icon = Icons.Default.Whatshot,
                title = "Top 10 Trending Today",
                subtitle = "Most watched micro-dramas",
                badgeColor = ReelRedPrimary
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(dramas.take(10)) { index, drama ->
                    DramaPosterCard(
                        drama = drama,
                        onClick = { onOpenDramaDetail(drama) },
                        showRank = index + 1
                    )
                }
            }
        }

        // --- 5. Billionaire Romance ---
        val billionaireDramas = dramas.filter { it.genre == DramaGenre.BILLIONAIRE || it.genre == DramaGenre.ROMANCE }
        if (billionaireDramas.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                DramaSectionHeader(
                    icon = Icons.Default.Star,
                    title = "Billionaire & CEO Romance",
                    subtitle = "Secret heirs and luxury passion",
                    badgeColor = ReelGoldPrimary
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(billionaireDramas) { drama ->
                        DramaPosterCard(
                            drama = drama,
                            onClick = { onOpenDramaDetail(drama) }
                        )
                    }
                }
            }
        }

        // --- 6. Alpha & Werewolf Hits ---
        val werewolfDramas = dramas.filter { it.genre == DramaGenre.WEREWOLF }
        if (werewolfDramas.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                DramaSectionHeader(
                    icon = Icons.Default.TrendingUp,
                    title = "Werewolf & Supernatural",
                    subtitle = "Fated mates and pack wars",
                    badgeColor = Color(0xFF9C27B0)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(werewolfDramas) { drama ->
                        DramaPosterCard(
                            drama = drama,
                            onClick = { onOpenDramaDetail(drama) }
                        )
                    }
                }
            }
        }

        // --- 7. Revenge & Thrillers ---
        val revengeDramas = dramas.filter { it.genre == DramaGenre.REVENGE || it.genre == DramaGenre.SUSPENSE }
        if (revengeDramas.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                DramaSectionHeader(
                    icon = Icons.Default.Whatshot,
                    title = "High-Stakes Revenge",
                    subtitle = "Satisfying retributions & twists",
                    badgeColor = ReelRedLight
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(revengeDramas) { drama ->
                        DramaPosterCard(
                            drama = drama,
                            onClick = { onOpenDramaDetail(drama) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturedHeroBanner(
    drama: Drama,
    isBookmarked: Boolean,
    onPlayClick: () -> Unit,
    onDetailClick: () -> Unit,
    onBookmarkClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("featured_hero_banner")
            .clickable(onClick = onDetailClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ReelSurfaceDark)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(drama.coverGradientStart),
                            Color(drama.coverGradientEnd),
                            ReelSurfaceDark
                        )
                    )
                )
        ) {
            // Ambient glow
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0x35E50914), Color.Transparent),
                            radius = 400f
                        )
                    )
            )

            // Content Overlay
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                // Top Tag
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(ReelRedPrimary)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "FEATURED HIT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontSize = 10.sp
                            )
                        )
                    }

                    Text(
                        text = "${drama.viewCount} Views • ${drama.totalEpisodes} Episodes",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = ReelGoldLight,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Title
                Text(
                    text = drama.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 20.sp,
                        lineHeight = 24.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Synopsis
                Text(
                    text = drama.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xDDFFFFFF),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onPlayClick,
                        modifier = Modifier.testTag("featured_play_button"),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ReelRedPrimary)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Watch Ep 1",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onBookmarkClick,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = null,
                                tint = if (isBookmarked) ReelGoldPrimary else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isBookmarked) "Saved" else "My List",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GenreChipsBar(
    selectedGenre: DramaGenre,
    onSelectGenre: (DramaGenre) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(DramaGenre.values()) { genre ->
            val isSelected = selectedGenre == genre
            FilterChip(
                selected = isSelected,
                onClick = { onSelectGenre(genre) },
                label = {
                    Text(
                        text = genre.displayName,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ReelRedPrimary,
                    selectedLabelColor = Color.White,
                    containerColor = ReelSurfaceVariantDark,
                    labelColor = ReelTextSecondary
                ),
                border = null,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
private fun ContinueWatchingSection(
    drama: Drama,
    episodeNumber: Int,
    progressSeconds: Int,
    onResume: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Continue Watching",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onResume),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = ReelSurfaceVariantDark)
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
                        .size(48.dp)
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
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
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
                        text = "Episode $episodeNumber • In progress",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = ReelGoldLight,
                            fontSize = 11.sp
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(ReelRedPrimary)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "RESUME",
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

@Composable
private fun DramaSectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    badgeColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(badgeColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(15.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = ReelTextSecondary,
                        fontSize = 11.sp
                    )
                )
            }
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "See more",
            tint = ReelTextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}
