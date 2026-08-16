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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.net.Uri
import com.example.data.admin.EpisodeField
import com.example.data.admin.EpisodeFormState
import com.example.data.model.Drama
import com.example.data.model.Episode
import com.example.data.model.ScriptLine
import com.example.data.ai.StoryGenerator
import com.example.data.firebase.MediaUploader
import com.example.ui.components.AdminMediaField
import com.example.ui.components.AdminSectionCard
import com.example.ui.components.AdminTextField
import com.example.ui.components.AdminToggleRow
import com.example.ui.theme.ReelBackgroundDark
import com.example.ui.theme.ReelGoldPrimary
import com.example.ui.theme.ReelRedPrimary
import com.example.ui.theme.ReelSuccess
import com.example.ui.theme.ReelSurfaceDark
import com.example.ui.theme.ReelSurfaceHighlight
import com.example.ui.theme.ReelSurfaceVariantDark
import com.example.ui.theme.ReelTextSecondary
import com.example.ui.theme.ReelTextTertiary

/**
 * Episode ("story") manager for one film: the ordered list of episodes plus the form used to add
 * or edit one. The form replaces the list rather than floating above it, so the long script editor
 * gets the full screen on a phone.
 */
@Composable
fun AdminEpisodeEditorScreen(
    drama: Drama,
    onSaveEpisode: (EpisodeFormState) -> Unit,
    onDeleteEpisode: (Episode) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    isSaving: Boolean = false,
    uploadStates: Map<MediaUploader.MediaKind, MediaUploader.UploadState> = emptyMap(),
    onUploadMedia: (Uri, MediaUploader.MediaKind, String?, (String) -> Unit) -> Unit = { _, _, _, _ -> },
    isGeneratingScript: Boolean = false,
    onGenerateScript: (EpisodeFormState, (StoryGenerator.GeneratedScript) -> Unit) -> Unit = { _, _ -> },
    pendingOutlines: List<StoryGenerator.GeneratedEpisode> = emptyList(),
    onCreateFromOutlines: () -> Unit = {},
    onDismissOutlines: () -> Unit = {}
) {
    var editingForm by remember(drama.id) { mutableStateOf<EpisodeFormState?>(null) }
    var pendingDelete by remember { mutableStateOf<Episode?>(null) }

    val form = editingForm
    if (form != null) {
        // Numbers already claimed by the *other* episodes; the one being edited keeps its own.
        val takenNumbers = drama.episodes
            .filter { it.id != form.id }
            .map { it.episodeNumber }
            .toSet()

        EpisodeForm(
            form = form,
            drama = drama,
            takenEpisodeNumbers = takenNumbers,
            isSaving = isSaving,
            uploadStates = uploadStates,
            onUploadMedia = onUploadMedia,
            isGeneratingScript = isGeneratingScript,
            onGenerateScript = onGenerateScript,
            onFormChange = { editingForm = it },
            onCancel = { editingForm = null },
            onSave = {
                onSaveEpisode(it)
                editingForm = null
            },
            modifier = modifier
        )
        return
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_episode_list_screen"),
        containerColor = ReelBackgroundDark,
        topBar = {
            AdminEditorTopBar(
                title = "Episodes",
                subtitle = drama.title,
                onBack = onBack
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { editingForm = EpisodeFormState.nextFor(drama) },
                containerColor = ReelRedPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("admin_add_episode_fab"),
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(text = "Add Episode", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (pendingOutlines.isNotEmpty()) {
                item {
                    OutlineBanner(
                        outlines = pendingOutlines,
                        onCreate = onCreateFromOutlines,
                        onDismiss = onDismissOutlines
                    )
                }
            }

            item {
                EpisodeSummaryBar(
                    total = drama.episodes.size,
                    free = drama.episodes.count { it.isFree },
                    isPublished = drama.isPublished
                )
            }

            if (drama.episodes.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(ReelSurfaceDark)
                            .padding(vertical = 32.dp, horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No episodes yet",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Add the first episode to give viewers something to play. " +
                                    "Keeping the first few free is what hooks them.",
                            style = MaterialTheme.typography.bodySmall.copy(color = ReelTextTertiary)
                        )
                    }
                }
            } else {
                items(drama.episodes, key = { it.id }) { episode ->
                    EpisodeRow(
                        episode = episode,
                        onEdit = { editingForm = EpisodeFormState.from(episode) },
                        onDelete = { pendingDelete = episode }
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
            title = { Text(text = "Delete episode ${target.episodeNumber}?") },
            text = { Text(text = "\"${target.title}\" will be removed from this film.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteEpisode(target)
                        pendingDelete = null
                    },
                    modifier = Modifier.testTag("admin_confirm_delete_episode_button")
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
private fun EpisodeForm(
    form: EpisodeFormState,
    drama: Drama,
    takenEpisodeNumbers: Set<Int>,
    isSaving: Boolean,
    uploadStates: Map<MediaUploader.MediaKind, MediaUploader.UploadState>,
    onUploadMedia: (Uri, MediaUploader.MediaKind, String?, (String) -> Unit) -> Unit,
    isGeneratingScript: Boolean,
    onGenerateScript: (EpisodeFormState, (StoryGenerator.GeneratedScript) -> Unit) -> Unit,
    onFormChange: (EpisodeFormState) -> Unit,
    onCancel: () -> Unit,
    onSave: (EpisodeFormState) -> Unit,
    modifier: Modifier = Modifier
) {
    var submitted by remember { mutableStateOf(false) }
    val errors = if (submitted) form.validate(takenEpisodeNumbers) else emptyMap()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_episode_form_screen"),
        containerColor = ReelBackgroundDark,
        topBar = {
            AdminEditorTopBar(
                title = if (form.editingExisting) "Edit Episode" else "New Episode",
                subtitle = drama.title,
                onBack = onCancel
            )
        },
        bottomBar = {
            AdminEditorActionBar(
                saveLabel = if (form.editingExisting) "Save Episode" else "Add Episode",
                isSaving = isSaving,
                onCancel = onCancel,
                onSave = {
                    submitted = true
                    if (form.isValid(takenEpisodeNumbers)) onSave(form)
                },
                saveTestTag = "admin_save_episode_button"
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
                AdminSectionCard(title = "Episode", subtitle = "Order and title in the player") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            AdminTextField(
                                value = form.episodeNumber,
                                onValueChange = { onFormChange(form.copy(episodeNumber = it)) },
                                label = "Number",
                                errorText = errors[EpisodeField.NUMBER],
                                numeric = true,
                                modifier = Modifier.width(120.dp),
                                testTag = "admin_field_episode_number"
                            )
                            AdminTextField(
                                value = form.durationSeconds,
                                onValueChange = { onFormChange(form.copy(durationSeconds = it)) },
                                label = "Duration (sec)",
                                errorText = errors[EpisodeField.DURATION],
                                numeric = true,
                                modifier = Modifier.weight(1f),
                                testTag = "admin_field_episode_duration"
                            )
                        }
                        AdminTextField(
                            value = form.title,
                            onValueChange = { onFormChange(form.copy(title = it)) },
                            label = "Episode title",
                            placeholder = "e.g. The Mask Comes Off",
                            errorText = errors[EpisodeField.TITLE],
                            testTag = "admin_field_episode_title"
                        )
                        AdminTextField(
                            value = form.previewSubtitle,
                            onValueChange = { onFormChange(form.copy(previewSubtitle = it)) },
                            label = "Preview line",
                            placeholder = "The teaser caption shown over the player",
                            singleLine = false,
                            minLines = 2,
                            testTag = "admin_field_episode_preview"
                        )
                    }
                }
            }

            item {
                AdminSectionCard(title = "Access", subtitle = "How viewers unlock this episode") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        AdminToggleRow(
                            title = "Free episode",
                            subtitle = if (form.isFree) {
                                "Plays for everyone, no coins needed"
                            } else {
                                "Locked behind coins or VIP"
                            },
                            checked = form.isFree,
                            onCheckedChange = {
                                // Clearing the price with the toggle keeps the two fields from
                                // disagreeing while the admin is still editing.
                                onFormChange(
                                    form.copy(
                                        isFree = it,
                                        coinCost = if (it) "0" else form.coinCost.takeIf { c -> c != "0" } ?: "20"
                                    )
                                )
                            },
                            testTag = "admin_toggle_episode_free"
                        )

                        if (!form.isFree) {
                            AdminTextField(
                                value = form.coinCost,
                                onValueChange = { onFormChange(form.copy(coinCost = it)) },
                                label = "Coin cost",
                                errorText = errors[EpisodeField.COIN_COST],
                                numeric = true,
                                imeAction = ImeAction.Done,
                                testTag = "admin_field_episode_cost"
                            )
                        }
                    }
                }
            }

            item {
                AdminSectionCard(
                    title = "Video",
                    subtitle = "Upload the episode, or paste a URL you already host"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        AdminMediaField(
                            label = "Episode video",
                            description = "Played full screen. Without it the episode falls back to the stylised scene.",
                            value = form.videoUrl,
                            kind = MediaUploader.MediaKind.VIDEO,
                            uploadState = uploadStates[MediaUploader.MediaKind.VIDEO],
                            onPickFile = { uri, name ->
                                onUploadMedia(uri, MediaUploader.MediaKind.VIDEO, name) { url ->
                                    onFormChange(form.copy(videoUrl = url))
                                }
                            },
                            onValueChange = { onFormChange(form.copy(videoUrl = it)) },
                            testTag = "admin_media_episode_video"
                        )
                        AdminMediaField(
                            label = "Thumbnail",
                            description = "Still frame shown in episode lists",
                            value = form.thumbnailUrl,
                            kind = MediaUploader.MediaKind.THUMBNAIL,
                            uploadState = uploadStates[MediaUploader.MediaKind.THUMBNAIL],
                            onPickFile = { uri, name ->
                                onUploadMedia(uri, MediaUploader.MediaKind.THUMBNAIL, name) { url ->
                                    onFormChange(form.copy(thumbnailUrl = url))
                                }
                            },
                            onValueChange = { onFormChange(form.copy(thumbnailUrl = it)) },
                            testTag = "admin_media_episode_thumb"
                        )
                        AdminMediaField(
                            label = "Voiceover track",
                            description = "Optional narration laid over a silent video",
                            value = form.voiceoverUrl,
                            kind = MediaUploader.MediaKind.VOICEOVER,
                            uploadState = uploadStates[MediaUploader.MediaKind.VOICEOVER],
                            onPickFile = { uri, name ->
                                onUploadMedia(uri, MediaUploader.MediaKind.VOICEOVER, name) { url ->
                                    onFormChange(form.copy(voiceoverUrl = url))
                                }
                            },
                            onValueChange = { onFormChange(form.copy(voiceoverUrl = it)) },
                            testTag = "admin_media_episode_voice"
                        )
                    }
                }
            }

            item {
                AdminSectionCard(
                    title = "Script",
                    subtitle = "Subtitle lines shown while the episode plays"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        WriteScriptWithAiButton(
                            isGenerating = isGeneratingScript,
                            onClick = {
                                onGenerateScript(form) { script ->
                                    // The generated script replaces the lines but keeps whatever
                                    // teaser the admin already wrote by hand.
                                    onFormChange(
                                        form.copy(
                                            scriptLines = script.lines,
                                            previewSubtitle = form.previewSubtitle.ifBlank {
                                                script.previewSubtitle
                                            }
                                        )
                                    )
                                }
                            }
                        )

                        ScriptLineEditor(
                            lines = form.scriptLines,
                            onLinesChange = { onFormChange(form.copy(scriptLines = it)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScriptLineEditor(
    lines: List<ScriptLine>,
    onLinesChange: (List<ScriptLine>) -> Unit
) {
    var speaker by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    var timestamp by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        lines.sortedBy { it.timestampSeconds }.forEach { line ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ReelSurfaceVariantDark)
                    .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTimestamp(line.timestampSeconds),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = ReelGoldPrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.width(44.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = line.speaker,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = ReelRedPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = line.text,
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = { onLinesChange(lines - line) }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove line",
                        tint = ReelTextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AdminTextField(
                value = speaker,
                onValueChange = { speaker = it },
                label = "Speaker",
                modifier = Modifier.weight(1f),
                testTag = "admin_field_script_speaker"
            )
            AdminTextField(
                value = timestamp,
                onValueChange = { timestamp = it },
                label = "At (sec)",
                numeric = true,
                modifier = Modifier.width(110.dp),
                testTag = "admin_field_script_time"
            )
        }

        AdminTextField(
            value = text,
            onValueChange = { text = it },
            label = "Line",
            placeholder = "What the character says",
            singleLine = false,
            minLines = 2,
            imeAction = ImeAction.Done,
            testTag = "admin_field_script_text"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (speaker.isNotBlank() && text.isNotBlank()) {
                        ReelRedPrimary
                    } else {
                        ReelSurfaceHighlight
                    }
                )
                .clickable(enabled = speaker.isNotBlank() && text.isNotBlank()) {
                    onLinesChange(
                        lines + ScriptLine(
                            speaker = speaker.trim(),
                            text = text.trim(),
                            timestampSeconds = timestamp.trim().toIntOrNull()?.coerceAtLeast(0) ?: 0
                        )
                    )
                    speaker = ""
                    text = ""
                    timestamp = ""
                }
                .padding(vertical = 10.dp)
                .testTag("admin_add_script_line_button"),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Add script line",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
private fun EpisodeSummaryBar(total: Int, free: Int, isPublished: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ReelSurfaceDark)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$total episode(s) • $free free",
            style = MaterialTheme.typography.bodySmall.copy(color = ReelTextSecondary)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (isPublished) ReelSuccess else ReelGoldPrimary)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isPublished) "Live" else "Draft",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isPublished) ReelSuccess else ReelGoldPrimary,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
private fun EpisodeRow(
    episode: Episode,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ReelSurfaceDark)
            .clickable(onClick = onEdit)
            .padding(12.dp)
            .testTag("admin_episode_row_${episode.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(ReelSurfaceVariantDark),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = episode.episodeNumber.toString(),
                style = MaterialTheme.typography.titleSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = episode.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (episode.isFree) Icons.Default.LockOpen else Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (episode.isFree) ReelSuccess else ReelGoldPrimary,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = buildString {
                        append(if (episode.isFree) "Free" else "${episode.coinCost} coins")
                        append(" • ")
                        append(formatTimestamp(episode.durationSeconds))
                        append(" • ")
                        append("${episode.scriptLines.size} script line(s)")
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = ReelTextTertiary,
                        fontSize = 11.sp
                    )
                )
            }
        }

        IconButton(onClick = onEdit) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit episode ${episode.episodeNumber}",
                tint = ReelTextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
        IconButton(
            onClick = onDelete,
            modifier = Modifier.testTag("admin_delete_episode_${episode.id}")
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete episode ${episode.episodeNumber}",
                tint = ReelRedPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

private fun formatTimestamp(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

/** Offers to turn a generated episode outline into real draft episodes. */
@Composable
private fun OutlineBanner(
    outlines: List<StoryGenerator.GeneratedEpisode>,
    onCreate: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(ReelSurfaceDark)
            .padding(14.dp)
            .testTag("admin_outline_banner")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = ReelGoldPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${outlines.size} episodes outlined by AI",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Create them as drafts, then add a video and script to each one.",
            style = MaterialTheme.typography.bodySmall.copy(
                color = ReelTextTertiary,
                fontSize = 11.sp
            )
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier
                    .weight(2f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(ReelRedPrimary)
                    .clickable(onClick = onCreate)
                    .padding(vertical = 9.dp)
                    .testTag("admin_create_from_outline"),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Create ${outlines.size} episodes",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(ReelSurfaceHighlight)
                    .clickable(onClick = onDismiss)
                    .padding(vertical = 9.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Dismiss",
                    style = MaterialTheme.typography.labelMedium.copy(color = ReelTextSecondary)
                )
            }
        }
    }
}

/** Asks Gemini to write the dialogue for the episode currently open in the form. */
@Composable
private fun WriteScriptWithAiButton(
    isGenerating: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isGenerating) ReelSurfaceHighlight else ReelGoldPrimary.copy(alpha = 0.18f))
            .clickable(enabled = !isGenerating, onClick = onClick)
            .padding(vertical = 10.dp)
            .testTag("admin_write_script_ai"),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isGenerating) {
            CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                color = ReelGoldPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = ReelGoldPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (isGenerating) "Writing the script..." else "Write this script with AI",
            style = MaterialTheme.typography.labelMedium.copy(
                color = ReelGoldPrimary,
                fontWeight = FontWeight.Bold
            )
        )
    }
}
