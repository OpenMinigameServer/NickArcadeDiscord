package io.github.openminigameserver.nickarcade.discord.core.io.database

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import java.util.*

data class DiscordUserPlayerLinkEntry(
    @JsonProperty("_id") val discordId: Long,
    val playerId: UUID
)