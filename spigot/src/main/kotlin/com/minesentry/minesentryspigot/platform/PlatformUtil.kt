package com.minesentry.minesentryspigot.platform

import com.minesentry.minesentry.command.MsCommandSource
import com.minesentry.minesentry.patch.MsServer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.chat.ComponentSerializer
import org.bukkit.Server
import org.bukkit.command.CommandSender

object PlatformUtil {

    private val gsonSerializer = GsonComponentSerializer.gson()

    /** モダンな[Component]をSpigotで使えるネイティブ形式に変換します。 */
    fun Component.toNative(): Array<BaseComponent> {
        val serialized = gsonSerializer.serialize(this)
        return ComponentSerializer.parse(serialized)
    }


    fun Server.patch(): MsServer = MsServerImpl(this)
    fun CommandSender.patch(): MsCommandSource = CommandSourceImpl(this)

}