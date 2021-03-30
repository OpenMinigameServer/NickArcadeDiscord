package io.github.openminigameserver.nickarcade.discord.core

import io.github.openminigameserver.nickarcade.discord.core.io.config.DiscordConfigurationFile
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus

class BotManager(private val config: DiscordConfigurationFile) {
    val jdaBot = JDABuilder.createLight(config.token)
        .setStatus(OnlineStatus.ONLINE)
        .build().awaitReady()

    val invitesChannel by lazy {
        jdaBot.getGuildChannelById(config.invitesChannelId)
    }

    val staffChannel by lazy {
        jdaBot.getGuildChannelById(config.staffChannelId)
    }

    fun stop() {
        jdaBot.shutdown()
    }
}