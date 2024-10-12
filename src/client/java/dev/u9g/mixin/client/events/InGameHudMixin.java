package dev.u9g.mixin.client.events;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.u9g.events.*;
import dev.u9g.util.UtilKt;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @WrapOperation(method = "renderExperienceBar", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerEntity;experienceLevel:I"))
    private int skyplus$renderExperienceBar$experienceLevel(ClientPlayerEntity instance, Operation<Integer> original) {
        var event = new ExperienceBarOverlayEvent(original.call(instance));
        ExperienceBarOverlayCallback.getEvent().invoker().invoke(event);
        return event.getLvl();
    }

    @WrapOperation(method = "renderExperienceBar", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerEntity;experienceProgress:F"))
    private float skyplus$renderExperienceBar$experienceProgress(ClientPlayerEntity instance, Operation<Float> original) {
        var event = new ExperienceProgressOverlayEvent(original.call(instance));
        ExperienceProgressOverlayCallback.getEvent().invoker().invoke(event);
        return event.getProgress();
    }

    @Inject(method = "setOverlayMessage", at = @At("HEAD"))
    private void skyplus$setOverlayMessage(Text message, boolean tinted, CallbackInfo ci) {
        OverlayTextCallback.getEvent().invoker().invoke(new OverlayTextEvent(message, message.getString().trim()));
    }

    @Inject(method = "setTitle", at = @At("HEAD"), cancellable = true)
    private void skyplus$setTitle(Text title, CallbackInfo ci) {
        TitleTextEvent tte = new TitleTextEvent(title, UtilKt.getSectionCharColor().replace(title.getString(), ""), false);
        TitleTextCallback.getEvent().invoker().invoke(tte);

        if (tte.component3()) {
            ci.cancel();
        }
    }

    @Inject(method = "setSubtitle", at = @At("HEAD"), cancellable = true)
    private void skyplus$setSubtitle(Text subtitle, CallbackInfo ci) {
        SubtitleTextEvent tte = new SubtitleTextEvent(subtitle, UtilKt.getSectionCharColor().replace(subtitle.getString(), ""), false);
        SubtitleTextCallback.getEvent().invoker().invoke(tte);

        if (tte.component3()) {
            ci.cancel();
        }
    }
}
