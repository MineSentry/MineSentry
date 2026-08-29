package com.minesentry.minesentry.spigot

import com.minesentry.minesentry.MineSentry
import com.minesentry.minesentry.command.MsCommand
import com.minesentry.minesentry.command.MsCommandSource
import com.minesentry.minesentry.event.MsPlayerLoginEvent
import com.minesentry.minesentry.event.MsPlayerPostLoginEvent
import com.minesentry.minesentry.patch.MsPlayerIdentity
import com.minesentry.minesentry.spigot.platform.PlatformUtil.toNative
import com.minesentry.minesentry.spigot.platform.MsPlayerImpl
import com.minesentry.minesentry.spigot.platform.SpigotPlatform
import com.mojang.brigadier.CommandDispatcher
import net.md_5.bungee.api.chat.BaseComponent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.plugin.java.JavaPlugin


class MineSentrySpigot : JavaPlugin(), Listener {

    val commandDispatcher = CommandDispatcher<MsCommandSource>()

    override fun onEnable() {
        MineSentry.inject(SpigotPlatform(
            logger = logger,
            dataDir = dataFolder,
            nativeServer = server
        ))

        MineSentry.onEnable {
            server.pluginManager.registerEvents(this, this)

            // コマンドを登録
            MsCommand.register(commandDispatcher)
            getCommand("minesentry")?.apply {
                val command = MineSentryCommand(commandDispatcher)
                setExecutor(command)
                tabCompleter = command
            }
        }

    }

    override fun onDisable() {
        MineSentry.onDisable()
    }

    @EventHandler
    fun onPlayerLogin(event: PlayerLoginEvent) {
        val loginEvent = MsPlayerLoginEvent(MsPlayerIdentity(event.player.name, event.player.uniqueId))
        MineSentry.onPlayerLogin(loginEvent)

        loginEvent.rejectionMessage?.let {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, BaseComponent.toLegacyText(*it.toNative()))
        }
    }

    @EventHandler
    fun onPostLogin(event: PlayerJoinEvent) {
        MineSentry.onPostLogin(MsPlayerPostLoginEvent(MsPlayerImpl(event.player)))
    }

}
