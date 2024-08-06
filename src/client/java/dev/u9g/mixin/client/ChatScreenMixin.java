package dev.u9g.mixin.client;

import dev.u9g.features.Settings;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Locale;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen {
    @Shadow
    ChatInputSuggestor chatInputSuggestor;

    protected ChatScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At("HEAD"), method = "keyPressed", cancellable = true)
    private void skyplus$keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (Settings.INSTANCE.getEnableMod() && Settings.INSTANCE.getSingleEscapeClosesChat() && keyCode == GLFW.GLFW_KEY_ESCAPE && this.chatInputSuggestor.keyPressed(keyCode, scanCode, modifiers) && this.client != null) {
            this.client.setScreen(null);
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "normalize(Ljava/lang/String;)Ljava/lang/String;", at = @At("HEAD"), cancellable = true)
    private void skyplus$sendMessage(String chatText, CallbackInfoReturnable<String> cir) {
        if (Settings.INSTANCE.getEnableMod()) {
            if (Settings.INSTANCE.getReplaceFixToFixAll() &&
                    chatText.toLowerCase(Locale.ROOT).startsWith("/fix") && !chatText.equals("/fixstuckpingonmyscreen")) {
                cir.setReturnValue("/fix all");
            } else if (Settings.INSTANCE.getTAlias() && chatText.toLowerCase(Locale.ROOT).equals("/t")) {
                cir.setReturnValue("/is tp Momica");
            } else if (Settings.INSTANCE.getZAlias() && chatText.toLowerCase(Locale.ROOT).equals("/z")) {
                cir.setReturnValue("/is tp Zercon");
            } else if (Settings.INSTANCE.getRedirectChatAToChatAlly() && (
                    chatText.toLowerCase(Locale.ROOT).equals("/c a") ||
                            chatText.toLowerCase(Locale.ROOT).equals("/chat a") ||
                            chatText.toLowerCase(Locale.ROOT).equals("/c alliance"))) {
                cir.setReturnValue("/c ally");
            }
        }
    }
}
