package dev.u9g.events

import net.fabricmc.fabric.api.event.EventFactory

fun interface ExperienceProgressOverlayCallback {
    operator fun invoke(event: ExperienceProgressOverlayEvent)

    companion object {
        @JvmStatic
        val event =
            EventFactory.createArrayBacked(ExperienceProgressOverlayCallback::class.java) { callbacks ->
                ExperienceProgressOverlayCallback { event ->
                    for (callback in callbacks)
                        callback(event)
                }
            }
    }
}

data class ExperienceProgressOverlayEvent(
    var progress: Float
)