package dev.u9g.features

import dev.u9g.events.OverlayTextCallback
import dev.u9g.features.Settings.whatAdventureToDisplay
import dev.u9g.webSocket
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient

var username2tpOut: Map<String, TPOut> = mapOf()

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
            username2tpOut = username2tpOut.filterValues { System.currentTimeMillis() - it.at < 1000 }
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
    }
}
