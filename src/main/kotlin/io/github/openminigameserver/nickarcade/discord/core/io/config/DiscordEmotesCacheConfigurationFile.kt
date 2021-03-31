package io.github.openminigameserver.nickarcade.discord.core.io.config

import io.github.openminigameserver.nickarcade.chat.model.ChatEmote
import java.util.*

data class DiscordEmotesCacheConfigurationFile(
    val cachedEntries: MutableMap<UUID, String> = mutableMapOf(),
    val cachedEmoteEntries: MutableList<ChatEmote> = mutableListOf()
)