package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.SlowMotionVideo
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Drama
import com.example.data.model.Episode
import com.example.ui.theme.ReelGoldLight
import com.example.ui.theme.ReelGoldPrimary
import com.example.ui.theme.ReelRedDark
import com.example.ui.theme.ReelRedLight
import com.example.ui.theme.ReelRedPrimary
import com.example.ui.theme.ReelSurfaceDark
import com.example.ui.theme.ReelTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DramaVideoPlayerView(
    drama: Drama,
    episodeIndex: Int,
    isPlaying: Boolean,
    progressSeconds: Int,
    playbackSpeed: Float,
    isBookmarked: Boolean,
    isUnlocked: Boolean,
    onTogglePlayPause: () -> Unit,
    onSeek: (Int) -> Unit,
    onSetSpeed: (Float) -> Unit,
    onToggleBookmark: () -> Unit,
    onToggleLike: () -> Unit,
    onOpenComments: () -> Unit,
    onOpenEpisodePicker: () -> Unit,
    onNextEpisode: () -> Unit,
    onPrevEpisode: () -> Unit,
    onPromptUnlock: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val currentEpisode = drama.episodes.getOrNull(episodeIndex) ?: Episode("default", drama.id, 1, "Episode 1")
    val duration = currentEpisode.durationSeconds

    var showHeartAnimation by remember { mutableStateOf(false) }
    var showPlayPauseIndicator by remember { mutableStateOf(false) }
    var speedMenuExpanded by remember { mutableStateOf(false) }
    var isLiked by remember { mutableStateOf(false) }

    // Dynamic camera slow pan effect
    val infiniteTransition = rememberInfiniteTransition(label = "camera_pan")
    val panOffset by infiniteTransition.animateFloat(
        initialValue = -30f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pan_offset"
    )

    // Current subtitle script line
    val currentScriptLine = remember(progressSeconds, currentEpisode) {
        currentEpisode.scriptLines.findLast { progressSeconds >= it.timestampSeconds }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        isLiked = true
                        onToggleLike()
                        showHeartAnimation = true
                        coroutineScope.launch {
                            delay(900)
                            showHeartAnimation = false
                        }
                    },
                    onTap = {
                        onTogglePlayPause()
                        showPlayPauseIndicator = true
                        coroutineScope.launch {
                            delay(700)
                            showPlayPauseIndicator = false
                        }
                    }
                )
            }
    ) {
        // --- 1. Realistic Cinematic Visual Canvas ---
        CinematicSceneCanvas(
            gradientStart = drama.coverGradientStart,
            gradientEnd = drama.coverGradientEnd,
            isPlaying = isPlaying,
            panOffset = panOffset,
            episodeNumber = currentEpisode.episodeNumber
        )

        // Vignette gradients (top and bottom for UI readability)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        0f to Color(0xAA000000),
                        0.18f to Color.Transparent,
                        0.60f to Color.Transparent,
                        1f to Color(0xEE000000)
                    )
                )
        )

        // --- 2. Center Subtitle / Script Line Overlay ---
        if (currentScriptLine != null && isUnlocked) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x99000000))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = currentScriptLine.speaker.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = ReelGoldPrimary,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "\"${currentScriptLine.text}\"",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    )
                }
            }
        }

        // --- 3. Double-Tap Burst Heart Animation ---
        AnimatedVisibility(
            visible = showHeartAnimation,
            enter = scaleIn(tween(250)) + fadeIn(),
            exit = scaleOut(tween(250)) + fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = ReelRedPrimary,
                modifier = Modifier.size(90.dp)
            )
        }

        // --- 4. Play/Pause Indicator Flash on Center Tap ---
        AnimatedVisibility(
            visible = showPlayPauseIndicator,
            enter = scaleIn(tween(150)) + fadeIn(),
            exit = scaleOut(tween(200)) + fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0x99000000)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // --- 5. Locked Overlay Banner (if not unlocked) ---
        if (!isUnlocked) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xCC05060A))
                    .clickable(onClick = onPromptUnlock),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(ReelRedDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = ReelGoldPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Episode ${currentEpisode.episodeNumber} is Locked",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = currentEpisode.previewSubtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ReelTextSecondary,
                            fontSize = 13.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(ReelRedPrimary)
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "Tap to Unlock (20 Coins or Free Ad)",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }

        // --- 6. Right Side Action Bar (Likes, Comments, Bookmark, Share, Episodes, Speed) ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 90.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Like Button
            ActionIconButton(
                icon = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                label = drama.likeCount,
                tint = if (isLiked) ReelRedPrimary else Color.White,
                onClick = {
                    isLiked = !isLiked
                    onToggleLike()
                },
                testTag = "like_action_button"
            )

            // Comments Button
            ActionIconButton(
                icon = Icons.Default.ChatBubble,
                label = "Comments",
                tint = Color.White,
                onClick = onOpenComments,
                testTag = "comments_action_button"
            )

            // Bookmark / Save to My List
            ActionIconButton(
                icon = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                label = if (isBookmarked) "Saved" else "Save",
                tint = if (isBookmarked) ReelGoldPrimary else Color.White,
                onClick = onToggleBookmark,
                testTag = "bookmark_action_button"
            )

            // Episodes List Button
            ActionIconButton(
                icon = Icons.Default.FormatListNumbered,
                label = "Ep ${currentEpisode.episodeNumber}/${drama.totalEpisodes}",
                tint = Color.White,
                onClick = onOpenEpisodePicker,
                testTag = "episodes_action_button"
            )

            // Speed Control Button
            Box {
                ActionIconButton(
                    icon = Icons.Default.Speed,
                    label = "${playbackSpeed}x",
                    tint = if (playbackSpeed != 1.0f) ReelGoldPrimary else Color.White,
                    onClick = { speedMenuExpanded = true },
                    testTag = "speed_action_button"
                )

                DropdownMenu(
                    expanded = speedMenuExpanded,
                    onDismissRequest = { speedMenuExpanded = false },
                    modifier = Modifier.background(ReelSurfaceDark)
                ) {
                    listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { sp ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "${sp}x Speed",
                                    color = if (sp == playbackSpeed) ReelRedPrimary else Color.White,
                                    fontWeight = if (sp == playbackSpeed) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                onSetSpeed(sp)
                                speedMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // Share Button
            ActionIconButton(
                icon = Icons.Default.Share,
                label = "Share",
                tint = Color.White,
                onClick = onShare,
                testTag = "share_action_button"
            )
        }

        // --- 7. Bottom Left Info & Controls Bar ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, end = 76.dp, bottom = 48.dp)
        ) {
            // Episode chip tag
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ReelRedPrimary)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "EPISODE ${currentEpisode.episodeNumber}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontSize = 11.sp
                        )
                    )
                }

                if (currentEpisode.isFree) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF2E7D32))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "FREE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                Text(
                    text = drama.genre.displayName,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = ReelGoldLight,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Drama Title
            Text(
                text = drama.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Episode Title & logline
            Text(
                text = "${currentEpisode.title} — ${currentEpisode.previewSubtitle}",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xCCFFFFFF),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Lead Cast
            Text(
                text = "Starring: " + drama.cast.take(2).joinToString(", "),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = ReelTextSecondary,
                    fontSize = 11.sp
                )
            )
        }

        // --- 8. Bottom Scrubber & Quick Prev/Next Navigation ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Time tracker: 00:32 / 01:25
                Text(
                    text = formatTime(progressSeconds) + " / " + formatTime(duration),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xCCFFFFFF),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                )

                // Quick Prev / Next episode buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onPrevEpisode,
                        enabled = episodeIndex > 0,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous Episode",
                            tint = if (episodeIndex > 0) Color.White else Color.DarkGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onNextEpisode,
                        enabled = episodeIndex < drama.episodes.size - 1,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next Episode",
                            tint = if (episodeIndex < drama.episodes.size - 1) Color.White else Color.DarkGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Slider scrubber
            Slider(
                value = progressSeconds.toFloat().coerceIn(0f, duration.toFloat().coerceAtLeast(1f)),
                onValueChange = { onSeek(it.toInt()) },
                valueRange = 0f..duration.toFloat().coerceAtLeast(1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp)
                    .testTag("playback_scrubber"),
                colors = SliderDefaults.colors(
                    thumbColor = ReelRedPrimary,
                    activeTrackColor = ReelRedPrimary,
                    inactiveTrackColor = Color(0x44FFFFFF)
                )
            )
        }
    }
}

@Composable
private fun ActionIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .testTag(testTag)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0x55000000)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 10.sp
            )
        )
    }
}

@Composable
private fun CinematicSceneCanvas(
    gradientStart: Long,
    gradientEnd: Long,
    isPlaying: Boolean,
    panOffset: Float,
    episodeNumber: Int
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Dynamic background gradient
        drawRect(
            brush = Brush.verticalGradient(
                listOf(
                    Color(gradientStart),
                    Color(gradientEnd),
                    Color(0xFF07080E)
                )
            )
        )

        // Architectural luxury cinema lighting beams
        val beamPath = Path().apply {
            moveTo(0f, 0f)
            lineTo(width * 0.4f + panOffset, height * 0.45f)
            lineTo(width * 0.9f + panOffset, height)
            lineTo(0f, height)
            close()
        }
        drawPath(
            path = beamPath,
            brush = Brush.linearGradient(
                colors = listOf(Color(0x18FFFFFF), Color.Transparent),
                start = Offset(0f, 0f),
                end = Offset(width, height)
            )
        )

        // Atmospheric glowing circular ambient spotlights
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x35E50914), Color.Transparent),
                center = Offset(width * 0.5f + panOffset, height * 0.35f),
                radius = width * 0.6f
            )
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x22FFD54F), Color.Transparent),
                center = Offset(width * 0.7f, height * 0.7f),
                radius = width * 0.45f
            )
        )

        // Subtle film grain lines
        for (i in 0..5) {
            val yPos = (height / 6) * i + (panOffset * 2)
            drawLine(
                color = Color(0x06FFFFFF),
                start = Offset(0f, yPos),
                end = Offset(width, yPos),
                strokeWidth = 1f
            )
        }
    }
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}
