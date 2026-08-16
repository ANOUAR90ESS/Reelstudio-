package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReelShortDao {

    // History
    @Query("SELECT * FROM watch_history ORDER BY lastWatchedTimestamp DESC")
    fun getAllWatchHistory(): Flow<List<WatchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveWatchHistory(history: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE dramaId = :dramaId")
    suspend fun removeWatchHistory(dramaId: String)

    @Query("DELETE FROM watch_history")
    suspend fun clearAllHistory()

    // Bookmarks / My List
    @Query("SELECT * FROM bookmarks ORDER BY savedTimestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE dramaId = :dramaId)")
    fun isBookmarked(dramaId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE dramaId = :dramaId")
    suspend fun removeBookmark(dramaId: String)

    // Unlocked Episodes
    @Query("SELECT * FROM unlocked_episodes WHERE dramaId = :dramaId")
    fun getUnlockedEpisodesForDrama(dramaId: String): Flow<List<UnlockedEpisodeEntity>>

    @Query("SELECT * FROM unlocked_episodes")
    fun getAllUnlockedEpisodes(): Flow<List<UnlockedEpisodeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun unlockEpisode(unlocked: UnlockedEpisodeEntity)

    // Account & Coins
    @Query("SELECT * FROM user_account WHERE id = 1 LIMIT 1")
    fun getUserAccount(): Flow<UserAccountEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserAccount(account: UserAccountEntity)

    @Query("UPDATE user_account SET coins = coins + :amount WHERE id = 1")
    suspend fun addCoins(amount: Int)

    @Query("UPDATE user_account SET coins = coins - :amount WHERE id = 1 AND coins >= :amount")
    suspend fun deductCoins(amount: Int): Int

    // Likes
    @Query("SELECT EXISTS(SELECT 1 FROM user_likes WHERE dramaId = :dramaId AND episodeNumber = :episodeNumber)")
    fun isEpisodeLiked(dramaId: String, episodeNumber: Int): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun likeEpisode(like: UserLikeEntity)

    @Query("DELETE FROM user_likes WHERE dramaId = :dramaId AND episodeNumber = :episodeNumber")
    suspend fun unlikeEpisode(dramaId: String, episodeNumber: Int)

    // Comments
    @Query("SELECT * FROM user_comments WHERE dramaId = :dramaId AND episodeNumber = :episodeNumber ORDER BY timestamp DESC")
    fun getComments(dramaId: String, episodeNumber: Int): Flow<List<LocalCommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: LocalCommentEntity)
}
