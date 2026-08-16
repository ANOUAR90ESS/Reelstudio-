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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Drama
import com.example.data.model.DramaGenre
import com.example.ui.components.DramaPosterCard
import com.example.ui.theme.ReelBackgroundDark
import com.example.ui.theme.ReelGoldPrimary
import com.example.ui.theme.ReelRedPrimary
import com.example.ui.theme.ReelSurfaceVariantDark
import com.example.ui.theme.ReelTextSecondary
import com.example.ui.theme.ReelTextTertiary

@Composable
fun DiscoverScreen(
    dramas: List<Drama>,
    searchQuery: String,
    selectedGenre: DramaGenre,
    onSearchChange: (String) -> Unit,
    onSelectGenre: (DramaGenre) -> Unit,
    onSelectDrama: (Drama) -> Unit,
    modifier: Modifier = Modifier
) {
    val popularTags = listOf("Billionaire", "Secret Identity", "Revenge", "Alpha Wolf", "Divorce", "Contract Love", "Triplets")

    val filteredDramas = dramas.filter { drama ->
        val matchesGenre = selectedGenre == DramaGenre.ALL || drama.genre == selectedGenre
        val q = searchQuery.trim().lowercase()
        val matchesQuery = q.isEmpty() ||
                drama.title.lowercase().contains(q) ||
                drama.description.lowercase().contains(q) ||
                drama.tags.any { it.lowercase().contains(q) } ||
                drama.cast.any { it.lowercase().contains(q) }
        matchesGenre && matchesQuery
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("discover_screen")
            .background(ReelBackgroundDark)
            .padding(top = 8.dp)
    ) {
        // --- 1. Search Bar ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = {
                    Text(
                        text = "Search billionaire, werewolf, revenge...",
                        style = MaterialTheme.typography.bodyMedium.copy(color = ReelTextTertiary)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = ReelTextSecondary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = ReelTextSecondary
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(ReelSurfaceVariantDark)
                    .testTag("discover_search_input"),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- 2. Popular Tags Chips ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Whatshot,
                contentDescription = "Hot",
                tint = ReelGoldPrimary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Trending Tags:",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = ReelGoldPrimary
                )
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(popularTags) { tag ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(ReelSurfaceVariantDark)
                        .clickable { onSearchChange(tag) }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "#$tag",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xDDFFFFFF),
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        // --- 3. Genre Selector Tabs ---
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
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

        Spacer(modifier = Modifier.height(8.dp))

        // --- 4. Results Grid ---
        if (filteredDramas.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No series found for \"$searchQuery\"",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Try searching for 'billionaire', 'alpha', or 'revenge'",
                        style = MaterialTheme.typography.bodySmall.copy(color = ReelTextSecondary)
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredDramas) { drama ->
                    DramaPosterCard(
                        drama = drama,
                        onClick = { onSelectDrama(drama) },
                        cardWidth = 110.dp
                    )
                }
            }
        }
    }
}
