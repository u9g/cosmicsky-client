package dev.u9g.features

import dev.u9g.events.OverlayTextCallback
import dev.u9g.events.WorldRenderLastCallback
import dev.u9g.features.Settings.whatAdventureToDisplay
import dev.u9g.mc
import dev.u9g.util.render.RenderInWorldContext
import dev.u9g.webSocket
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.minecraft.util.math.Direction

var uuid2tpOut: Map<String, TPOut> = mapOf()

data class TPOut(val secLeft: String, val at: Long = System.currentTimeMillis())

class TPOutAnnouncer {
    private val regex = "\\(!\\) You will be shuttled from this Adventure in (\\d+)s...".toRegex()

    init {
        OverlayTextCallback.event.register {
            val match = regex.matchEntire(it.msg)
            if (match != null) {
                val (secLeft) = match.destructured

                webSocket.sendText(
                    jsonObjectOf(
                        "type" to "tpTimer",
                        "tpType" to "outOfAdv",
                        "secLeft" to secLeft
                    )
                )
            }
        }

        ClientTickEvents.END_CLIENT_TICK.register {
            uuid2tpOut = uuid2tpOut.filterValues { System.currentTimeMillis() - it.at < 1000 }
        }

        HudRenderCallback.EVENT.register { draw, _ ->
            draw.drawText(
                MinecraftClient.getInstance().textRenderer,
                whatAdventureToDisplay,
                2,
                MinecraftClient.getInstance().window.scaledHeight - 11,
                0xFFFFFF,
                true
            )
        }

        WorldRenderLastCallback.event.register { event ->
            MinecraftClient.getInstance().world?.let { world ->
                RenderInWorldContext.renderInWorld(event) {
                    mc.player?.let { player ->
                        for ((uuid, tpOut) in uuid2tpOut.entries) {
                            world.players.find { it.uuidAsString == uuid }?.let {
                                val d = it.distanceTo(player)
                                if (player != it && d < 20) {
                                    withFacingThePlayer(it.pos.offset(Direction.UP, 2.8)) {
                                        text(Text.of("§e" + tpOut.secLeft + "s"))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
