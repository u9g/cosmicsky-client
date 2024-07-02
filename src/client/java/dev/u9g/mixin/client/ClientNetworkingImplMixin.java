package dev.u9g.mixin.client;

import net.fabricmc.fabric.impl.networking.client.ClientNetworkingImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientNetworkingImpl.class, remap = false)
public class ClientNetworkingImplMixin {
    @Inject(at = @At("HEAD"), method = "setClientConfigurationAddon", cancellable = true)
    private static void skyplus$setClientConfigurationAddon(CallbackInfo info) {
        // This code is injected into the start of Minecraft.run()V
        info.cancel();
    }


}