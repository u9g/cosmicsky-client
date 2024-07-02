package dev.u9g.features

import dev.u9g.events.LivingEntityDeathCallback
import dev.u9g.mc
import dev.u9g.util.RollingAverage
import dev.u9g.util.getScoreboardLines
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import kotlin.math.round

class MobCps {
    private val rollingAverage = RollingAverage()

    init {
        LivingEntityDeathCallback.event.register {
            mc.player?.distanceTo(it.entity)?.let { dist ->
                if (dist < 3) {
                    rollingAverage.addEvent()
                }
            }
        }

        HudRenderCallback.EVENT.register { ctx, _ ->
            val avg = round(rollingAverage.getRollingAverage()).toInt()
            if (Settings.shouldShowMobsPerSecond && avg > 0 && getScoreboardLines().any {
                    it.string.startsWith("Island EXP (LVL ")
                }) {
                val toRender = "$avg mps"
                ctx.drawText(
                    mc.textRenderer,
                    toRender,
                    (MinecraftClient.getInstance().window.scaledWidth / 2) - mc.textRenderer.getWidth(toRender) / 2,
                    (MinecraftClient.getInstance().window.scaledHeight / 2) - 5,
                    0xFFFFFF,
                    true
                )
            }
        }
    }
}