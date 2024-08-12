package dev.u9g.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.u9g.features.Settings;
import dev.u9g.features.TeamFocus;
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Locale;

@Mixin(HeadFeatureRenderer.class)
public class HeadFeatureRendererMixin {
    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getEquippedStack(Lnet/minecraft/entity/EquipmentSlot;)Lnet/minecraft/item/ItemStack;"), method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V")
    private ItemStack skyplus$getEquippedStack(LivingEntity instance, EquipmentSlot equipmentSlot, Operation<ItemStack> original) {
        if (instance instanceof PlayerEntity pe && pe.getGameProfile().getName().toLowerCase(Locale.ROOT).equals(Settings.INSTANCE.getFocusedPlayerUsername().toLowerCase(Locale.ROOT))) {
            return TeamFocus.INSTANCE.getItem();
        }
        return original.call(instance, equipmentSlot);
    }
}
