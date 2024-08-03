package dev.u9g.features

import dev.u9g.mc
import kotlinx.serialization.Serializable
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.item.TooltipContext
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import kotlin.math.min

@Serializable
data class ClueScrollSettings(
    var middleXPercent: Double = 50.0,
    var middleYPercent: Double = 40.0,
    var isEnabled: Boolean = true
)

object ClueScrollManager {
    var settings: ClueScrollSettings = ClueScrollSettings()
    var showAllClueScrollsOnHud = false
    val R = "(\\d+)/(\\d+)".toRegex()

    private val toggleShowAllClueScrollsOnHudKey =
        KeyBinding("Toggle showing all clue scrolls on hud", InputUtil.GLFW_KEY_Z, "Clue Scroll Hud")

    init {
        KeyBindingHelper.registerKeyBinding(toggleShowAllClueScrollsOnHudKey)
        var lines = listOf<Text>()


        ClientTickEvents.END_CLIENT_TICK.register {
            while (toggleShowAllClueScrollsOnHudKey.wasPressed()) {
                showAllClueScrollsOnHud = !showAllClueScrollsOnHud
            }

            lines = mc.player?.inventory?.main?.let { items ->
                items.filterIndexed { a, b ->
                    if (!showAllClueScrollsOnHud) {
                        a < 9
                    } else {
                        true
                    }
                }.mapNotNull { item ->
                    if (item.nbt?.getString("persistentItem") == "clue_scroll") {
                        item.getTooltip(null, TooltipContext.BASIC)
                            .findLast { it.string.startsWith(" * ") }
                    } else {
                        null
                    }
                }
            } ?: emptyList()
        }

        HudRenderCallback.EVENT.register { a, b ->
            val centerX = (mc.window.scaledWidth.toFloat() * (settings.middleXPercent / 100)).toInt()
            var centerY = (mc.window.scaledHeight.toFloat() * (settings.middleYPercent / 100)).toInt()

            if (CooldownManager.settings.isEnabled && Settings.enableMod) {
                fun drawTextCentered(msg: Text, centerX: Int, centerY: Int) {
                    a.drawText(
                        mc.textRenderer,
                        msg,
                        (centerX - (mc.textRenderer.getWidth(msg).toFloat() * 0.5)).toInt(),
                        (centerY - 3.5).toInt(),
                        -1,
                        true
                    )
                }

                a.matrices.push()

                for (line in lines) {
                    drawTextCentered(line, centerX, centerY)

                    centerY -= 11
                }

                a.matrices.pop()
            }
        }
    }

    data class ClueScrollInfo(val progress: Double, val line: String, val lineNumber: Int)

    @JvmOverloads
    fun getClueScrollInfo(
        item: ItemStack,
        original: List<Text> = item.getTooltip(null, TooltipContext.BASIC)
    ): ClueScrollInfo? {
        val nbt = item.nbt
        if (nbt != null && "clue_scroll" == nbt.getString("persistentItem")) {
            for (i in original.indices.reversed()) {
                val t = original[i]

                val line = t.string

                if (line.startsWith(" * ")) {
                    val found = R.find(line, 0)
                    if (found != null) {
                        val groups = found.groups
                        val finished = groups[1]
                        val total = groups[2]

                        try {
                            if (finished != null && total != null) {
                                val finishedD = finished.value.toDouble()
                                val totalD = total.value.toDouble()
                                val percentDone = min(finishedD / totalD, 1.0)

                                return ClueScrollInfo(percentDone, line, i)
                            }
                        } catch (e: Exception) {
                            return null
                        }
                    }
                }
            }
        }

        return null
    }
}