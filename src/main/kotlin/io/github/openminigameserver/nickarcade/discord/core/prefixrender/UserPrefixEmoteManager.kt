package io.github.openminigameserver.nickarcade.discord.core.prefixrender

import com.mongodb.client.model.Filters
import io.github.openminigameserver.nickarcade.core.data.sender.player.ArcadePlayer
import io.github.openminigameserver.nickarcade.core.data.sender.player.ArcadePlayerData
import io.github.openminigameserver.nickarcade.core.manager.PlayerDataManager
import io.github.openminigameserver.nickarcade.discord.botManager
import io.github.openminigameserver.nickarcade.discord.core.io.database.DiscordUserPlayerLinkEntry
import io.github.openminigameserver.nickarcade.discord.discordEmotesCacheConfiguration
import io.github.openminigameserver.nickarcade.discord.discordEmotesCacheConfigurationFile
import io.github.openminigameserver.nickarcade.discord.emotesCache
import io.github.openminigameserver.nickarcade.discord.plugin.events.getRawEmotesForUser
import io.github.openminigameserver.nickarcade.plugin.extensions.launchAsync
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Icon
import net.dv8tion.jda.api.requests.restaction.GuildAction
import org.litote.kmongo.lookup
import org.litote.kmongo.match
import org.litote.kmongo.not
import org.litote.kmongo.path
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.floor

object UserPrefixEmoteManager {

    suspend fun renderPlayerPrefixesToEmotes(isCleanup: Boolean = false) {
        if (isCleanup) cleanupGuilds()
        val errorCount = AtomicInteger()
        val emotesCount = AtomicInteger()

        PlayerDataManager.playerDataCollection.aggregate<ArcadePlayerData>(
            listOf(
                lookup(
                    from = "discordLinks",
                    localField = ArcadePlayerData::uuid.path(),
                    foreignField = DiscordUserPlayerLinkEntry::playerId.path(),
                    newAs = "discordLinks"
                ),
                match(
                    not(Filters.size("discordLinks", 0))
                )
            )

        ).consumeEach {
            updateUserEmotes(it, isCleanup, emotesCount, errorCount)
        }

        discordEmotesCacheConfigurationFile.save(discordEmotesCacheConfiguration)
        println("Added a total of ${emotesCount.get()} emotes")
    }

    private fun updateUserEmotes(
        it: ArcadePlayerData,
        cleanup: Boolean,
        emotesCount: AtomicInteger,
        errorCount: AtomicInteger
    ) {
        val player = ArcadePlayer(it)

        val cacheEntry = discordEmotesCacheConfiguration.cachedEntries[it.uuid]

        val entryCacheValue = player.getChatName(true)
        if (cleanup || cacheEntry == null || cacheEntry != entryCacheValue) {
            //Delete old emotes
            launchAsync {
                println("Deleting old emotes for ${player.actualDisplayName}")
                arrayOf("", "disc_").flatMap { getRawEmotesForUser(it + player.actualDisplayName) ?: emptyList() }
                    .forEach { emote ->
                        emote.delete().complete()
                        emotesCache?.removeIf { it.idLong == emote.idLong }
                    }
                println("Deleted old emotes for ${player.actualDisplayName}")
            }

            renderTextToEmotes(
                emotesCount,
                errorCount,
                player.effectivePrefix + player.actualDisplayName,
                player.actualDisplayName
            )
            renderTextToEmotes(
                emotesCount, errorCount, player.effectivePrefix + "@" + player.actualDisplayName,
                "disc_${player.actualDisplayName}"
            )

            discordEmotesCacheConfiguration.cachedEntries[it.uuid] = entryCacheValue
        }
    }

    private fun renderTextToEmotes(
        emotesCount: AtomicInteger,
        errorCount: AtomicInteger,
        text: String,
        name: String
    ) {
        val emotes = MinecraftTextRender.renderTextToEmotesImage(text)
        emotes.forEachIndexed { i, emote ->
            createEmote(emotesCount, errorCount, emote, "${name}_$i")
        }
    }

    private fun createEmote(
        emotesCount: AtomicInteger,
        errorCount: AtomicInteger,
        emote: ByteArray,
        name: String
    ) {
        val emotesGuild = getOrCreateGuild(emotesCount.incrementAndGet(), errorCount.get())
        val emoteAction = emotesGuild.createEmote(name, Icon.from(emote, Icon.IconType.PNG))
        try {
            val result = emoteAction.timeout(5, TimeUnit.SECONDS).complete()
        } catch (e: Throwable) {
            println("Error occurred ${e.message}. Trying on another guild")
            createEmote(
                emotesCount,
                errorCount.also { it.incrementAndGet() },
                emote,
                name
            )
        }
    }

    private fun cleanupGuilds() {
        botManager!!.jdaBot.guilds.forEach {
            if (it.name.startsWith("NickArcadeEmotes-")) {
                it.delete().complete()
                println("Deleted guild $it")
            }
        }
        guilds.clear()
    }

    var guilds = mutableMapOf<Int, Guild>()
    private fun getOrCreateGuild(i: Int, errorCount: Int = 0): Guild {
        val key = floor(i / 50F).toInt() + errorCount
        return guilds.getOrPut(key) {
            val keyName = "NickArcadeEmotes-$key"
            val jdaBot = botManager!!.jdaBot
            jdaBot.createGuild(keyName)
                .addChannel(GuildAction.ChannelData(ChannelType.TEXT, "general"))
                .complete()

            var guildResult: Guild? = null
            while (jdaBot.getGuildsByName(keyName, true).firstOrNull()?.also { guildResult = it } == null) {
                Thread.sleep(1)
            }
            println("Created guild with name $keyName")
            return@getOrPut guildResult!!
        }
    }

}