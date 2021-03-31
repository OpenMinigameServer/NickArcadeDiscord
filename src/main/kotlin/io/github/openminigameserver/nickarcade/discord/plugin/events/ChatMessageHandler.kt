package io.github.openminigameserver.nickarcade.discord.plugin.events

import cloud.commandframework.jda.JDACommandSender
import io.github.openminigameserver.nickarcade.chat.events.impl.AsyncChatChannelMessageSentEvent
import io.github.openminigameserver.nickarcade.chat.events.impl.PrivateMessageDeliverAttemptEvent
import io.github.openminigameserver.nickarcade.chat.events.impl.PrivateMessageDeliverResult
import io.github.openminigameserver.nickarcade.chat.model.ChatChannelType
import io.github.openminigameserver.nickarcade.chat.model.ChatEmote
import io.github.openminigameserver.nickarcade.core.data.sender.ArcadeSender
import io.github.openminigameserver.nickarcade.core.data.sender.misc.ArcadeWatcherSender
import io.github.openminigameserver.nickarcade.core.data.sender.player.ArcadePlayer
import io.github.openminigameserver.nickarcade.core.events.data.PlayerDataReloadEvent
import io.github.openminigameserver.nickarcade.discord.botManager
import io.github.openminigameserver.nickarcade.discord.core.commands.jda.commandSenderCache
import io.github.openminigameserver.nickarcade.discord.core.interop.senders.jda.LinkedJDACommandSender
import io.github.openminigameserver.nickarcade.discord.core.interop.senders.mc.LinkedJDAArcadeSender
import io.github.openminigameserver.nickarcade.discord.core.io.database.PlayerLinkingManager
import io.github.openminigameserver.nickarcade.discord.emotesCache
import io.github.openminigameserver.nickarcade.discord.getCrafatarIcon
import io.github.openminigameserver.nickarcade.plugin.extensions.event
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.ListedEmote
import net.dv8tion.jda.api.entities.TextChannel
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer

fun handleChatMessages() {
    event<PlayerDataReloadEvent> {
        val link = PlayerLinkingManager.getLinkByPlayerId(player.uuid)
        if (link != null) {
            val cached = commandSenderCache.getIfPresent(link.discordId)
            if (cached != null && cached is LinkedJDACommandSender) {
                cached.arcadeSender.player = player
            }
        }
    }

    event<PrivateMessageDeliverAttemptEvent>(forceBlocking = true) {
        if (result == PrivateMessageDeliverResult.PLAYER_OFFLINE) {
            val link = PlayerLinkingManager.getLinkByPlayerId(target.uuid)
            if (link != null) {
                var resultSender: JDACommandSender? = commandSenderCache.getIfPresent(link.discordId)

                if (resultSender == null) {
                    val userById =
                        kotlin.runCatching { botManager!!.staffChannel?.guild?.retrieveMemberById(link.discordId)?.complete() }.getOrNull()?.user
                    if (userById != null) resultSender = LinkedJDACommandSender(userById, link)
                }

                if (resultSender is LinkedJDACommandSender) {
                    target = resultSender.arcadeSender
                    result = PrivateMessageDeliverResult.DELIVERED
                }
            }
        }
    }

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

fun ArcadeSender.getDiscordChatName(): String {
    return when (this) {
        is ArcadePlayer -> {
            return (getEmotesForUser(actualDisplayName) ?: actualDisplayName)
        }
        is LinkedJDAArcadeSender -> {
            return getEmotesForUser("disc_" + this.getActualDisplayName()) ?: getActualDisplayName()
        }
        else -> this.getActualDisplayName()
    }
}

private fun getEmotesForUser(user: String): String? {
    return getRawEmotesForUser(user)
        ?.joinToString("") { it.asMention }
}

fun getRawEmotesForUser(user: String): List<ListedEmote>? {
    val prefix = "${user}_"
    return emotesCache?.filter { it.name.startsWith(prefix) }?.sortedBy { it.name.removePrefix(prefix).toInt() }
}

fun getRawEmotesForChatEmote(emote: ChatEmote): List<ListedEmote>? {
    val prefix = "emote_${emote.ordinal}_"
    return emotesCache?.filter { it.name.startsWith(prefix) }?.sortedBy { it.name.removePrefix(prefix).toInt() }
}

fun getEmotesForChatEmote(emote: ChatEmote): String? {
    return getRawEmotesForChatEmote(emote)
        ?.joinToString("") { it.asMention }
}
