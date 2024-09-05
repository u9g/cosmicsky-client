package dev.u9g.features

import dev.u9g.events.ExperienceBarOverlayCallback
import dev.u9g.events.ExperienceProgressOverlayCallback
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.item.TooltipContext

object IsToolsPickaxeOnHotbar {
    val R = " \\(([\\d,.]+)/([\\d,.]+)\\)".toRegex()

    init {
        var data: Pair<Int, Float>? = null

        ClientTickEvents.END_CLIENT_TICK.register {
            if (Settings.enableMod) return@register

            val stack = MinecraftClient.getInstance().player?.inventory?.mainHandStack ?: return@register

            val nbt = stack.nbt ?: return@register

            if (nbt.getString("persistentItem") == "pickaxe_skill_item") {
                val line = stack.getTooltip(null, TooltipContext.BASIC).find { line -> R.matches(line.string) } ?: run {
                    data = null
                    return@register
                }

                val match = R.find(line.string) ?: run {
                    data = null
                    return@register
                }

                val (a, b) = match.destructured
                val x = a.replace(",", "").toFloatOrNull()
                val y = b.replace(",", "").toFloatOrNull()

                if (x != null && y != null) {
                    val level = nbt.getCompound("pickaxeData").getInt("level")

                    data = Pair(level, x / y)
                } else {
                    data = null
                }
            }
        }

        ExperienceBarOverlayCallback.event.register { event ->
            data?.let {
                event.lvl = it.first
            }
        }

        ExperienceProgressOverlayCallback.event.register { event ->
            data?.let {
                event.progress = it.second
            }
        }
    }
}