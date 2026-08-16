package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.ai.StoryGenerator
import com.example.data.model.DramaGenre
import com.example.ui.components.AdminChoiceRow
import com.example.ui.components.AdminTextField
import com.example.ui.theme.ReelGoldPrimary
import com.example.ui.theme.ReelRedDark
import com.example.ui.theme.ReelRedPrimary
import com.example.ui.theme.ReelSurfaceDark
import com.example.ui.theme.ReelSurfaceHighlight
import com.example.ui.theme.ReelSurfaceVariantDark
import com.example.ui.theme.ReelTextSecondary
import com.example.ui.theme.ReelTextTertiary

/**
 * The AI writing tool: an admin types an idea, Gemini returns a full series concept, and the admin
 * reviews it before anything touches the form.
 *
 * Nothing is applied automatically — a generated concept is a draft proposal, and the admin decides
 * whether it is worth keeping.
 */
@Composable
fun AiStoryGeneratorSheet(
    isOpen: Boolean,
    genre: DramaGenre,
    isGenerating: Boolean,
    result: StoryGenerator.GeneratedStory?,
    errorMessage: String?,
    onGenerate: (idea: String, genre: DramaGenre, episodeCount: Int) -> Unit,
    onApply: (StoryGenerator.GeneratedStory) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    var idea by remember { mutableStateOf("") }
    var selectedGenre by remember(genre) { mutableStateOf(genre) }
    var episodeCount by remember { mutableStateOf("10") }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(ReelSurfaceDark)
                .heightIn(max = 620.dp)
                .testTag("ai_story_generator_sheet")
        ) {
            GeneratorHeader()

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                AdminTextField(
                    value = idea,
                    onValueChange = { idea = it },
                    label = "Your idea",
                    placeholder = "A chef discovers her arranged husband is her biggest rival",
                    singleLine = false,
                    minLines = 3,
                    testTag = "ai_idea_field"
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Genre",
                    style = MaterialTheme.typography.labelMedium.copy(color = ReelTextSecondary)
                )
                Spacer(modifier = Modifier.height(6.dp))
                AdminChoiceRow(
                    options = DramaGenre.entries.filter { it != DramaGenre.ALL },
                    selected = selectedGenre,
                    onSelect = { selectedGenre = it },
                    labelOf = { it.displayName }
                )

                Spacer(modifier = Modifier.height(12.dp))

                AdminTextField(
                    value = episodeCount,
                    onValueChange = { episodeCount = it },
                    label = "How many episodes to outline",
                    numeric = true,
                    testTag = "ai_episode_count_field"
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.error
                        )
                    )
                }

                if (result != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    GeneratedStoryPreview(result)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ReelSurfaceVariantDark)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text(text = "Close", color = ReelTextSecondary)
                }

                if (result != null) {
                    Button(
                        onClick = { onApply(result) },
                        modifier = Modifier
                            .weight(2f)
                            .testTag("ai_apply_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ReelRedPrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Text(text = "Use this story", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            onGenerate(idea, selectedGenre, episodeCount.trim().toIntOrNull() ?: 10)
                        },
                        enabled = !isGenerating && idea.isNotBlank(),
                        modifier = Modifier
                            .weight(2f)
                            .testTag("ai_generate_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ReelRedPrimary,
                            contentColor = Color.White,
                            disabledContainerColor = ReelSurfaceHighlight,
                            disabledContentColor = ReelTextTertiary
                        )
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = if (isGenerating) "Writing..." else "Generate story",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GeneratorHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(ReelRedDark, ReelSurfaceDark)))
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(ReelGoldPrimary.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = ReelGoldPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = "AI Story Writer",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "Gemini drafts the series — you review before it is used",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = ReelTextTertiary,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
private fun GeneratedStoryPreview(story: StoryGenerator.GeneratedStory) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ReelSurfaceVariantDark)
            .padding(14.dp)
    ) {
        Text(
            text = story.title,
            style = MaterialTheme.typography.titleSmall.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = story.synopsis,
            style = MaterialTheme.typography.bodySmall.copy(color = ReelTextSecondary)
        )

        if (story.tags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = story.tags.joinToString(" • "),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = ReelGoldPrimary,
                    fontSize = 10.sp
                )
            )
        }

        if (story.episodes.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "${story.episodes.size} EPISODES OUTLINED",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = ReelTextTertiary,
                    fontSize = 9.sp,
                    letterSpacing = 0.6.sp
                )
            )
            Spacer(modifier = Modifier.height(6.dp))

            story.episodes.forEach { episode ->
                Row(modifier = Modifier.padding(vertical = 3.dp)) {
                    Text(
                        text = "${episode.episodeNumber}.",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = ReelRedPrimary,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.width(24.dp)
                    )
                    Column {
                        Text(
                            text = episode.title,
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                        )
                        if (episode.hook.isNotBlank()) {
                            Text(
                                text = episode.hook,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = ReelTextTertiary,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
