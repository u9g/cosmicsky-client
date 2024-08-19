package dev.u9g.features

import net.minecraft.client.gui.hud.ChatHudLine
import net.minecraft.text.Text

data class MessageData(private val isWarpMessage: Boolean, val text: Text) {
    companion object {
        @JvmStatic
        fun create(text: Text): MessageData {
            return MessageData(false, text)
        }
    }

    fun shouldHide(): Boolean {
        if (true) return false

        // check config if to hide iswarp msg
        return isWarpMessage || CooldownManager.settings.filterRegex.matches(text.string) || !CooldownManager.settings.filterAllowRegex.matches(
            text.string
        )
    }
}

var filteredVisibleMessages: List<ChatHudLine.Visible> = listOf()