package dev.u9g.mixin.client;

import dev.u9g.features.Settings;
import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Locale;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    @Inject(method = "normalize(Ljava/lang/String;)Ljava/lang/String;", at = @At("HEAD"), cancellable = true)
    private void skyplus$sendMessage(String chatText, CallbackInfoReturnable<String> cir) {
        if (Settings.INSTANCE.getEnableMod() &&
                Settings.INSTANCE.getReplaceFixToFixAll() &&
                chatText.toLowerCase(Locale.ROOT).startsWith("/fix")) {
            cir.setReturnValue("/fix all");
        }
    }
}
