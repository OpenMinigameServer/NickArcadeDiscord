package io.github.openminigameserver.nickarcade.discord.core.interop.senders.jda

import cloud.commandframework.jda.JDACommandSender

class UnlinkedJDACommandSender(internal val original: JDACommandSender) : JDACommandSender(
    original.event.orElse(null), original
        .user, original.channel
)