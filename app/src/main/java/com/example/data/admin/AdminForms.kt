package com.example.data.admin

import com.example.data.model.Drama
import com.example.data.model.DramaGenre
import com.example.data.model.Episode
import com.example.data.model.ScriptLine
import java.util.Locale
import java.util.UUID

/**
 * Editable state for the "create / edit film" form, kept as plain Kotlin so it can be validated and
 * unit-tested without an Android or Compose runtime.
 *
 * Numeric fields are held as strings because that is what a text field produces: a half-typed "4."
 * has to survive a recomposition, and rejecting it belongs in [validate], not in the keystroke path.
 */
data class DramaFormState(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val genre: DramaGenre = DramaGenre.BILLIONAIRE,
    val badge: String? = null,
    val rating: String = "4.8",
    val viewCount: String = "0",
    val likeCount: String = "0",
    val totalEpisodes: String = "0",
    val releaseYear: String = "2024",
    val director: String = "",
    val cast: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val coverGradientStart: Long = AdminPalette.presets.first().start,
    val coverGradientEnd: Long = AdminPalette.presets.first().end,
    val isPublished: Boolean = false,
    val createdBy: String = "",
    /** Non-null while editing an existing film; null when composing a new one. */
    val editingExisting: Boolean = false
) {

    fun validate(): Map<DramaField, String> {
        val errors = mutableMapOf<DramaField, String>()

        if (title.isBlank()) {
            errors[DramaField.TITLE] = "Title is required"
        } else if (title.trim().length < 3) {
            errors[DramaField.TITLE] = "Title must be at least 3 characters"
        } else if (title.trim().length > MAX_TITLE_LENGTH) {
            errors[DramaField.TITLE] = "Title must be under $MAX_TITLE_LENGTH characters"
        }

        if (description.isBlank()) {
            errors[DramaField.DESCRIPTION] = "Synopsis is required"
        } else if (description.trim().length < 20) {
            errors[DramaField.DESCRIPTION] = "Write at least 20 characters so viewers know the plot"
        }

        val ratingValue = rating.trim().toFloatOrNull()
        if (ratingValue == null) {
            errors[DramaField.RATING] = "Rating must be a number"
        } else if (ratingValue < 0f || ratingValue > 5f) {
            errors[DramaField.RATING] = "Rating must be between 0.0 and 5.0"
        }

        val yearValue = releaseYear.trim().toIntOrNull()
        if (yearValue == null) {
            errors[DramaField.RELEASE_YEAR] = "Release year must be a number"
        } else if (yearValue < 1950 || yearValue > MAX_RELEASE_YEAR) {
            errors[DramaField.RELEASE_YEAR] = "Release year must be between 1950 and $MAX_RELEASE_YEAR"
        }

        val totalValue = totalEpisodes.trim().toIntOrNull()
        if (totalValue == null) {
            errors[DramaField.TOTAL_EPISODES] = "Episode count must be a number"
        } else if (totalValue < 0) {
            errors[DramaField.TOTAL_EPISODES] = "Episode count cannot be negative"
        }

        return errors
    }

    val isValid: Boolean get() = validate().isEmpty()

    /**
     * Builds the domain model. Call only when [isValid]; invalid numbers fall back to their
     * defaults rather than throwing, so a caller that skips validation still gets a usable film.
     */
    fun toDrama(): Drama = Drama(
        id = id.ifBlank { generateDramaId(title) },
        title = title.trim(),
        description = description.trim(),
        coverGradientStart = coverGradientStart,
        coverGradientEnd = coverGradientEnd,
        badge = badge?.takeIf { it.isNotBlank() },
        genre = genre,
        rating = rating.trim().toFloatOrNull()?.coerceIn(0f, 5f) ?: 4.8f,
        viewCount = viewCount.trim().ifBlank { "0" },
        likeCount = likeCount.trim().ifBlank { "0" },
        totalEpisodes = totalEpisodes.trim().toIntOrNull()?.coerceAtLeast(0) ?: 0,
        releaseYear = releaseYear.trim().toIntOrNull() ?: 2024,
        cast = cast.map { it.trim() }.filter { it.isNotEmpty() },
        director = director.trim(),
        tags = tags.map { it.trim() }.filter { it.isNotEmpty() },
        isCustom = true,
        isPublished = isPublished,
        createdBy = createdBy
    )

    companion object {
        const val MAX_TITLE_LENGTH = 120
        const val MAX_RELEASE_YEAR = 2100

        fun from(drama: Drama): DramaFormState = DramaFormState(
            id = drama.id,
            title = drama.title,
            description = drama.description,
            genre = drama.genre,
            badge = drama.badge,
            rating = drama.rating.toString(),
            viewCount = drama.viewCount,
            likeCount = drama.likeCount,
            totalEpisodes = drama.totalEpisodes.toString(),
            releaseYear = drama.releaseYear.toString(),
            director = drama.director,
            cast = drama.cast,
            tags = drama.tags,
            coverGradientStart = drama.coverGradientStart,
            coverGradientEnd = drama.coverGradientEnd,
            isPublished = drama.isPublished,
            createdBy = drama.createdBy,
            editingExisting = true
        )

        /**
         * Slug + short random suffix. The slug keeps ids readable in the database and in Firestore;
         * the suffix keeps two films with the same title from colliding.
         */
        fun generateDramaId(title: String): String {
            val slug = title.trim().lowercase(Locale.US)
                .replace(Regex("[^a-z0-9]+"), "_")
                .trim('_')
                .take(40)
                .ifBlank { "film" }
            return "admin_${slug}_${UUID.randomUUID().toString().take(6)}"
        }
    }
}

enum class DramaField {
    TITLE, DESCRIPTION, RATING, RELEASE_YEAR, TOTAL_EPISODES
}

/**
 * Editable state for one episode ("story") of a film.
 */
data class EpisodeFormState(
    val id: String = "",
    val dramaId: String = "",
    val episodeNumber: String = "1",
    val title: String = "",
    val durationSeconds: String = "85",
    val isFree: Boolean = false,
    val coinCost: String = "20",
    val previewSubtitle: String = "",
    val scriptLines: List<ScriptLine> = emptyList(),
    val editingExisting: Boolean = false
) {

    /**
     * @param takenEpisodeNumbers numbers already used by *other* episodes of the same film, so two
     * stories cannot claim episode 3 and shadow each other in the player.
     */
    fun validate(takenEpisodeNumbers: Set<Int> = emptySet()): Map<EpisodeField, String> {
        val errors = mutableMapOf<EpisodeField, String>()

        if (title.isBlank()) {
            errors[EpisodeField.TITLE] = "Episode title is required"
        }

        val number = episodeNumber.trim().toIntOrNull()
        if (number == null) {
            errors[EpisodeField.NUMBER] = "Episode number must be a number"
        } else if (number < 1) {
            errors[EpisodeField.NUMBER] = "Episode number starts at 1"
        } else if (number in takenEpisodeNumbers) {
            errors[EpisodeField.NUMBER] = "Episode $number already exists in this film"
        }

        val duration = durationSeconds.trim().toIntOrNull()
        if (duration == null) {
            errors[EpisodeField.DURATION] = "Duration must be a number of seconds"
        } else if (duration < MIN_DURATION_SECONDS) {
            errors[EpisodeField.DURATION] = "Duration must be at least $MIN_DURATION_SECONDS seconds"
        } else if (duration > MAX_DURATION_SECONDS) {
            errors[EpisodeField.DURATION] = "Short-form episodes cap at $MAX_DURATION_SECONDS seconds"
        }

        val cost = coinCost.trim().toIntOrNull()
        if (cost == null) {
            errors[EpisodeField.COIN_COST] = "Coin cost must be a number"
        } else if (cost < 0) {
            errors[EpisodeField.COIN_COST] = "Coin cost cannot be negative"
        } else if (!isFree && cost == 0) {
            errors[EpisodeField.COIN_COST] = "A paid episode needs a cost above 0, or mark it free"
        }

        return errors
    }

    fun isValid(takenEpisodeNumbers: Set<Int> = emptySet()): Boolean =
        validate(takenEpisodeNumbers).isEmpty()

    fun toEpisode(): Episode {
        val number = episodeNumber.trim().toIntOrNull()?.coerceAtLeast(1) ?: 1
        return Episode(
            id = id.ifBlank { "${dramaId}_ep_${number}_${UUID.randomUUID().toString().take(4)}" },
            dramaId = dramaId,
            episodeNumber = number,
            title = title.trim(),
            durationSeconds = durationSeconds.trim().toIntOrNull()?.coerceAtLeast(1) ?: 85,
            isFree = isFree,
            // Free episodes must not carry a price, or the unlock dialog would charge for them.
            coinCost = if (isFree) 0 else coinCost.trim().toIntOrNull()?.coerceAtLeast(0) ?: 20,
            previewSubtitle = previewSubtitle.trim(),
            scriptLines = scriptLines.filter { it.speaker.isNotBlank() && it.text.isNotBlank() }
        )
    }

    companion object {
        const val MIN_DURATION_SECONDS = 5
        const val MAX_DURATION_SECONDS = 1800

        fun from(episode: Episode): EpisodeFormState = EpisodeFormState(
            id = episode.id,
            dramaId = episode.dramaId,
            episodeNumber = episode.episodeNumber.toString(),
            title = episode.title,
            durationSeconds = episode.durationSeconds.toString(),
            isFree = episode.isFree,
            coinCost = episode.coinCost.toString(),
            previewSubtitle = episode.previewSubtitle,
            scriptLines = episode.scriptLines,
            editingExisting = true
        )

        /** Pre-fills a new episode as the next number in the film, keeping the first three free. */
        fun nextFor(drama: Drama): EpisodeFormState {
            val nextNumber = (drama.episodes.maxOfOrNull { it.episodeNumber } ?: 0) + 1
            return EpisodeFormState(
                dramaId = drama.id,
                episodeNumber = nextNumber.toString(),
                isFree = nextNumber <= 3,
                coinCost = if (nextNumber <= 3) "0" else "20"
            )
        }
    }
}

enum class EpisodeField {
    TITLE, NUMBER, DURATION, COIN_COST
}

/** Cover gradient presets offered in the film editor. */
object AdminPalette {

    data class Preset(val name: String, val start: Long, val end: Long)

    val presets: List<Preset> = listOf(
        Preset("Burgundy", 0xFF8A001A, 0xFF14070C),
        Preset("Royal Violet", 0xFF280B45, 0xFF0D061A),
        Preset("Midnight Blue", 0xFF0B2E5C, 0xFF05101F),
        Preset("Emerald", 0xFF06412F, 0xFF04120D),
        Preset("Sunset Gold", 0xFF8A5200, 0xFF1A0F00),
        Preset("Crimson", 0xFFB3000F, 0xFF1F0407),
        Preset("Steel Noir", 0xFF2B2F44, 0xFF090A10),
        Preset("Rose Dusk", 0xFF7A1146, 0xFF1A0611)
    )

    val badges: List<String?> = listOf(null, "HOT", "NEW", "TOP 1", "VIP", "TRENDING")
}
