package dev.u9g.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.u9g.features.ClueScrollManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemMixin {
    @Inject(at = @At("HEAD"), method = "isItemBarVisible", cancellable = true)
    private void skyplus$isItemBarVisible(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        try {
            var clueScrollInfo = ClueScrollManager.INSTANCE.getClueScrollInfo(stack);
            if (clueScrollInfo != null) {
                if (clueScrollInfo.getProgress() > 0) {
                    cir.setReturnValue(true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Inject(at = @At("HEAD"), method = "getItemBarStep", cancellable = true)
    private void skyplus$getItemBarStep(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        try {
            var clueScrollInfo = ClueScrollManager.INSTANCE.getClueScrollInfo(stack);
            if (clueScrollInfo != null) {
                double progress = clueScrollInfo.getProgress();
                if (progress > 0) {
                    cir.setReturnValue((int) (progress * 13));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @ModifyExpressionValue(at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F"), method = "getItemBarColor")
    private float skyplus$getItemBarColor(float original, @Local(argsOnly = true) ItemStack stack) {
        try {
            var clueScrollInfo = ClueScrollManager.INSTANCE.getClueScrollInfo(stack);
            if (clueScrollInfo != null) {
                double progress = clueScrollInfo.getProgress();
                if (progress > 0) {
                    return (float) progress;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return original;
    }
}
