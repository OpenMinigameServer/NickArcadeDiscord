package io.github.openminigameserver.nickarcade.discord.core.prefixrender

import io.github.openminigameserver.nickarcade.core.data.sender.player.ArcadePlayer
import io.github.openminigameserver.nickarcade.core.manager.PlayerDataManager
import io.github.openminigameserver.nickarcade.discord.botManager
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Icon
import net.dv8tion.jda.api.requests.restaction.GuildAction
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.floor

object UserPrefixEmoteManager {

    suspend fun renderPlayerPrefixesToEmotes() {
        cleanupGuilds()
        val errorCount = AtomicInteger()
        val emotesCount = AtomicInteger()

        PlayerDataManager.playerDataCollection.find().consumeEach {
            val player = ArcadePlayer(it)
            val text = (player.effectivePrefix + player.actualDisplayName)
            val emotes = MinecraftTextRender.renderTextToImage(text)
            emotes.forEachIndexed { i, emote ->
                createEmote(emotesCount, errorCount, emote, "${player.actualDisplayName}_$i")
            }
        }

        println("Added a total of ${emotesCount.get()} emotes")
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