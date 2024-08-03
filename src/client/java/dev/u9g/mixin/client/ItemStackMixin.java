package dev.u9g.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.u9g.features.ClueScrollManager;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @ModifyReturnValue(at = @At("RETURN"), method = "getTooltip")
    private List<Text> skyplus$getTooltip(List<Text> original) {
        ItemStack item = (ItemStack) (Object) this;

        ClueScrollManager.ClueScrollInfo csi = ClueScrollManager.INSTANCE.getClueScrollInfo(item, original);

        if (csi != null) {
            var percentDone = Math.min(csi.getProgress(), 1);
            var cutoff = (int) Math.floor(percentDone * csi.getLine().length());
            var before = csi.getLine().substring(0, cutoff);
            var after = csi.getLine().substring(cutoff);

            var root = Text.of("");

            root.getSiblings().addAll(
                    Text.of(before).getWithStyle(Style.EMPTY.withColor(Formatting.GREEN))
            );

            root.getSiblings().addAll(
                    Text.of(after).getWithStyle(Style.EMPTY.withColor(Formatting.RED))
            );

            original.set(csi.getLineNumber(), root);
        }

        return original;
    }
}
