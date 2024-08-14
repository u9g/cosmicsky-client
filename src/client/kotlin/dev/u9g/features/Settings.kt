package dev.u9g.features

import com.mojang.brigadier.arguments.StringArgumentType
import dev.u9g.commands.RestArgumentType
import dev.u9g.commands.get
import dev.u9g.commands.thenArgument
import dev.u9g.commands.thenExecute
import dev.u9g.events.ChatMessageReceivedCallback
import dev.u9g.events.CommandCallback

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
    var colorMaxEnchants: Boolean = true
    var focusedPlayerUsername: String = "gg"
    var enableMod: Boolean = false

    init {
        ChatMessageReceivedCallback.event.register {
            if (it.msg == "(!) Your inventory is currently full! Make some room to pickup more items!") {
            }
        }

        CommandCallback.event.register {
            it.deleteCommand("f")
            it.register("f") {
                thenArgument("username", StringArgumentType.string()) { username ->
                    thenExecute {
                        Websocket.sendText(
                            jsonObjectOf(
                                "type" to "focus",
                                "username" to this[username]
                            )
                        )
                    }
                }
            }

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