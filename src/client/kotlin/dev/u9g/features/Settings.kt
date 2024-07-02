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
    var disableSwingingAtLowDurability: Boolean = true
    var shouldPingMakeSounds: Boolean = true
    var shouldShowDeathPings: Boolean = true
    var replaceFixToFixAll: Boolean = true
    var shouldShowMobsPerSecond: Boolean = true
    var enableMod: Boolean = false

    private var internalWhatAdventureToDisplay: String? = null
    var whatAdventureToDisplay: String?
        get() = internalWhatAdventureToDisplay
        set(value) {
            webSocket.sendText(
                jsonObjectOf(
                    "type" to "settingsCmd",
                    "cmd" to "what_adventure_to_display ${value.toString()}"
                )
            )
            internalWhatAdventureToDisplay = value
        }

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