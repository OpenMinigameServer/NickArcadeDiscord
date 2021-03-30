package io.github.openminigameserver.nickarcade.discord.core.io.database

import io.github.openminigameserver.nickarcade.core.database
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.eq
import java.util.*

object PlayerLinkingManager {

    private val playerLinkCollection: CoroutineCollection<DiscordUserPlayerLinkEntry> by lazy {
        database.getCollection("discordLinks")
    }

    suspend fun getLink(discordId: Long): DiscordUserPlayerLinkEntry? {
        return playerLinkCollection.findOneById(discordId)
    }

    val playerIdLinkMap = mutableMapOf<UUID, DiscordUserPlayerLinkEntry>()
    suspend fun getLinkByPlayerId(playerId: UUID): DiscordUserPlayerLinkEntry? {
        return playerIdLinkMap[playerId] ?: playerLinkCollection.findOne(DiscordUserPlayerLinkEntry::playerId eq playerId)?.also {
            playerIdLinkMap[playerId] = it
        }
    }

    suspend fun linkAccounts(discordId: Long, playerId: UUID) {
        playerLinkCollection.insertOne(DiscordUserPlayerLinkEntry(discordId, playerId))
    }

}