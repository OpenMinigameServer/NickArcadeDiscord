package io.github.openminigameserver.nickarcade.discord.core.commands.jda

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.specifier.Greedy
import io.github.openminigameserver.hypixelapi.models.HypixelPackageRank
import io.github.openminigameserver.nickarcade.chat.utils.PrivateMessageUtils
import io.github.openminigameserver.nickarcade.chat.utils.lastReply
import io.github.openminigameserver.nickarcade.core.data.sender.player.ArcadePlayer
import io.github.openminigameserver.nickarcade.core.manager.PlayerDataManager
import io.github.openminigameserver.nickarcade.discord.core.interop.senders.jda.JDAAudience
import io.github.openminigameserver.nickarcade.discord.core.interop.senders.jda.LinkedJDACommandSender
import io.github.openminigameserver.nickarcade.plugin.extensions.command
import io.github.openminigameserver.nickarcade.plugin.helper.commands.RequiredRank
import io.github.openminigameserver.profile.ProfileApi
import net.kyori.adventure.text.Component.*
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.*
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
    @CommandMethod("w|msg|message|tell <target>")
    fun startPrivateMessage(
        sender: LinkedJDACommandSender,
        @Argument("target") target: ArcadePlayer,
    ) {
        val arcadeSender = sender.arcadeSender
        val audience = (arcadeSender.audience as JDAAudience)
        audience.lastSender = target.uuid
        arcadeSender.lastReply = target
        audience.sendMessage(text {
            it.append(text("Opened a chat conversation with ", NamedTextColor.GREEN))
            it.append(text(target.getChatName(actualData = true, colourPrefixOnly = false)))
            it.append(text(" for the next 5 minutes.\nReplying in this channel will automatically reply to them.",
                NamedTextColor.GREEN
            ))
        })


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
    inline fun <reified T: Enum<T>> T.next(): T {
        val values = enumValues<T>()
        val nextOrdinal = (ordinal + 1) % values.size
        return values[nextOrdinal]
    }

    val letters = "abcdefghijklmnopqrstuvxyz01234567890'?!\"#$%&/()="
    @CommandMethod("virtualgift <name>")
    fun sendVirtualGift(sender: LinkedJDACommandSender, @Argument("name") target: String) {
        val jdaArcadeSender = sender.arcadeSender
        command(jdaArcadeSender) {
            val profile = ProfileApi.getProfileByName(target)!!
            val uniqueId = profile.uuid!!
            val data = PlayerDataManager.getPlayerData(uniqueId, target)
            var nextRank = data.effectiveRank.next().coerceAtMost(HypixelPackageRank.SUPERSTAR)

            var prefix = text(nextRank.defaultPrefix.trim().replace("[", "").replace("]", ""))
            if (nextRank <= HypixelPackageRank.NORMAL) {
                nextRank = HypixelPackageRank.VIP
            }

            sender.sendMessage(text { builder ->
                builder.append(newline())
                builder.append(text(letters.random(), YELLOW))
                builder.append(text(letters.random(), RED))
                builder.append(text(letters.random(), DARK_RED))
                builder.append(space())

                builder.append(text(jdaArcadeSender.getChatName(actualData = true, colourPrefixOnly = false)))
                builder.append(text(" virtually gifted", YELLOW))
                if (nextRank != HypixelPackageRank.SUPERSTAR) {
                    builder.append(text(" the ", YELLOW))
                } else {
                    builder.append(text(" 30 Days", GOLD))
                    builder.append(text(" of ", YELLOW))
                }
                builder.append(prefix)
                builder.append(text(" rank", YELLOW))
                builder.append(text(" to ", YELLOW))
                data.data.overrides.rankOverride = nextRank
                builder.append(text(data.getChatName(actualData = true, colourPrefixOnly = true)))
                builder.append(text("! ", YELLOW))

                builder.append(space())
                builder.append(text(letters.random(), DARK_RED))
                builder.append(text(letters.random(), RED))
                builder.append(text(letters.random(), YELLOW))

                builder.append(newline())
                builder.append(text("(This is a fake gift message and is intended for entertainment purposes only)", RED))
                builder.append(newline())
            })
        }
    }
}