package dev.u9g.features

import dev.u9g.commands.RestArgumentType
import dev.u9g.commands.get
import dev.u9g.commands.thenArgument
import dev.u9g.commands.thenExecute
import dev.u9g.events.CommandCallback
import dev.u9g.webSocket

object Settings {
    var showPingsInGame: Boolean = true
    var showPingsInChat: Boolean = false

    fun start() {
        CommandCallback.event.register {
            it.register("skyplussettings") {
                thenExecute {
                    webSocket.sendText(
                        jsonObjectOf(
                            "type" to "showSettings"
                        )
                    )
                }

                thenArgument("command", RestArgumentType) { cmd ->
                    thenExecute {
                        webSocket.sendText(
                            jsonObjectOf(
                                "type" to "settingsCmd",
                                "cmd" to this[cmd]
                            )
                        )
                    }
                }
            }
        }
    }
}