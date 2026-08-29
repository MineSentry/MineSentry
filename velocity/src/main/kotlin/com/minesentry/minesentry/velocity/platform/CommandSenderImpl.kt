package com.minesentry.minesentry.velocity.platform

import com.minesentry.minesentry.MineSentry
import com.minesentry.minesentry.command.MsCommandSender
import com.minesentry.minesentry.patch.MsServer
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.Component

class CommandSenderImpl(
    private val native: CommandSource
): MsCommandSender() {

    override val name: String = when (native) {
        is Player -> native.username
        else -> "CONSOLE"
    }
    override val server: MsServer
        get() = MineSentry.platform.server

    override fun hasPermission(permission: String): Boolean
        = native.hasPermission(permission)

    override fun sendMessage(component: Component) {
        native.sendMessage(component)
    }

}
