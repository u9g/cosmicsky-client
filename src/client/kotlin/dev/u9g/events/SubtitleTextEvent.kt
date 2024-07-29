package dev.u9g.events

import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.text.Text

fun interface SubtitleTextCallback {
    operator fun invoke(event: SubtitleTextEvent)

    companion object {
        @JvmStatic
        val event =
            EventFactory.createArrayBacked(SubtitleTextCallback::class.java) { callbacks ->
                SubtitleTextCallback { event ->
                    for (callback in callbacks)
                        callback(event)
                }
            }
    }
}

data class SubtitleTextEvent(
    val text: Text,
    val msg: String,
    var isCancelled: Boolean
)
