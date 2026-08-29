package com.minesentry.minesentry.platform

import com.minesentry.minesentry.command.MsCommandSource
import com.minesentry.minesentry.patch.MsPlayer
import com.minesentry.minesentry.patch.MsServer
import com.mojang.serialization.JsonOps
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.network.chat.Component as NativeComponent

object PlatformUtil {

    private val gsonSerializer = GsonComponentSerializer.gson()

    fun ServerPlayer.patch(): MsPlayer = MsPlayerImpl(this)
    fun MinecraftServer.patch(): MsServer = MsServerImpl(this)
    fun CommandSourceStack.patch(): MsCommandSource = ModCommandSource(this)

    fun Component.toNative(): NativeComponent {
        val json = gsonSerializer.serializeToTree(this)
        return ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, json).orThrow
    }

}