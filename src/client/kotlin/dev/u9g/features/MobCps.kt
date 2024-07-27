package dev.u9g.features

import dev.u9g.events.ChatMessageReceivedCallback
import dev.u9g.events.LivingEntityDeathCallback
import dev.u9g.mc
import dev.u9g.util.RollingAverage
import dev.u9g.util.getScoreboardLines
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient

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
            val avg = rollingAverage.getRollingAverage().toInt()
            if (Settings.shouldShowMobsPerSecond && avg > 0 && getScoreboardLines().any {
                    it.string.startsWith("Island EXP (LVL ")
                }) {
                val toRender = "$avg mps"
                val color = if (avg <= 4) {
                    // red
                    0xff0000
                } else if (avg <= 7) {
                    // orange
                    0xff9900
                } else if (avg <= 9) {
                    // yellow
                    0xffff66
                } else if (avg <= 10) {
                    // green
                    0x66ff66
                } else if (avg <= 12) {
                    // blue
                    0x66ffff
                } else if (avg <= 13) {
                    // pink
                    0xff99ff
                } else {
                    0xcc33ff
                }

                ctx.drawText(
                    mc.textRenderer,
                    toRender,
                    (MinecraftClient.getInstance().window.scaledWidth / 2) - mc.textRenderer.getWidth(toRender) / 2,
                    (MinecraftClient.getInstance().window.scaledHeight / 2) - 5 - 7,
                    color,
                    true
                )
            }
        }

        ChatMessageReceivedCallback.event.register {
            if (it.msg == "(!) Your inventory is currently full! Make some room to pickup more items!") {
                MinecraftClient.getInstance().player?.networkHandler?.sendChatCommand("sell all")
            }
        }
    }
}