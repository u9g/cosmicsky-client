package dev.u9g.events

import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.client.gui.DrawContext
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.world.World

fun interface BeforeDrawItemCallback {
    operator fun invoke(event: BeforeDrawItemEvent)

    companion object {
        @JvmStatic
        val event =
            EventFactory.createArrayBacked(BeforeDrawItemCallback::class.java) { callbacks ->
                BeforeDrawItemCallback { event ->
                    for (callback in callbacks)
                        callback(event)
                }
            }
    }
}

data class BeforeDrawItemEvent(
    val instance: DrawContext,
    val entity: LivingEntity?,
    val world: World?,
    val stack: ItemStack,
    val x: Int,
    val y: Int,
    val seed: Int,
    val z: Int,
    var drawOriginalItem: Boolean
)