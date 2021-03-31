package io.github.openminigameserver.nickarcade.discord.core

import io.github.openminigameserver.nickarcade.chat.utils.PrivateMessageUtils
import io.github.openminigameserver.nickarcade.discord.core.commands.jda.commandSenderCache
import io.github.openminigameserver.nickarcade.discord.core.interop.senders.jda.LinkedJDACommandSender
import io.github.openminigameserver.nickarcade.discord.core.io.config.DiscordConfigurationFile
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent
import net.dv8tion.jda.api.hooks.EventListener
import net.kyori.adventure.text.Component.text

class BotManager(private val config: DiscordConfigurationFile) {
    val jdaBot = JDABuilder.createLight(config.token)
        .setStatus(OnlineStatus.ONLINE)
        .build().awaitReady().also {
            it.addEventListener(object : EventListener {
                override fun onEvent(event: GenericEvent) {
                    if (event is PrivateMessageReceivedEvent) {
                        val cacheEntry = commandSenderCache.getIfPresent(event.author.idLong)
                        if (cacheEntry != null && cacheEntry is LinkedJDACommandSender) {
                            if (!event.message.contentRaw.startsWith("/")) {
                                PrivateMessageUtils.replyPrivateMessage(
                                    cacheEntry.arcadeSender,
                                    text(event.message.contentDisplay)
                                )
                            }
                        }
                    }
                }
            })
        }

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