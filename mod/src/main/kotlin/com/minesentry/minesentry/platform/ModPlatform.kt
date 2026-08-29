package com.minesentry.minesentry.platform

import com.minesentry.minesentry.MsPlatform
import com.minesentry.minesentry.patch.MsServer
import com.minesentry.minesentry.platform.PlatformUtil.patch
import net.minecraft.server.MinecraftServer
import java.io.File
import java.util.logging.Logger

class ModPlatform(
    nativeServer: MinecraftServer,
    override val logger: Logger,
    override val dataDir: File,
): MsPlatform {

    override val server: MsServer = nativeServer.patch()

}