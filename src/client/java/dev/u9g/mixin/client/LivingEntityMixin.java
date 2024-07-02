package dev.u9g.mixin.client;

import dev.u9g.events.LivingEntityDeathCallback;
import dev.u9g.events.LivingEntityDeathEvent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
class LivingEntityMixin {
    @Inject(method = "onDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setPose(Lnet/minecraft/entity/EntityPose;)V"))
    private void skyplus$onDeath(DamageSource damageSource, CallbackInfo ci) {
        LivingEntityDeathCallback.getEvent().invoker().invoke(new LivingEntityDeathEvent((LivingEntity) (Object) this));
    }
}