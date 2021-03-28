package io.github.openminigameserver.nickarcade.discord.plugin.events

import io.github.openminigameserver.nickarcade.discord.botManager
import io.github.openminigameserver.nickarcade.discord.getCrafatarIcon
import io.github.openminigameserver.nickarcade.moderation.plugin.events.impl.PlayerInviteAddEvent
import io.github.openminigameserver.nickarcade.moderation.plugin.events.impl.PlayerInviteRemoveEvent
import io.github.openminigameserver.nickarcade.plugin.extensions.event
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.TextChannel
import java.util.*

fun handleInviteEvents() {
    event<PlayerInviteAddEvent> {
        val invitesChannel = botManager?.invitesChannel
        if (invitesChannel is TextChannel) {
            invitesChannel.sendMessage(
                EmbedBuilder()
                    .setAuthor("NickArcade")
                    .setTitle("Player Invite - Log")
                    .setDescription("A new invite by a player has been performed.")
                    .setColor(0xffaa00)
                    .addField("Inviter", "```$inviteeName```", true)
                    .addField("Invited", "```$invitedName```", true)
                    .setThumbnail(getCrafatarIcon(invite.invited))
                    .setFooter(inviteeName, if (invite.inviter != UUID(0,0)) getCrafatarIcon(invite.inviter) else null)
                    .setTimestamp(Clock.System.now().toJavaInstant())
                    .build()
            ).complete()
        }
    }
    event<PlayerInviteRemoveEvent> {
        val invitesChannel = botManager?.invitesChannel
        if (invitesChannel is TextChannel) {
            invitesChannel.sendMessage(
                EmbedBuilder()
                    .setAuthor("NickArcade")
                    .setTitle("Player Invite - Log")
                    .setDescription("A player invite was removed by a staff member.")
                    .setColor(0xaa0000)
                    .addField("Un-invitee", "```$inviteeName```", true)
                    .addField("Uninvited", "```$invitedName```", true)
                    .setThumbnail(getCrafatarIcon(invitedUUID))
                    .build()
            ).complete()
        }
    }
}
