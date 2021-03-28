package io.github.openminigameserver.nickarcade.discord.core

import io.github.openminigameserver.nickarcade.discord.core.io.config.DiscordConfigurationFile
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity

class BotManager(val config: DiscordConfigurationFile) {
    val jdaBot = JDABuilder.createLight(config.token)
        .setStatus(OnlineStatus.ONLINE)
        .setActivity(Activity.watching("NickArcade"))
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