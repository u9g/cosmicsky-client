package dev.u9g.util.render

import com.mojang.blaze3d.systems.RenderSystem
import dev.u9g.mc
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.render.*
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.joml.Matrix4f

@RenderContextDSL
class FacingThePlayerContext(private val worldContext: RenderInWorldContext) {
    val matrixStack by worldContext::matrixStack
    fun text(
        vararg texts: Text,
        verticalAlign: RenderInWorldContext.VerticalAlign = RenderInWorldContext.VerticalAlign.CENTER
    ) {
        if (texts.isEmpty()) {
            return
        }

        for ((index, text) in texts.withIndex()) {
            worldContext.matrixStack.push()
            val width = mc.textRenderer.getWidth(text)
            worldContext.matrixStack.translate(-width / 2F, verticalAlign.align(index, texts.size), 0F)
            val vertexConsumer: VertexConsumer =
                worldContext.vertexConsumers.getBuffer(RenderLayer.getTextBackgroundSeeThrough())
            val matrix4f = worldContext.matrixStack.peek().positionMatrix
            vertexConsumer.vertex(matrix4f, -1.0f, -1.0f, 0.0f).color(0x70808080)
                .light(LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE).next()
            vertexConsumer.vertex(matrix4f, -1.0f, mc.textRenderer.fontHeight.toFloat(), 0.0f).color(0x70808080)
                .light(LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE).next()
            vertexConsumer.vertex(matrix4f, width.toFloat(), mc.textRenderer.fontHeight.toFloat(), 0.0f)
                .color(0x70808080)
                .light(LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE).next()
            vertexConsumer.vertex(matrix4f, width.toFloat(), -1.0f, 0.0f).color(0x70808080)
                .light(LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE).next()
            worldContext.matrixStack.translate(0F, 0F, 0.01F)

            mc.textRenderer.draw(
                text,
                0F,
                0F,
                -1,
                false,
                worldContext.matrixStack.peek().positionMatrix,
                worldContext.vertexConsumers,
                TextRenderer.TextLayerType.SEE_THROUGH,
                0,
                LightmapTextureManager.MAX_LIGHT_COORDINATE
            )
            worldContext.matrixStack.pop()
        }
    }


    fun texture(
        texture: Identifier, width: Int, height: Int,
        u1: Float, v1: Float,
        u2: Float, v2: Float,
    ) {
        RenderSystem.setShaderTexture(0, texture)
        RenderSystem.setShader(GameRenderer::getPositionColorTexProgram)
        val hw = width / 2F
        val hh = height / 2F
        val matrix4f: Matrix4f = worldContext.matrixStack.peek().positionMatrix
        val buf = Tessellator.getInstance().buffer
        buf.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE)
        buf.fixedColor(255, 255, 255, 255)
        buf.vertex(matrix4f, -hw, -hh, 0F)
            .texture(u1, v1).next()
        buf.vertex(matrix4f, -hw, +hh, 0F)
            .texture(u1, v2).next()
        buf.vertex(matrix4f, +hw, +hh, 0F)
            .texture(u2, v2).next()
        buf.vertex(matrix4f, +hw, -hh, 0F)
            .texture(u2, v1).next()
        buf.unfixColor()
        BufferRenderer.drawWithGlobalProgram(buf.end())
    }

}