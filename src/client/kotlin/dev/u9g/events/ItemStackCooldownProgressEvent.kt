package dev.u9g.events;

import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.item.ItemStack

fun interface ItemStackCooldownProgressCallback {
    operator fun invoke(event: ItemStackCooldownProgressEvent)

    companion object {
        @JvmStatic
        val event =
            EventFactory.createArrayBacked(ItemStackCooldownProgressCallback::class.java) { callbacks ->
                ItemStackCooldownProgressCallback { event ->
                    for (callback in callbacks)
                        callback(event)
                }
            }
    }
}

data class ItemStackCooldownProgressEvent(
    val item: ItemStack,

    var isSet: Boolean,
    var progress: Float
) {
    constructor(stack: ItemStack) : this(stack, false, 0.0f)

    fun overrideProgress(progress: Float) {
        this.isSet = true
        this.progress = progress
    }
}