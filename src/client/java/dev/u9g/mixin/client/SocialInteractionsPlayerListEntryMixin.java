package dev.u9g.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.u9g.features.Settings;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsPlayerListEntry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SocialInteractionsPlayerListEntry.class)
public class SocialInteractionsPlayerListEntryMixin {
    @Shadow
    @Final
    private String name;

    @Shadow
    @Final
    public static int DARK_GRAY_COLOR;

    @Shadow
    @Final
    public static int GRAY_COLOR;

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V"))
    private void skyplus$setPlayers(DrawContext instance, int x1, int y1, int x2, int y2, int color, Operation<Void> original) {
        original.call(instance, x1, y1, x2, y2, Settings.INSTANCE.getTeamMembers().contains(this.name) ? DARK_GRAY_COLOR : GRAY_COLOR);
    }
}
