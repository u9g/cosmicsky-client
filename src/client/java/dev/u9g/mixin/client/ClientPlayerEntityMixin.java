package dev.u9g.mixin.client;

import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static dev.u9g.features.WaypointsKt.onDeath;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {
    @Shadow
    private boolean showsDeathScreen;

    @Inject(method = "showsDeathScreen", at = @At("HEAD"), cancellable = true)
    private void skyplus$showsDeathScreen(CallbackInfoReturnable<Boolean> cir) {
        if (this.showsDeathScreen) {
            onDeath();
        }
    }
}
