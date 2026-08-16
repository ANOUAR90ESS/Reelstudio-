package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.BookmarkEntity
import com.example.data.local.UnlockedEpisodeEntity
import com.example.data.model.Drama
import com.example.ui.components.CoinsBadge
import com.example.ui.components.DramaVideoPlayerView
import com.example.ui.components.VipBadge
import kotlin.math.abs

@Composable
fun ForYouScreen(
    drama: Drama?,
    currentEpisodeIndex: Int,
    isPlaying: Boolean,
    progressSeconds: Int,
    playbackSpeed: Float,
    isVip: Boolean,
    coins: Int,
    bookmarks: List<BookmarkEntity>,
    unlockedList: List<UnlockedEpisodeEntity>,
    onTogglePlayPause: () -> Unit,
    onSeek: (Int) -> Unit,
    onSetSpeed: (Float) -> Unit,
    onToggleBookmark: (String) -> Unit,
    onToggleLike: (String, Int) -> Unit,
    onOpenComments: () -> Unit,
    onOpenEpisodePicker: () -> Unit,
    onNextEpisode: () -> Unit,
    onPrevEpisode: () -> Unit,
    onSwipeVertical: (Boolean) -> Unit,
    onPromptUnlock: (Drama) -> Unit,
    onCoinsClick: () -> Unit,
    onVipClick: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (drama == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
        )
        return
    }

    val currentEpisode = drama.episodes.getOrNull(currentEpisodeIndex)
    val isBookmarked = bookmarks.any { it.dramaId == drama.id }
    val isUnlocked = currentEpisode?.let { ep ->
        ep.isFree || isVip || unlockedList.any { it.dramaId == drama.id && it.episodeNumber == ep.episodeNumber }
    } ?: true

    var dragAccumulator by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("for_you_screen")
            .background(Color.Black)
            .pointerInput(drama.id, currentEpisodeIndex) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (dragAccumulator < -60f) {
                            // Swiped UP -> Next episode / drama
                            onSwipeVertical(true)
                        } else if (dragAccumulator > 60f) {
                            // Swiped DOWN -> Previous episode / drama
                            onSwipeVertical(false)
                        }
                        dragAccumulator = 0f
                    },
                    onVerticalDrag = { _, dragAmount ->
                        dragAccumulator += dragAmount
                    }
                )
            }
    ) {
        AnimatedContent(
            targetState = drama.id to currentEpisodeIndex,
            transitionSpec = {
                (slideInVertically { it } + fadeIn()).togetherWith(slideOutVertically { -it } + fadeOut())
            },
            label = "for_you_player_transition"
        ) { _ ->
            DramaVideoPlayerView(
                drama = drama,
                episodeIndex = currentEpisodeIndex,
                isPlaying = isPlaying,
                progressSeconds = progressSeconds,
                playbackSpeed = playbackSpeed,
                isBookmarked = isBookmarked,
                isUnlocked = isUnlocked,
                onTogglePlayPause = onTogglePlayPause,
                onSeek = onSeek,
                onSetSpeed = onSetSpeed,
                onToggleBookmark = { onToggleBookmark(drama.id) },
                onToggleLike = {
                    currentEpisode?.let { ep ->
                        onToggleLike(drama.id, ep.episodeNumber)
                    }
                },
                onOpenComments = onOpenComments,
                onOpenEpisodePicker = onOpenEpisodePicker,
                onNextEpisode = onNextEpisode,
                onPrevEpisode = onPrevEpisode,
                onPromptUnlock = { onPromptUnlock(drama) },
                onShare = onShare
            )
        }

        // Floating Top Header (ReelShort badge + VIP + Coins)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.foundation.layout.Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)
                ) {
                    androidx.compose.material3.Text(
                        text = "For You",
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                            color = Color.White
                        )
                    )
                }

                androidx.compose.foundation.layout.Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                ) {
                    VipBadge(isVip = isVip, onClick = onVipClick)
                    CoinsBadge(coins = coins, onClick = onCoinsClick)
                }
            }
        }
    }
}
