package dev.u9g.features

import com.mojang.brigadier.arguments.StringArgumentType
import dev.u9g.commands.get
import dev.u9g.commands.thenArgument
import dev.u9g.commands.thenExecute
import dev.u9g.events.CommandCallback
import dev.u9g.webSocket
import net.minecraft.client.MinecraftClient

object Teams {
    init {
        CommandCallback.event.register {
            it.register("createteam") {
                thenArgument("team name", StringArgumentType.string()) { teamName ->
                    thenExecute {
                        MinecraftClient.getInstance().player?.let { player ->
                            webSocket.sendText(
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
                        webSocket.sendText(
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
                        webSocket.sendText(
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
                    webSocket.sendText(
                        jsonObjectOf(
                            "type" to "leaveTeam"
                        )
                    )
                }
            }
            it.register("kickfromteam") {
                thenArgument("player name", StringArgumentType.string()) { playerName ->
                    thenExecute {
                        webSocket.sendText(
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
                    webSocket.sendText(
                        jsonObjectOf(
                            "type" to "listTeamMembers"
                        )
                    )
                }
            }
            it.register("disbandteam") {
                thenExecute {
                    webSocket.sendText(
                        jsonObjectOf(
                            "type" to "disbandTeam"
                        )
                    )
                }
            }
        }
    }
}