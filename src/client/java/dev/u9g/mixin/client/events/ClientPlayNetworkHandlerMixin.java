package dev.u9g.mixin.client.events;

import dev.u9g.events.*;
import kotlin.text.Regex;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Unique
    Regex r = new Regex("§.");

    @Inject(at = @At("HEAD"), method = "onGameMessage", cancellable = true)
    private void skyplus$onChat(GameMessageS2CPacket packet, CallbackInfo ci) {
        ChatMessageReceivedEvent cmre = new ChatMessageReceivedEvent(packet.content(), r.replace(packet.content().getString(), ""), false);
        ChatMessageReceivedCallback.getEvent().invoker().invoke(cmre);

        if (cmre.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "onTitle", cancellable = true)
    private void skyplus$onTitle(TitleS2CPacket packet, CallbackInfo ci) {
        TitleTextEvent tte = new TitleTextEvent(packet.getTitle(), r.replace(packet.getTitle().getString(), ""), false);
        TitleTextCallback.getEvent().invoker().invoke(tte);

        if (tte.component3()) {
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "onSubtitle", cancellable = true)
    private void skyplus$onSubtitle(SubtitleS2CPacket packet, CallbackInfo ci) {
        SubtitleTextEvent tte = new SubtitleTextEvent(packet.getSubtitle(), r.replace(packet.getSubtitle().getString(), ""), false);
        SubtitleTextCallback.getEvent().invoker().invoke(tte);

        if (tte.component3()) {
            ci.cancel();
        }
    }

    // packet.sound.getKeyOrValue().left().value.value.path.equals("entity.bat.takeoff")

    @Inject(at = @At("HEAD"), method = "onPlaySound", cancellable = true)
    private void skyplus$onSound(PlaySoundS2CPacket packet, CallbackInfo ci) {
        PlaySoundEvent pse = new PlaySoundEvent(packet.getSound().getKeyOrValue().left().map(x -> x.getValue().getPath()).orElse(null), false);
        PlaySoundCallback.getEvent().invoker().invoke(pse);

        if (pse.component2()) {
            ci.cancel();
        }
    }
}
