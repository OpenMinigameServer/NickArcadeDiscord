package io.github.openminigameserver.nickarcade.discord.core.commands.jda

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.specifier.Greedy
import io.github.openminigameserver.hypixelapi.models.HypixelPackageRank
import io.github.openminigameserver.nickarcade.chat.utils.PrivateMessageUtils
import io.github.openminigameserver.nickarcade.core.data.sender.player.ArcadePlayer
import io.github.openminigameserver.nickarcade.discord.core.interop.senders.jda.LinkedJDACommandSender
import io.github.openminigameserver.nickarcade.plugin.helper.commands.RequiredRank
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

object DiscordMessageCommands {
    @CommandMethod("w|msg|message|tell <target> <message>")
    fun sendPrivateMessage(
        sender: LinkedJDACommandSender,
        @Argument("target") target: ArcadePlayer,
        @Argument("message") @Greedy message: String
    ) {
        PrivateMessageUtils.sendPrivateMessage(sender.arcadeSender, target, text(message))
    }

    @CommandMethod("r <message>")
    fun replyPrivateMessage(
        sender: LinkedJDACommandSender,
        @Argument("message") @Greedy message: String
    ) {
        PrivateMessageUtils.replyPrivateMessage(sender.arcadeSender, text(message))
    }

    @CommandMethod("echo <message>")
    fun echoMessage(
        sender: LinkedJDACommandSender,
        @Argument("message") @Greedy message: String
    ) {
        sender.sendMessage(
            text("Your message:", NamedTextColor.YELLOW).append(newline())
                .append(LegacyComponentSerializer.legacyAmpersand().deserialize(message))
        )
    }
    @RequiredRank(HypixelPackageRank.ADMIN)
    @CommandMethod("render <message>")
    fun renderMessage(
        sender: LinkedJDACommandSender,
        @Argument("message") @Greedy message: String
    ) {
        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message))
    }
}