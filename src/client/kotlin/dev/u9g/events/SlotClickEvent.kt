package dev.u9g.events

import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType

fun interface SlotClickCallback {
    operator fun invoke(event: SlotClickEvent)

    companion object {
        @JvmStatic
        val event =
            EventFactory.createArrayBacked(SlotClickCallback::class.java) { callbacks ->
                SlotClickCallback { event ->
                    for (callback in callbacks)
                        callback(event)
                }
            }
    }
}

data class SlotClickEvent(
    val slot: Slot,
    val stack: ItemStack,
    val button: Int,
    val actionType: SlotActionType,
)
