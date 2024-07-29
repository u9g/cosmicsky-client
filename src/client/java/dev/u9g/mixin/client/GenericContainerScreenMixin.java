package dev.u9g.mixin.client;

import dev.u9g.features.Settings;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GenericContainerScreen.class)
public class GenericContainerScreenMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void skyplus$render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (Settings.INSTANCE.getEnableMod()) {
            if (((Screen) (Object) this).getTitle().getString().equals("Island Members")) {
                ci.cancel();
            }
        }
    }
}
