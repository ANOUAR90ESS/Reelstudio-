package com.example.data.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Parsing of model output. A generative model is not a contract: it can wrap JSON in a fence, add a
 * preamble, renumber episodes, or emit a timestamp past the end of the episode. None of that may
 * reach the catalog, and none of it may crash the console.
 *
 * Runs under Robolectric because the parser uses `org.json`, which is an Android framework class.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class StoryGeneratorParsingTest {

    private val validStory = """
        {
          "title": "Married To My Billionaire Rival",
          "synopsis": "Two rival heirs are forced into a contract marriage.",
          "tags": ["Billionaire", "Revenge"],
          "cast": ["Elena Sterling", "Marcus Thorne"],
          "director": "David Lin",
          "episodes": [
            {"episodeNumber": 1, "title": "The Contract", "hook": "She signs without reading."},
            {"episodeNumber": 2, "title": "The Rival", "hook": "He walks in as her husband."}
          ]
        }
    """.trimIndent()

    @Test
    fun `a clean json response parses`() {
        val story = StoryGenerator.parseStory(StoryGenerator.extractJsonObject(validStory), 10)

        assertEquals("Married To My Billionaire Rival", story.title)
        assertEquals(listOf("Billionaire", "Revenge"), story.tags)
        assertEquals(2, story.episodes.size)
        assertEquals("The Contract", story.episodes.first().title)
    }

    @Test
    fun `a fenced code block is unwrapped`() {
        val fenced = "```json\n$validStory\n```"
        assertEquals(
            "Married To My Billionaire Rival",
            StoryGenerator.parseStory(StoryGenerator.extractJsonObject(fenced), 10).title
        )
    }

    @Test
    fun `a chatty preamble is ignored`() {
        val chatty = "Sure! Here is your series:\n$validStory\nHope you like it."
        assertEquals(2, StoryGenerator.parseStory(StoryGenerator.extractJsonObject(chatty), 10).episodes.size)
    }

    @Test
    fun `a response with no json fails loudly`() {
        assertThrows(IllegalStateException::class.java) {
            StoryGenerator.extractJsonObject("I'm afraid I can't help with that.")
        }
    }

    @Test
    fun `a story missing its title is rejected rather than half applied`() {
        val json = StoryGenerator.extractJsonObject("""{"synopsis": "Something happens."}""")
        assertThrows(IllegalStateException::class.java) { StoryGenerator.parseStory(json, 5) }
    }

    @Test
    fun `episode numbers are taken from position so duplicates cannot collide`() {
        val duplicated = """
            {
              "title": "T", "synopsis": "S",
              "episodes": [
                {"episodeNumber": 1, "title": "A"},
                {"episodeNumber": 1, "title": "B"},
                {"episodeNumber": 1, "title": "C"}
              ]
            }
        """.trimIndent()

        val story = StoryGenerator.parseStory(StoryGenerator.extractJsonObject(duplicated), 10)
        assertEquals(listOf(1, 2, 3), story.episodes.map { it.episodeNumber })
    }

    @Test
    fun `more episodes than requested are trimmed`() {
        val many = buildString {
            append("""{"title":"T","synopsis":"S","episodes":[""")
            append((1..20).joinToString(",") { """{"episodeNumber":$it,"title":"E$it"}""" })
            append("]}")
        }

        assertEquals(5, StoryGenerator.parseStory(StoryGenerator.extractJsonObject(many), 5).episodes.size)
    }

    @Test
    fun `episodes without a title are dropped`() {
        val partial = """
            {"title":"T","synopsis":"S","episodes":[{"title":"Good"},{"hook":"no title"}]}
        """.trimIndent()

        assertEquals(1, StoryGenerator.parseStory(StoryGenerator.extractJsonObject(partial), 10).episodes.size)
    }

    @Test
    fun `script lines are sorted and clamped into the runtime`() {
        val json = StoryGenerator.extractJsonObject(
            """
            {
              "previewSubtitle": "It begins.",
              "lines": [
                {"speaker": "A", "text": "third", "timestampSeconds": 400},
                {"speaker": "B", "text": "first", "timestampSeconds": 5},
                {"speaker": "C", "text": "second", "timestampSeconds": 20}
              ]
            }
            """.trimIndent()
        )

        val script = StoryGenerator.parseScript(json, durationSeconds = 90)

        assertEquals(listOf("first", "second", "third"), script.lines.map { it.text })
        assertTrue(script.lines.all { it.timestampSeconds <= 90 })
        assertEquals("It begins.", script.previewSubtitle)
    }

    @Test
    fun `script lines missing a speaker or text are dropped`() {
        val json = StoryGenerator.extractJsonObject(
            """
            {"lines":[
              {"speaker":"A","text":"kept","timestampSeconds":1},
              {"speaker":"","text":"no speaker","timestampSeconds":2},
              {"speaker":"C","text":"","timestampSeconds":3}
            ]}
            """.trimIndent()
        )

        val script = StoryGenerator.parseScript(json, durationSeconds = 90)
        assertEquals(1, script.lines.size)
        assertEquals("kept", script.lines.first().text)
    }

    @Test
    fun `a script with no usable lines fails rather than saving an empty episode`() {
        val json = StoryGenerator.extractJsonObject("""{"previewSubtitle":"x","lines":[]}""")
        assertThrows(IllegalStateException::class.java) { StoryGenerator.parseScript(json, 90) }
    }
}
