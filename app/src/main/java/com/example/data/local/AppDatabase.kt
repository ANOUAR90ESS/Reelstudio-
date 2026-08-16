package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        WatchHistoryEntity::class,
        BookmarkEntity::class,
        UnlockedEpisodeEntity::class,
        UserAccountEntity::class,
        UserLikeEntity::class,
        LocalCommentEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reelShortDao(): ReelShortDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "reelshort_database"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed initial user account and sample unlocked episodes
                        scope.launch(Dispatchers.IO) {
                            val dao = getDatabase(context, scope).reelShortDao()
                            dao.saveUserAccount(
                                UserAccountEntity(
                                    id = 1,
                                    coins = 200, // 200 welcome bonus coins
                                    isVip = false,
                                    checkinStreak = 1,
                                    lastCheckinDateEpochDay = 0L,
                                    totalEpisodesWatched = 3
                                )
                            )
                            // Free pre-unlocked episodes for drama 1
                            dao.unlockEpisode(UnlockedEpisodeEntity("drama_billionaire_husband", 1))
                            dao.unlockEpisode(UnlockedEpisodeEntity("drama_billionaire_husband", 2))
                            dao.unlockEpisode(UnlockedEpisodeEntity("drama_billionaire_husband", 3))
                            dao.addBookmark(BookmarkEntity("drama_billionaire_husband"))
                            dao.saveWatchHistory(
                                WatchHistoryEntity(
                                    dramaId = "drama_billionaire_husband",
                                    episodeNumber = 2,
                                    progressSeconds = 45
                                )
                            )
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
