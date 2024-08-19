package dev.u9g.mixin.client;

import dev.u9g.features.GoGo;
import dev.u9g.features.Settings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow
    @Nullable
    public ClientPlayerEntity player;
//
//    @Shadow
//    @Nullable
//    public ClientWorld world;

    @Shadow
    protected abstract void doItemUse();

    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void skyplus$doAttack(CallbackInfoReturnable<Boolean> cir) {
        if (this.player == null) return;

        ItemStack item = this.player.getStackInHand(Hand.MAIN_HAND);

        if (item.getItem() == Items.PLAYER_HEAD) {
            if (!GoGo.INSTANCE.getHasUsed()) {
                this.doItemUse();
                GoGo.INSTANCE.setHasUsed(true);
            } else {
                player.getInventory().selectedSlot = 0;
                GoGo.INSTANCE.setHasUsed(false);
            }
            cir.setReturnValue(false);
            return;
        }

//        if (GoGo.INSTANCE.getNextI() != null) {
//            int i = GoGo.INSTANCE.getNextI();
//            SkyplusClientKt.getMc().interactionManager.clickSlot(0, 36 + 1 + i, 0, SlotActionType.PICKUP, player);
//            SkyplusClientKt.getMc().interactionManager.clickSlot(0, 28 + i, 0, SlotActionType.PICKUP, player);
//            SkyplusClientKt.getMc().interactionManager.clickSlot(0, 36 + 1 + i, 0, SlotActionType.PICKUP, player);
//            if (i < 8) {
//                GoGo.INSTANCE.setNextI(i + 1);
//            } else {
//                GoGo.INSTANCE.setNextI(null);
//            }
//            cir.setReturnValue(false);
//            return;
//        }

        if (GoGo.INSTANCE.getGoNext()) {
            int next = GoGo.INSTANCE.findNext();
            if (next != -1) {
                player.getInventory().selectedSlot = next;
            }/* else {
                GoGo.INSTANCE.setNextI(0);
            }*/
            GoGo.INSTANCE.setGoNext(false);
            cir.setReturnValue(false);
            return;
        }

        if (Settings.INSTANCE.getEnableMod() && Settings.INSTANCE.getDisableSwingingAtLowDurability() && item.getMaxDamage() - item.getDamage() < 10 && item.getItem().isDamageable()) {
            cir.setReturnValue(false);
            this.player.sendMessage(Text.of("§c§l(!) §cYou cannot hit anymore! Item durability is below 10! §eYou can toggle this in /skyplussettings"));
        }
    }

    @Inject(method = "doItemUse", at = @At("HEAD"), cancellable = true)
    private void skyplus$doItemUse(CallbackInfo ci) {
        if (this.player == null) return;
        ItemStack item = this.player.getStackInHand(Hand.MAIN_HAND);
        if (Settings.INSTANCE.getEnableMod() && Settings.INSTANCE.getDisableSwingingAtLowDurability() && item.getMaxDamage() - item.getDamage() < 10 && item.getItem().isDamageable()) {
            ci.cancel();
            this.player.sendMessage(Text.of("§c§l(!) §cYou cannot hit anymore! Item durability is below 10! §eYou can toggle this in /skyplussettings"));
        }
    }

    @Inject(method = "handleBlockBreaking", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/hit/BlockHitResult;getSide()Lnet/minecraft/util/math/Direction;"), cancellable = true)
    private void skyplus$handleBlockBreaking(boolean breaking, CallbackInfo ci) {
        if (this.player == null) return;
        ItemStack item = this.player.getStackInHand(Hand.MAIN_HAND);
        if (Settings.INSTANCE.getEnableMod() && Settings.INSTANCE.getDisableSwingingAtLowDurability() && item.getMaxDamage() - item.getDamage() < 10 && item.getItem().isDamageable()) {
            ci.cancel();
            this.player.sendMessage(Text.of("§c§l(!) §cYou cannot hit anymore! Item durability is below 10! §eYou can toggle this in /skyplussettings"));
        }
    }

//    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;updateBlockBreakingProgress(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)Z"), method = "handleBlockBreaking")
//    private boolean skyplus$handleBlockBreaking(ClientPlayerInteractionManager instance, BlockPos pos, Direction direction, Operation<Boolean> original) {
//        if (this.player == null || this.world == null) return original.call(instance, pos, direction);
//
//        if (Settings.INSTANCE.getEnableMod() && !Settings.INSTANCE.getShouldAllowBreakingGlass()) {
//            BlockState bs = this.world.getBlockState(pos);
//            if (!bs.isIn(BlockTags.IMPERMEABLE) && original.call(instance, pos, direction)) {
//                return true;
//            }
//            this.player.sendMessage(Text.of("§c§l(!) §cYou cannot break glass! Glass break is disabled! §eYou can toggle this in /skyplussettings"));
//            return false;
//        } else {
//            return original.call(instance, pos, direction);
//        }
//    }
}