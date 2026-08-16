package com.example.data.admin

import com.example.data.model.DramaGenre
import com.example.data.model.ScriptLine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Validation and conversion rules for the admin authoring forms. These run on the JVM: the forms
 * are deliberately plain Kotlin so the rules that decide what reaches the catalog are testable
 * without a device.
 */
class DramaFormStateTest {

    private fun validForm() = DramaFormState(
        title = "Married To My Billionaire Rival",
        description = "A contract marriage between two rival heirs turns into something neither planned.",
        genre = DramaGenre.BILLIONAIRE,
        rating = "4.7",
        releaseYear = "2025",
        totalEpisodes = "40"
    )

    @Test
    fun `a complete form validates`() {
        assertTrue(validForm().validate().isEmpty())
    }

    @Test
    fun `title is required and must be substantial`() {
        assertEquals("Title is required", validForm().copy(title = "  ").validate()[DramaField.TITLE])
        assertEquals(
            "Title must be at least 3 characters",
            validForm().copy(title = "Hi").validate()[DramaField.TITLE]
        )
        val longTitle = "x".repeat(DramaFormState.MAX_TITLE_LENGTH + 1)
        assertTrue(validForm().copy(title = longTitle).validate().containsKey(DramaField.TITLE))
    }

    @Test
    fun `synopsis must be long enough to be useful`() {
        assertTrue(validForm().copy(description = "Too short").validate().containsKey(DramaField.DESCRIPTION))
    }

    @Test
    fun `rating must parse and stay within the five point scale`() {
        assertTrue(validForm().copy(rating = "high").validate().containsKey(DramaField.RATING))
        assertTrue(validForm().copy(rating = "5.1").validate().containsKey(DramaField.RATING))
        assertTrue(validForm().copy(rating = "-1").validate().containsKey(DramaField.RATING))
        assertFalse(validForm().copy(rating = "5.0").validate().containsKey(DramaField.RATING))
    }

    @Test
    fun `release year must be a plausible number`() {
        assertTrue(validForm().copy(releaseYear = "nineteen").validate().containsKey(DramaField.RELEASE_YEAR))
        assertTrue(validForm().copy(releaseYear = "1900").validate().containsKey(DramaField.RELEASE_YEAR))
    }

    @Test
    fun `episode count cannot be negative`() {
        assertTrue(validForm().copy(totalEpisodes = "-3").validate().containsKey(DramaField.TOTAL_EPISODES))
    }

    @Test
    fun `converting to a domain film trims text and marks it as admin authored`() {
        val drama = validForm()
            .copy(
                title = "  Spaced Title  ",
                cast = listOf(" Elena ", "", "Marcus"),
                tags = listOf("Revenge ", " ")
            )
            .toDrama()

        assertEquals("Spaced Title", drama.title)
        assertEquals(listOf("Elena", "Marcus"), drama.cast)
        assertEquals(listOf("Revenge"), drama.tags)
        assertTrue(drama.isCustom)
    }

    @Test
    fun `new films default to unpublished so nothing reaches viewers by accident`() {
        assertFalse(DramaFormState().isPublished)
        assertFalse(validForm().toDrama().isPublished)
    }

    @Test
    fun `generated ids are slugged and unique per film`() {
        val first = DramaFormState.generateDramaId("The Double Life of My Billionaire Husband!")
        val second = DramaFormState.generateDramaId("The Double Life of My Billionaire Husband!")

        assertTrue(first.startsWith("admin_the_double_life_of_my_billionaire"))
        assertNotEquals(first, second)
    }

    @Test
    fun `editing an existing film round trips through the form`() {
        val original = validForm().copy(id = "admin_test_1").toDrama()
        val reloaded = DramaFormState.from(original)

        assertTrue(reloaded.editingExisting)
        assertEquals(original.title, reloaded.toDrama().title)
        assertEquals(original.id, reloaded.toDrama().id)
        assertEquals(original.genre, reloaded.genre)
    }
}

class EpisodeFormStateTest {

    private fun validForm() = EpisodeFormState(
        dramaId = "admin_film_1",
        episodeNumber = "4",
        title = "The Mask Comes Off",
        durationSeconds = "90",
        coinCost = "20"
    )

    @Test
    fun `a complete episode validates`() {
        assertTrue(validForm().isValid())
    }

    @Test
    fun `episode numbers already used by the film are rejected`() {
        val errors = validForm().validate(takenEpisodeNumbers = setOf(4))
        assertEquals("Episode 4 already exists in this film", errors[EpisodeField.NUMBER])
    }

    @Test
    fun `the episode being edited keeps its own number`() {
        assertTrue(validForm().validate(takenEpisodeNumbers = setOf(1, 2, 3)).isEmpty())
    }

    @Test
    fun `duration is bounded to short form runtimes`() {
        assertTrue(validForm().copy(durationSeconds = "2").validate().containsKey(EpisodeField.DURATION))
        assertTrue(validForm().copy(durationSeconds = "5000").validate().containsKey(EpisodeField.DURATION))
        assertTrue(validForm().copy(durationSeconds = "abc").validate().containsKey(EpisodeField.DURATION))
    }

    @Test
    fun `a paid episode cannot cost nothing`() {
        val errors = validForm().copy(isFree = false, coinCost = "0").validate()
        assertEquals("A paid episode needs a cost above 0, or mark it free", errors[EpisodeField.COIN_COST])
    }

    @Test
    fun `a free episode never carries a price`() {
        val episode = validForm().copy(isFree = true, coinCost = "50").toEpisode()

        assertTrue(episode.isFree)
        assertEquals(0, episode.coinCost)
    }

    @Test
    fun `blank script lines are dropped on conversion`() {
        val episode = validForm().copy(
            scriptLines = listOf(
                ScriptLine("Natalie", "Who are you really?", 5),
                ScriptLine("", "orphan line", 10),
                ScriptLine("Sebastian", "", 12)
            )
        ).toEpisode()

        assertEquals(1, episode.scriptLines.size)
        assertEquals("Natalie", episode.scriptLines.first().speaker)
    }

    @Test
    fun `the next episode is prefilled and the first three are free`() {
        val drama = DramaFormState(
            title = "Test Film",
            description = "A description that is definitely long enough to pass validation."
        ).toDrama()

        val first = EpisodeFormState.nextFor(drama)
        assertEquals("1", first.episodeNumber)
        assertTrue(first.isFree)

        val withThree = drama.copy(
            episodes = (1..3).map { EpisodeFormState(dramaId = drama.id, episodeNumber = "$it", title = "E$it").toEpisode() }
        )
        val fourth = EpisodeFormState.nextFor(withThree)
        assertEquals("4", fourth.episodeNumber)
        assertFalse(fourth.isFree)
    }
}

class ScriptLineEncodingTest {

    @Test
    fun `script lines survive an encode decode round trip`() {
        val line = ScriptLine("Sebastian", "In my world, contracts are rewritten.", 48)
        val decoded = AdminMappers.decodeScriptLine(AdminMappers.encodeScriptLine(line))

        assertEquals(line, decoded)
    }

    @Test
    fun `dialogue containing separators and newlines is preserved`() {
        val line = ScriptLine(
            speaker = "Narrator",
            text = "First part\nSecond part | with a pipe, a comma; and a colon:",
            timestampSeconds = 7
        )

        assertEquals(line, AdminMappers.decodeScriptLine(AdminMappers.encodeScriptLine(line)))
    }

    @Test
    fun `a malformed row decodes to null rather than throwing`() {
        assertNull(AdminMappers.decodeScriptLine("not-an-encoded-line"))
        assertNull(AdminMappers.decodeScriptLine(""))
    }

    @Test
    fun `unknown genre names fall back to ALL`() {
        assertEquals(DramaGenre.BILLIONAIRE, AdminMappers.genreOf("BILLIONAIRE"))
        assertEquals(DramaGenre.ALL, AdminMappers.genreOf("NOT_A_GENRE"))
    }
}
