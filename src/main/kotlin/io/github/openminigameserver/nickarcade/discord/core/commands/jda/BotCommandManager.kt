package io.github.openminigameserver.nickarcade.discord.core.commands.jda

import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator
import cloud.commandframework.jda.JDA4CommandManager
import cloud.commandframework.jda.JDACommandSender
import cloud.commandframework.permission.CommandPermission
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import io.github.openminigameserver.nickarcade.core.data.sender.player.ArcadePlayer
import io.github.openminigameserver.nickarcade.discord.botManager
import io.github.openminigameserver.nickarcade.discord.core.interop.senders.jda.LinkedJDACommandSender
import io.github.openminigameserver.nickarcade.discord.core.interop.senders.jda.UnlinkedJDACommandSender
import io.github.openminigameserver.nickarcade.discord.core.io.database.PlayerLinkingManager
import io.github.openminigameserver.nickarcade.discord.plugin.DiscordPlugin
import io.github.openminigameserver.nickarcade.plugin.helper.commands.HypixelPackageRankPermission
import io.github.openminigameserver.nickarcade.plugin.helper.commands.parsers.PlayerDataParser
import io.leangen.geantyref.TypeToken
import kotlinx.coroutines.runBlocking
import org.bukkit.plugin.java.JavaPlugin
import org.checkerframework.checker.nullness.qual.NonNull
import java.util.concurrent.TimeUnit
import java.util.function.Function

class BotCommandManager : JDA4CommandManager<JDACommandSender>(
    botManager!!.jdaBot,
    {
//        if (it.user.name == "NickAc")
            "/"
//        else "CTz&[4ctosaqi&H"
    },
    null,
    AsynchronousCommandExecutionCoordinator.newBuilder<JDACommandSender>().withAsynchronousParsing().build(),
    computeCommandSender(),
    { if (it is UnlinkedJDACommandSender) it.original else if (it is LinkedJDACommandSender) it.original else it }) {
    init {
        registerCommandPreProcessor {
            JavaPlugin.getPlugin(DiscordPlugin::class.java).logger.info(
                "${it.commandContext.sender.user.asTag} executed command /${
                    it.inputQueue.joinToString(
                        " "
                    )
                }"
            )
        }

        parserRegistry.registerParserSupplier(TypeToken.get(ArcadePlayer::class.java)) {
            PlayerDataParser<JDACommandSender>().apply {
                isCachedWhenLoaded = false
            }
        }
    }

    override fun hasPermission(sender: JDACommandSender, permission: CommandPermission): Boolean {
        if (sender is LinkedJDACommandSender && permission is HypixelPackageRankPermission) {
            return sender.arcadeSender.hasAtLeastRank(permission.rank, true)
        }
        return super.hasPermission(sender, permission)
    }
}

val commandSenderCache: Cache<Long, JDACommandSender> = CacheBuilder.newBuilder()
    .expireAfterAccess(5, TimeUnit.MINUTES)
    .build()

private fun computeCommandSender(): Function<@NonNull JDACommandSender, @NonNull JDACommandSender> =
    Function {
        commandSenderCache.get(it.user.idLong) {
            runBlocking {
                val link = PlayerLinkingManager.getLink(it.user.idLong)
                if (link != null)
                    LinkedJDACommandSender(it, link)
                else
                    UnlinkedJDACommandSender(it)
            }
        }
    }