package com.minesentry.minesentryspigot.platform

import com.minesentry.minesentry.patch.MsPlayer
import com.minesentry.minesentry.patch.MsServer
import org.bukkit.Server

class MsServerImpl(
    private val server: Server
): MsServer {

    override fun getOnlinePlayers(): List<MsPlayer> {
        return server.onlinePlayers.map { MsPlayerImpl(it) }
    }

}