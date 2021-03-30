package io.github.openminigameserver.nickarcade.discord.core.interop.senders.mc

import io.github.openminigameserver.hypixelapi.models.HypixelPackageRank
import io.github.openminigameserver.nickarcade.core.data.sender.ArcadeSender
import io.github.openminigameserver.nickarcade.core.data.sender.player.extra.ExtraDataValue
import io.github.openminigameserver.nickarcade.discord.core.interop.senders.jda.JDAAudience
import io.github.openminigameserver.nickarcade.discord.core.interop.senders.jda.LinkedJDACommandSender
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.audience.Audience
import org.bukkit.command.CommandSender

class LinkedJDAArcadeSender(private val sender: LinkedJDACommandSender) : ArcadeSender(sender.link.playerId) {
    internal var player = runBlocking { sender.getPlayer() }
    override val audience: Audience
        = JDAAudience(sender)

    override val commandSender: CommandSender
        get() = player.commandSender
    override val displayName: String
        get() = player.actualDisplayName
    override val extraData: MutableMap<String, ExtraDataValue> = mutableMapOf()

    override fun getChatName(actualData: Boolean, colourPrefixOnly: Boolean): String {
        return player.computeEffectivePrefix(actualData) + "@$displayName"
    }

    override fun hasAtLeastRank(rank: HypixelPackageRank, actualData: Boolean): Boolean {
        return player.hasAtLeastRank(rank, actualData)
    }
}