package com.minesentry.minesentry.spigot.platform

import com.minesentry.minesentry.patch.MsPlayer
import com.minesentry.minesentry.patch.MsServer
import com.minesentry.minesentry.spigot.platform.PlatformUtil.patch
import com.minesentry.minesentry.spigot.platform.PlatformUtil.toNative
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import java.util.UUID

class MsPlayerImpl(
    private val player: Player
): MsPlayer() {

    override val name: String = player.name
    override val uuid: UUID = player.uniqueId
    override val server: MsServer = player.server.patch()

    override fun hasPermission(permission: String): Boolean
        = player.hasPermission(permission)

    override fun sendMessage(component: Component) {
        player.spigot().sendMessage(*component.toNative())
    }

}