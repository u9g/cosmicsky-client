package dev.u9g.events

import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.text.Text

fun interface TitleTextCallback {
    operator fun invoke(event: TitleTextEvent)

    companion object {
        @JvmStatic
        val event =
            EventFactory.createArrayBacked(TitleTextCallback::class.java) { callbacks ->
                TitleTextCallback { event ->
                    for (callback in callbacks)
                        callback(event)
                }
            }
    }
}

data class TitleTextEvent(
    val text: Text,
    val msg: String,
    var isCancelled: Boolean
)
