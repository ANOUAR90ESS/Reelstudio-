package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
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

    // ==========================================
    // ADMIN CONSOLE: films & episodes authoring
    // ==========================================

    @Query("SELECT * FROM admin_dramas ORDER BY updatedAt DESC")
    fun getAllAdminDramas(): Flow<List<DramaEntity>>

    @Query("SELECT * FROM admin_episodes ORDER BY dramaId ASC, episodeNumber ASC")
    fun getAllAdminEpisodes(): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM admin_dramas WHERE id = :dramaId LIMIT 1")
    suspend fun getAdminDrama(dramaId: String): DramaEntity?

    @Query("SELECT * FROM admin_episodes WHERE dramaId = :dramaId ORDER BY episodeNumber ASC")
    suspend fun getAdminEpisodes(dramaId: String): List<EpisodeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAdminDrama(drama: DramaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAdminEpisode(episode: EpisodeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAdminEpisodes(episodes: List<EpisodeEntity>)

    @Query("DELETE FROM admin_dramas WHERE id = :dramaId")
    suspend fun deleteAdminDrama(dramaId: String)

    @Query("DELETE FROM admin_episodes WHERE dramaId = :dramaId")
    suspend fun deleteAdminEpisodesForDrama(dramaId: String)

    @Query("DELETE FROM admin_episodes WHERE id = :episodeId")
    suspend fun deleteAdminEpisode(episodeId: String)

    @Query("UPDATE admin_dramas SET isPublished = :published, updatedAt = :updatedAt WHERE id = :dramaId")
    suspend fun setAdminDramaPublished(dramaId: String, published: Boolean, updatedAt: Long)

    @Query("UPDATE admin_dramas SET totalEpisodes = (SELECT COUNT(*) FROM admin_episodes WHERE dramaId = :dramaId), updatedAt = :updatedAt WHERE id = :dramaId")
    suspend fun refreshAdminDramaEpisodeCount(dramaId: String, updatedAt: Long)

    /**
     * Deleting a film also drops every episode it owns and any viewer state that pointed at it, so
     * a removed film cannot leave orphaned bookmarks or purchased-episode rows behind.
     */
    @Transaction
    suspend fun deleteAdminDramaCascade(dramaId: String) {
        deleteAdminEpisodesForDrama(dramaId)
        deleteAdminDrama(dramaId)
        removeBookmark(dramaId)
        removeWatchHistory(dramaId)
        deleteUnlockedEpisodesForDrama(dramaId)
    }

    @Query("DELETE FROM unlocked_episodes WHERE dramaId = :dramaId")
    suspend fun deleteUnlockedEpisodesForDrama(dramaId: String)
}
