package com.minesentry.minesentry.platform

import com.minesentry.minesentry.command.MsCommandSender
import com.minesentry.minesentry.command.MsCommandSource
import com.minesentry.minesentry.patch.MsServer
import com.minesentry.minesentry.platform.PlatformUtil.patch
import com.minesentry.minesentry.platform.PlatformUtil.toNative
import net.kyori.adventure.text.Component
import net.minecraft.commands.CommandSourceStack

//? if >=1.21.11
import net.minecraft.server.permissions.Permissions

class ModCommandSource(
    private val native: CommandSourceStack,
) : MsCommandSource {

    override val sender: MsCommandSender = ModCommandSender(native)

    override fun hasPermission(permission: String): Boolean {
        return sender.hasPermission(permission)
    }

}

private class ModCommandSender(
    private val native: CommandSourceStack,
) : MsCommandSender() {

    override val name: String = native.textName
    override val server: MsServer = native.server.patch()

    override fun hasPermission(permission: String): Boolean {
        // ponytail: MODローダー共通の文字列権限APIはないため、導入するまではOPレベル2へ集約する。
        //? if >=1.21.11
        return native.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)

        //? if <1.21.11
        //return native.hasPermission(2)
    }

    override fun sendMessage(component: Component) {
        native.sendSystemMessage(component.toNative())
    }

}
