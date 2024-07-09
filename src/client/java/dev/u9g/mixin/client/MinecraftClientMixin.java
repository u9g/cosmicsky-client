package dev.u9g.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.u9g.features.Settings;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
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

    @Shadow
    @Nullable
    public ClientWorld world;

    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void skyplus$doAttack(CallbackInfoReturnable<Boolean> cir) {
        if (this.player == null) return;
        ItemStack item = this.player.getStackInHand(Hand.MAIN_HAND);
        if (Settings.INSTANCE.getEnableMod() && Settings.INSTANCE.getDisableSwingingAtLowDurability() && item.getMaxDamage() - item.getDamage() < 10 && item.getItem().isDamageable()) {
            cir.setReturnValue(false);
            this.player.sendMessage(Text.of("§c§l(!) §cYou cannot hit anymore! Item durability is below 10! §eYou can toggle this in /skyplussettings"));
        }
    }

    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;updateBlockBreakingProgress(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)Z"), method = "handleBlockBreaking")
    private boolean skyplus$handleBlockBreaking(ClientPlayerInteractionManager instance, BlockPos pos, Direction direction, Operation<Boolean> original) {
        if (this.player == null || this.world == null) return original.call(instance, pos, direction);

        if (Settings.INSTANCE.getEnableMod() && Settings.INSTANCE.getShouldAllowBreakingGlass()) {
            BlockState bs = this.world.getBlockState(pos);
            if (!bs.isIn(BlockTags.IMPERMEABLE) && original.call(instance, pos, direction)) {
                this.player.sendMessage(Text.of("§c§l(!) §cYou cannot break glass! Glass break is disabled! §eYou can toggle this in /skyplussettings"));
                return true;
            }
            return false;
        } else {
            return original.call(instance, pos, direction);
        }
    }
}