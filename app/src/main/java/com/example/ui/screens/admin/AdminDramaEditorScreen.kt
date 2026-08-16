package com.example.ui.screens.admin

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.admin.AdminPalette
import com.example.data.admin.DramaField
import com.example.data.admin.DramaFormState
import com.example.data.model.DramaGenre
import com.example.ui.components.AdminChipInput
import com.example.ui.components.AdminChoiceRow
import com.example.ui.components.AdminGradientPicker
import com.example.ui.components.AdminSectionCard
import com.example.ui.components.AdminTextField
import com.example.ui.components.AdminToggleRow
import com.example.ui.theme.ReelBackgroundDark
import com.example.ui.theme.ReelGoldPrimary
import com.example.ui.theme.ReelRedPrimary
import com.example.ui.theme.ReelSurfaceDark
import com.example.ui.theme.ReelTextSecondary
import com.example.ui.theme.ReelTextTertiary

/**
 * Create / edit form for a film.
 *
 * The form owns its draft state so typing never round-trips through the database; the caller only
 * hears about it on save. Validation runs on submit and again on every keystroke once the admin has
 * tried to save, which keeps a pristine form free of red before it has been attempted.
 */
@Composable
fun AdminDramaEditorScreen(
    initialForm: DramaFormState,
    onSave: (DramaFormState) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    isSaving: Boolean = false
) {
    var form by remember(initialForm.id, initialForm.editingExisting) { mutableStateOf(initialForm) }
    var submitted by remember { mutableStateOf(false) }

    val errors = if (submitted) form.validate() else emptyMap()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_drama_editor_screen"),
        containerColor = ReelBackgroundDark,
        topBar = {
            AdminEditorTopBar(
                title = if (form.editingExisting) "Edit Film" else "New Film",
                subtitle = form.title.ifBlank { "Untitled film" },
                onBack = onCancel
            )
        },
        bottomBar = {
            AdminEditorActionBar(
                saveLabel = if (form.editingExisting) "Save Changes" else "Create Film",
                isSaving = isSaving,
                onCancel = onCancel,
                onSave = {
                    submitted = true
                    if (form.isValid) onSave(form)
                },
                saveTestTag = "admin_save_film_button"
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                FilmCoverPreview(
                    title = form.title,
                    genre = form.genre,
                    badge = form.badge,
                    gradientStart = form.coverGradientStart,
                    gradientEnd = form.coverGradientEnd
                )
            }

            item {
                AdminSectionCard(
                    title = "Story",
                    subtitle = "What viewers read before pressing play"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        AdminTextField(
                            value = form.title,
                            onValueChange = { form = form.copy(title = it) },
                            label = "Film title",
                            placeholder = "e.g. Married To My Billionaire Rival",
                            errorText = errors[DramaField.TITLE],
                            testTag = "admin_field_title"
                        )
                        AdminTextField(
                            value = form.description,
                            onValueChange = { form = form.copy(description = it) },
                            label = "Synopsis",
                            placeholder = "Hook the viewer in a few sentences...",
                            errorText = errors[DramaField.DESCRIPTION],
                            singleLine = false,
                            minLines = 4,
                            testTag = "admin_field_description"
                        )
                    }
                }
            }

            item {
                AdminSectionCard(
                    title = "Category",
                    subtitle = "Drives the genre filters on Home and Discover"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Genre",
                            style = MaterialTheme.typography.labelMedium.copy(color = ReelTextSecondary)
                        )
                        AdminChoiceRow(
                            options = DramaGenre.entries.filter { it != DramaGenre.ALL },
                            selected = form.genre,
                            onSelect = { form = form.copy(genre = it) },
                            labelOf = { it.displayName }
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Badge",
                            style = MaterialTheme.typography.labelMedium.copy(color = ReelTextSecondary)
                        )
                        AdminChoiceRow(
                            options = AdminPalette.badges,
                            selected = form.badge,
                            onSelect = { form = form.copy(badge = it) },
                            labelOf = { it ?: "None" }
                        )
                    }
                }
            }

            item {
                AdminSectionCard(
                    title = "Cover",
                    subtitle = "Poster gradient used across the app"
                ) {
                    AdminGradientPicker(
                        presets = AdminPalette.presets.map { Triple(it.name, it.start, it.end) },
                        selectedStart = form.coverGradientStart,
                        selectedEnd = form.coverGradientEnd,
                        onSelect = { start, end ->
                            form = form.copy(coverGradientStart = start, coverGradientEnd = end)
                        }
                    )
                }
            }

            item {
                AdminSectionCard(
                    title = "Credits",
                    subtitle = "Shown on the film detail page"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        AdminTextField(
                            value = form.director,
                            onValueChange = { form = form.copy(director = it) },
                            label = "Director",
                            testTag = "admin_field_director"
                        )
                        AdminChipInput(
                            values = form.cast,
                            onValuesChange = { form = form.copy(cast = it) },
                            placeholder = "Add cast member",
                            testTag = "admin_field_cast"
                        )
                        AdminChipInput(
                            values = form.tags,
                            onValuesChange = { form = form.copy(tags = it) },
                            placeholder = "Add tag",
                            testTag = "admin_field_tags"
                        )
                    }
                }
            }

            item {
                AdminSectionCard(
                    title = "Numbers",
                    subtitle = "Rating and the counters shown on the poster"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            AdminTextField(
                                value = form.rating,
                                onValueChange = { form = form.copy(rating = it) },
                                label = "Rating (0-5)",
                                errorText = errors[DramaField.RATING],
                                numeric = true,
                                modifier = Modifier.weight(1f),
                                testTag = "admin_field_rating"
                            )
                            AdminTextField(
                                value = form.releaseYear,
                                onValueChange = { form = form.copy(releaseYear = it) },
                                label = "Release year",
                                errorText = errors[DramaField.RELEASE_YEAR],
                                numeric = true,
                                modifier = Modifier.weight(1f),
                                testTag = "admin_field_year"
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            AdminTextField(
                                value = form.viewCount,
                                onValueChange = { form = form.copy(viewCount = it) },
                                label = "Views label",
                                placeholder = "12.4M",
                                modifier = Modifier.weight(1f)
                            )
                            AdminTextField(
                                value = form.likeCount,
                                onValueChange = { form = form.copy(likeCount = it) },
                                label = "Likes label",
                                placeholder = "840K",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        AdminTextField(
                            value = form.totalEpisodes,
                            onValueChange = { form = form.copy(totalEpisodes = it) },
                            label = "Announced episode count",
                            placeholder = "Total planned episodes for the series",
                            errorText = errors[DramaField.TOTAL_EPISODES],
                            numeric = true,
                            imeAction = ImeAction.Done,
                            testTag = "admin_field_total_episodes"
                        )
                    }
                }
            }

            item {
                AdminSectionCard(
                    title = "Visibility",
                    subtitle = "Drafts stay inside the console"
                ) {
                    AdminToggleRow(
                        title = "Published",
                        subtitle = if (form.isPublished) {
                            "Visible to every viewer in Home, Discover and For You"
                        } else {
                            "Only admins can see this film"
                        },
                        checked = form.isPublished,
                        onCheckedChange = { form = form.copy(isPublished = it) },
                        testTag = "admin_toggle_published"
                    )
                }
            }

            item {
                Text(
                    text = if (form.editingExisting) {
                        "Episodes are managed separately — save here, then open Episodes from the dashboard."
                    } else {
                        "After creating the film you can add its episodes from the dashboard."
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = ReelTextTertiary,
                        fontSize = 11.sp
                    ),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun FilmCoverPreview(
    title: String,
    genre: DramaGenre,
    badge: String?,
    gradientStart: Long,
    gradientEnd: Long
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ReelSurfaceDark)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 72.dp, height = 104.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.verticalGradient(listOf(Color(gradientStart), Color(gradientEnd)))
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MovieFilter,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "LIVE PREVIEW",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = ReelTextTertiary,
                    fontSize = 9.sp,
                    letterSpacing = 0.6.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title.ifBlank { "Untitled film" },
                style = MaterialTheme.typography.titleSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = genre.displayName,
                    style = MaterialTheme.typography.bodySmall.copy(color = ReelTextSecondary)
                )
                if (!badge.isNullOrBlank()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ReelRedPrimary)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun AdminEditorTopBar(
    title: String,
    subtitle: String,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ReelSurfaceDark)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("admin_editor_back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = ReelTextTertiary,
                    fontSize = 11.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
internal fun AdminEditorActionBar(
    saveLabel: String,
    isSaving: Boolean,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    saveTestTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ReelSurfaceDark)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Cancel", color = ReelTextSecondary)
        }

        Button(
            onClick = onSave,
            enabled = !isSaving,
            modifier = Modifier
                .weight(2f)
                .testTag(saveTestTag),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ReelRedPrimary,
                contentColor = Color.White,
                disabledContainerColor = ReelGoldPrimary.copy(alpha = 0.4f)
            )
        ) {
            Text(
                text = if (isSaving) "Saving..." else saveLabel,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
