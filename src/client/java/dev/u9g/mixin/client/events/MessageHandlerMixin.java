package dev.u9g.mixin.client.events;

import dev.u9g.events.ChatMessageReceivedCallback;
import dev.u9g.events.ChatMessageReceivedEvent;
import dev.u9g.util.UtilKt;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MessageHandler.class)
class MessageHandlerMixin {
    @Inject(at = @At("HEAD"), method = "onGameMessage", cancellable = true)
    private void skyplus$onChat(Text message, boolean overlay, CallbackInfo ci) {
        ChatMessageReceivedEvent cmre = new ChatMessageReceivedEvent(message, UtilKt.getSectionCharColor().replace(message.getString(), ""), false);
        ChatMessageReceivedCallback.getEvent().invoker().invoke(cmre);

        if (cmre.isCancelled()) {
            ci.cancel();
        }
    }
}