package dev.u9g.features

import dev.u9g.events.GetTooltipCallback
import net.minecraft.client.item.TooltipContext
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object AddCosmicIdToLore {
    init {
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
    }
}