package dev.u9g.features

import dev.u9g.events.HeadingRendererCallback
import dev.u9g.events.OverlayTextCallback
import dev.u9g.webSocket
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.network.OtherClientPlayerEntity
import net.minecraft.text.Text

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

        HeadingRendererCallback.event.register {
            if (it.distance <= 4096.0) {
                val player = it.entity
                if (player is OtherClientPlayerEntity) {
                    uuid2tpOut[player.uuidAsString]?.let { tpOut ->
                        it.matrixStack.push()
                        it.matrixStack.translate(0.0, 0.5, 0.0)
                        it.renderer.accept(Text.of("Out in §e§l" + tpOut.secLeft + "s"))
                        it.matrixStack.pop()
                    }
                }
            }
        }
    }
}