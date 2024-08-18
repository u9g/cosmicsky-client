package dev.u9g.features

import com.mojang.brigadier.arguments.StringArgumentType
import dev.u9g.commands.get
import dev.u9g.commands.thenArgument
import dev.u9g.commands.thenExecute
import dev.u9g.events.CommandCallback
import net.minecraft.client.MinecraftClient

fun pvInfoOfUsername(username: String) {
    Websocket.sendText(
        jsonObjectOf(
            "type" to "pv_info_of",
            "player_username" to username
        )
    )
}

object Teams {
    init {
        CommandCallback.event.register {
            it.register("adminbypasswalls") {
                thenExecute {
                    pvInfoOfUsername(MinecraftClient.getInstance().session.username)
                }
            }

            it.register("createteam") {
                thenArgument("team name", StringArgumentType.string()) { teamName ->
                    thenExecute {
                        MinecraftClient.getInstance().player?.let { player ->
                            Websocket.sendText(
                                jsonObjectOf(
                                    "type" to "createTeam",
                                    "teamName" to this[teamName]
                                )
                            )
                        }
                    }
                }
            }
            it.register("invitetoteam") {
                thenArgument("player name", StringArgumentType.string()) { playerName ->
                    thenExecute {
                        Websocket.sendText(
                            jsonObjectOf(
                                "type" to "invitetoteam",
                                "playerInvited" to this[playerName]
                            )
                        )
                    }
                }
            }
            it.register("jointeam") {
                thenArgument("team name", StringArgumentType.string()) { teamName ->
                    thenExecute {
                        Websocket.sendText(
                            jsonObjectOf(
                                "type" to "joinTeam",
                                "teamName" to this[teamName]
                            )
                        )
                    }
                }
            }
            it.register("leaveTeam") {
                thenExecute {
                    Websocket.sendText(
                        jsonObjectOf(
                            "type" to "leaveTeam"
                        )
                    )
                }
            }
            it.register("kickfromteam") {
                thenArgument("player name", StringArgumentType.string()) { playerName ->
                    thenExecute {
                        Websocket.sendText(
                            jsonObjectOf(
                                "type" to "kickFromTeam",
                                "playerName" to this[playerName]
                            )
                        )
                    }
                }
            }
            it.register("listteammembers") {
                thenExecute {
                    Websocket.sendText(
                        jsonObjectOf(
                            "type" to "listTeamMembers"
                        )
                    )
                }
            }
            it.register("disbandteam") {
                thenExecute {
                    Websocket.sendText(
                        jsonObjectOf(
                            "type" to "disbandTeam"
                        )
                    )
                }
            }
        }
    }
}