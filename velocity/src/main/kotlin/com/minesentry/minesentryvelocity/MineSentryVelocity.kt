package com.minesentry.minesentryvelocity

import com.google.inject.Inject
import com.minesentry.minesentry.MineSentry
import com.minesentry.minesentry.command.MsCommand
import com.minesentry.minesentry.event.MsPlayerLoginEvent
import com.minesentry.minesentry.event.MsPlayerPostLoginEvent
import com.minesentry.minesentry.patch.MsPlayerIdentity
import com.minesentry.minesentryvelocity.platform.MsPlayerImpl
import com.minesentry.minesentryvelocity.platform.PlatformUtil.patch
import com.minesentry.minesentryvelocity.platform.VelocityPlatform
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.tree.LiteralCommandNode
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.event.ResultedEvent
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.LoginEvent
import com.velocitypowered.api.event.connection.PostLoginEvent
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import org.slf4j.Logger
import java.nio.file.Path
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger as JulLogger

@Plugin(
    id = "minesentry",
    name = "MineSentry",
    url = "https://minesentry.com",
)
class MineSentryVelocity @Inject constructor(
    private val proxy: ProxyServer,
    slf4jLogger: Logger,
    @param:DataDirectory dataDirectory: Path,
) {

    private val logger = julLogger(slf4jLogger)
    private val dataDir = dataDirectory.toFile()

    @Subscribe
    fun onProxyInitialize(event: ProxyInitializeEvent) {
        MineSentry.inject(VelocityPlatform(
            logger = logger,
            dataDir = dataDir,
            nativeServer = proxy,
        ))

        MineSentry.onEnable {
            registerCommands()
        }
    }

    @Subscribe
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        MineSentry.onDisable()
    }

    @Subscribe
    fun onPlayerLogin(event: LoginEvent) {
        val player = event.player
        val loginEvent = MsPlayerLoginEvent(MsPlayerIdentity(player.username, player.uniqueId))
        MineSentry.onPlayerLogin(loginEvent)

        loginEvent.rejectionMessage?.let {
            event.result = ResultedEvent.ComponentResult.denied(it)
        }
    }

    @Subscribe
    fun onPostLogin(event: PostLoginEvent) {
        MineSentry.onPostLogin(MsPlayerPostLoginEvent(MsPlayerImpl(event.player)))
    }

    private fun registerCommands() {
        val dispatcher = CommandDispatcher<CommandSource>()
        MsCommand.register(dispatcher) { it.patch() }

        val commandManager = proxy.commandManager
        dispatcher.root.children.forEach { node ->
            val brigadier = BrigadierCommand(node as LiteralCommandNode<CommandSource>)
            commandManager.register(
                commandManager.metaBuilder(brigadier).plugin(this).build(),
                brigadier,
            )
        }
    }

    private fun julLogger(native: Logger): JulLogger =
        object : JulLogger("MineSentry", null) {
            override fun log(record: LogRecord) {
                when {
                    record.level.intValue() >= Level.SEVERE.intValue() -> native.error(record.message, record.thrown)
                    record.level.intValue() >= Level.WARNING.intValue() -> native.warn(record.message, record.thrown)
                    record.level.intValue() >= Level.INFO.intValue() -> native.info(record.message, record.thrown)
                    else -> native.debug(record.message, record.thrown)
                }
            }
        }.apply { level = Level.ALL }

}
