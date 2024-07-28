package dev.u9g.mixin.client.events;

import dev.u9g.events.PlaySoundCallback;
import dev.u9g.events.PlaySoundEvent;
import dev.u9g.events.TitleTextCallback;
import dev.u9g.events.TitleTextEvent;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(at = @At("HEAD"), method = "onTitle", cancellable = true)
    private void skyplus$onTitle(TitleS2CPacket packet, CallbackInfo ci) {
        TitleTextEvent tte = new TitleTextEvent(packet.getTitle(), packet.getTitle().getString(), false);
        TitleTextCallback.getEvent().invoker().invoke(tte);

        if (tte.component3()) {
            ci.cancel();
        }
    }

    // packet.sound.getKeyOrValue().left().value.value.path.equals("entity.bat.takeoff")

    @Inject(at = @At("HEAD"), method = "onPlaySound", cancellable = true)
    private void skyplus$onTitle(PlaySoundS2CPacket packet, CallbackInfo ci) {
        PlaySoundEvent pse = new PlaySoundEvent(packet.getSound().getKeyOrValue().left().map(x -> x.getValue().getPath()).orElse(null), false);
        PlaySoundCallback.getEvent().invoker().invoke(pse);

        if (pse.component2()) {
            ci.cancel();
        }
    }
}
