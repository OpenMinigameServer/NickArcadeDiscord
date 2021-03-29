package io.github.openminigameserver.nickarcade.discord.core.commands.jda

import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator
import cloud.commandframework.jda.JDA4CommandManager
import cloud.commandframework.jda.JDACommandSender
import io.github.openminigameserver.nickarcade.core.data.sender.player.ArcadePlayer
import io.github.openminigameserver.nickarcade.core.manager.PlayerDataManager
import io.github.openminigameserver.nickarcade.discord.botManager
import io.github.openminigameserver.nickarcade.discord.core.interop.senders.jda.LinkedJDACommandSender
import io.github.openminigameserver.nickarcade.discord.core.interop.senders.jda.UnlinkedJDACommandSender
import io.github.openminigameserver.nickarcade.discord.core.io.database.PlayerLinkingManager
import io.github.openminigameserver.nickarcade.plugin.helper.commands.parsers.PlayerDataParser
import io.leangen.geantyref.TypeToken
import kotlinx.coroutines.runBlocking
import org.checkerframework.checker.nullness.qual.NonNull
import java.util.function.Function

class BotCommandManager : JDA4CommandManager<JDACommandSender>(
    botManager!!.jdaBot,
    { "/" },
    { _, _ -> true },
    AsynchronousCommandExecutionCoordinator.newBuilder<JDACommandSender>().withAsynchronousParsing().build(),
    computeCommandSender(),
    { if (it is UnlinkedJDACommandSender) it.original else if (it is LinkedJDACommandSender) it.original else it }) {
    init {
        parserRegistry.registerParserSupplier(TypeToken.get(ArcadePlayer::class.java)) {
            PlayerDataParser()
        }

        registerCommandPostProcessor { ctx ->
            ctx.commandContext.asMap().forEach {
                val value = it.value
                if (value is ArcadePlayer && !value.isOnline) {
                    PlayerDataManager.removePlayerData(value.uuid)
                }
            }
        }
    }
}

private fun computeCommandSender(): Function<@NonNull JDACommandSender, @NonNull JDACommandSender> =
    Function {
        runBlocking {
            val link = PlayerLinkingManager.getLink(it.user.idLong)
            if (link != null)
                LinkedJDACommandSender(it, link)
            else
                UnlinkedJDACommandSender(it)
        }
    }