package io.github.openminigameserver.nickarcade.discord

import cloud.commandframework.annotations.AnnotationParser
import cloud.commandframework.jda.JDACommandSender
import io.github.openminigameserver.nickarcade.core.io.config.ArcadeConfigurationFile
import io.github.openminigameserver.nickarcade.discord.core.BotManager
import io.github.openminigameserver.nickarcade.discord.core.commands.jda.BotCommandManager
import io.github.openminigameserver.nickarcade.discord.core.io.config.DiscordConfigurationFile
import io.github.openminigameserver.nickarcade.discord.core.io.config.DiscordEmotesCacheConfigurationFile
import net.dv8tion.jda.api.entities.ListedEmote
import java.util.*

lateinit var discordConfiguration: DiscordConfigurationFile
lateinit var discordEmotesCacheConfigurationFile: ArcadeConfigurationFile
lateinit var discordEmotesCacheConfiguration: DiscordEmotesCacheConfigurationFile

var botManager: BotManager? = null
var emotesCache: MutableList<ListedEmote>? = null
lateinit var botCommandManager: BotCommandManager
lateinit var botAnnotationParser: AnnotationParser<JDACommandSender>

fun getCrafatarIcon(uuid: UUID): String {
    return "https://crafatar.com/avatars/${uuid}?size=100&overlay=true"
}