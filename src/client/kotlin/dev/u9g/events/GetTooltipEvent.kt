package dev.u9g.events

import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.item.ItemStack
import net.minecraft.text.Text

fun interface GetTooltipCallback {
    operator fun invoke(event: GetTooltipEvent)

    companion object {
        @JvmStatic
        val event =
            EventFactory.createArrayBacked(GetTooltipCallback::class.java) { callbacks ->
                GetTooltipCallback { event ->
                    for (callback in callbacks)
                        callback(event)
                }
            }
    }
}

data class GetTooltipEvent(
    val tooltipLines: MutableList<Text>,
    val stack: ItemStack
)