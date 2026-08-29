package com.minesentry.minesentryvelocity.platform

import com.minesentry.minesentry.command.MsCommandSource
import com.minesentry.minesentry.patch.MsServer
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.ProxyServer

object PlatformUtil {

    fun ProxyServer.patch(): MsServer = MsServerImpl(this)
    fun CommandSource.patch(): MsCommandSource = CommandSourceImpl(this)

}
