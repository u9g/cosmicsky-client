package dev.u9g.events

import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.item.ItemStack
import net.minecraft.text.Text

fun interface GetItemNameCallback {
    operator fun invoke(event: GetItemNameEvent)

    companion object {
        @JvmStatic
        val event =
            EventFactory.createArrayBacked(GetItemNameCallback::class.java) { callbacks ->
                GetItemNameCallback { event ->
                    for (callback in callbacks)
                        callback(event)
                }
            }
    }
}

data class GetItemNameEvent(
    var name: Text,
    val stack: ItemStack
)