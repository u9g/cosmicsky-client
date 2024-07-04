package dev.u9g.util.render

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.*
import net.minecraft.util.Identifier
import org.joml.Matrix4f
import org.joml.Vector2f
import kotlin.math.atan2
import kotlin.math.tan

object RenderCircleProgress {

    fun renderCircle(
        drawContext: DrawContext,
        texture: Identifier,
        progress: Float,
        u1: Float,
        u2: Float,
        v1: Float,
        v2: Float,
        red: Int = 255,
        green: Int = 255,
        blue: Int = 255,
        alpha: Int = 255
    ) {
        RenderSystem.setShaderTexture(0, texture)
        RenderSystem.setShader { GameRenderer.getPositionColorTexProgram() }
        RenderSystem.enableBlend()
        val matrix: Matrix4f = drawContext.matrices.peek().positionMatrix
        val bufferBuilder = Tessellator.getInstance().buffer
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR_TEXTURE)
        bufferBuilder.fixedColor(red, green, blue, alpha)

        val corners = listOf(
            Vector2f(0F, -1F),
            Vector2f(1F, -1F),
            Vector2f(1F, 0F),
            Vector2f(1F, 1F),
            Vector2f(0F, 1F),
            Vector2f(-1F, 1F),
            Vector2f(-1F, 0F),
            Vector2f(-1F, -1F),
        )

        for (i in (0 until 8)) {
            if (progress < i / 8F) {
                break
            }
            val second = corners[(i + 1) % 8]
            val first = corners[i]
            if (progress <= (i + 1) / 8F) {
                val internalProgress = 1 - (progress - i / 8F) * 8F
                val angle = lerpAngle(
                    atan2(second.y, second.x),
                    atan2(first.y, first.x),
                    internalProgress
                )
                if (angle < tau / 8 || angle >= tau * 7 / 8) {
                    second.set(1F, tan(angle))
                } else if (angle < tau * 3 / 8) {
                    second.set(1 / tan(angle), 1F)
                } else if (angle < tau * 5 / 8) {
                    second.set(-1F, -tan(angle))
                } else {
                    second.set(-1 / tan(angle), -1F)
                }
            }

            fun ilerp(f: Float): Float =
                ilerp(-1f, 1f, f)

            bufferBuilder
                .vertex(matrix, second.x, second.y, 0F)
                .texture(lerp(u1, u2, ilerp(second.x)), lerp(v1, v2, ilerp(second.y)))
                .next()
            bufferBuilder
                .vertex(matrix, first.x, first.y, 0F)
                .texture(lerp(u1, u2, ilerp(first.x)), lerp(v1, v2, ilerp(first.y)))
                .next()
            bufferBuilder
                .vertex(matrix, 0F, 0F, 0F)
                .texture(lerp(u1, u2, ilerp(0F)), lerp(v1, v2, ilerp(0F)))
                .next()
        }
        bufferBuilder.unfixColor()
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end())
        RenderSystem.disableBlend()
    }
}
