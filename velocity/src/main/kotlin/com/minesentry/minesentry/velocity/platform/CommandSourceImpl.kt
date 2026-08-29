package com.minesentry.minesentry.velocity.platform

import com.minesentry.minesentry.command.MsCommandSender
import com.minesentry.minesentry.command.MsCommandSource
import com.velocitypowered.api.command.CommandSource

class CommandSourceImpl(
    private val native: CommandSource
): MsCommandSource {

    override val sender: MsCommandSender = CommandSenderImpl(native)

    override fun hasPermission(permission: String): Boolean {
        return native.hasPermission(permission)
    }

}
