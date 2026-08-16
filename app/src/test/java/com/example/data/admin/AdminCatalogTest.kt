package com.example.data.admin

import com.example.data.local.Converters
import com.example.data.local.DramaEntity
import com.example.data.model.Drama
import com.example.data.model.DramaGenre
import com.example.data.model.Episode
import com.example.data.model.SampleDramas
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The rules that decide what an admin sees versus what a viewer sees, plus the entity mapping the
 * catalog depends on.
 */
class AdminCatalogTest {

    private fun film(id: String, published: Boolean, episodes: Int = 0) = Drama(
        id = id,
        title = "Film $id",
        description = "Description",
        coverGradientStart = 0xFF8A001A,
        coverGradientEnd = 0xFF14070C,
        genre = DramaGenre.ROMANCE,
        isCustom = true,
        isPublished = published,
        episodes = (1..episodes).map {
            Episode(
                id = "${id}_ep_$it",
                dramaId = id,
                episodeNumber = it,
                title = "Episode $it",
                isFree = it <= 2,
                coinCost = if (it <= 2) 0 else 20
            )
        }
    )

    @Test
    fun `viewers only ever see published films alongside the samples`() {
        val authored = listOf(film("a", published = true), film("b", published = false))

        // Mirrors DramaRepository.dramas: published admin films first, then the bundled samples.
        val visible = authored.filter { it.isPublished } + SampleDramas.dramas

        assertTrue(visible.any { it.id == "a" })
        assertFalse(visible.any { it.id == "b" })
        assertTrue(visible.containsAll(SampleDramas.dramas))
    }

    @Test
    fun `stats count films and episodes across drafts and published`() {
        val stats = AdminStats.of(
            listOf(
                film("a", published = true, episodes = 5),
                film("b", published = false, episodes = 3)
            )
        )

        assertEquals(2, stats.totalFilms)
        assertEquals(1, stats.publishedFilms)
        assertEquals(1, stats.draftFilms)
        assertEquals(8, stats.totalEpisodes)
        assertEquals(4, stats.freeEpisodes)
        assertEquals(4, stats.lockedEpisodes)
    }

    @Test
    fun `stats of an empty catalog are all zero`() {
        assertEquals(AdminStats(), AdminStats.of(emptyList()))
    }

    @Test
    fun `a film round trips through its database entity`() {
        val original = film("round_trip", published = true, episodes = 2).copy(
            cast = listOf("Elena Sterling", "Marcus Thorne"),
            tags = listOf("Revenge", "Sweet Love"),
            director = "David Lin",
            badge = "HOT",
            rating = 4.6f
        )

        val entity: DramaEntity = original.toEntity()
        val restored = entity.toDrama(original.episodes)

        assertEquals(original.title, restored.title)
        assertEquals(original.genre, restored.genre)
        assertEquals(original.cast, restored.cast)
        assertEquals(original.tags, restored.tags)
        assertEquals(original.badge, restored.badge)
        assertEquals(original.rating, restored.rating, 0.001f)
        assertEquals(original.isPublished, restored.isPublished)
        assertEquals(2, restored.episodes.size)
    }

    @Test
    fun `episodes round trip with their script lines`() {
        val episode = EpisodeFormState(
            dramaId = "film_1",
            episodeNumber = "2",
            title = "The Mask Comes Off",
            durationSeconds = "95",
            coinCost = "30",
            scriptLines = listOf(
                com.example.data.model.ScriptLine("Natalie", "Who are you really?", 5),
                com.example.data.model.ScriptLine("Sebastian", "The man who owns this city.", 18)
            )
        ).toEpisode()

        val restored = episode.toEntity().toEpisode()

        assertEquals(episode.title, restored.title)
        assertEquals(episode.durationSeconds, restored.durationSeconds)
        assertEquals(episode.coinCost, restored.coinCost)
        assertEquals(episode.scriptLines, restored.scriptLines)
    }

    @Test
    fun `the announced episode count never understates what actually exists`() {
        val entity = film("counts", published = true).toEntity().copy(totalEpisodes = 2)
        val episodes = (1..5).map {
            Episode(id = "e$it", dramaId = "counts", episodeNumber = it, title = "E$it")
        }

        assertEquals(5, entity.toDrama(episodes).totalEpisodes)
    }

    @Test
    fun `episodes are always ordered by number`() {
        val entity = film("ordering", published = true).toEntity()
        val shuffled = listOf(3, 1, 2).map {
            Episode(id = "e$it", dramaId = "ordering", episodeNumber = it, title = "E$it")
        }

        assertEquals(listOf(1, 2, 3), entity.toDrama(shuffled).episodes.map { it.episodeNumber })
    }
}

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `string lists round trip`() {
        val values = listOf("Billionaire", "Secret Identity", "Revenge")
        assertEquals(values, converters.toStringList(converters.fromStringList(values)))
    }

    @Test
    fun `empty and null lists collapse to an empty list`() {
        assertEquals(emptyList<String>(), converters.toStringList(converters.fromStringList(emptyList())))
        assertEquals(emptyList<String>(), converters.toStringList(null))
        assertEquals("", converters.fromStringList(null))
    }

    @Test
    fun `values containing the separator cannot split a row`() {
        val sneaky = listOf("Tag${Converters.SEPARATOR}Injected", "Normal")
        assertEquals(2, converters.toStringList(converters.fromStringList(sneaky)).size)
    }

    @Test
    fun `commas and pipes in values are preserved`() {
        val values = listOf("Alexander Vance, Jr.", "Elena | Sterling")
        assertEquals(values, converters.toStringList(converters.fromStringList(values)))
    }
}

class AdminConfigTest {

    @Test
    fun `bootstrap owners are matched case insensitively`() {
        val owner = AdminConfig.bootstrapAdminEmails.first()

        assertTrue(AdminConfig.isBootstrapAdmin(owner))
        assertTrue(AdminConfig.isBootstrapAdmin(owner.uppercase()))
        assertTrue(AdminConfig.isBootstrapAdmin("  $owner  "))
    }

    @Test
    fun `everyone else is not an admin`() {
        assertFalse(AdminConfig.isBootstrapAdmin("someone.else@example.com"))
        assertFalse(AdminConfig.isBootstrapAdmin(""))
        assertFalse(AdminConfig.isBootstrapAdmin(null))
    }
}

class UserProfileRoleTest {

    @Test
    fun `the admin role grants console access`() {
        val admin = com.example.data.firebase.UserProfile(
            userId = "u1",
            email = "someone@example.com",
            role = AdminConfig.ROLE_ADMIN
        )
        assertTrue(admin.isAdmin)
    }

    @Test
    fun `an ordinary viewer does not`() {
        val viewer = com.example.data.firebase.UserProfile(userId = "u2", email = "viewer@example.com")
        assertFalse(viewer.isAdmin)
    }

    @Test
    fun `a bootstrap owner is an admin even before the role is written`() {
        val owner = com.example.data.firebase.UserProfile(
            userId = "u3",
            email = AdminConfig.bootstrapAdminEmails.first(),
            role = AdminConfig.ROLE_USER
        )
        assertTrue(owner.isAdmin)
    }

    @Test
    fun `the role survives the firestore map round trip`() {
        val admin = com.example.data.firebase.UserProfile(
            userId = "u4",
            email = "admin@example.com",
            role = AdminConfig.ROLE_ADMIN
        )
        val restored = com.example.data.firebase.UserProfile.fromMap(admin.toMap())

        assertEquals(AdminConfig.ROLE_ADMIN, restored.role)
        assertTrue(restored.isAdmin)
    }

    @Test
    fun `a profile written before roles existed defaults to viewer`() {
        val legacy = mapOf<String, Any?>("userId" to "u5", "email" to "legacy@example.com")
        val restored = com.example.data.firebase.UserProfile.fromMap(legacy)

        assertEquals(AdminConfig.ROLE_USER, restored.role)
        assertFalse(restored.isAdmin)
    }
}
