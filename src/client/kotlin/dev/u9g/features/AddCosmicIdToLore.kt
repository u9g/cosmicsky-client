package dev.u9g.features

import dev.u9g.events.GetItemNameCallback
import dev.u9g.events.GetTooltipCallback
import dev.u9g.mc
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.item.TooltipContext
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object AddCosmicIdToLore {
    private val copyCosmicId = KeyBinding("Copy Cosmic Id", InputUtil.GLFW_KEY_RIGHT_CONTROL, "Developer")

    init {
        KeyBindingHelper.registerKeyBinding(copyCosmicId)
        ClientTickEvents.END_CLIENT_TICK.register {
            if (!Settings.enableMod) return@register

            if (InputUtil.isKeyPressed(mc.window.handle, copyCosmicId.boundKey.code)) {
                val currentScreen = mc.currentScreen
                if (currentScreen is HandledScreen<*>) {
                    currentScreen.focusedSlot?.let {
                        it.stack?.nbt?.getLong("c_iid")?.let { cid ->
                            if (cid != 0L) {
                                mc.keyboard.clipboard = cid.toString()
                            }
                        }
                    }
                }
            }
        }

        GetTooltipCallback.event.register { event ->
            if (!Settings.enableMod) return@register

            if (event.context == TooltipContext.ADVANCED) {
                event.stack.nbt?.let {
                    if (it.contains("c_iid")) {
                        event.tooltipLines.add(Text.of(Formatting.DARK_GRAY.toString() + "c_iid: " + it.getLong("c_iid")))
                    }
                }
            }
        }

        GetItemNameCallback.event.register { event ->
            if (!Settings.enableMod) return@register

            event.stack.nbt?.let {
                if (it.contains("c_iid")) {
                    Settings.itemNames[it.getLong("c_iid")]?.let { newName ->
                        event.name = Text.of(newName)
                    }
                }
            }
        }
    }
}