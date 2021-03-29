package io.github.openminigameserver.nickarcade.discord.core.io.database

import io.github.openminigameserver.nickarcade.core.database
import org.litote.kmongo.coroutine.CoroutineCollection
import java.util.*

object PlayerLinkingManager {

    private val playerLinkCollection: CoroutineCollection<DiscordUserPlayerLinkEntry> by lazy {
        database.getCollection("discordLinks")
    }

    suspend fun getLink(discordId: Long): DiscordUserPlayerLinkEntry? {
        return playerLinkCollection.findOneById(discordId)
    }

    suspend fun linkAccounts(discordId: Long, playerId: UUID) {
        playerLinkCollection.insertOne(DiscordUserPlayerLinkEntry(discordId, playerId))
    }

}