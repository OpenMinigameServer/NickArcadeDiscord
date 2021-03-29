package io.github.openminigameserver.nickarcade.discord.plugin.events

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
                    .addField("Sender", sender.getDiscordChatName(), true)
                    .addField("", "```$message```", false)
                    .setTimestamp(Clock.System.now().toJavaInstant())
                    .build()
            ).complete()
        }
    }
}

private fun ArcadeSender.getActualDisplayName() = if (this is ArcadePlayer) this.actualDisplayName else this.displayName

private fun ArcadeSender.getDiscordChatName(): String {
    return if (this is ArcadePlayer) {
        return (emotes?.filter { it.name.startsWith("${actualDisplayName}_") }?.sortedBy { it.name }
            ?.joinToString("") { it.asMention } ?: actualDisplayName)
    } else this.getActualDisplayName()
}
