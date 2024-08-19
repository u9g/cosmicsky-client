package dev.u9g.mixin.client;

import dev.u9g.WithMsgData;
import dev.u9g.features.MessageData;
import net.minecraft.client.gui.hud.ChatHudLine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ChatHudLine.Visible.class)
public class ChatHudLineVisibleMixin implements WithMsgData {
    @Unique
    MessageData skyplus$msgData = null;

    @Override
    public MessageData getMsgData() {
        return skyplus$msgData;
    }

    @Override
    public void setMsgData(MessageData msgData) {
        skyplus$msgData = msgData;
    }
}
