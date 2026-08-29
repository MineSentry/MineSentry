package com.minesentry.minesentry.mod.mixin;

//? if <1.20.2 {
/*import com.minesentry.minesentry.mod.MineSentryMod;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.UUID;

@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginPacketListenerImplMixin {

    @Shadow
    private GameProfile gameProfile;

    @Shadow
    public abstract void disconnect(Component component);

    @Inject(
            method = "handleAcceptedLogin",
            at = @At("HEAD"),
            cancellable = true
    )
    private void beforeAcceptPlayer(CallbackInfo ci) {
        UUID uuid = gameProfile.getId();
        String name = gameProfile.getName();

        Optional<Component> result = MineSentryMod.Companion.onPlayerLogin(uuid, name);
        if (result.isPresent()) {
            disconnect(result.get());
            ci.cancel();
        }
    }

}
*///? }