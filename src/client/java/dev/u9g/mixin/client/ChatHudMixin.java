package dev.u9g.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.u9g.features.CooldownManager;
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.ClickEvent;
import org.apache.commons.compress.utils.Lists;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(ChatHud.class)
public class ChatHudMixin {
    @WrapOperation(at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/ChatHud;visibleMessages:Ljava/util/List;"), method = "getIndicatorAt")
    private List<ChatHudLine.Visible> skyplus$getIndicatorAt(ChatHud instance, Operation<List<ChatHudLine.Visible>> original) {
        return overwriteVisibleMessageAccesses(instance, original);
    }

    @WrapOperation(at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/ChatHud;visibleMessages:Ljava/util/List;"), method = "render")
    private List<ChatHudLine.Visible> skyplus$render(ChatHud instance, Operation<List<ChatHudLine.Visible>> original) {
        return overwriteVisibleMessageAccesses(instance, original);
    }

    @WrapOperation(at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/ChatHud;visibleMessages:Ljava/util/List;"), method = "getTextStyleAt")
    private List<ChatHudLine.Visible> skyplus$getTextStyleAt(ChatHud instance, Operation<List<ChatHudLine.Visible>> original) {
        return overwriteVisibleMessageAccesses(instance, original);
    }

    @WrapOperation(at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/ChatHud;visibleMessages:Ljava/util/List;"), method = "getMessageLineIndex")
    private List<ChatHudLine.Visible> skyplus$getMessageLineIndex(ChatHud instance, Operation<List<ChatHudLine.Visible>> original) {
        return overwriteVisibleMessageAccesses(instance, original);
    }

    @WrapOperation(at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/ChatHud;visibleMessages:Ljava/util/List;"), method = "getMessageIndex")
    private List<ChatHudLine.Visible> skyplus$getMessageIndex(ChatHud instance, Operation<List<ChatHudLine.Visible>> original) {
        return overwriteVisibleMessageAccesses(instance, original);
    }

    @WrapOperation(at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/ChatHud;visibleMessages:Ljava/util/List;"), method = "scroll")
    private List<ChatHudLine.Visible> skyplus$scroll(ChatHud instance, Operation<List<ChatHudLine.Visible>> original) {
        return overwriteVisibleMessageAccesses(instance, original);
    }

    @Unique
    private List<ChatHudLine.Visible> overwriteVisibleMessageAccesses(ChatHud instance, Operation<List<ChatHudLine.Visible>> original) {
        var visibleMessages = original.call(instance);

        if (CooldownManager.INSTANCE.getSettings().getShouldShowIsWarpsInChat() && CooldownManager.INSTANCE.getSettings().getShouldShowCfs()) {
            return visibleMessages;
        }

        ArrayList<ChatHudLine.Visible> vv = Lists.newArrayList();
        IntSet s = new IntLinkedOpenHashSet();

//        int i = -1;
//        StringBuilder sb = new StringBuilder();

        for (var element : visibleMessages) {
//            if (i == -1) i = element.addedTime();

            AtomicBoolean shouldAdd = new AtomicBoolean(true);
            if (s.contains(element.addedTime())) continue;

//            StringBuilder sb2 = new StringBuilder();

            element.content().accept((index, style, codepoint) -> {
//                sb2.appendCodePoint(codepoint);

                ClickEvent ce = style.getClickEvent();
                if ((!CooldownManager.INSTANCE.getSettings().getShouldShowCfs() && codepoint == 9632/* ■ */) || (!CooldownManager.INSTANCE.getSettings().getShouldShowIsWarpsInChat() && ce != null && ce.getValue().startsWith("/is warp "))) {
                    s.add(element.addedTime());
                    shouldAdd.set(false);
                }
                return true;
            });

//            if (i == element.addedTime()) {
//                sb.insert(0, sb2);
//            } else {
////                System.out.println("message: " + sb);
//                if (CooldownManager.INSTANCE.getSettings().getFilterRegex().matches(sb)) {
//                    shouldAdd.set(false);
//                    s.add(i);
//                }
//                i = element.addedTime();
//                sb = sb2;
//            }

            if (!shouldAdd.get()) continue;

            vv.add(element);
        }

        vv.removeIf((x) -> s.contains(x.addedTime()));

        return vv;
    }
}
