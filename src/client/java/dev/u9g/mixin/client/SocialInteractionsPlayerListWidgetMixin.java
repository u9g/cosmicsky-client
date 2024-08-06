package dev.u9g.mixin.client;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsPlayerListEntry;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsPlayerListWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;
import java.util.UUID;

@Mixin(SocialInteractionsPlayerListWidget.class)
class SocialInteractionsPlayerListWidgetMixin {
    @WrapWithCondition(method = "setPlayers(Ljava/util/Collection;Ljava/util/Map;)V", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
    private <K, V> boolean skyplus$setPlayers(Map<UUID, SocialInteractionsPlayerListEntry> instance, K k, V v) {
        return !(v instanceof SocialInteractionsPlayerListEntry vz && vz.getName().startsWith(" slot_"));
    }
}