package com.example.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.example.data.firebase.MediaUploader
import com.example.ui.theme.ReelGoldPrimary
import com.example.ui.theme.ReelRedPrimary
import com.example.ui.theme.ReelSuccess
import com.example.ui.theme.ReelSurfaceHighlight
import com.example.ui.theme.ReelSurfaceVariantDark
import com.example.ui.theme.ReelTextSecondary
import com.example.ui.theme.ReelTextTertiary

/**
 * One media slot in the admin editors: pick a file from the device and upload it, or paste a URL
 * that is already hosted somewhere.
 *
 * Both paths end in the same place — a URL stored on the film or episode — so an admin who already
 * has a CDN is never forced through Storage, and an admin who does not never has to find one.
 */
@Composable
fun AdminMediaField(
    label: String,
    description: String,
    value: String,
    kind: MediaUploader.MediaKind,
    uploadState: MediaUploader.UploadState?,
    onPickFile: (android.net.Uri, String?) -> Unit,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String? = null
) {
    var showUrlField by remember { mutableStateOf(false) }

    val pickerRequest = remember(kind) {
        when (kind) {
            MediaUploader.MediaKind.VIDEO ->
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)

            MediaUploader.MediaKind.POSTER, MediaUploader.MediaKind.THUMBNAIL ->
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)

            // The photo picker cannot return audio, so voiceover files come through OpenDocument.
            MediaUploader.MediaKind.VOICEOVER ->
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
        }
    }

    val visualPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { onPickFile(it, it.lastPathSegment) } }

    val documentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { onPickFile(it, it.lastPathSegment) } }

    val launchPicker = {
        if (kind == MediaUploader.MediaKind.VOICEOVER) {
            documentPicker.launch(arrayOf("audio/*"))
        } else {
            visualPicker.launch(pickerRequest)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ReelSurfaceVariantDark)
            .padding(12.dp)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = ReelTextTertiary,
                        fontSize = 11.sp
                    )
                )
            }

            if (value.isNotBlank()) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Attached",
                    tint = ReelSuccess,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (uploadState) {
            is MediaUploader.UploadState.InProgress -> {
                UploadProgress(state = uploadState)
            }

            is MediaUploader.UploadState.Failed -> {
                Text(
                    text = uploadState.error.localizedMessage ?: "Upload failed",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.error
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                MediaActions(
                    hasValue = value.isNotBlank(),
                    onUpload = launchPicker,
                    onToggleUrl = { showUrlField = !showUrlField },
                    onClear = { onValueChange("") }
                )
            }

            else -> {
                if (value.isNotBlank()) {
                    AttachedFileRow(url = value, onClear = { onValueChange("") })
                    Spacer(modifier = Modifier.height(8.dp))
                }
                MediaActions(
                    hasValue = value.isNotBlank(),
                    onUpload = launchPicker,
                    onToggleUrl = { showUrlField = !showUrlField },
                    onClear = { onValueChange("") }
                )
            }
        }

        if (showUrlField) {
            Spacer(modifier = Modifier.height(10.dp))
            AdminTextField(
                value = value,
                onValueChange = onValueChange,
                label = "Media URL",
                placeholder = "https://...",
                imeAction = ImeAction.Done
            )
        }
    }
}

@Composable
private fun UploadProgress(state: MediaUploader.UploadState.InProgress) {
    Column(modifier = Modifier.fillMaxWidth()) {
        val fraction = state.fraction
        if (fraction == null) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = ReelRedPrimary,
                trackColor = ReelSurfaceHighlight
            )
        } else {
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier.fillMaxWidth(),
                color = ReelRedPrimary,
                trackColor = ReelSurfaceHighlight
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = buildString {
                append("Uploading")
                if (fraction != null) append(" ${(fraction * 100).toInt()}%")
                if (state.totalBytes > 0) {
                    append(" • ${formatBytes(state.bytesTransferred)} / ${formatBytes(state.totalBytes)}")
                }
            },
            style = MaterialTheme.typography.labelSmall.copy(
                color = ReelTextSecondary,
                fontSize = 11.sp
            )
        )
    }
}

@Composable
private fun AttachedFileRow(url: String, onClear: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(ReelSurfaceHighlight)
            .padding(start = 10.dp, end = 2.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Link,
            contentDescription = null,
            tint = ReelGoldPrimary,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = url,
            style = MaterialTheme.typography.bodySmall.copy(
                color = ReelTextSecondary,
                fontSize = 11.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onClear, modifier = Modifier.size(28.dp)) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove media",
                tint = ReelTextTertiary,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun MediaActions(
    hasValue: Boolean,
    onUpload: () -> Unit,
    onToggleUrl: () -> Unit,
    onClear: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MediaActionChip(
            label = if (hasValue) "Replace file" else "Upload file",
            onClick = onUpload,
            highlighted = true,
            modifier = Modifier.weight(1f)
        )
        MediaActionChip(
            label = "Use URL",
            onClick = onToggleUrl,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MediaActionChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (highlighted) ReelRedPrimary else ReelSurfaceHighlight)
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (highlighted) {
            Icon(
                imageVector = Icons.Default.CloudUpload,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                color = if (highlighted) Color.White else ReelTextSecondary,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

private fun formatBytes(bytes: Long): String = when {
    bytes >= 1_000_000_000 -> "%.1f GB".format(bytes / 1_000_000_000.0)
    bytes >= 1_000_000 -> "%.1f MB".format(bytes / 1_000_000.0)
    bytes >= 1_000 -> "%.0f KB".format(bytes / 1_000.0)
    else -> "$bytes B"
}
