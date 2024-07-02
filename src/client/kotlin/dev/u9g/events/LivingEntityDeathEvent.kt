package dev.u9g.events

import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.LivingEntity

fun interface LivingEntityDeathCallback {
    operator fun invoke(event: LivingEntityDeathEvent)

    companion object {
        @JvmStatic
        val event =
            EventFactory.createArrayBacked(LivingEntityDeathCallback::class.java) { callbacks ->
                LivingEntityDeathCallback { event ->
                    for (callback in callbacks)
                        callback(event)
                }
            }
    }
}

data class LivingEntityDeathEvent(
    val entity: LivingEntity
)