package dev.u9g.mixin.client;

import dev.u9g.features.Settings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Shadow
    @Nullable
    public ClientPlayerEntity player;

    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void skyplus$doAttack(CallbackInfoReturnable<Boolean> cir) {
        if (this.player == null) return;
        ItemStack item = this.player.getStackInHand(Hand.MAIN_HAND);
        if (Settings.INSTANCE.getEnableMod() && Settings.INSTANCE.getDisableSwingingAtLowDurability() && item.getMaxDamage() - item.getDamage() < 10 && item.getItem().isDamageable()) {
            cir.setReturnValue(false);
            this.player.sendMessage(Text.of("§c§l(!) §cYou cannot hit anymore! Item durability is below 10! §eYou can toggle this in /skyplussettings"));
        }
    }
}
