package dev.u9g.mixin.client.events;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.u9g.events.ItemStackCooldownProgressCallback;
import dev.u9g.events.ItemStackCooldownProgressEvent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DrawContext.class)
public class DrawContextMixin {
    @WrapOperation(method = "drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/ItemCooldownManager;getCooldownProgress(Lnet/minecraft/item/Item;F)F"))
    private float skyplus$handleBlockBreaking(ItemCooldownManager instance, Item item, float tickDelta, Operation<Float> original, @Local(argsOnly = true) ItemStack stack) {
        ItemStackCooldownProgressEvent iscpe = new ItemStackCooldownProgressEvent(stack);
        ItemStackCooldownProgressCallback.getEvent().invoker().invoke(iscpe);


        if (iscpe.component2()) {
            return iscpe.component3();
        }
        return original.call(instance, item, tickDelta);
    }
}
