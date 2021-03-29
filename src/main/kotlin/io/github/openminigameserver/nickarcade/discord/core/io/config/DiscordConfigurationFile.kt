package io.github.openminigameserver.nickarcade.discord.core.io.config

data class DiscordConfigurationFile(
    var token: String = "",
    var invitesChannelId: Long = 0L,
    var staffChannelId: Long = 0L,
    var emotesGuildId: Long = 0L,
) {
    fun isValid(): Boolean {
        return token.isNotBlank() && invitesChannelId != 0L && staffChannelId != 0L && emotesGuildId != 0L
    }
}
