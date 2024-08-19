package dev.u9g.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import dev.u9g.WithMsgData;
import dev.u9g.features.MessageData;
import dev.u9g.features.MessageHiderKt;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ChatHud.class)
class ChatHudMixin {
    @Shadow
    @Final
    private List<ChatHudLine.Visible> visibleMessages;

    @Inject(at = @At("HEAD"), method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V")
    private void skyplus$addMessage$HEAD(Text message, MessageSignatureData signature, int ticks, MessageIndicator indicator, boolean refresh, CallbackInfo ci, @Share("data") LocalRef<MessageData> argRef) {
        argRef.set(MessageData.create(message));
    }

    @ModifyArg(
            method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V",
            at = @At(value = "INVOKE", target = "Ljava/util/List;add(ILjava/lang/Object;)V"), index = 1)
    private Object skyplus$addMessage$INVOKE$ChatHudLine$init(Object element, @Share("data") LocalRef<MessageData> argRef) {
        ((WithMsgData) element).setMsgData(argRef.get());

        return element;
    }

    @WrapOperation(at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/ChatHud;visibleMessages:Ljava/util/List;"), method = "render")
    private List<ChatHudLine.Visible> skyplus$render$visibleMessages(ChatHud instance, Operation<List<ChatHudLine.Visible>> original) {
        return MessageHiderKt.getFilteredVisibleMessages();
    }

    @WrapOperation(at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/ChatHud;visibleMessages:Ljava/util/List;"), method = "scroll")
    private List<ChatHudLine.Visible> skyplus$scroll$visibleMessages(ChatHud instance, Operation<List<ChatHudLine.Visible>> original) {
        return MessageHiderKt.getFilteredVisibleMessages();
    }

    @WrapOperation(at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/ChatHud;visibleMessages:Ljava/util/List;"), method = "getTextStyleAt")
    private List<ChatHudLine.Visible> skyplus$getTextStyleAt$visibleMessages(ChatHud instance, Operation<List<ChatHudLine.Visible>> original) {
        return MessageHiderKt.getFilteredVisibleMessages();
    }

    @WrapOperation(at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/ChatHud;visibleMessages:Ljava/util/List;"), method = "getIndicatorAt")
    private List<ChatHudLine.Visible> skyplus$getIndicatorAt$visibleMessages(ChatHud instance, Operation<List<ChatHudLine.Visible>> original) {
        return MessageHiderKt.getFilteredVisibleMessages();
    }

    @WrapOperation(at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/ChatHud;visibleMessages:Ljava/util/List;"), method = "getMessageIndex")
    private List<ChatHudLine.Visible> skyplus$getMessageIndex$visibleMessages(ChatHud instance, Operation<List<ChatHudLine.Visible>> original) {
        return MessageHiderKt.getFilteredVisibleMessages();
    }

    @WrapOperation(at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/ChatHud;visibleMessages:Ljava/util/List;"), method = "getMessageLineIndex")
    private List<ChatHudLine.Visible> skyplus$getMessageLineIndex$visibleMessages(ChatHud instance, Operation<List<ChatHudLine.Visible>> original) {
        return MessageHiderKt.getFilteredVisibleMessages();
    }

    @Unique
    private void refilterVisibleMessages() {
        var newFilteredVisibleMessages = new ArrayList<ChatHudLine.Visible>();
        for (var visibleMessage : visibleMessages) {
            if (((WithMsgData) (Object) visibleMessage).getMsgData().shouldHide()) {
                continue;
            }
            newFilteredVisibleMessages.add(visibleMessage);
        }
        MessageHiderKt.setFilteredVisibleMessages(newFilteredVisibleMessages);
    }

    @Inject(at = @At(value = "TAIL"), method = "clear")
    private void skyplus$clear(boolean clearHistory, CallbackInfo ci) {
        refilterVisibleMessages();
    }

    @Inject(at = @At(value = "TAIL"), method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V")
    private void skyplus$addMessage(Text message, MessageSignatureData signature, int ticks, MessageIndicator indicator, boolean refresh, CallbackInfo ci) {
        refilterVisibleMessages();
    }

    @Inject(at = @At(value = "TAIL"), method = "refresh")
    private void skyplus$refresh(CallbackInfo ci) {
        refilterVisibleMessages();
    }
}