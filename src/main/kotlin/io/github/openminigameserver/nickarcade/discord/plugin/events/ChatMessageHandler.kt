package io.github.openminigameserver.nickarcade.discord.plugin.events

import io.github.openminigameserver.hypixelapi.models.HypixelPackageRank
import io.github.openminigameserver.hypixelapi.utis.MinecraftChatColor
import io.github.openminigameserver.nickarcade.chat.events.impl.AsyncChatChannelMessageSentEvent
import io.github.openminigameserver.nickarcade.chat.model.ChatChannelType
import io.github.openminigameserver.nickarcade.core.data.sender.ArcadeSender
import io.github.openminigameserver.nickarcade.core.data.sender.misc.ArcadeWatcherSender
import io.github.openminigameserver.nickarcade.core.data.sender.player.ArcadePlayer
import io.github.openminigameserver.nickarcade.discord.botManager
import io.github.openminigameserver.nickarcade.discord.emotes
import io.github.openminigameserver.nickarcade.discord.getCrafatarIcon
import io.github.openminigameserver.nickarcade.plugin.extensions.event
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.TextChannel
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer

fun handleChatMessages() {
    event<AsyncChatChannelMessageSentEvent> {
        val discordChannel = botManager?.staffChannel
        if (discordChannel is TextChannel && channel.type == ChatChannelType.STAFF && sender !is ArcadeWatcherSender) {
            val message = PlainComponentSerializer.plain().serialize(message)
            discordChannel.sendMessage(
                EmbedBuilder()
                    .setAuthor(
                        sender.getActualDisplayName(),
                        null,
                        if (sender is ArcadePlayer) getCrafatarIcon(sender.uuid) else null
                    )
                    .setDescription("${sender.getDiscordChatName()}```$message```")
                    .setTimestamp(Clock.System.now().toJavaInstant())
                    .build()
            ).complete()
        }
    }
}

private fun ArcadeSender.getActualDisplayName() = if (this is ArcadePlayer) this.actualDisplayName else this.displayName

private fun ArcadeSender.getDiscordChatName(): String {
    return if (this is ArcadePlayer) {
        val rank = effectiveRank
        if (rank > HypixelPackageRank.NORMAL && data.overrides.prefixOverride == null) {
            var emoteName = "r_${rank.ordinal}"
            if (rank == HypixelPackageRank.MVP_PLUS || rank == HypixelPackageRank.SUPERSTAR) {
                emoteName += "_" + (data.overrides.rankPlusColorOverride ?: data.hypixelData?.rankPlusColor
                ?: MinecraftChatColor.RED).ordinal
            }
            if (rank == HypixelPackageRank.SUPERSTAR) {
                emoteName += "_" + (data.overrides.monthlyRankColorOverride ?: data.hypixelData?.monthlyRankColor
                ?: MinecraftChatColor.GOLD).ordinal
            }
            return (emotes?.filter { it.name.startsWith(emoteName) }?.sortedBy { it.name }
                ?.joinToString("") { it.asMention } ?: "") + actualDisplayName
        }
        return actualDisplayName
    } else this.getActualDisplayName()
}
