package io.github.openminigameserver.nickarcade.discord.core.interop.senders.jda

import cloud.commandframework.jda.JDACommandSender
import io.github.openminigameserver.nickarcade.core.data.sender.player.ArcadePlayer
import io.github.openminigameserver.nickarcade.core.manager.PlayerDataManager
import io.github.openminigameserver.nickarcade.discord.core.interop.senders.mc.LinkedJDAArcadeSender
import io.github.openminigameserver.nickarcade.discord.core.io.database.DiscordUserPlayerLinkEntry
import net.dv8tion.jda.api.entities.User
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.ForwardingAudience

class LinkedJDACommandSender(internal val original: JDACommandSender, val link: DiscordUserPlayerLinkEntry) : JDACommandSender(
    original.event.orElse(null), original
        .user, original.channel
), ForwardingAudience {
    constructor(user: User, link: DiscordUserPlayerLinkEntry) : this(SimpleJDACommandSender(user), link)

    val arcadeSender by lazy { LinkedJDAArcadeSender(this) }

    suspend fun getPlayer(): ArcadePlayer = PlayerDataManager.getPlayerData(link.playerId, original.user.name)
    override fun audiences(): MutableIterable<Audience> {
        return arrayListOf(arcadeSender.audience)
    }
}