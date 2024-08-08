package dev.u9g.mixin.client.events;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.u9g.events.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
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

    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;IIII)V"), method = "drawItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;III)V")
    private void skyplus$drawItem(DrawContext instance, LivingEntity entity, World world, ItemStack stack, int x, int y, int seed, int z, Operation<Void> original) {
        BeforeDrawItemEvent bie = new BeforeDrawItemEvent(instance, entity, world, stack, x, y, seed, z, true);
        BeforeDrawItemCallback.getEvent().invoker().invoke(bie);

        if (bie.getDrawOriginalItem()) {
            original.call(instance, entity, world, stack, x, y, seed, z);
        }

        AfterDrawItemCallback.getEvent().invoker().invoke(new AfterDrawItemEvent(instance, entity, world, stack, x, y, seed, z));
    }
}
