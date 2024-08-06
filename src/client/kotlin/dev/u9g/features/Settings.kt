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
    var replaceFixToFixAll: Boolean = true
    var shouldShowMobsPerSecond: Boolean = true
    var shouldAllowBreakingGlass: Boolean = false
    var tAlias: Boolean = true
    var zAlias: Boolean = true
    var singleEscapeClosesChat: Boolean = true
    var shouldHidePOINotification: Boolean = true
    var redirectChatAToChatAlly: Boolean = true
    var teamMembers: List<String> = listOf()
    var enableMod: Boolean = false

    init {
        CommandCallback.event.register {
            it.register("skyplussettings") {
                thenExecute {
                    Websocket.sendText(
                        jsonObjectOf(
                            "type" to "showSettings"
                        )
                    )
                }

                thenArgument("command", RestArgumentType) { cmd ->
                    thenExecute {
                        Websocket.sendText(
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