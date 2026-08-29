package com.minesentry.minesentry.velocity.platform

import com.minesentry.minesentry.patch.MsPlayer
import com.minesentry.minesentry.patch.MsServer
import com.velocitypowered.api.proxy.ProxyServer

class MsServerImpl(
    private val proxy: ProxyServer
): MsServer {

    override fun getOnlinePlayers(): List<MsPlayer> {
        return proxy.allPlayers.map { MsPlayerImpl(it) }
    }

}
