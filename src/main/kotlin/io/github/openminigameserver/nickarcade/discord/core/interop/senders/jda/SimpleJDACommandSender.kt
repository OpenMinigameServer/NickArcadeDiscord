package io.github.openminigameserver.nickarcade.discord.core.interop.senders.jda

import cloud.commandframework.jda.JDACommandSender
import net.dv8tion.jda.api.entities.User

class SimpleJDACommandSender(user: User) : JDACommandSender(null, user, user.openPrivateChannel().complete()) {
}