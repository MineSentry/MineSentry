package com.minesentry.minesentry.platform

import com.minesentry.minesentry.patch.MsPlayer
import com.minesentry.minesentry.patch.MsServer
import com.minesentry.minesentry.platform.PlatformUtil.patch
import net.minecraft.server.MinecraftServer

class MsServerImpl(
    private val native: MinecraftServer
): MsServer {

    override fun getOnlinePlayers(): List<MsPlayer>
        = native.playerList.players.map { it.patch() }

}