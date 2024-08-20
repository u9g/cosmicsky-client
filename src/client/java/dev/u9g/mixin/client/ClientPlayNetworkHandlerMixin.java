package dev.u9g.mixin.client;

import dev.u9g.events.MsgOrCmd;
import dev.u9g.events.UserSendMsgOrCmdCallback;
import dev.u9g.events.UserSendMsgOrCmdEvent;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(at = @At("HEAD"), method = "sendChatCommand", cancellable = true)
    private void skyplus$sendChatCommand(String command, CallbackInfo ci) {
        var event = new UserSendMsgOrCmdEvent(command, MsgOrCmd.CMD, false);
        UserSendMsgOrCmdCallback.getEvent().invoker().invoke(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "sendChatMessage", cancellable = true)
    private void skyplus$sendChatMessage(String content, CallbackInfo ci) {
        var event = new UserSendMsgOrCmdEvent(content, MsgOrCmd.MSG, false);
        UserSendMsgOrCmdCallback.getEvent().invoker().invoke(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
