package dev.u9g.events

import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.text.Text

fun interface OverlayTextCallback {
    operator fun invoke(event: OverlayTextEvent)

    companion object {
        @JvmStatic
        val event =
            EventFactory.createArrayBacked(OverlayTextCallback::class.java) { callbacks ->
                OverlayTextCallback { event ->
                    for (callback in callbacks)
                        callback(event)
                }
            }
    }
}

data class OverlayTextEvent(
    val text: Text,
    val msg: String
)