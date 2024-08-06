package dev.u9g.mixin.client.events;

import dev.u9g.events.PlaySoundCallback;
import dev.u9g.events.PlaySoundEvent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(World.class)
public class WorldMixin {
    @Inject(at = @At("HEAD"), method = "playSound(Lnet/minecraft/entity/player/PlayerEntity;DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;)V", cancellable = true)
    private void skyplus$onChat(PlayerEntity source, double x, double y, double z, SoundEvent sound, SoundCategory category, CallbackInfo ci) {
        PlaySoundEvent pse = new PlaySoundEvent(sound.getId().getPath(), false);
        PlaySoundCallback.getEvent().invoker().invoke(pse);

        if (pse.component2()) {
            ci.cancel();
        }
    }
}
