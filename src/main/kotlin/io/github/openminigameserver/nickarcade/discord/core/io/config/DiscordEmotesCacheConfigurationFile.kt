package io.github.openminigameserver.nickarcade.discord.core.io.config

import java.util.*

data class DiscordEmotesCacheConfigurationFile(
    val cachedEntries: MutableMap<UUID, String> = mutableMapOf()
)