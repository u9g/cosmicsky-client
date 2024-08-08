package dev.u9g.mixin.client.events;

import dev.u9g.events.GetTooltipCallback;
import dev.u9g.events.GetTooltipEvent;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(at = @At("TAIL"), method = "getTooltip")
    private void skyplus$getTooltip(@Nullable PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir) {
        ItemStack item = (ItemStack) (Object) this;

        GetTooltipCallback.getEvent().invoker().invoke(new GetTooltipEvent(cir.getReturnValue(), item));
    }
}