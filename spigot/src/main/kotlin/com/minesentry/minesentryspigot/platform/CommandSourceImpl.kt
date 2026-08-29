package com.minesentry.minesentryspigot.platform

import com.minesentry.minesentry.command.MsCommandSender
import com.minesentry.minesentry.command.MsCommandSource
import org.bukkit.command.CommandSender

class CommandSourceImpl(
    private val native: CommandSender
): MsCommandSource {

    override val sender: MsCommandSender = CommandSenderImpl(native)

    override fun hasPermission(permission: String): Boolean {
        return native.hasPermission(permission)
    }

}