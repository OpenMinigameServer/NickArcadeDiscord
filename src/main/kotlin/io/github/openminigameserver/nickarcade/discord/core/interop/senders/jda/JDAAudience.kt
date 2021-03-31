package io.github.openminigameserver.nickarcade.discord.core.interop.senders.jda

import io.github.openminigameserver.nickarcade.chat.components.PrivateMessageComponent
import io.github.openminigameserver.nickarcade.discord.core.commands.jda.reply
import io.github.openminigameserver.nickarcade.discord.core.prefixrender.MinecraftTextRender.renderComponentToImage
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.PrivateChannel
import net.dv8tion.jda.api.requests.RestAction
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.MessageType
import net.kyori.adventure.identity.Identified
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import java.util.*


class JDAAudience(private val sender: LinkedJDACommandSender) : Audience {
    override fun sendMessage(message: ComponentLike) {
        handleComponentLikeMessage(message)
    }

    override fun sendMessage(message: ComponentLike, type: MessageType) {
        handleComponentLikeMessage(message)
    }

    override fun sendMessage(source: Identified, message: ComponentLike, type: MessageType) {
        handleComponentLikeMessage(message)
    }

    override fun sendMessage(source: Identity, message: ComponentLike, type: MessageType) {
        handleComponentLikeMessage(message)
    }

    var lastSender: UUID? = null
    private fun handleComponentLikeMessage(message: ComponentLike) {
        sendMessage(message.asComponent())
        if (message is PrivateMessageComponent) {
            if (message.user != sender.user && lastSender != message.user.uuid) {
                lastSender = message.user.uuid
                sendMessage(text {
                    it.append(text("Opened a chat conversation with ", GREEN))
                    it.append(text(message.user.getChatName(actualData = true, colourPrefixOnly = false)))
                    it.append(text(" for the next 5 minutes.\nReplying in this channel will automatically reply to them.", GREEN))
                })
            }
        }
    }

    private var errorOccurred = false
    override fun sendMessage(source: Identity, message: Component, type: MessageType) {
        runCodeInPrivateDMs {
            val legacySection = LegacyComponentSerializer.legacySection()

            val embed = EmbedBuilder()
                .setImage("attachment://message.png")
            it.sendFile(renderComponentToImage(legacySection.deserialize(legacySection.serialize(message)), 5, 2), "message.png")
                .embed(embed.build())
        }
    }

    private fun runCodeInPrivateDMs(function: (PrivateChannel) -> RestAction<Message>) {
        if (errorOccurred) return
        try {
            sender.user.openPrivateChannel().flatMap(function).complete()
        } catch (e: Throwable) {
            errorOccurred = true
            sender.reply("Unable to send you the output of that command in a private message.\nCheck your privacy settings.")
        }
    }
}