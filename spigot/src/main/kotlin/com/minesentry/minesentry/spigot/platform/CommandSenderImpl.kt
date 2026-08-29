package com.minesentry.minesentry.spigot.platform

import com.minesentry.minesentry.command.MsCommandSender
import com.minesentry.minesentry.patch.MsServer
import com.minesentry.minesentry.spigot.platform.PlatformUtil.patch
import com.minesentry.minesentry.spigot.platform.PlatformUtil.toNative
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender

class CommandSenderImpl(
    private val native: CommandSender
): MsCommandSender() {

    override val name: String = native.name
    override val server: MsServer = native.server.patch()

    override fun hasPermission(permission: String): Boolean
        = native.hasPermission(permission)

    override fun sendMessage(component: Component) {
        val nativeComponents = component.toNative()
        native.spigot().sendMessage(*nativeComponents)
    }

}