package com.minesentry.minesentryspigot.platform

import com.minesentry.minesentry.command.MsCommandSender
import com.minesentry.minesentry.patch.MsServer
import com.minesentry.minesentryspigot.platform.PlatformUtil.patch
import com.minesentry.minesentryspigot.platform.PlatformUtil.toNative
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