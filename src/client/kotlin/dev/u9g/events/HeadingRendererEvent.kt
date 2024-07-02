package dev.u9g.events

import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.text.Text
import java.util.function.Consumer

fun interface HeadingRendererCallback {
    operator fun invoke(event: HeadingRendererEvent)

    companion object {
        @JvmStatic
        val event =
            EventFactory.createArrayBacked(HeadingRendererCallback::class.java) { callbacks ->
                HeadingRendererCallback { event ->
                    for (callback in callbacks)
                        callback(event)
                }
            }
    }
}

data class HeadingRendererEvent(
    val distance: Double,
    val matrixStack: MatrixStack,
    val entity: Entity,
    val renderer: Consumer<Text>
)

//fun interface RendererInterface {
//    fun render(
//        player: AbstractClientPlayerEntity?,
//        component: Text?,
//        stack: MatrixStack?,
//        source: VertexConsumerProvider?,
//        packedLight: Int,
//        partialTicks: Float
//    )
//}