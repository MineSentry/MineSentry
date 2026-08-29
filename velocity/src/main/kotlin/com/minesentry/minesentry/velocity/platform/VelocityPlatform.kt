package com.minesentry.minesentry.velocity.platform

import com.minesentry.minesentry.MsPlatform
import com.minesentry.minesentry.patch.MsServer
import com.minesentry.minesentry.velocity.platform.PlatformUtil.patch
import com.velocitypowered.api.proxy.ProxyServer
import java.io.File
import java.util.logging.Logger

class VelocityPlatform(
    nativeServer: ProxyServer,
    override val logger: Logger,
    override val dataDir: File,
): MsPlatform {

    override val server: MsServer = nativeServer.patch()

}
