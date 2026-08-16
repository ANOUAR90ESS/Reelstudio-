package com.example.data.firebase

import android.util.Log
import com.example.data.admin.AdminConfig
import com.example.data.model.Drama
import com.example.data.model.DramaGenre
import com.example.data.model.Episode
import com.example.data.model.ScriptLine
import kotlinx.coroutines.tasks.await

/**
 * Firestore mirror of the admin-authored catalog.
 *
 * Every call is best-effort: Firebase is optional in this project (no `google-services.json` is
 * checked in), so a device with no backend still runs the console against Room alone. Failures are
 * returned as a failed [Result] and logged, never thrown at the UI.
 *
 * Write access is enforced by `firestore.rules` — this object being reachable from the client is
 * not what makes an admin an admin.
 */
object AdminFirestore {

    private const val TAG = "AdminFirestore"
    private const val COLLECTION_DRAMAS = "dramas"
    private const val COLLECTION_EPISODES = "episodes"
    private const val COLLECTION_USERS = "users"

    private val firestore get() = FirebaseHelper.firestore

    /** Writes the film document and replaces its episode subcollection. */
    suspend fun publishDrama(drama: Drama): Result<Unit> = runCatching {
        val dramaRef = firestore.collection(COLLECTION_DRAMAS).document(drama.id)
        dramaRef.set(drama.toFirestoreMap()).await()

        val episodesRef = dramaRef.collection(COLLECTION_EPISODES)
        // Drop episodes that were deleted locally, otherwise removed stories linger for viewers.
        val existing = episodesRef.get().await()
        val keepIds = drama.episodes.map { it.id }.toSet()
        existing.documents
            .filter { it.id !in keepIds }
            .forEach { it.reference.delete().await() }

        drama.episodes.forEach { episode ->
            episodesRef.document(episode.id).set(episode.toFirestoreMap()).await()
        }
        Unit
    }.onFailure { e ->
        Log.e(TAG, "Failed to publish drama ${drama.id}: ${e.message}", e)
    }

    suspend fun deleteDrama(dramaId: String): Result<Unit> = runCatching {
        val dramaRef = firestore.collection(COLLECTION_DRAMAS).document(dramaId)
        dramaRef.collection(COLLECTION_EPISODES).get().await().documents.forEach {
            it.reference.delete().await()
        }
        dramaRef.delete().await()
        Unit
    }.onFailure { e ->
        Log.e(TAG, "Failed to delete drama $dramaId: ${e.message}", e)
    }

    /** Reads back the published catalog, e.g. for a second admin device or a fresh install. */
    suspend fun fetchPublishedDramas(): Result<List<Drama>> = runCatching {
        val snapshot = firestore.collection(COLLECTION_DRAMAS)
            .whereEqualTo("isPublished", true)
            .get()
            .await()

        snapshot.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null
            val episodes = firestore.collection(COLLECTION_DRAMAS)
                .document(doc.id)
                .collection(COLLECTION_EPISODES)
                .get()
                .await()
                .documents
                .mapNotNull { epDoc -> epDoc.data?.let { episodeFromMap(doc.id, it) } }
                .sortedBy { it.episodeNumber }
            dramaFromMap(doc.id, data, episodes)
        }
    }.onFailure { e ->
        Log.e(TAG, "Failed to fetch published dramas: ${e.message}", e)
    }

    /**
     * Promotes an account to admin. Only ever called for the bootstrap owners listed in
     * [AdminConfig] — every other promotion has to be done by an existing admin, and the rules file
     * rejects a client that tries to set its own role otherwise.
     */
    suspend fun ensureAdminRole(userId: String, email: String): Result<Unit> = runCatching {
        if (!AdminConfig.isBootstrapAdmin(email)) {
            return@runCatching Unit
        }
        firestore.collection(COLLECTION_USERS)
            .document(userId)
            .update("role", AdminConfig.ROLE_ADMIN, "updatedAt", System.currentTimeMillis())
            .await()
        Unit
    }.onFailure { e ->
        Log.w(TAG, "Could not upgrade $userId to admin: ${e.message}")
    }

    // ------------------------------------------------------------------
    // Mapping
    // ------------------------------------------------------------------

    private fun Drama.toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "title" to title,
        "description" to description,
        "coverGradientStart" to coverGradientStart,
        "coverGradientEnd" to coverGradientEnd,
        "badge" to badge,
        "genre" to genre.name,
        "rating" to rating.toDouble(),
        "viewCount" to viewCount,
        "likeCount" to likeCount,
        "totalEpisodes" to totalEpisodes,
        "releaseYear" to releaseYear,
        "cast" to cast,
        "director" to director,
        "tags" to tags,
        "posterUrl" to posterUrl,
        "trailerUrl" to trailerUrl,
        "isPublished" to isPublished,
        "createdBy" to createdBy,
        "updatedAt" to System.currentTimeMillis()
    )

    private fun Episode.toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "dramaId" to dramaId,
        "episodeNumber" to episodeNumber,
        "title" to title,
        "durationSeconds" to durationSeconds,
        "isFree" to isFree,
        "coinCost" to coinCost,
        "previewSubtitle" to previewSubtitle,
        "videoUrl" to videoUrl,
        "thumbnailUrl" to thumbnailUrl,
        "voiceoverUrl" to voiceoverUrl,
        "scriptLines" to scriptLines.map {
            mapOf(
                "speaker" to it.speaker,
                "text" to it.text,
                "timestampSeconds" to it.timestampSeconds
            )
        },
        "updatedAt" to System.currentTimeMillis()
    )

    private fun dramaFromMap(id: String, map: Map<String, Any?>, episodes: List<Episode>): Drama =
        Drama(
            id = map["id"] as? String ?: id,
            title = map["title"] as? String ?: "Untitled",
            description = map["description"] as? String ?: "",
            coverGradientStart = (map["coverGradientStart"] as? Number)?.toLong() ?: 0xFF8A001A,
            coverGradientEnd = (map["coverGradientEnd"] as? Number)?.toLong() ?: 0xFF14070C,
            badge = (map["badge"] as? String)?.takeIf { it.isNotBlank() },
            genre = DramaGenre.entries.firstOrNull { it.name == map["genre"] } ?: DramaGenre.ALL,
            rating = (map["rating"] as? Number)?.toFloat() ?: 4.8f,
            viewCount = map["viewCount"] as? String ?: "0",
            likeCount = map["likeCount"] as? String ?: "0",
            totalEpisodes = (map["totalEpisodes"] as? Number)?.toInt() ?: episodes.size,
            releaseYear = (map["releaseYear"] as? Number)?.toInt() ?: 2024,
            @Suppress("UNCHECKED_CAST")
            cast = (map["cast"] as? List<String>).orEmpty(),
            director = map["director"] as? String ?: "",
            @Suppress("UNCHECKED_CAST")
            tags = (map["tags"] as? List<String>).orEmpty(),
            episodes = episodes,
            posterUrl = map["posterUrl"] as? String ?: "",
            trailerUrl = map["trailerUrl"] as? String ?: "",
            isCustom = true,
            isPublished = map["isPublished"] as? Boolean ?: false,
            createdBy = map["createdBy"] as? String ?: "",
            updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: 0L
        )

    private fun episodeFromMap(dramaId: String, map: Map<String, Any?>): Episode {
        @Suppress("UNCHECKED_CAST")
        val rawLines = (map["scriptLines"] as? List<Map<String, Any?>>).orEmpty()
        return Episode(
            id = map["id"] as? String ?: "",
            dramaId = map["dramaId"] as? String ?: dramaId,
            episodeNumber = (map["episodeNumber"] as? Number)?.toInt() ?: 1,
            title = map["title"] as? String ?: "Untitled episode",
            durationSeconds = (map["durationSeconds"] as? Number)?.toInt() ?: 85,
            isFree = map["isFree"] as? Boolean ?: false,
            coinCost = (map["coinCost"] as? Number)?.toInt() ?: 20,
            previewSubtitle = map["previewSubtitle"] as? String ?: "",
            videoUrl = map["videoUrl"] as? String ?: "",
            thumbnailUrl = map["thumbnailUrl"] as? String ?: "",
            voiceoverUrl = map["voiceoverUrl"] as? String ?: "",
            scriptLines = rawLines.map {
                ScriptLine(
                    speaker = it["speaker"] as? String ?: "",
                    text = it["text"] as? String ?: "",
                    timestampSeconds = (it["timestampSeconds"] as? Number)?.toInt() ?: 0
                )
            }
        )
    }
}
