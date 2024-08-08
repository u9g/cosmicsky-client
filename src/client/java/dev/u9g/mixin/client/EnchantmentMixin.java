package dev.u9g.mixin.client;

import dev.u9g.features.Settings;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public abstract class EnchantmentMixin {
    @Shadow
    protected abstract String getOrCreateTranslationKey();

    @Inject(at = @At("TAIL"), method = "getName", cancellable = true)
    private void skyplus$getName(int level, CallbackInfoReturnable<Text> cir) {
        String translationKey = this.getOrCreateTranslationKey();

        if (Settings.INSTANCE.getColorMaxEnchants() && ((translationKey.equals("enchantment.minecraft.protection") && level == 4) || (translationKey.equals("enchantment.minecraft.sharpness") && level == 5))) {
            var root = Text.of("");
            root.getSiblings().addAll(Text.of(cir.getReturnValue().getString()).getWithStyle(Style.EMPTY.withColor(Formatting.YELLOW)));
            cir.setReturnValue(root);
        }
    }
}
