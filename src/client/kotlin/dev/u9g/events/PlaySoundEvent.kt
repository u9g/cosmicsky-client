package dev.u9g.events

import net.fabricmc.fabric.api.event.EventFactory

fun interface PlaySoundCallback {
    operator fun invoke(event: PlaySoundEvent)

    companion object {
        @JvmStatic
        val event =
            EventFactory.createArrayBacked(PlaySoundCallback::class.java) { callbacks ->
                PlaySoundCallback { event ->
                    for (callback in callbacks)
                        callback(event)
                }
            }
    }
}

data class PlaySoundEvent(
    val soundKey: String?,
    var isCancelled: Boolean
)
