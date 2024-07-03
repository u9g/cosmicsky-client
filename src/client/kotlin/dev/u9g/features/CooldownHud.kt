package dev.u9g.features

import dev.u9g.events.ChatMessageReceivedCallback
import dev.u9g.mc
import dev.u9g.util.render.RenderCircleProgress
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.util.Identifier

const val FIVE_MINS = 5.0f * 60.0f * 1_000.0f

class CooldownHud {
    private var lastHeal = 0L
    private var lastFeed = 0L

    init {
        HudRenderCallback.EVENT.register { draw, tickDelta ->
            draw.matrices.push()
            draw.matrices.translate(mc.window.scaledWidth / 2F, mc.window.scaledHeight / 2F, 0F)
            draw.matrices.scale(10f, 10f, 1F)
            val sinceLastHeal = System.currentTimeMillis() - lastHeal
            RenderCircleProgress.renderCircle(
                draw, Identifier("skyplus", "circle.png"),
                if (sinceLastHeal > FIVE_MINS) {
                    1.0f
                } else {
                    sinceLastHeal.toFloat() / FIVE_MINS
                },
                0f, 1f, 0f, 1f
            )
            RenderCircleProgress.renderCircle(
                draw, Identifier("skyplus", "circle.png"),
                if (lastFeed > FIVE_MINS) {
                    1.0f
                } else {
                    lastFeed.toFloat() / FIVE_MINS
                },
                0f, 1f, 0f, 1f
            )
            draw.matrices.pop()
        }

        ChatMessageReceivedCallback.event.register {
            when (it.msg) {
                "(!) Healed" -> {
                    lastHeal = System.currentTimeMillis()
                }

                "Appetite has been satiated." -> {
                    lastFeed = System.currentTimeMillis()
                }
            }
        }
    }
}