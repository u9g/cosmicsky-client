package dev.u9g.mixin.client.events;

import com.llamalad7.mixinextras.sugar.Local;
import dev.u9g.events.SlotClickCallback;
import dev.u9g.events.SlotClickEvent;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerEventInteractionManagerMixin {
    @Inject(method = "clickSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"))
    private void onSlotClick(int syncId, int slotId, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci, @Local ScreenHandler handler) {
        if (0 <= slotId && slotId < handler.slots.size()) {
            SlotClickCallback.getEvent().invoker().invoke(new SlotClickEvent(
                    handler.getSlot(slotId),
                    handler.getSlot(slotId).getStack(),
                    button,
                    actionType
            ));
        }
    }
}
