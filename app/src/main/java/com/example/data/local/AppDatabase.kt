package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
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
        LocalCommentEntity::class,
        DramaEntity::class,
        EpisodeEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reelShortDao(): ReelShortDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * v1 -> v2 adds the admin authoring tables. It is additive on purpose: existing viewers
         * keep their coins, bookmarks, history and unlocked episodes when they update.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `admin_dramas` (
                        `id` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `coverGradientStart` INTEGER NOT NULL,
                        `coverGradientEnd` INTEGER NOT NULL,
                        `badge` TEXT,
                        `genre` TEXT NOT NULL,
                        `rating` REAL NOT NULL,
                        `viewCount` TEXT NOT NULL,
                        `likeCount` TEXT NOT NULL,
                        `totalEpisodes` INTEGER NOT NULL,
                        `releaseYear` INTEGER NOT NULL,
                        `cast` TEXT NOT NULL,
                        `director` TEXT NOT NULL,
                        `tags` TEXT NOT NULL,
                        `isPublished` INTEGER NOT NULL,
                        `createdBy` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `admin_episodes` (
                        `id` TEXT NOT NULL,
                        `dramaId` TEXT NOT NULL,
                        `episodeNumber` INTEGER NOT NULL,
                        `title` TEXT NOT NULL,
                        `durationSeconds` INTEGER NOT NULL,
                        `isFree` INTEGER NOT NULL,
                        `coinCost` INTEGER NOT NULL,
                        `previewSubtitle` TEXT NOT NULL,
                        `scriptLines` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_admin_episodes_dramaId` ON `admin_episodes` (`dramaId`)"
                )
            }
        }

        /**
         * v2 -> v3 attaches media to the catalog: poster/trailer on a film, video, thumbnail and
         * voiceover on an episode. Additive again — drafts already authored keep their text and
         * simply start out with no media.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                listOf(
                    "ALTER TABLE `admin_dramas` ADD COLUMN `posterUrl` TEXT NOT NULL DEFAULT ''",
                    "ALTER TABLE `admin_dramas` ADD COLUMN `trailerUrl` TEXT NOT NULL DEFAULT ''",
                    "ALTER TABLE `admin_episodes` ADD COLUMN `videoUrl` TEXT NOT NULL DEFAULT ''",
                    "ALTER TABLE `admin_episodes` ADD COLUMN `thumbnailUrl` TEXT NOT NULL DEFAULT ''",
                    "ALTER TABLE `admin_episodes` ADD COLUMN `voiceoverUrl` TEXT NOT NULL DEFAULT ''"
                ).forEach(db::execSQL)
            }
        }

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
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
