package dev.u9g.mixin.client;

import dev.u9g.events.ChatMessageReceivedCallback;
import dev.u9g.events.MessageReceivedEvent;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {
    @Inject(at = @At("HEAD"), method = "logChatMessage", locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void skyplus$write(Text message, MessageIndicator indicator, CallbackInfo ci) {
        ChatMessageReceivedCallback.getEvent().invoker().invoke(new MessageReceivedEvent(message, message.getString().trim()));
    }
}
