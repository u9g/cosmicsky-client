package dev.u9g.util.render

import com.mojang.blaze3d.systems.RenderSystem
import dev.u9g.events.WorldRenderLastEvent
import dev.u9g.mc
import dev.u9g.util.FirmFormatters
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.VertexBuffer
import net.minecraft.client.render.*
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.ColorHelper.Argb
import net.minecraft.util.math.Vec3d
import org.joml.Matrix4f
import org.joml.Vector3f
import kotlin.math.pow

@RenderContextDSL
class RenderInWorldContext private constructor(
    private val tesselator: Tessellator,
    val matrixStack: MatrixStack,
    private val camera: Camera,
    private val tickDelta: Float,
    val vertexConsumers: VertexConsumerProvider.Immediate,
) {
    private val buffer = tesselator.buffer

    fun color(red: Float, green: Float, blue: Float, alpha: Float) {
        RenderSystem.setShaderColor(red, green, blue, alpha)
    }

    fun block(blockPos: BlockPos) {
        RenderSystem.setShader(GameRenderer::getPositionColorProgram)
        matrixStack.push()
        matrixStack.translate(blockPos.x.toFloat(), blockPos.y.toFloat(), blockPos.z.toFloat())
        buildCube(matrixStack.peek().positionMatrix, buffer)
        tesselator.draw()
        matrixStack.pop()
    }

    enum class VerticalAlign {
        TOP, BOTTOM, CENTER;

        fun align(index: Int, count: Int): Float {
            return when (this) {
                CENTER -> (index - count / 2F) * (1 + mc.textRenderer.fontHeight.toFloat())
                BOTTOM -> (index - count) * (1 + mc.textRenderer.fontHeight.toFloat())
                TOP -> (index) * (1 + mc.textRenderer.fontHeight.toFloat())
            }
        }
    }

    fun waypoint(position: BlockPos, label: Text) {
        text(
            position.toCenterPos(),
            label,
            Text.literal("§e${FirmFormatters.formatDistance(mc.player?.pos?.distanceTo(position.toCenterPos()) ?: 42069.0)}")
        )
    }

    fun withFacingThePlayer(position: Vec3d, block: FacingThePlayerContext.() -> Unit) {
        matrixStack.push()
        matrixStack.translate(position.x, position.y, position.z)
        val actualCameraDistance = position.distanceTo(camera.pos)
        val distanceToMoveTowardsCamera = if (actualCameraDistance < 10) 0.0 else -(actualCameraDistance - 10.0)
        val vec = position.subtract(camera.pos).multiply(distanceToMoveTowardsCamera / actualCameraDistance)
        matrixStack.translate(vec.x, vec.y, vec.z)
        matrixStack.multiply(camera.rotation)
        matrixStack.scale(-0.025F, -0.025F, -1F)

        FacingThePlayerContext(this).run(block)

        matrixStack.pop()
        vertexConsumers.drawCurrentLayer()
    }

    fun sprite(position: Vec3d, sprite: Sprite, width: Int, height: Int) {
        texture(
            position, sprite.atlasId, width, height, sprite.minU, sprite.minV, sprite.maxU, sprite.maxV
        )
    }

    fun texture(
        position: Vec3d, texture: Identifier, width: Int, height: Int,
        u1: Float, v1: Float,
        u2: Float, v2: Float,
    ) {
        withFacingThePlayer(position) {
            texture(texture, width, height, u1, v1, u2, v2)
        }
    }

    fun text(position: Vec3d, vararg texts: Text, verticalAlign: VerticalAlign = VerticalAlign.CENTER) {
        withFacingThePlayer(position) {
            text(*texts, verticalAlign = verticalAlign)
        }
    }

    fun tinyBlock(vec3d: Vec3d, size: Float) {
        RenderSystem.setShader(GameRenderer::getPositionColorProgram)
        matrixStack.push()
        matrixStack.translate(vec3d.x, vec3d.y, vec3d.z)
        matrixStack.scale(size, size, size)
        matrixStack.translate(-.5, -.5, -.5)
        buildCube(matrixStack.peek().positionMatrix, buffer)
        tesselator.draw()
        matrixStack.pop()
    }

    fun wireframeCube(blockPos: BlockPos, lineWidth: Float = 10F) {
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram)
        matrixStack.push()
        RenderSystem.lineWidth(lineWidth / camera.pos.squaredDistanceTo(blockPos.toCenterPos()).pow(0.25).toFloat())
        matrixStack.translate(blockPos.x.toFloat(), blockPos.y.toFloat(), blockPos.z.toFloat())
        buildWireFrameCube(matrixStack.peek(), buffer)
        tesselator.draw()
        matrixStack.pop()
    }

    fun line(vararg points: Vec3d, lineWidth: Float = 10F) {
        line(points.toList(), lineWidth)
    }

    fun tracer(toWhere: Vec3d, lineWidth: Float = 3f) {
        val cameraForward = Vector3f(0f, 0f, 1f).rotate(camera.rotation)
        line(camera.pos.add(Vec3d(cameraForward)), toWhere, lineWidth = lineWidth)
    }

    fun line(points: List<Vec3d>, lineWidth: Float = 10F) {
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram)
        RenderSystem.lineWidth(lineWidth)
        buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES)
        buffer.fixedColor(255, 255, 255, 255)

        val matrix = matrixStack.peek()
        var lastNormal: Vector3f? = null
        points.zipWithNext().forEach { (a, b) ->
            val normal = Vector3f(b.x.toFloat(), b.y.toFloat(), b.z.toFloat())
                .sub(a.x.toFloat(), a.y.toFloat(), a.z.toFloat())
                .normalize()
            val lastNormal0 = lastNormal ?: normal
            lastNormal = normal
            buffer.vertex(matrix.positionMatrix, a.x.toFloat(), a.y.toFloat(), a.z.toFloat())
                .normal(matrix.normalMatrix, lastNormal0.x, lastNormal0.y, lastNormal0.z)
                .next()
            buffer.vertex(matrix.positionMatrix, b.x.toFloat(), b.y.toFloat(), b.z.toFloat())
                .normal(matrix.normalMatrix, normal.x, normal.y, normal.z)
                .next()
        }
        buffer.unfixColor()

        tesselator.draw()
    }

    private fun line(
        matrix: MatrixStack.Entry, buffer: BufferBuilder,
        x1: Number, y1: Number, z1: Number,
        x2: Number, y2: Number, z2: Number,
        lineWidth: Float
    ) {
        val camera = MinecraftClient.getInstance().cameraEntity ?: return
        RenderSystem.lineWidth(
            lineWidth / camera.pos.squaredDistanceTo(
                Vec3d(x1.toDouble(), y1.toDouble(), z1.toDouble())
            ).pow(0.25).toFloat()
        )
        line(
            matrix,
            buffer,
            Vector3f(x1.toFloat(), y1.toFloat(), z1.toFloat()),
            Vector3f(x2.toFloat(), y2.toFloat(), z2.toFloat())
        )
    }

    private fun line(matrix: MatrixStack.Entry, buffer: BufferBuilder, from: Vector3f, to: Vector3f) {
        val normal = to.sub(from, Vector3f()).mul(-1F)
        buffer.vertex(matrix.positionMatrix, from.x, from.y, from.z)
            .normal(matrix.normalMatrix, normal.x, normal.y, normal.z)
            .color(0xFFFFFFFF.toInt())
        buffer.vertex(matrix.positionMatrix, to.x, to.y, to.z)
            .normal(matrix.normalMatrix, normal.x, normal.y, normal.z)
            .color(0xFFFFFFFF.toInt())
    }

    /**
     * Draws a box in world space, given the coordinates of the box, thickness of the lines, and color.
     * TODO: write a more custom rendering function so we don't have to do this ugly translation of
     * Minecraft's screen space rendering logic to a world space rendering function.
     */
    fun drawWireFrame(
        box: Box,
        color: Int,
        thickness: Float,
        depthTest: Boolean = true
    ) {
        val matrices = this.matrixStack
        matrices.push()
        val prevShader = RenderSystem.getShader()
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram)
        RenderSystem.disableBlend()
        RenderSystem.disableCull()
        // RenderSystem.defaultBlendFunc()
        RenderSystem.setShaderColor(
            Argb.getRed(color) / 255f,
            Argb.getGreen(color) / 255f,
            Argb.getBlue(color) / 255f,
            Argb.getAlpha(color) / 255f
        )
        if (!depthTest) {
            RenderSystem.disableDepthTest()
            RenderSystem.depthMask(false)
        } else {
            RenderSystem.enableDepthTest()
        }
        matrices.translate(-this.camera.pos.x, -this.camera.pos.y, -this.camera.pos.z)
        val tess = RenderSystem.renderThreadTesselator()
        val buf = tess.buffer
        tess.buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES)
        val me = matrices.peek()

        buf.color(255, 255, 255, 255)

        // X Axis aligned lines
        line(me, buf, box.minX, box.minY, box.minZ, box.maxX, box.minY, box.minZ, thickness)
        line(me, buf, box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.minZ, thickness)
        line(me, buf, box.minX, box.minY, box.maxZ, box.maxX, box.minY, box.maxZ, thickness)
        line(me, buf, box.minX, box.maxY, box.maxZ, box.maxX, box.maxY, box.maxZ, thickness)

        // Y Axis aligned lines
        line(me, buf, box.minX, box.minY, box.minZ, box.minX, box.maxY, box.minZ, thickness)
        line(me, buf, box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ, thickness)
        line(me, buf, box.minX, box.minY, box.maxZ, box.minX, box.maxY, box.maxZ, thickness)
        line(me, buf, box.maxX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ, thickness)

        // Z Axis aligned lines
        line(me, buf, box.minX, box.minY, box.minZ, box.minX, box.minY, box.maxZ, thickness)
        line(me, buf, box.maxX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ, thickness)
        line(me, buf, box.minX, box.maxY, box.minZ, box.minX, box.maxY, box.maxZ, thickness)
        line(me, buf, box.maxX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ, thickness)

        BufferRenderer.drawWithGlobalProgram(buf.end())

        RenderSystem.depthMask(true)
        RenderSystem.enableDepthTest()
        RenderSystem.enableBlend()
        RenderSystem.setShaderColor(
            1f, 1f, 1f, 1f
        )
        RenderSystem.setShader { prevShader }
        RenderSystem.enableCull()
        matrices.pop()
    }

    /**
     * This draw line function is intended to be used for drawing very few lines, as it's not the most efficient.
     * For drawing many lines in a series, save them to an array and use the drawLineArray function.
     */
    fun drawLine(
        startPos: Vec3d,
        endPos: Vec3d,
        color: Int,
        thickness: Float,
        depthTest: Boolean = true
    ) {
        val matrices = this.matrixStack
        matrices.push()
        val prevShader = RenderSystem.getShader()
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram)
        RenderSystem.disableBlend()
        RenderSystem.disableCull()
        // RenderSystem.defaultBlendFunc()
        RenderSystem.setShaderColor(
            Argb.getRed(color) / 255f,
            Argb.getGreen(color) / 255f,
            Argb.getBlue(color) / 255f,
            Argb.getAlpha(color) / 255f
        )
        if (!depthTest) {
            RenderSystem.disableDepthTest()
            RenderSystem.depthMask(false)
        } else {
            RenderSystem.enableDepthTest()
        }
        matrices.translate(-this.camera.pos.x, -this.camera.pos.y, -this.camera.pos.z)
        val tess = RenderSystem.renderThreadTesselator()
        val buf = tess.buffer
        buf.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES)
        val me = matrices.peek()

        buf.color(255, 255, 255, 255)

        line(
            me,
            buf,
            startPos.x.toFloat(),
            startPos.y.toFloat(),
            startPos.z.toFloat(),
            endPos.x.toFloat(),
            endPos.y.toFloat(),
            endPos.z.toFloat(),
            thickness
        )

        BufferRenderer.drawWithGlobalProgram(buf.end())

        RenderSystem.depthMask(true)
        RenderSystem.enableDepthTest()
        RenderSystem.enableBlend()
        RenderSystem.setShaderColor(
            1f, 1f, 1f, 1f
        )
        RenderSystem.setShader { prevShader }
        RenderSystem.enableCull()
        matrices.pop()
    }

    /**
     * This function is intended to be used for drawing many lines in a series, as it's more efficient than the
     * drawLine function being called many times in series.
     */
    fun drawLineArray(
        posArr: List<Vec3d>,
        color: Int,
        thickness: Float,
        depthTest: Boolean = true
    ) {
        val matrices = this.matrixStack
        matrices.push()
        val prevShader = RenderSystem.getShader()
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram)
        RenderSystem.disableBlend()
        RenderSystem.disableCull()
        // RenderSystem.defaultBlendFunc()
        RenderSystem.setShaderColor(
            Argb.getRed(color) / 255f,
            Argb.getGreen(color) / 255f,
            Argb.getBlue(color) / 255f,
            Argb.getAlpha(color) / 255f
        )
        if (!depthTest) {
            RenderSystem.disableDepthTest()
            RenderSystem.depthMask(false)
        } else {
            RenderSystem.enableDepthTest()
        }
        matrices.translate(-this.camera.pos.x, -this.camera.pos.y, -this.camera.pos.z)
        val tess = RenderSystem.renderThreadTesselator()
        val buf = tess.buffer
        buf.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES)
        val me = matrices.peek()

        buf.color(255, 255, 255, 255)

        for (i in 0 until posArr.size - 1) {
            val startPos = posArr[i]
            val endPos = posArr[i + 1]
            line(
                me,
                buf,
                startPos.x.toFloat(),
                startPos.y.toFloat(),
                startPos.z.toFloat(),
                endPos.x.toFloat(),
                endPos.y.toFloat(),
                endPos.z.toFloat(),
                thickness
            )
        }

        BufferRenderer.drawWithGlobalProgram(buf.end())

        RenderSystem.depthMask(true)
        RenderSystem.enableDepthTest()
        RenderSystem.enableBlend()
        RenderSystem.setShaderColor(
            1f, 1f, 1f, 1f
        )
        RenderSystem.setShader { prevShader }
        RenderSystem.enableCull()
        matrices.pop()
    }

    /**
     * Draws a filled box at a given position
     */
    fun drawBox(
        x: Double,
        y: Double,
        z: Double,
        width: Double,
        height: Double,
        depth: Double,
        color: Int,
        depthTest: Boolean
    ) {
        if (!depthTest) {
            RenderSystem.disableDepthTest()
            //RenderSystem.depthMask(false)
        } else {
            RenderSystem.enableDepthTest()
        }
        RenderSystem.setShader(GameRenderer::getPositionColorProgram)
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()

        val matrices = this.matrixStack
        val tes = Tessellator.getInstance()
        val buf = tes.buffer
        buf.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR)
        matrices.push()
        matrices.translate(x - this.camera.pos.x, y - this.camera.pos.y, z - this.camera.pos.z)
        WorldRenderer.renderFilledBox(
            matrices, buf, 0.0, 0.0, 0.0, width, height, depth,
            Argb.getRed(color) / 255f,
            Argb.getGreen(color) / 255f,
            Argb.getBlue(color) / 255f,
            Argb.getAlpha(color) / 255f
        )

        BufferRenderer.drawWithGlobalProgram(buf.end())
        RenderSystem.enableDepthTest()
        matrices.pop()
    }

    companion object {
        private fun doLine(
            matrix: MatrixStack.Entry,
            buf: BufferBuilder,
            i: Float,
            j: Float,
            k: Float,
            x: Float,
            y: Float,
            z: Float
        ) {
            val normal = Vector3f(x, y, z)
                .sub(i, j, k)
                .normalize()
            buf.vertex(matrix.positionMatrix, i, j, k)
                // TODO: should this be matrix.normalMatrix?
                //       the actual code just puts matrix...
                .normal(matrix.normalMatrix, normal.x, normal.y, normal.z)
                .next()
            buf.vertex(matrix.positionMatrix, x, y, z)
                // TODO: should this be matrix.normalMatrix?
                //       the actual code just puts matrix...
                .normal(matrix.normalMatrix, normal.x, normal.y, normal.z)
                .next()
        }


        private fun buildWireFrameCube(matrix: MatrixStack.Entry, buf: BufferBuilder) {
            buf.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES)
            buf.fixedColor(255, 255, 255, 255)

            for (i in 0..1) {
                for (j in 0..1) {
                    val i = i.toFloat()
                    val j = j.toFloat()
                    doLine(matrix, buf, 0F, i, j, 1F, i, j)
                    doLine(matrix, buf, i, 0F, j, i, 1F, j)
                    doLine(matrix, buf, i, j, 0F, i, j, 1F)
                }
            }
            buf.unfixColor()
        }

        private fun buildCube(matrix: Matrix4f, buf: BufferBuilder) {
            buf.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR)
            buf.fixedColor(255, 255, 255, 255)
            buf.vertex(matrix, 0.0F, 0.0F, 0.0F).next()
            buf.vertex(matrix, 0.0F, 0.0F, 1.0F).next()
            buf.vertex(matrix, 0.0F, 1.0F, 1.0F).next()
            buf.vertex(matrix, 1.0F, 1.0F, 0.0F).next()
            buf.vertex(matrix, 0.0F, 0.0F, 0.0F).next()
            buf.vertex(matrix, 0.0F, 1.0F, 0.0F).next()
            buf.vertex(matrix, 1.0F, 0.0F, 1.0F).next()
            buf.vertex(matrix, 0.0F, 0.0F, 0.0F).next()
            buf.vertex(matrix, 1.0F, 0.0F, 0.0F).next()
            buf.vertex(matrix, 1.0F, 1.0F, 0.0F).next()
            buf.vertex(matrix, 1.0F, 0.0F, 0.0F).next()
            buf.vertex(matrix, 0.0F, 0.0F, 0.0F).next()
            buf.vertex(matrix, 0.0F, 0.0F, 0.0F).next()
            buf.vertex(matrix, 0.0F, 1.0F, 1.0F).next()
            buf.vertex(matrix, 0.0F, 1.0F, 0.0F).next()
            buf.vertex(matrix, 1.0F, 0.0F, 1.0F).next()
            buf.vertex(matrix, 0.0F, 0.0F, 1.0F).next()
            buf.vertex(matrix, 0.0F, 0.0F, 0.0F).next()
            buf.vertex(matrix, 0.0F, 1.0F, 1.0F).next()
            buf.vertex(matrix, 0.0F, 0.0F, 1.0F).next()
            buf.vertex(matrix, 1.0F, 0.0F, 1.0F).next()
            buf.vertex(matrix, 1.0F, 1.0F, 1.0F).next()
            buf.vertex(matrix, 1.0F, 0.0F, 0.0F).next()
            buf.vertex(matrix, 1.0F, 1.0F, 0.0F).next()
            buf.vertex(matrix, 1.0F, 0.0F, 0.0F).next()
            buf.vertex(matrix, 1.0F, 1.0F, 1.0F).next()
            buf.vertex(matrix, 1.0F, 0.0F, 1.0F).next()
            buf.vertex(matrix, 1.0F, 1.0F, 1.0F).next()
            buf.vertex(matrix, 1.0F, 1.0F, 0.0F).next()
            buf.vertex(matrix, 0.0F, 1.0F, 0.0F).next()
            buf.vertex(matrix, 1.0F, 1.0F, 1.0F).next()
            buf.vertex(matrix, 0.0F, 1.0F, 0.0F).next()
            buf.vertex(matrix, 0.0F, 1.0F, 1.0F).next()
            buf.vertex(matrix, 1.0F, 1.0F, 1.0F).next()
            buf.vertex(matrix, 0.0F, 1.0F, 1.0F).next()
            buf.vertex(matrix, 1.0F, 0.0F, 1.0F).next()
            buf.unfixColor()
        }


        fun renderInWorld(event: WorldRenderLastEvent, block: RenderInWorldContext. () -> Unit) {
            RenderSystem.disableDepthTest()
            RenderSystem.enableBlend()
            RenderSystem.defaultBlendFunc()
            RenderSystem.disableCull()

            event.matrices.push()
            event.matrices.translate(-event.camera.pos.x, -event.camera.pos.y, -event.camera.pos.z)

            val ctx = RenderInWorldContext(
                RenderSystem.renderThreadTesselator(),
                event.matrices,
                event.camera,
                event.tickDelta,
                event.vertexConsumers
            )

            block(ctx)

            event.matrices.pop()

            RenderSystem.setShaderColor(1F, 1F, 1F, 1F)
            VertexBuffer.unbind()
            RenderSystem.enableDepthTest()
            RenderSystem.enableCull()
            RenderSystem.disableBlend()
        }
    }
}