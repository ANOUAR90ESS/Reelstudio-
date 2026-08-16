package com.example.data.local

import androidx.room.Entity
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
