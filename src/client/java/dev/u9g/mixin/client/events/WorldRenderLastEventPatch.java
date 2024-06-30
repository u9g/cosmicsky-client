package dev.u9g.mixin.client.events;

import com.llamalad7.mixinextras.sugar.Local;
import dev.u9g.events.WorldRenderLastCallback;
import dev.u9g.events.WorldRenderLastEvent;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRenderLastEventPatch {
    @Shadow
    @Final
    private BufferBuilderStorage bufferBuilders;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderChunkDebugInfo(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/render/Camera;)V", shift = At.Shift.BEFORE))
    public void onWorldRenderLast(MatrixStack matrices,
                                  float tickDelta, long limitTime, boolean renderBlockOutline,
                                  Camera camera, GameRenderer gameRenderer,
                                  LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix,

                                  CallbackInfo ci, @Local(index = 1) MatrixStack matrixStack) {
        WorldRenderLastCallback.getEvent().invoker().invoke(
                new WorldRenderLastEvent(matrixStack, tickDelta, renderBlockOutline,
                        camera, gameRenderer, lightmapTextureManager,
                        this.bufferBuilders.getEntityVertexConsumers())
        );
    }
}