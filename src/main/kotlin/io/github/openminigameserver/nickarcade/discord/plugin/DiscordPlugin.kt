package io.github.openminigameserver.nickarcade.discord.plugin

import io.github.openminigameserver.nickarcade.core.IoC
import io.github.openminigameserver.nickarcade.core.io.config.ArcadeConfigurationFile
import io.github.openminigameserver.nickarcade.discord.botManager
import io.github.openminigameserver.nickarcade.discord.core.BotManager
import io.github.openminigameserver.nickarcade.discord.discordConfiguration
import io.github.openminigameserver.nickarcade.discord.plugin.events.handleChatMessages
import io.github.openminigameserver.nickarcade.discord.plugin.events.handleInviteEvents
import org.bukkit.plugin.java.JavaPlugin

class DiscordPlugin : JavaPlugin() {

    override fun onEnable() {
        if (!loadConfiguration()) {
            isEnabled = false
            return
        }
        botManager = BotManager(discordConfiguration)
        IoC += botManager!!

        handleInviteEvents()
        handleChatMessages()
    }

    override fun onDisable() {
        botManager?.stop()
    }

    private fun loadConfiguration(): Boolean {
        discordConfiguration = ArcadeConfigurationFile("config.yml", this).load()
        return discordConfiguration.isValid()
    }
}