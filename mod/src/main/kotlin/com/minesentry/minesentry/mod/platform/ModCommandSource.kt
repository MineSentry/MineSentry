package com.minesentry.minesentry.mod.platform

import com.minesentry.minesentry.command.MsCommandSender
import com.minesentry.minesentry.command.MsCommandSource
import com.minesentry.minesentry.patch.MsServer
import com.minesentry.minesentry.mod.platform.PlatformUtil.patch
import com.minesentry.minesentry.mod.platform.PlatformUtil.toNative
import net.kyori.adventure.text.Component
import net.minecraft.commands.CommandSourceStack

//? if >=1.21.11
import net.minecraft.server.permissions.Permissions

//? if <1.19 {
/*import com.minesentry.minesentry.mod.mixin.CommandSourceStackAccessor
import net.minecraft.Util
*///? }

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
        /** 1.19 から sendSystemMessage が使用できるようになった。（以前は sendSuccess, sendFailure のみ） */
        //? if >=1.19 {
        native.sendSystemMessage(component.toNative())
        //? } else {
        /*val source = (native as CommandSourceStackAccessor).getSource()
        source.sendMessage(component.toNative(), Util.NIL_UUID)
        *///? }
    }

}
