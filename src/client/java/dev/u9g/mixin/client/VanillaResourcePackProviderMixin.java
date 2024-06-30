package dev.u9g.mixin.client;

import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.VanillaResourcePackProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(VanillaResourcePackProvider.class)
public class VanillaResourcePackProviderMixin {
    @Inject(method = "register", at = @At("RETURN"))
    private void addBuiltinResourcePacks(Consumer<ResourcePackProfile> consumer, CallbackInfo ci) {
        // Register mod and built-in resource packs after the vanilla built-in resource packs are registered.
//        if ((Object) this instanceof DefaultClientResourcePackProvider) {
//            consumer.accept(makeResourcePackProfile("Momica's Pack", Text.literal("Momica's Pack")));
//        }
    }
}
