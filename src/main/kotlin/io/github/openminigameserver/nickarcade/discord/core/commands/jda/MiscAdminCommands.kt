package io.github.openminigameserver.nickarcade.discord.core.commands.jda

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandMethod
import io.github.openminigameserver.hypixelapi.models.HypixelPackageRank
import io.github.openminigameserver.nickarcade.core.data.sender.ArcadeSender
import io.github.openminigameserver.nickarcade.core.data.sender.player.ArcadePlayer
import io.github.openminigameserver.nickarcade.discord.core.io.database.PlayerLinkingManager
import io.github.openminigameserver.nickarcade.discord.plugin.DiscordPlugin.Companion.createRankEmotes
import io.github.openminigameserver.nickarcade.plugin.extensions.command
import io.github.openminigameserver.nickarcade.plugin.helper.commands.RequiredRank
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

object MiscAdminCommands {

    @RequiredRank(HypixelPackageRank.ADMIN)
    @CommandMethod("debugrefreshuseremotes")
    fun refreshUserEmotes(sender: ArcadeSender) {
        createRankEmotes()
    }

    @RequiredRank(HypixelPackageRank.ADMIN)
    @CommandMethod("link admin <player> <id>")
    fun linkCommand2(sender: ArcadeSender, @Argument("player") player: ArcadePlayer, @Argument("id") id: Long) =
        command(sender) {
            PlayerLinkingManager.linkAccounts(id, player.uuid)
            sender.audience.sendMessage(Component.text("Discord account linking was successful.", NamedTextColor.GREEN))

        }


}