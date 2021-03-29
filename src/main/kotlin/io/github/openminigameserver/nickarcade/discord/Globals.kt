package io.github.openminigameserver.nickarcade.discord

import cloud.commandframework.annotations.AnnotationParser
import cloud.commandframework.jda.JDACommandSender
import io.github.openminigameserver.nickarcade.discord.core.BotManager
import io.github.openminigameserver.nickarcade.discord.core.commands.jda.BotCommandManager
import io.github.openminigameserver.nickarcade.discord.core.io.config.DiscordConfigurationFile
import net.dv8tion.jda.api.entities.ListedEmote
import java.util.*

lateinit var discordConfiguration: DiscordConfigurationFile

var botManager: BotManager? = null
var emotes: List<ListedEmote>? = null
lateinit var botCommandManager: BotCommandManager
lateinit var botAnnotationParser: AnnotationParser<JDACommandSender>

fun getCrafatarIcon(uuid: UUID): String {
    return "https://crafatar.com/avatars/${uuid}?size=100&overlay=true"
}