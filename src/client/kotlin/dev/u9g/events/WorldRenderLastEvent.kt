package dev.u9g.events

import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.client.render.Camera
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack

fun interface WorldRenderLastCallback {
    operator fun invoke(event: WorldRenderLastEvent)

    companion object {
        @JvmStatic
        val event =
            EventFactory.createArrayBacked(WorldRenderLastCallback::class.java) { callbacks ->
                WorldRenderLastCallback { event ->
                    for (callback in callbacks)
                        callback(event)
                }
            }
    }
}

data class WorldRenderLastEvent(
    val matrices: MatrixStack,
    val tickDelta: Float,
    val renderBlockOutline: Boolean,
    val camera: Camera,
    val gameRenderer: GameRenderer,
    val lightmapTextureManager: LightmapTextureManager,
    val vertexConsumers: VertexConsumerProvider.Immediate,
)