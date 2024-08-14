package dev.u9g.mixin.client;

import dev.u9g.features.CooldownManager;
import dev.u9g.features.Settings;
import kotlin.text.Regex;
import kotlin.text.RegexOption;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;

@Mixin(ChatScreen.class)
public class ChatScreenMixin extends Screen {
    @Shadow
    ChatInputSuggestor chatInputSuggestor;

    @Unique
    CheckboxWidget showIslandWarpsInChat;

    @Unique
    CheckboxWidget showCfsInChat;

    @Unique
    TextFieldWidget filter;

    protected ChatScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At("TAIL"), method = "init")
    private void skyplus$init(CallbackInfo ci) {
        this.showIslandWarpsInChat = CheckboxWidget.builder(Text.of("Show Island Warps"), MinecraftClient.getInstance().textRenderer)
                .checked(CooldownManager.INSTANCE.getSettings().getShouldShowIsWarpsInChat())
                .callback((checkbox, checked) -> CooldownManager.INSTANCE.setShouldShowIsWarpsInChat(checked)).build();

        this.showCfsInChat = CheckboxWidget.builder(Text.of("Show Cfs"), MinecraftClient.getInstance().textRenderer)
                .pos(0, 12)
                .checked(CooldownManager.INSTANCE.getSettings().getShouldShowCfs())
                .callback((checkbox, checked) -> CooldownManager.INSTANCE.setShouldShowCfs(checked)).build();

        this.filter = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, (int) ((double) this.height * 0.9), 12, Text.of("Filter"));
        this.filter.setY(30);
        this.filter.setX(1);
        this.filter.setMaxLength(99999999);
        this.filter.setChangedListener((newStr) -> {
            try {
                CooldownManager.INSTANCE.getSettings().setFilter(newStr);
                CooldownManager.INSTANCE.getSettings().setFilterRegex(new Regex(newStr, new HashSet<>(Collections.singleton(RegexOption.IGNORE_CASE))));
            } catch (Exception e) {
                CooldownManager.INSTANCE.getSettings().setFilter("");
                CooldownManager.INSTANCE.getSettings().setFilterRegex(new Regex(""));
            }
        });

        this.filter.setText(CooldownManager.INSTANCE.getSettings().getFilter());
        this.filter.setCursor(0, false);

        this.addDrawable(this.showIslandWarpsInChat);
        this.addDrawable(this.showCfsInChat);
        this.addDrawable(this.filter);
    }

    @Inject(at = @At("TAIL"), method = "mouseClicked")
    private void skyplus$mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        this.showIslandWarpsInChat.mouseClicked(mouseX, mouseY, button);
        this.showCfsInChat.mouseClicked(mouseX, mouseY, button);
        this.filter.mouseClicked(mouseX, mouseY, button);
    }

    @Inject(at = @At("HEAD"), method = "keyPressed", cancellable = true)
    private void skyplus$keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (Settings.INSTANCE.getEnableMod() && Settings.INSTANCE.getSingleEscapeClosesChat() && keyCode == GLFW.GLFW_KEY_ESCAPE && this.chatInputSuggestor.keyPressed(keyCode, scanCode, modifiers) && MinecraftClient.getInstance() != null) {
            MinecraftClient.getInstance().setScreen(null);
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
