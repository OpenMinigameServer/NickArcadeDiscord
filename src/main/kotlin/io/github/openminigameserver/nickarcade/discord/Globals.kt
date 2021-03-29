package io.github.openminigameserver.nickarcade.discord

import io.github.openminigameserver.nickarcade.discord.core.BotManager
import io.github.openminigameserver.nickarcade.discord.core.io.config.DiscordConfigurationFile
import net.dv8tion.jda.api.entities.ListedEmote
import java.util.*

lateinit var discordConfiguration: DiscordConfigurationFile

var botManager: BotManager? = null
var emotes: List<ListedEmote>? = null

fun getCrafatarIcon(uuid: UUID): String {
    return "https://crafatar.com/avatars/${uuid}?size=100&overlay=true"
}