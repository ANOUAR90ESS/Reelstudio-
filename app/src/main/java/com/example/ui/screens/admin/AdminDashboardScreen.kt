package com.example.ui.screens.admin

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.example.data.admin.AdminStats
import com.example.data.model.Drama
import com.example.ui.components.AdminStatCard
import com.example.ui.theme.ReelBackgroundDark
import com.example.ui.theme.ReelGoldPrimary
import com.example.ui.theme.ReelRedDark
import com.example.ui.theme.ReelRedPrimary
import com.example.ui.theme.ReelSuccess
import com.example.ui.theme.ReelSurfaceDark
import com.example.ui.theme.ReelSurfaceHighlight
import com.example.ui.theme.ReelSurfaceVariantDark
import com.example.ui.theme.ReelTextSecondary
import com.example.ui.theme.ReelTextTertiary

/**
 * Landing screen of the admin console: headline numbers, then every authored film with its
 * publish state and the actions that operate on it.
 */
@Composable
fun AdminDashboardScreen(
    adminName: String,
    adminEmail: String,
    stats: AdminStats,
    dramas: List<Drama>,
    onCreateFilm: () -> Unit,
    onEditFilm: (Drama) -> Unit,
    onManageEpisodes: (Drama) -> Unit,
    onTogglePublish: (Drama) -> Unit,
    onDeleteFilm: (Drama) -> Unit,
    onExitConsole: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pendingDelete by remember { mutableStateOf<Drama?>(null) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_dashboard_screen"),
        containerColor = ReelBackgroundDark,
        topBar = {
            AdminConsoleTopBar(
                adminName = adminName,
                adminEmail = adminEmail,
                onExitConsole = onExitConsole
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateFilm,
                containerColor = ReelRedPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("admin_create_film_fab"),
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = {
                    Text(text = "New Film", fontWeight = FontWeight.Bold)
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AdminStatCard(
                        label = "Films",
                        value = stats.totalFilms.toString(),
                        accent = ReelRedPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    AdminStatCard(
                        label = "Published",
                        value = stats.publishedFilms.toString(),
                        accent = ReelSuccess,
                        modifier = Modifier.weight(1f)
                    )
                    AdminStatCard(
                        label = "Drafts",
                        value = stats.draftFilms.toString(),
                        accent = ReelGoldPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AdminStatCard(
                        label = "Episodes",
                        value = stats.totalEpisodes.toString(),
                        accent = ReelRedPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    AdminStatCard(
                        label = "Free",
                        value = stats.freeEpisodes.toString(),
                        accent = ReelSuccess,
                        modifier = Modifier.weight(1f)
                    )
                    AdminStatCard(
                        label = "Locked",
                        value = stats.lockedEpisodes.toString(),
                        accent = ReelGoldPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Text(
                    text = "Catalog",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (dramas.isEmpty()) {
                item { AdminEmptyCatalog(onCreateFilm = onCreateFilm) }
            } else {
                items(dramas, key = { it.id }) { drama ->
                    AdminFilmRow(
                        drama = drama,
                        onEdit = { onEditFilm(drama) },
                        onManageEpisodes = { onManageEpisodes(drama) },
                        onTogglePublish = { onTogglePublish(drama) },
                        onDelete = { pendingDelete = drama }
                    )
                }
            }
        }
    }

    val target = pendingDelete
    if (target != null) {
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            containerColor = ReelSurfaceDark,
            titleContentColor = Color.White,
            textContentColor = ReelTextSecondary,
            title = { Text(text = "Delete \"${target.title}\"?") },
            text = {
                Text(
                    text = "This removes the film, its ${target.episodes.size} episode(s), and any " +
                            "viewer bookmarks or unlocks pointing at it. This cannot be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteFilm(target)
                        pendingDelete = null
                    },
                    modifier = Modifier.testTag("admin_confirm_delete_button")
                ) {
                    Text(text = "Delete", color = ReelRedPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(text = "Cancel", color = ReelTextSecondary)
                }
            }
        )
    }
}

@Composable
private fun AdminConsoleTopBar(
    adminName: String,
    adminEmail: String,
    onExitConsole: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(listOf(ReelRedDark, ReelBackgroundDark))
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(ReelGoldPrimary.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = ReelGoldPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Admin Console",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                )
                Text(
                    text = adminEmail.ifBlank { adminName },
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = ReelTextSecondary,
                        fontSize = 11.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        IconButton(
            onClick = onExitConsole,
            modifier = Modifier.testTag("admin_exit_button")
        ) {
            Icon(
                imageVector = Icons.Default.Logout,
                contentDescription = "Exit admin console",
                tint = ReelTextSecondary
            )
        }
    }
}

@Composable
private fun AdminFilmRow(
    drama: Drama,
    onEdit: () -> Unit,
    onManageEpisodes: () -> Unit,
    onTogglePublish: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ReelSurfaceDark)
            .testTag("admin_film_row_${drama.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onEdit)
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(width = 52.dp, height = 72.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(drama.coverGradientStart),
                                Color(drama.coverGradientEnd)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MovieFilter,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.75f),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = drama.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${drama.genre.displayName} • ${drama.episodes.size} episodes • ${drama.releaseYear}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = ReelTextTertiary,
                        fontSize = 11.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                PublishPill(isPublished = drama.isPublished)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ReelSurfaceVariantDark.copy(alpha = 0.6f))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AdminRowAction(
                icon = Icons.Default.Edit,
                label = "Edit",
                onClick = onEdit,
                modifier = Modifier
                    .weight(1f)
                    .testTag("admin_edit_${drama.id}")
            )
            AdminRowAction(
                icon = Icons.Default.PlaylistPlay,
                label = "Episodes",
                onClick = onManageEpisodes,
                modifier = Modifier
                    .weight(1f)
                    .testTag("admin_episodes_${drama.id}")
            )
            AdminRowAction(
                icon = Icons.Default.MovieFilter,
                label = if (drama.isPublished) "Unpublish" else "Publish",
                tint = if (drama.isPublished) ReelGoldPrimary else ReelSuccess,
                onClick = onTogglePublish,
                modifier = Modifier
                    .weight(1f)
                    .testTag("admin_publish_${drama.id}")
            )
            AdminRowAction(
                icon = Icons.Default.Delete,
                label = "Delete",
                tint = ReelRedPrimary,
                onClick = onDelete,
                modifier = Modifier
                    .weight(1f)
                    .testTag("admin_delete_${drama.id}")
            )
        }
    }
}

@Composable
private fun AdminRowAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = ReelTextSecondary
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(color = tint, fontSize = 10.sp)
        )
    }
}

@Composable
private fun PublishPill(isPublished: Boolean) {
    val color = if (isPublished) ReelSuccess else ReelGoldPrimary
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = if (isPublished) "LIVE FOR VIEWERS" else "DRAFT — HIDDEN",
            style = MaterialTheme.typography.labelSmall.copy(
                color = color,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp
            )
        )
    }
}

@Composable
private fun AdminEmptyCatalog(onCreateFilm: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ReelSurfaceDark)
            .clickable(onClick = onCreateFilm)
            .padding(vertical = 32.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(ReelSurfaceHighlight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MovieFilter,
                contentDescription = null,
                tint = ReelRedPrimary,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "No films yet",
            style = MaterialTheme.typography.titleSmall.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Create your first film, add its episodes, then publish it to make it visible to viewers.",
            style = MaterialTheme.typography.bodySmall.copy(color = ReelTextTertiary),
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}
