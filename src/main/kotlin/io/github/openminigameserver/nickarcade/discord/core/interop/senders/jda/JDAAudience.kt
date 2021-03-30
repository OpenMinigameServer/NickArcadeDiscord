package io.github.openminigameserver.nickarcade.discord.core.interop.senders.jda

import cloud.commandframework.jda.JDACommandSender
import io.github.openminigameserver.nickarcade.chat.components.PrivateMessageComponent
import io.github.openminigameserver.nickarcade.discord.core.commands.jda.reply
import io.github.openminigameserver.nickarcade.discord.core.prefixrender.MinecraftTextRender.renderComponentToImage
import io.github.openminigameserver.nickarcade.discord.plugin.events.getDiscordChatName
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.PrivateChannel
import net.dv8tion.jda.api.requests.RestAction
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.MessageType
import net.kyori.adventure.identity.Identified
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer

class JDAAudience(private val sender: JDACommandSender) : Audience {
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

    private fun handleComponentLikeMessage(message: ComponentLike) {
        if (message is PrivateMessageComponent) {
            runCodeInPrivateDMs {
                it.sendMessage(EmbedBuilder()
                    .addField(message.action, message.user.getDiscordChatName(), false)
                    .addField("Message", PlainComponentSerializer.plain().serialize(message.message), false)
                    .build())
            }
        } else {
            sendMessage(message.asComponent())
        }
    }

    private var errorOccurred = false
    override fun sendMessage(source: Identity, message: Component, type: MessageType) {
        runCodeInPrivateDMs {
            it.sendFile(renderComponentToImage(message, 5, 2), "message.png")
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