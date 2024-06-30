package dev.u9g.features

import dev.u9g.commands.thenExecute
import dev.u9g.events.CommandCallback
import dev.u9g.webSocket
import net.minecraft.client.MinecraftClient

object Settings {
    var showPings: Boolean = true
    var showPingsInChat: Boolean = false

    fun start() {
        CommandCallback.event.register {
            it.register("skyplussettings") {
                thenExecute {
                    MinecraftClient.getInstance().player?.let { player ->
                        webSocket.sendText(
                            jsonObjectOf(
                                "type" to "showSettings"
                            )
                        )
                    }
                }
            }
        }
    }
}