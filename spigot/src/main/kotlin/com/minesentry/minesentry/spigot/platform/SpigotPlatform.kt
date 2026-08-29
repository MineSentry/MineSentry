package com.minesentry.minesentry.spigot.platform

import com.minesentry.minesentry.MsPlatform
import com.minesentry.minesentry.patch.MsServer
import com.minesentry.minesentry.spigot.platform.PlatformUtil.patch
import org.bukkit.Server
import java.io.File
import java.util.logging.Logger

class SpigotPlatform(
    nativeServer: Server,
    override val logger: Logger,
    override val dataDir: File,
): MsPlatform {

    override val server: MsServer = nativeServer.patch()

}