package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey
    val dramaId: String,
    val episodeNumber: Int,
    val progressSeconds: Int,
    val lastWatchedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey
    val dramaId: String,
    val savedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "unlocked_episodes", primaryKeys = ["dramaId", "episodeNumber"])
data class UnlockedEpisodeEntity(
    val dramaId: String,
    val episodeNumber: Int,
    val unlockedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_account")
data class UserAccountEntity(
    @PrimaryKey
    val id: Int = 1,
    val coins: Int = 120, // Starting bonus
    val isVip: Boolean = false,
    val vipExpiryTimestamp: Long = 0L,
    val checkinStreak: Int = 1,
    val lastCheckinDateEpochDay: Long = 0L,
    val totalEpisodesWatched: Int = 0
)

@Entity(tableName = "user_likes", primaryKeys = ["dramaId", "episodeNumber"])
data class UserLikeEntity(
    val dramaId: String,
    val episodeNumber: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_comments")
data class LocalCommentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dramaId: String,
    val episodeNumber: Int,
    val userName: String,
    val userAvatarColor: Long,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val likes: Int = 0
)

/**
 * A film authored from the admin console. Bundled sample films are not stored here — the catalog
 * shown to viewers is the union of [com.example.data.model.SampleDramas] and the published rows of
 * this table.
 */
@Entity(tableName = "admin_dramas")
data class DramaEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val coverGradientStart: Long,
    val coverGradientEnd: Long,
    val badge: String? = null,
    /** Name of a [com.example.data.model.DramaGenre] constant. */
    val genre: String,
    val rating: Float = 4.8f,
    val viewCount: String = "0",
    val likeCount: String = "0",
    val totalEpisodes: Int = 0,
    val releaseYear: Int = 2024,
    val cast: List<String> = emptyList(),
    val director: String = "",
    val tags: List<String> = emptyList(),
    val posterUrl: String = "",
    val trailerUrl: String = "",
    /** Drafts are invisible to viewers; publishing pushes the film into the public catalog. */
    val isPublished: Boolean = false,
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/** An episode ("story") belonging to an admin-authored film. */
@Entity(
    tableName = "admin_episodes",
    indices = [Index(value = ["dramaId"])]
)
data class EpisodeEntity(
    @PrimaryKey
    val id: String,
    val dramaId: String,
    val episodeNumber: Int,
    val title: String,
    val durationSeconds: Int = 85,
    val isFree: Boolean = false,
    val coinCost: Int = 20,
    val previewSubtitle: String = "",
    val videoUrl: String = "",
    val thumbnailUrl: String = "",
    val voiceoverUrl: String = "",
    /** Encoded script lines, see [com.example.data.admin.AdminMappers]. */
    val scriptLines: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
