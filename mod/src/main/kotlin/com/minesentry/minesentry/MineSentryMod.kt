package com.minesentry.minesentry

import com.minesentry.minesentry.command.MsCommand
import com.minesentry.minesentry.event.MsPlayerLoginEvent
import com.minesentry.minesentry.event.MsPlayerPostLoginEvent
import com.minesentry.minesentry.patch.MsPlayerIdentity
import com.minesentry.minesentry.platform.ModPlatform
import com.minesentry.minesentry.platform.MsPlayerImpl
import com.minesentry.minesentry.platform.PlatformUtil.patch
import com.minesentry.minesentry.platform.PlatformUtil.toNative
import com.mojang.brigadier.CommandDispatcher
import net.minecraft.commands.CommandSourceStack
import net.minecraft.server.MinecraftServer
import org.slf4j.LoggerFactory
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger

//? if fabric {
/*import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents

*///? }

//? if neoforge {
/*import net.neoforged.fml.common.Mod
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.RegisterCommandsEvent
import net.neoforged.neoforge.event.entity.player.PlayerNegotiationEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent
import net.neoforged.neoforge.event.server.ServerStartingEvent
import net.minecraft.server.level.ServerPlayer
*///? }

//? if neoforge {
/*@Mod(MineSentryMod.MOD_ID)
@EventBusSubscriber
*///? }
class MineSentryMod
//? if fabric
    //: ModInitializer
{

    companion object {
        const val MOD_ID = "minesentry"
        private val nativeLogger = LoggerFactory.getLogger("MineSentry")
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
                dataDir = server.serverDirectory.resolve("config/MineSentry/").toFile()
            )

            MineSentry.inject(platform)
            MineSentry.onEnable {}
        }

        private fun registerCommands(dispatcher: CommandDispatcher<CommandSourceStack>) {
            MsCommand.register(dispatcher) { it.patch() }
        }

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
            val loginEvent = MsPlayerLoginEvent(MsPlayerIdentity(event.profile.name, event.profile.id))
            MineSentry.onPlayerLogin(loginEvent)
            loginEvent.rejectionMessage?.let { event.connection.disconnect(it.toNative()) }
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
    /*override fun onInitialize() {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            registerCommands(dispatcher)
        }
        ServerLifecycleEvents.SERVER_STARTING.register { server ->
            inject(server)
        }
        ServerConfigurationConnectionEvents.BEFORE_CONFIGURE.register { handler, _ ->
            val loginEvent = MsPlayerLoginEvent(MsPlayerIdentity(handler.owner.name, handler.owner.id))
            MineSentry.onPlayerLogin(loginEvent)
            loginEvent.rejectionMessage?.let { handler.disconnect(it.toNative()) }
        }
        ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
            MineSentry.onPostLogin(MsPlayerPostLoginEvent(MsPlayerImpl(handler.player)))
        }
    }
    *///? }

}
