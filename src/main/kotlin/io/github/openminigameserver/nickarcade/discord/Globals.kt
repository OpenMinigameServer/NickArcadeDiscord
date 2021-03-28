package io.github.openminigameserver.nickarcade.discord

import io.github.openminigameserver.nickarcade.discord.core.BotManager
import io.github.openminigameserver.nickarcade.discord.core.io.config.DiscordConfigurationFile
import java.util.*

lateinit var discordConfiguration: DiscordConfigurationFile

var botManager: BotManager? = null

fun getCrafatarIcon(uuid: UUID): String {
    return "https://crafatar.com/avatars/${uuid}?size=100&overlay=true"
}