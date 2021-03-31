package io.github.openminigameserver.nickarcade.discord.plugin

import cloud.commandframework.annotations.AnnotationParser
import cloud.commandframework.jda.JDACommandSender
import io.github.openminigameserver.nickarcade.core.IoC
import io.github.openminigameserver.nickarcade.core.commandAnnotationParser
import io.github.openminigameserver.nickarcade.core.io.config.ArcadeConfigurationFile
import io.github.openminigameserver.nickarcade.discord.botAnnotationParser
import io.github.openminigameserver.nickarcade.discord.botCommandManager
import io.github.openminigameserver.nickarcade.discord.botManager
import io.github.openminigameserver.nickarcade.discord.core.BotManager
import io.github.openminigameserver.nickarcade.discord.core.commands.jda.BotCommandManager
import io.github.openminigameserver.nickarcade.discord.core.commands.jda.DiscordMessageCommands
import io.github.openminigameserver.nickarcade.discord.core.commands.jda.LinkCommands
import io.github.openminigameserver.nickarcade.discord.core.commands.jda.MiscAdminCommands
import io.github.openminigameserver.nickarcade.discord.discordConfiguration
import io.github.openminigameserver.nickarcade.discord.plugin.events.handleChatMessages
import io.github.openminigameserver.nickarcade.discord.plugin.events.handleInviteEvents
import io.github.openminigameserver.nickarcade.plugin.extensions.permission
import io.github.openminigameserver.nickarcade.plugin.helper.commands.RequiredRank
import org.bukkit.plugin.java.JavaPlugin
import java.util.function.BiFunction

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

    }

    private fun prepareCommandManager() {
        botCommandManager = BotCommandManager()

        botAnnotationParser = setupAnnotationParser()

        botAnnotationParser.parse(LinkCommands)
        botAnnotationParser.parse(DiscordMessageCommands)
        commandAnnotationParser.parse(MiscAdminCommands)
    }

    private fun setupAnnotationParser(): AnnotationParser<JDACommandSender> {
        return AnnotationParser(
            botCommandManager,
            JDACommandSender::class.java
        ) { botCommandManager.createDefaultCommandMeta() }.apply {
            registerBuilderModifier(RequiredRank::class.java, BiFunction { annotation, builder ->
                return@BiFunction builder.permission(annotation.value)
            })
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