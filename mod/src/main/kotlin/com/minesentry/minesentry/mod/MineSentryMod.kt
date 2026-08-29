package com.minesentry.minesentry.mod

import com.minesentry.minesentry.MineSentry
import com.minesentry.minesentry.MsPlatform
import com.minesentry.minesentry.command.MsCommand
import com.minesentry.minesentry.event.MsPlayerLoginEvent
import com.minesentry.minesentry.event.MsPlayerPostLoginEvent
import com.minesentry.minesentry.patch.MsPlayerIdentity
import com.minesentry.minesentry.mod.platform.ModPlatform
import com.minesentry.minesentry.mod.platform.MsPlayerImpl
import com.minesentry.minesentry.mod.platform.PlatformUtil.patch
import com.minesentry.minesentry.mod.platform.PlatformUtil.toNative
import com.mojang.brigadier.CommandDispatcher
import net.minecraft.commands.CommandSourceStack
import net.minecraft.server.MinecraftServer
import net.minecraft.network.chat.Component
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger
import java.util.Optional
import java.util.UUID

/** 1.18.2 前後でロガーが変わっている */
//? if >=1.18.2 {
import org.slf4j.LoggerFactory
//? } else {
/*import org.apache.logging.log4j.LogManager
*///? }

//? if fabric {
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
//? }

/** 1.19から FabricAPI が v2 になった */
//? if fabric && >=1.19 {
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
//? } else if fabric {
/*import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
*///? }

/** 1.20.2から ServerConfigurationConnectionEvents が実装された */
//? if fabric && >=1.20.2
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents

//? if neoforge {
/*import net.neoforged.fml.common.Mod
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.RegisterCommandsEvent
import net.neoforged.neoforge.event.entity.player.PlayerNegotiationEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent
import net.neoforged.neoforge.event.server.ServerStartingEvent
import net.minecraft.server.level.ServerPlayer
*///? }

/** 1.21以降は EventBusSubscriber が独立した。（以前は Mod.EventBusSubscriber ） */
//? if neoforge && >=1.20.6
//import net.neoforged.fml.common.EventBusSubscriber


//? if neoforge
//@Mod(MineSentryMod.MOD_ID)
//? if neoforge && >=1.20.6
//@EventBusSubscriber
//? if neoforge && <1.20.6
//@Mod.EventBusSubscriber
class MineSentryMod
//? if fabric
    : ModInitializer
{

    companion object {
        const val MOD_ID = "minesentry"
        private val nativeLogger =
            //? if >=1.18.2
            LoggerFactory.getLogger("MineSentry")
            //? if <1.18.2
            //LogManager.getLogger("MineSentry")
        val logger: Logger = object : Logger("MineSentry", null) {
            override fun log(record: LogRecord) {
                when {
                    record.level.intValue() >= Level.SEVERE.intValue() -> nativeLogger.error(record.message, record.thrown)
                    record.level.intValue() >= Level.WARNING.intValue() -> nativeLogger.warn(record.message, record.thrown)
                    record.level.intValue() >= Level.INFO.intValue() -> nativeLogger.info(record.message, record.thrown)
                    else -> nativeLogger.debug(record.message, record.thrown)
                }
            }
        }.apply { level = Level.ALL }

        lateinit var platform: MsPlatform private set

        /** 共通 */
        private fun inject(server: MinecraftServer) {
            platform = ModPlatform(
                nativeServer = server,
                logger = logger,
                dataDir = server.serverDirectory.resolve("config/MineSentry/")
                //? if >=1.21
                    .toFile()
            )

            MineSentry.inject(platform)
            MineSentry.onEnable {}
        }

        private fun registerCommands(dispatcher: CommandDispatcher<CommandSourceStack>) {
            MsCommand.register(dispatcher) { it.patch() }
        }

        /**
         * プレイヤーがログインした際
         * @return 参加を拒否する場合は Component、許可する場合は Optional.empty() を返します。
         */
        fun onPlayerLogin(uuid: UUID, mcid: String): Optional<Component> {
            val loginEvent = MsPlayerLoginEvent(MsPlayerIdentity(mcid, uuid))
            MineSentry.onPlayerLogin(loginEvent)

            loginEvent.rejectionMessage?.let { return Optional.of(it.toNative()) }

            val test = Optional.empty<Component>()
            if (test.isPresent) test.get()

            return Optional.empty()
        }

        /** NeoForge向けの実装 */
        //? if neoforge {
        /*@SubscribeEvent
        @JvmStatic
        fun onRegisterCommands(event: RegisterCommandsEvent) {
            registerCommands(event.dispatcher)
        }

        @SubscribeEvent
        @JvmStatic
        fun onServerStarting(event: ServerStartingEvent) {
            inject(event.server)
            MineSentry.onEnable {}
        }

        @SubscribeEvent
        @JvmStatic
        fun onPlayerLogin(event: PlayerNegotiationEvent) {
            val result = onPlayerLogin(event.profile.id, event.profile.name)
            if (result.isPresent) event.connection.disconnect(result.get())
        }

        @SubscribeEvent
        @JvmStatic
        fun onPostLogin(event: PlayerLoggedInEvent) {
            val player = event.entity as ServerPlayer
            MineSentry.onPostLogin(MsPlayerPostLoginEvent(MsPlayerImpl(player)))
        }
        *///? }

    }

    /** Fabric 実装 */
    //? if fabric {
    override fun onInitialize() {
        /** コマンド登録 - FabricAPIが v2 か v1 かによる違い */
        //? if >=1.19 {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            registerCommands(dispatcher)
        }
        //? } else {
        /*CommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            registerCommands(dispatcher)
        }
        *///? }

        /** ログイン時の処理 - 1.20.2より前は [ServerLoginPacketListenerImplMixin]から onPlayerLogin が呼び出されます */
        //? if >=1.20.2 {
        ServerConfigurationConnectionEvents.BEFORE_CONFIGURE.register { handler, _ ->
            val loginEvent = MsPlayerLoginEvent(MsPlayerIdentity(handler.owner.name, handler.owner.id))
            MineSentry.onPlayerLogin(loginEvent)
            loginEvent.rejectionMessage?.let { handler.disconnect(it.toNative()) }
        }
        //? }

        ServerLifecycleEvents.SERVER_STARTING.register { server ->
            inject(server)
        }
        ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
            MineSentry.onPostLogin(MsPlayerPostLoginEvent(MsPlayerImpl(handler.player)))
        }
    }
    //? }

}
