package com.minesentry.minesentry.mod.platform

import com.minesentry.minesentry.mod.MineSentryMod
import com.minesentry.minesentry.patch.MsPlayer
import com.minesentry.minesentry.patch.MsServer
import com.minesentry.minesentry.mod.platform.PlatformUtil.toNative
import net.kyori.adventure.text.Component
import net.minecraft.server.level.ServerPlayer
import java.util.UUID

//? if >=1.21.11
import net.minecraft.server.permissions.Permissions

class MsPlayerImpl(
    private val native: ServerPlayer,
): MsPlayer() {

    override val name: String = native.scoreboardName
    override val uuid: UUID = native.uuid
    override val server: MsServer = MineSentryMod.platform.server

    override fun hasPermission(permission: String): Boolean {
        // ponytail: MODローダー共通の文字列権限APIはないため、導入するまではOPレベル2へ集約する。
        //? if >=1.21.11
        return native.createCommandSourceStack().permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)

        //? if <1.21.11
        //return native.createCommandSourceStack().hasPermission(2)
    }

    override fun sendMessage(component: Component) {
        /** 1.19 から sendSystemMessage となった */
        //? if >=1.19 {
        native.sendSystemMessage(component.toNative())
        //? } else {
        /*native.displayClientMessage(component.toNative(), true)
        *///? }
    }
}
