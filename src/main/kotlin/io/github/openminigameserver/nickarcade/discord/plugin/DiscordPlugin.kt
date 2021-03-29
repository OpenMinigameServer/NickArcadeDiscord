package io.github.openminigameserver.nickarcade.discord.plugin

import io.github.openminigameserver.nickarcade.core.IoC
import io.github.openminigameserver.nickarcade.core.io.config.ArcadeConfigurationFile
import io.github.openminigameserver.nickarcade.discord.botManager
import io.github.openminigameserver.nickarcade.discord.core.BotManager
import io.github.openminigameserver.nickarcade.discord.core.prefixrender.UserPrefixEmoteManager
import io.github.openminigameserver.nickarcade.discord.discordConfiguration
import io.github.openminigameserver.nickarcade.discord.emotes
import io.github.openminigameserver.nickarcade.discord.plugin.events.handleChatMessages
import io.github.openminigameserver.nickarcade.discord.plugin.events.handleInviteEvents
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

        handleInviteEvents()
        handleChatMessages()

        emotes = botManager!!.jdaBot.guilds.flatMap {
            it.retrieveEmotes().complete()
        }
        createRankEmotes()
    }

    private fun createRankEmotes() {
        val time = measureTime {
            UserPrefixEmoteManager.renderRanksToEmotes()
        }

        println("Took $time to add rank emotes")
    }

    override fun onDisable() {
        botManager?.stop()
    }

    private fun loadConfiguration(): Boolean {
        discordConfiguration = ArcadeConfigurationFile("config.yml", this).load()
        return discordConfiguration.isValid()
    }
}