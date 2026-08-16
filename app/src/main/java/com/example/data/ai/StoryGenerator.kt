package com.example.data.ai

import android.util.Log
import com.example.data.model.DramaGenre
import com.example.data.model.ScriptLine
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.generationConfig
import org.json.JSONArray
import org.json.JSONObject

/**
 * Story writing with Gemini, through Firebase AI Logic.
 *
 * This is the one AI provider the app can call directly: `firebase-ai` proxies the request through
 * Firebase, so there is no API key in the APK to extract. Video and voice providers cannot work
 * this way — see `docs/AI_PRODUCTION.md`.
 *
 * The model is asked for JSON and the response is parsed defensively. A model that returns prose,
 * a fenced code block, or a field of the wrong type produces a clear failure rather than a crash or
 * a half-filled form.
 */
object StoryGenerator {

    private const val TAG = "StoryGenerator"
    private const val MODEL_NAME = "gemini-2.5-flash"

    private val model: GenerativeModel by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                temperature = 0.9f
                responseMimeType = "application/json"
            }
        )
    }

    /** A complete film concept: the metadata for the film plus an outline of its episodes. */
    data class GeneratedStory(
        val title: String,
        val synopsis: String,
        val tags: List<String>,
        val cast: List<String>,
        val director: String,
        val episodes: List<GeneratedEpisode>
    )

    data class GeneratedEpisode(
        val episodeNumber: Int,
        val title: String,
        val hook: String
    )

    /** The dialogue for one episode. */
    data class GeneratedScript(
        val previewSubtitle: String,
        val lines: List<ScriptLine>
    )

    /**
     * Turns a one-line idea into a full film concept.
     *
     * @param idea what the admin typed, e.g. "a chef who discovers her rival is her arranged husband"
     * @param episodeCount how many episode outlines to produce
     */
    suspend fun generateStory(
        idea: String,
        genre: DramaGenre,
        episodeCount: Int
    ): Result<GeneratedStory> = runCatching {
        val safeCount = episodeCount.coerceIn(1, 30)
        val prompt = """
            You are a head writer for a vertical short-form drama app. Episodes are 60-120 seconds
            long and every one must end on a cliffhanger that forces the next tap.

            Idea: $idea
            Genre: ${genre.displayName}
            Episodes to outline: $safeCount

            Reply with JSON only, in exactly this shape:
            {
              "title": "punchy series title, under 60 characters",
              "synopsis": "2-4 sentences that sell the series to a viewer",
              "tags": ["4-6 short theme tags"],
              "cast": ["3-5 character names"],
              "director": "a plausible director name",
              "episodes": [
                {"episodeNumber": 1, "title": "episode title", "hook": "one sentence cliffhanger"}
              ]
            }
        """.trimIndent()

        val raw = model.generateContent(prompt).text
            ?: throw IllegalStateException("The model returned an empty response")

        parseStory(extractJsonObject(raw), safeCount)
    }.onFailure { e ->
        Log.e(TAG, "Story generation failed: ${e.message}", e)
    }

    /**
     * Writes the dialogue for a single episode, timed across its runtime.
     *
     * @param durationSeconds used to spread the timestamps, so the subtitles land across the whole
     * episode instead of bunching at the start.
     */
    suspend fun generateScript(
        seriesTitle: String,
        synopsis: String,
        episodeTitle: String,
        episodeNumber: Int,
        durationSeconds: Int,
        cast: List<String>
    ): Result<GeneratedScript> = runCatching {
        val safeDuration = durationSeconds.coerceIn(15, 600)
        val prompt = """
            Write the dialogue for one episode of a vertical short-form drama.

            Series: $seriesTitle
            Synopsis: $synopsis
            Episode $episodeNumber: $episodeTitle
            Runtime: $safeDuration seconds
            Characters: ${cast.joinToString(", ").ifBlank { "invent 2-3 characters" }}

            Write 5-9 lines. Timestamps must be between 0 and ${safeDuration - 5}, strictly
            increasing, and spread across the runtime. The last line must be a cliffhanger.

            Reply with JSON only, in exactly this shape:
            {
              "previewSubtitle": "one teaser line shown over the player",
              "lines": [
                {"speaker": "NAME", "text": "the spoken line", "timestampSeconds": 0}
              ]
            }
        """.trimIndent()

        val raw = model.generateContent(prompt).text
            ?: throw IllegalStateException("The model returned an empty response")

        parseScript(extractJsonObject(raw), safeDuration)
    }.onFailure { e ->
        Log.e(TAG, "Script generation failed: ${e.message}", e)
    }

    // ------------------------------------------------------------------
    // Parsing
    // ------------------------------------------------------------------

    /**
     * Pulls the JSON object out of a response. `responseMimeType` normally guarantees bare JSON,
     * but models still occasionally wrap it in a ```json fence or add a sentence of preamble, and
     * losing a whole generation to that would be needlessly annoying.
     */
    internal fun extractJsonObject(raw: String): JSONObject {
        val trimmed = raw.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        val start = trimmed.indexOf('{')
        val end = trimmed.lastIndexOf('}')
        if (start == -1 || end <= start) {
            throw IllegalStateException("The model did not return JSON")
        }
        return JSONObject(trimmed.substring(start, end + 1))
    }

    internal fun parseStory(json: JSONObject, expectedEpisodes: Int): GeneratedStory {
        val title = json.optString("title").trim()
        val synopsis = json.optString("synopsis").trim()
        if (title.isEmpty() || synopsis.isEmpty()) {
            throw IllegalStateException("The generated story is missing a title or synopsis")
        }

        val episodes = json.optJSONArray("episodes").toObjectList()
            .mapIndexedNotNull { index, item ->
                val epTitle = item.optString("title").trim()
                if (epTitle.isEmpty()) return@mapIndexedNotNull null
                GeneratedEpisode(
                    // Trust the position over the model's numbering: a repeated or skipped number
                    // would collide with an existing episode on save.
                    episodeNumber = index + 1,
                    title = epTitle,
                    hook = item.optString("hook").trim()
                )
            }
            .take(expectedEpisodes)

        return GeneratedStory(
            title = title,
            synopsis = synopsis,
            tags = json.optJSONArray("tags").toStringList(),
            cast = json.optJSONArray("cast").toStringList(),
            director = json.optString("director").trim(),
            episodes = episodes
        )
    }

    internal fun parseScript(json: JSONObject, durationSeconds: Int): GeneratedScript {
        val lines = json.optJSONArray("lines").toObjectList()
            .mapNotNull { item ->
                val speaker = item.optString("speaker").trim()
                val text = item.optString("text").trim()
                if (speaker.isEmpty() || text.isEmpty()) return@mapNotNull null
                ScriptLine(
                    speaker = speaker,
                    text = text,
                    // A timestamp past the end would never be shown, so clamp into the runtime.
                    timestampSeconds = item.optInt("timestampSeconds", 0).coerceIn(0, durationSeconds)
                )
            }
            .sortedBy { it.timestampSeconds }

        if (lines.isEmpty()) {
            throw IllegalStateException("The generated script had no usable lines")
        }

        return GeneratedScript(
            previewSubtitle = json.optString("previewSubtitle").trim(),
            lines = lines
        )
    }

    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        return (0 until length()).mapNotNull { optString(it).trim().takeIf { s -> s.isNotEmpty() } }
    }

    private fun JSONArray?.toObjectList(): List<JSONObject> {
        if (this == null) return emptyList()
        return (0 until length()).mapNotNull { optJSONObject(it) }
    }
}
