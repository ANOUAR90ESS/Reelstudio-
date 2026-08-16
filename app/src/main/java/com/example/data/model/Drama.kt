package com.example.data.model

data class Drama(
    val id: String,
    val title: String,
    val description: String,
    val coverGradientStart: Long,
    val coverGradientEnd: Long,
    val badge: String? = null, // "HOT", "NEW", "TOP 1", "VIP"
    val genre: DramaGenre,
    val rating: Float = 4.9f,
    val viewCount: String = "12.4M",
    val likeCount: String = "840K",
    val totalEpisodes: Int = 45,
    val releaseYear: Int = 2024,
    val cast: List<String> = listOf("Alexander Vance", "Elena Sterling", "Marcus Thorne"),
    val director: String = "David Lin",
    val tags: List<String> = listOf("Billionaire", "Secret Identity", "Revenge", "Romance"),
    val episodes: List<Episode> = emptyList(),
    /** Remote poster artwork. When blank the app falls back to [coverGradientStart]/[coverGradientEnd]. */
    val posterUrl: String = "",
    /** Optional trailer played on the detail screen. */
    val trailerUrl: String = "",
    /** True for catalog entries created from the admin console (as opposed to the bundled samples). */
    val isCustom: Boolean = false,
    /** Admin-authored films stay hidden from viewers until they are published. Samples are always live. */
    val isPublished: Boolean = true,
    /** UID of the admin who authored this film; empty for the bundled samples. */
    val createdBy: String = "",
    val updatedAt: Long = 0L
)

data class Episode(
    val id: String,
    val dramaId: String,
    val episodeNumber: Int,
    val title: String,
    val durationSeconds: Int = 85,
    val isFree: Boolean = false,
    val coinCost: Int = 20,
    val previewSubtitle: String = "The shocking confrontation begins...",
    /**
     * Playable video for this episode. When blank the player falls back to the stylised canvas
     * scene, so an episode is still previewable while its video is being produced.
     */
    val videoUrl: String = "",
    /** Still frame shown before playback starts and in episode lists. */
    val thumbnailUrl: String = "",
    /** Optional separate voiceover track, e.g. a generated narration laid over a silent video. */
    val voiceoverUrl: String = "",
    val scriptLines: List<ScriptLine> = emptyList()
) {
    val hasVideo: Boolean get() = videoUrl.isNotBlank()
}

data class ScriptLine(
    val speaker: String,
    val text: String,
    val timestampSeconds: Int
)

enum class DramaGenre(val displayName: String) {
    ALL("All"),
    BILLIONAIRE("Billionaire"),
    WEREWOLF("Werewolf & Alpha"),
    REVENGE("Revenge"),
    ROMANCE("Romance"),
    SWEET_LOVE("Sweet Love"),
    SUSPENSE("Urban & Suspense")
}

data class DramaComment(
    val id: String,
    val dramaId: String,
    val episodeNumber: Int,
    val userName: String,
    val userAvatarColor: Long,
    val content: String,
    val timestamp: String,
    val likes: Int = 12,
    val isLiked: Boolean = false
)

data class CoinPackage(
    val id: String,
    val coins: Int,
    val bonusCoins: Int,
    val priceUsd: String,
    val isPopular: Boolean = false,
    val tag: String? = null
)

data class DailyTask(
    val id: String,
    val title: String,
    val description: String,
    val rewardCoins: Int,
    val targetCount: Int,
    val currentCount: Int,
    val isClaimed: Boolean = false
)
