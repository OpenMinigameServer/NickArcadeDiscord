package io.github.openminigameserver.nickarcade.discord.plugin

import cloud.commandframework.annotations.AnnotationParser
import cloud.commandframework.jda.JDACommandSender
import io.github.openminigameserver.nickarcade.core.IoC
import io.github.openminigameserver.nickarcade.core.io.config.ArcadeConfigurationFile
import io.github.openminigameserver.nickarcade.discord.*
import io.github.openminigameserver.nickarcade.discord.core.BotManager
import io.github.openminigameserver.nickarcade.discord.core.commands.jda.BotCommandManager
import io.github.openminigameserver.nickarcade.discord.core.commands.jda.LinkCommands
import io.github.openminigameserver.nickarcade.discord.core.prefixrender.UserPrefixEmoteManager
import io.github.openminigameserver.nickarcade.discord.plugin.events.handleChatMessages
import io.github.openminigameserver.nickarcade.discord.plugin.events.handleInviteEvents
import io.github.openminigameserver.nickarcade.plugin.extensions.launchAsync
import org.bukkit.plugin.java.JavaPlugin
import kotlin.time.measureTime

class DiscordPlugin : JavaPlugin() {

    override fun onEnable() {
        if (!loadConfiguration()) {
            isEnabled = false
            return
        }
        botManager = BotManager(discordConfiguration)
        IoC += botManager!!

        prepareCommandManager()

        handleInviteEvents()
        handleChatMessages()

        createRankEmotes()
    }

    private fun prepareCommandManager() {
        botCommandManager = BotCommandManager()

        botAnnotationParser = AnnotationParser(
            botCommandManager,
            JDACommandSender::class.java
        ) { botCommandManager.createDefaultCommandMeta() }

        botAnnotationParser.parse(LinkCommands)
    }

    private fun createRankEmotes() {
        launchAsync {
            val time = measureTime {
                UserPrefixEmoteManager.renderPlayerPrefixesToEmotes()
            }

            println("Took $time to add all player emotes")

            emotes = botManager!!.jdaBot.guilds.flatMap {
                it.retrieveEmotes().complete()
            }
        }
    }

    override fun onDisable() {
        botManager?.stop()
    }

    private fun loadConfiguration(): Boolean {
        discordConfiguration = ArcadeConfigurationFile("config.yml", this).load()
        return discordConfiguration.isValid()
    }
}