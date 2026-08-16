package com.example.data.repository

import com.example.data.local.BookmarkEntity
import com.example.data.local.LocalCommentEntity
import com.example.data.local.ReelShortDao
import com.example.data.local.UnlockedEpisodeEntity
import com.example.data.local.UserAccountEntity
import com.example.data.local.UserLikeEntity
import com.example.data.local.WatchHistoryEntity
import com.example.data.model.DailyTask
import com.example.data.model.Drama
import com.example.data.model.DramaComment
import com.example.data.model.DramaGenre
import com.example.data.model.Episode
import com.example.data.model.SampleDramas
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

class DramaRepository(private val dao: ReelShortDao) {

    private val _dramas = MutableStateFlow<List<Drama>>(SampleDramas.dramas)
    val dramas: StateFlow<List<Drama>> = _dramas.asStateFlow()

    private val _dailyTasks = MutableStateFlow<List<DailyTask>>(SampleDramas.defaultDailyTasks)
    val dailyTasks: StateFlow<List<DailyTask>> = _dailyTasks.asStateFlow()

    val userAccount: Flow<UserAccountEntity> = dao.getUserAccount().map {
        it ?: UserAccountEntity(id = 1, coins = 200, isVip = false, checkinStreak = 1)
    }

    val watchHistory: Flow<List<WatchHistoryEntity>> = dao.getAllWatchHistory()
    val bookmarks: Flow<List<BookmarkEntity>> = dao.getAllBookmarks()
    val allUnlockedEpisodes: Flow<List<UnlockedEpisodeEntity>> = dao.getAllUnlockedEpisodes()

    fun getDramaById(dramaId: String): Drama? {
        return _dramas.value.find { it.id == dramaId }
    }

    fun getDramasByGenre(genre: DramaGenre): List<Drama> {
        return if (genre == DramaGenre.ALL) {
            _dramas.value
        } else {
            _dramas.value.filter { it.genre == genre }
        }
    }

    fun searchDramas(query: String, genre: DramaGenre = DramaGenre.ALL): List<Drama> {
        val q = query.trim().lowercase()
        return _dramas.value.filter { drama ->
            val matchesGenre = genre == DramaGenre.ALL || drama.genre == genre
            val matchesQuery = q.isEmpty() ||
                    drama.title.lowercase().contains(q) ||
                    drama.description.lowercase().contains(q) ||
                    drama.tags.any { it.lowercase().contains(q) } ||
                    drama.cast.any { it.lowercase().contains(q) }
            matchesGenre && matchesQuery
        }
    }

    fun isBookmarked(dramaId: String): Flow<Boolean> = dao.isBookmarked(dramaId)

    suspend fun toggleBookmark(dramaId: String, currentStatus: Boolean) {
        if (currentStatus) {
            dao.removeBookmark(dramaId)
        } else {
            dao.addBookmark(BookmarkEntity(dramaId))
        }
    }

    fun isEpisodeLiked(dramaId: String, episodeNumber: Int): Flow<Boolean> =
        dao.isEpisodeLiked(dramaId, episodeNumber)

    suspend fun toggleLike(dramaId: String, episodeNumber: Int, isLiked: Boolean) {
        if (isLiked) {
            dao.unlikeEpisode(dramaId, episodeNumber)
        } else {
            dao.likeEpisode(UserLikeEntity(dramaId, episodeNumber))
            incrementTaskProgress("task_like_5")
        }
    }

    suspend fun saveProgress(dramaId: String, episodeNumber: Int, progressSeconds: Int) {
        dao.saveWatchHistory(
            WatchHistoryEntity(
                dramaId = dramaId,
                episodeNumber = episodeNumber,
                progressSeconds = progressSeconds,
                lastWatchedTimestamp = System.currentTimeMillis()
            )
        )
        incrementTaskProgress("task_watch_3")
    }

    suspend fun removeHistoryItem(dramaId: String) {
        dao.removeWatchHistory(dramaId)
    }

    suspend fun clearAllHistory() {
        dao.clearAllHistory()
    }

    fun isEpisodeUnlocked(dramaId: String, episode: Episode, isVip: Boolean): Flow<Boolean> {
        if (isVip || episode.isFree) {
            return kotlinx.coroutines.flow.flowOf(true)
        }
        return dao.getUnlockedEpisodesForDrama(dramaId).map { list ->
            list.any { it.episodeNumber == episode.episodeNumber }
        }
    }

    suspend fun unlockEpisodeWithCoins(dramaId: String, episodeNumber: Int, cost: Int): Boolean {
        val rows = dao.deductCoins(cost)
        if (rows > 0) {
            dao.unlockEpisode(UnlockedEpisodeEntity(dramaId, episodeNumber))
            return true
        }
        return false
    }

    suspend fun unlockEpisodeFreeReward(dramaId: String, episodeNumber: Int) {
        dao.unlockEpisode(UnlockedEpisodeEntity(dramaId, episodeNumber))
    }

    suspend fun performDailyCheckIn(currentAccount: UserAccountEntity): Int {
        val todayEpochDay = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
        val streak = if (currentAccount.lastCheckinDateEpochDay == todayEpochDay - 1) {
            (currentAccount.checkinStreak % 7) + 1
        } else if (currentAccount.lastCheckinDateEpochDay == todayEpochDay) {
            return 0 // Already checked in today
        } else {
            1
        }

        val bonusCoins = when (streak) {
            1 -> 50
            2 -> 60
            3 -> 80
            4 -> 100
            5 -> 120
            6 -> 150
            7 -> 300
            else -> 50
        }

        val updated = currentAccount.copy(
            coins = currentAccount.coins + bonusCoins,
            checkinStreak = streak,
            lastCheckinDateEpochDay = todayEpochDay
        )
        dao.saveUserAccount(updated)
        claimTask("task_daily_checkin")
        return bonusCoins
    }

    suspend fun purchaseCoins(amount: Int, bonus: Int) {
        dao.addCoins(amount + bonus)
    }

    suspend fun activateVip(durationDays: Int = 30) {
        val expiry = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(durationDays.toLong())
        val account = dao.getUserAccount()
        account.collect { current ->
            val curr = current ?: UserAccountEntity()
            dao.saveUserAccount(
                curr.copy(
                    isVip = true,
                    vipExpiryTimestamp = expiry
                )
            )
            return@collect
        }
    }

    fun getComments(dramaId: String, episodeNumber: Int): Flow<List<DramaComment>> {
        val sampleComments = SampleDramas.sampleComments.filter {
            it.dramaId == dramaId && it.episodeNumber == episodeNumber
        }
        return dao.getComments(dramaId, episodeNumber).map { localList ->
            val mappedLocal = localList.map {
                DramaComment(
                    id = "local_${it.id}",
                    dramaId = it.dramaId,
                    episodeNumber = it.episodeNumber,
                    userName = it.userName,
                    userAvatarColor = it.userAvatarColor,
                    content = it.content,
                    timestamp = "Just now",
                    likes = it.likes,
                    isLiked = false
                )
            }
            mappedLocal + sampleComments
        }
    }

    suspend fun postComment(dramaId: String, episodeNumber: Int, text: String, userName: String = "You") {
        val entity = LocalCommentEntity(
            dramaId = dramaId,
            episodeNumber = episodeNumber,
            userName = userName,
            userAvatarColor = 0xFFE50914,
            content = text,
            timestamp = System.currentTimeMillis(),
            likes = 1
        )
        dao.insertComment(entity)
    }

    fun incrementTaskProgress(taskId: String) {
        _dailyTasks.value = _dailyTasks.value.map { task ->
            if (task.id == taskId && !task.isClaimed) {
                val newCount = (task.currentCount + 1).coerceAtMost(task.targetCount)
                task.copy(currentCount = newCount)
            } else {
                task
            }
        }
    }

    suspend fun claimTask(taskId: String): Int {
        var reward = 0
        _dailyTasks.value = _dailyTasks.value.map { task ->
            if (task.id == taskId && task.currentCount >= task.targetCount && !task.isClaimed) {
                reward = task.rewardCoins
                task.copy(isClaimed = true)
            } else {
                task
            }
        }
        if (reward > 0) {
            dao.addCoins(reward)
        }
        return reward
    }
}
