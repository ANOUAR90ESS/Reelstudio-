package com.example.data.admin

import com.example.data.local.DramaEntity
import com.example.data.local.EpisodeEntity
import com.example.data.model.Drama
import com.example.data.model.DramaGenre
import com.example.data.model.Episode
import com.example.data.model.ScriptLine

/**
 * Translation between the admin authoring tables and the domain models the player screens consume.
 *
 * Script lines are the only nested structure. They are flattened into one string per line so the
 * whole episode still fits a single row: `timestamp` + [FIELD_SEPARATOR] + `speaker` +
 * [FIELD_SEPARATOR] + `text`. The dialogue itself is free text and is allowed to contain anything,
 * so the split is limited to three parts and the separator is stripped from the speaker on write.
 */
object AdminMappers {

    const val FIELD_SEPARATOR = "\u001E"

    fun encodeScriptLine(line: ScriptLine): String =
        listOf(
            line.timestampSeconds.toString(),
            line.speaker.replace(FIELD_SEPARATOR, " "),
            line.text
        ).joinToString(FIELD_SEPARATOR)

    fun decodeScriptLine(raw: String): ScriptLine? {
        val parts = raw.split(FIELD_SEPARATOR, limit = 3)
        if (parts.size < 3) return null
        return ScriptLine(
            speaker = parts[1],
            text = parts[2],
            timestampSeconds = parts[0].toIntOrNull() ?: 0
        )
    }

    fun genreOf(name: String): DramaGenre =
        DramaGenre.entries.firstOrNull { it.name == name } ?: DramaGenre.ALL
}

fun EpisodeEntity.toEpisode(): Episode = Episode(
    id = id,
    dramaId = dramaId,
    episodeNumber = episodeNumber,
    title = title,
    durationSeconds = durationSeconds,
    isFree = isFree,
    coinCost = coinCost,
    previewSubtitle = previewSubtitle,
    videoUrl = videoUrl,
    thumbnailUrl = thumbnailUrl,
    voiceoverUrl = voiceoverUrl,
    scriptLines = scriptLines.mapNotNull { AdminMappers.decodeScriptLine(it) }
)

fun Episode.toEntity(createdAt: Long = System.currentTimeMillis()): EpisodeEntity = EpisodeEntity(
    id = id,
    dramaId = dramaId,
    episodeNumber = episodeNumber,
    title = title,
    durationSeconds = durationSeconds,
    isFree = isFree,
    coinCost = coinCost,
    previewSubtitle = previewSubtitle,
    videoUrl = videoUrl,
    thumbnailUrl = thumbnailUrl,
    voiceoverUrl = voiceoverUrl,
    scriptLines = scriptLines.map { AdminMappers.encodeScriptLine(it) },
    createdAt = createdAt,
    updatedAt = System.currentTimeMillis()
)

fun DramaEntity.toDrama(episodes: List<Episode> = emptyList()): Drama = Drama(
    id = id,
    title = title,
    description = description,
    coverGradientStart = coverGradientStart,
    coverGradientEnd = coverGradientEnd,
    badge = badge?.takeIf { it.isNotBlank() },
    genre = AdminMappers.genreOf(genre),
    rating = rating,
    viewCount = viewCount,
    likeCount = likeCount,
    // The declared count is a marketing figure ("50 episodes total"); never let it drop below
    // what actually exists or the detail screen would advertise fewer episodes than it lists.
    totalEpisodes = maxOf(totalEpisodes, episodes.size),
    releaseYear = releaseYear,
    cast = cast,
    director = director,
    tags = tags,
    episodes = episodes.sortedBy { it.episodeNumber },
    posterUrl = posterUrl,
    trailerUrl = trailerUrl,
    isCustom = true,
    isPublished = isPublished,
    createdBy = createdBy,
    updatedAt = updatedAt
)

fun Drama.toEntity(createdAt: Long = System.currentTimeMillis()): DramaEntity = DramaEntity(
    id = id,
    title = title,
    description = description,
    coverGradientStart = coverGradientStart,
    coverGradientEnd = coverGradientEnd,
    badge = badge,
    genre = genre.name,
    rating = rating,
    viewCount = viewCount,
    likeCount = likeCount,
    totalEpisodes = totalEpisodes,
    releaseYear = releaseYear,
    cast = cast,
    director = director,
    tags = tags,
    posterUrl = posterUrl,
    trailerUrl = trailerUrl,
    isPublished = isPublished,
    createdBy = createdBy,
    createdAt = createdAt,
    updatedAt = System.currentTimeMillis()
)
