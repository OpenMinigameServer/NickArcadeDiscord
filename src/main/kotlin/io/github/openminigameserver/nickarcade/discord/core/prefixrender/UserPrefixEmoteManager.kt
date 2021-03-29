package io.github.openminigameserver.nickarcade.discord.core.prefixrender

import io.github.openminigameserver.hypixelapi.models.HypixelPackageRank
import io.github.openminigameserver.hypixelapi.models.HypixelPackageRank.*
import io.github.openminigameserver.hypixelapi.utis.MinecraftChatColor
import io.github.openminigameserver.nickarcade.discord.botManager
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Icon
import net.dv8tion.jda.api.requests.restaction.GuildAction
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.floor

object UserPrefixEmoteManager {

    private val ranks = EnumSet.allOf(HypixelPackageRank::class.java).also { it.removeAll(arrayOf(NONE, NORMAL)) }
    fun renderRanksToEmotes() {
        cleanupGuilds()
        val errorCount = AtomicInteger()
        val rankColors = EnumSet.range(MinecraftChatColor.BLACK, MinecraftChatColor.WHITE)
        val emotesCount = AtomicInteger()
        ranks.forEach { rank ->
            if (rank == MVP_PLUS || rank == SUPERSTAR) {
                (if (rank == SUPERSTAR) arrayOf(MinecraftChatColor.AQUA, MinecraftChatColor.GOLD) else arrayOf(
                    MinecraftChatColor.RESET
                )).forEach { monthlyRankColor ->
                    rankColors.forEach { rankPlusColor ->
                        val emotes = UserPrefixRender.renderTextToImage(rank.computePrefixForPlayer(rankPlusColor, monthlyRankColor))
                        emotes.forEachIndexed { i, emote ->
                            createEmote(emotesCount, errorCount, rank, i, emote, emotes, rankPlusColor, monthlyRankColor)
                        }
                    }
                }

            } else {
                val emotes = UserPrefixRender.renderTextToImage(rank.defaultPrefix)
                emotes.forEachIndexed { i, emote ->
                    createEmote(emotesCount, errorCount, rank, i, emote, emotes)
                }
            }
        }
        println("Added a total of ${emotesCount.get()} emotes")
    }

    private fun createEmote(
        emotesCount: AtomicInteger,
        errorCount: AtomicInteger,
        rank: HypixelPackageRank,
        emoteCount: Int,
        emote: ByteArray,
        emotes: Array<ByteArray>,
        rankPlusColor: MinecraftChatColor = MinecraftChatColor.RESET,
        monthlyRankColor: MinecraftChatColor = MinecraftChatColor.RESET
    ) {
        val emotesGuild = getOrCreateGuild(emotesCount.incrementAndGet(), errorCount.get())
        var name = "r_${rank.ordinal}"
        if (rank == MVP_PLUS || rank == SUPERSTAR) {
            name += "_${rankPlusColor.ordinal}"
        }
        if (rank == SUPERSTAR) {
            name += "_${monthlyRankColor.ordinal}"
        }
        name += "_$emoteCount"
        val emoteAction = emotesGuild.createEmote(name, Icon.from(emote, Icon.IconType.PNG))
        try {
            val result = emoteAction.timeout(5, TimeUnit.SECONDS).complete()
            println("Created emote $name (${emoteCount + 1}/${emotes.size}) for $rank")
        } catch (e: Throwable) {
            println("Error occurred ${e.message}. Trying on another guild")
            createEmote(emotesCount, errorCount.also { it.incrementAndGet() }, rank, emoteCount, emote, emotes)
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