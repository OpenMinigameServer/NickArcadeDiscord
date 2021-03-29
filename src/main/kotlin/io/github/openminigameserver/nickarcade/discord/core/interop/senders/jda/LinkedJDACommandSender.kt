package io.github.openminigameserver.nickarcade.discord.core.interop.senders.jda

import cloud.commandframework.jda.JDACommandSender
import io.github.openminigameserver.nickarcade.core.data.sender.player.ArcadePlayer
import io.github.openminigameserver.nickarcade.core.manager.PlayerDataManager
import io.github.openminigameserver.nickarcade.discord.core.io.database.DiscordUserPlayerLinkEntry

class LinkedJDACommandSender(internal val original: JDACommandSender, val link: DiscordUserPlayerLinkEntry) : JDACommandSender(
    original.event.orElse(null), original
        .user, original.channel
) {
    suspend fun getPlayer(): ArcadePlayer = PlayerDataManager.getPlayerData(link.playerId, original.user.name)
}