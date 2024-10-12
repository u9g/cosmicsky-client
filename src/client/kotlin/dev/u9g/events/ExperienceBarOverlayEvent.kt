package dev.u9g.events

import net.fabricmc.fabric.api.event.EventFactory

fun interface ExperienceBarOverlayCallback {
    operator fun invoke(event: ExperienceBarOverlayEvent)

    companion object {
        @JvmStatic
        val event =
            EventFactory.createArrayBacked(ExperienceBarOverlayCallback::class.java) { callbacks ->
                ExperienceBarOverlayCallback { event ->
                    for (callback in callbacks)
                        callback(event)
                }
            }
    }
}

data class ExperienceBarOverlayEvent(
    var lvl: Int
)