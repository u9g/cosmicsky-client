package dev.u9g.features

import dev.u9g.events.AfterDrawItemCallback
import dev.u9g.mc
import net.minecraft.client.item.TooltipContext
import net.minecraft.util.Identifier

object MaxLevelPetOverlay {
    private val warningExclaim = Identifier("skyplus", "newitemmarks.png")

    init {
        AfterDrawItemCallback.event.register {
            if (Settings.enableMod) return@register

            it.instance.push()
            it.instance.translate(0f, 0f, 200f)
            it.stack.nbt?.let { nbt ->
                if (nbt.getString("persistentItem") == "inventory_pet" && it.stack.getTooltip(
                        mc.player,
                        TooltipContext.BASIC
                    )
                        .last()?.string == "MAX LEVEL"
                ) {
                    it.instance.drawTexture(
                        warningExclaim,
                        it.x + 9,
                        it.y,
                        1f, 1f,
                        8,
                        8,
                        16,
                        16
                    )
                }
            }
            it.instance.pop()
        }
    }
}