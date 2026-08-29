package com.minesentry.minesentry.velocity.platform

import com.minesentry.minesentry.MineSentry
import com.minesentry.minesentry.patch.MsPlayer
import com.minesentry.minesentry.patch.MsServer
import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.Component
import java.util.UUID

class MsPlayerImpl(
    private val player: Player
): MsPlayer() {

    override val name: String = player.username
    override val uuid: UUID = player.uniqueId
    override val server: MsServer
        get() = MineSentry.platform.server

    override fun hasPermission(permission: String): Boolean
        = player.hasPermission(permission)

    override fun sendMessage(component: Component) {
        player.sendMessage(component)
    }

}
