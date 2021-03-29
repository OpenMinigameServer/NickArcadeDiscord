package io.github.openminigameserver.nickarcade.discord.core.commands.jda

import io.github.openminigameserver.nickarcade.discord.core.interop.senders.jda.UnlinkedJDACommandSender
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.entities.MessageEmbed

fun UnlinkedJDACommandSender.reply(message: String) {
    if (event.isPresent) {
        event.get().message.reply(message).complete()
        return
    }
    channel.sendMessage(user.asMention + " " + message).complete()
}

fun UnlinkedJDACommandSender.reply(message: MessageEmbed) {
    if (event.isPresent) {
        event.get().message.reply(message).complete()
        return
    }
    channel.sendMessage(
        MessageBuilder()
            .setContent(user.asMention)
            .setEmbed(message).build()
    ).complete()
}