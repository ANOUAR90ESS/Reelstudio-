package com.example.ui.components

import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlin.math.abs

/**
 * Real video playback for an episode.
 *
 * The app drives playback from its own state — [isPlaying], [progressSeconds] and [speed] live in
 * the ViewModel and are shared with the fallback canvas renderer — so this surface is a follower:
 * it mirrors that state onto ExoPlayer rather than owning it. The player's own controls are hidden
 * because the app already draws its scrubber and gesture layer on top.
 */
@OptIn(UnstableApi::class)
@Composable
fun EpisodeVideoSurface(
    videoUrl: String,
    isPlaying: Boolean,
    progressSeconds: Int,
    speed: Float,
    modifier: Modifier = Modifier,
    onPlaybackEnded: () -> Unit = {}
) {
    val context = LocalContext.current

    val exoPlayer = remember(videoUrl) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            // Vertical short-form: the next episode is a swipe away, so never loop the current one.
            repeatMode = Player.REPEAT_MODE_OFF
            prepare()
        }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    onPlaybackEnded()
                }
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    LaunchedEffect(isPlaying) {
        exoPlayer.playWhenReady = isPlaying
    }

    LaunchedEffect(speed) {
        exoPlayer.playbackParameters = PlaybackParameters(speed.coerceIn(0.25f, 3f))
    }

    LaunchedEffect(progressSeconds) {
        // The app ticks its own progress every second, which would fight the player if every tick
        // caused a seek. Only correct the player when the two have genuinely drifted apart —
        // i.e. the user scrubbed or jumped episodes.
        val target = progressSeconds * 1000L
        if (abs(exoPlayer.currentPosition - target) > SEEK_TOLERANCE_MS) {
            exoPlayer.seekTo(target)
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = false
                // Vertical drama fills the frame; letterboxing would waste a phone screen.
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                setShutterBackgroundColor(android.graphics.Color.BLACK)
            }
        },
        onRelease = { view -> view.player = null }
    )
}

private const val SEEK_TOLERANCE_MS = 1500L
