package io.github.openminigameserver.nickarcade.discord.core.commands.jda

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandMethod
import io.github.openminigameserver.nickarcade.core.data.sender.player.ArcadePlayer
import io.github.openminigameserver.nickarcade.discord.core.interop.senders.jda.UnlinkedJDACommandSender
import io.github.openminigameserver.nickarcade.discord.core.io.database.PlayerLinkingManager
import io.github.openminigameserver.nickarcade.plugin.extensions.clickEvent
import io.github.openminigameserver.nickarcade.plugin.extensions.launchAsync
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.TextDecoration

object LinkCommands {

    @CommandMethod("link <player>")
    fun linkCommand(sender: UnlinkedJDACommandSender, @Argument("player") player: ArcadePlayer) {
        if (!player.isOnline) {
            sender.reply("Unable to find player ${player.actualDisplayName} online!\nPlease join the server to link your account.")
            return
        }
        player.audience.sendMessage(
            text {
                it.append(text("Discord linking request for account ${sender.user.asTag}.", GREEN))
                it.append(newline())
                it.append(text("Click here to confirm linking this account.", GOLD, TextDecoration.UNDERLINED))
            }.clickEvent {
                launchAsync {
                    PlayerLinkingManager.linkAccounts(sender.user.idLong, player.uuid)
                    player.audience.sendMessage(text("Discord account linking was successful.", GREEN))
                }
            }
        )
        sender.reply("Link request sent to ${player.actualDisplayName}!\nPlease check your in-game chat to confirm it.")
    }
}