package dev.u9g.mixin.client;

import dev.u9g.events.OverlayTextCallback;
import dev.u9g.events.OverlayTextEvent;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Inject(method = "setOverlayMessage", at = @At("HEAD"))
    private void skyplus$setOverlayMessage(Text message, boolean tinted, CallbackInfo ci) {
        OverlayTextCallback.getEvent().invoker().invoke(new OverlayTextEvent(message, message.getString().trim()));
    }
}
