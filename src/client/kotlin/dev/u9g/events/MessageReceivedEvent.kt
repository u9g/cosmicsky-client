package dev.u9g.events

import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.text.Text

fun interface ChatMessageReceivedCallback {
    operator fun invoke(event: ChatMessageReceivedEvent)

    companion object {
        @JvmStatic
        val event =
            EventFactory.createArrayBacked(ChatMessageReceivedCallback::class.java) { callbacks ->
                ChatMessageReceivedCallback { event ->
                    for (callback in callbacks)
                        callback(event)
                }
            }
    }
}

data class ChatMessageReceivedEvent(
    val text: Text,
    val msg: String,
    var isCancelled: Boolean
)