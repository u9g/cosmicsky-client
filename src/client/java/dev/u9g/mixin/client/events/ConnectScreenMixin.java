package dev.u9g.mixin.client.events;

import dev.u9g.events.ServerConnectCallback;
import net.minecraft.client.gui.screen.ConnectScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConnectScreen.class)
public class ConnectScreenMixin {
    @Inject(at = @At("RETURN"), method = "<init>")
    private void skyplus$init(CallbackInfo info) {
        ServerConnectCallback.getEvent().invoker().invoke();
    }
}
