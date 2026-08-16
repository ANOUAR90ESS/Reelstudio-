package com.example.data.admin

import com.example.data.firebase.AdminFirestore
import com.example.data.local.ReelShortDao
import com.example.data.model.Drama
import com.example.data.model.Episode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Write side of the catalog: everything the admin console does to films and episodes goes through
 * here.
 *
 * Room is the source of truth so the console keeps working with no network and with Firebase
 * unconfigured. Firestore is a best-effort mirror — a failed push is reported back to the caller
 * but never rolls back the local write, otherwise a flaky connection would eat an admin's work.
 */
class AdminContentRepository(
    private val dao: ReelShortDao,
    private val remote: AdminFirestore = AdminFirestore
) {

    /** Every admin-authored film, drafts included. The console shows these; viewers do not. */
    val adminDramas: Flow<List<Drama>> =
        combine(dao.getAllAdminDramas(), dao.getAllAdminEpisodes()) { dramaRows, episodeRows ->
            val episodesByDrama = episodeRows.groupBy { it.dramaId }
            dramaRows.map { row ->
                row.toDrama(
                    episodes = episodesByDrama[row.id]
                        .orEmpty()
                        .map { it.toEpisode() }
                        .sortedBy { episode -> episode.episodeNumber }
                )
            }
        }

    suspend fun getDrama(dramaId: String): Drama? {
        val row = dao.getAdminDrama(dramaId) ?: return null
        return row.toDrama(dao.getAdminEpisodes(dramaId).map { it.toEpisode() })
    }

    /**
     * Creates or updates a film. Returns the stored film so callers can keep editing the exact row
     * that landed in the database (in particular the generated id for a brand new film).
     */
    suspend fun saveDrama(drama: Drama): Result<Drama> = runCatching {
        val existing = dao.getAdminDrama(drama.id)
        val entity = drama.toEntity(createdAt = existing?.createdAt ?: System.currentTimeMillis())
        dao.saveAdminDrama(entity)
        dao.refreshAdminDramaEpisodeCount(drama.id, System.currentTimeMillis())
        val stored = getDrama(drama.id) ?: drama
        if (stored.isPublished) {
            remote.publishDrama(stored)
        }
        stored
    }

    suspend fun deleteDrama(dramaId: String): Result<Unit> = runCatching {
        dao.deleteAdminDramaCascade(dramaId)
        remote.deleteDrama(dramaId)
        Unit
    }

    /**
     * Flips a film between draft and live. Publishing mirrors it (with every episode) to Firestore;
     * unpublishing removes the remote copy so other devices stop seeing it.
     */
    suspend fun setPublished(dramaId: String, published: Boolean): Result<Drama?> = runCatching {
        dao.setAdminDramaPublished(dramaId, published, System.currentTimeMillis())
        val stored = getDrama(dramaId)
        if (stored != null) {
            if (published) remote.publishDrama(stored) else remote.deleteDrama(dramaId)
        }
        stored
    }

    suspend fun saveEpisode(episode: Episode): Result<Episode> = runCatching {
        val existing = dao.getAdminEpisodes(episode.dramaId).firstOrNull { it.id == episode.id }
        dao.saveAdminEpisode(episode.toEntity(createdAt = existing?.createdAt ?: System.currentTimeMillis()))
        dao.refreshAdminDramaEpisodeCount(episode.dramaId, System.currentTimeMillis())
        syncPublishedDrama(episode.dramaId)
        episode
    }

    suspend fun deleteEpisode(dramaId: String, episodeId: String): Result<Unit> = runCatching {
        dao.deleteAdminEpisode(episodeId)
        dao.refreshAdminDramaEpisodeCount(dramaId, System.currentTimeMillis())
        syncPublishedDrama(dramaId)
        Unit
    }

    /** Pushes the film again if it is live, so a mid-flight episode edit reaches viewers. */
    private suspend fun syncPublishedDrama(dramaId: String) {
        val stored = getDrama(dramaId) ?: return
        if (stored.isPublished) {
            remote.publishDrama(stored)
        }
    }

    fun stats(dramas: List<Drama>): AdminStats = AdminStats.of(dramas)
}

/** Headline numbers shown at the top of the admin dashboard. */
data class AdminStats(
    val totalFilms: Int = 0,
    val publishedFilms: Int = 0,
    val draftFilms: Int = 0,
    val totalEpisodes: Int = 0,
    val freeEpisodes: Int = 0,
    val lockedEpisodes: Int = 0
) {
    companion object {
        fun of(dramas: List<Drama>): AdminStats {
            val episodes = dramas.flatMap { it.episodes }
            return AdminStats(
                totalFilms = dramas.size,
                publishedFilms = dramas.count { it.isPublished },
                draftFilms = dramas.count { !it.isPublished },
                totalEpisodes = episodes.size,
                freeEpisodes = episodes.count { it.isFree },
                lockedEpisodes = episodes.count { !it.isFree }
            )
        }
    }
}
