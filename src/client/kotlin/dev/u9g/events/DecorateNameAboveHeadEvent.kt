package dev.u9g.events

import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.text.MutableText

fun interface DecorateNameAboveHeadCallback {
    operator fun invoke(event: DecorateNameAboveHeadEvent)

    companion object {
        @JvmStatic
        val event =
            EventFactory.createArrayBacked(DecorateNameAboveHeadCallback::class.java) { callbacks ->
                DecorateNameAboveHeadCallback { event ->
                    for (callback in callbacks)
                        callback(event)
                }
            }
    }
}

data class DecorateNameAboveHeadEvent(
    val username: String,
    var textToSend: MutableText
)