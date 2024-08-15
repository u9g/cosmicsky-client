package dev.u9g.features

import dev.u9g.events.ChatMessageReceivedCallback
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text

object NearFixer {
    private var lastNear = 0L

    private val nearRegex = "^(?:Nearby Players \\(\\d+\\): )?(.+)$".toRegex()
    private val individualPlayerRegex = "(.+) \\((\\d)+m\\)".toRegex()

    init {
        ClientSendMessageEvents.COMMAND.register {
            if (Settings.enableMod && it.startsWith("near")) {
                lastNear = System.currentTimeMillis()
            }
        }
        ChatMessageReceivedCallback.event.register {
            if (Settings.enableMod && System.currentTimeMillis() - lastNear < 1000) {
                for (match in nearRegex.findAll(it.msg)) {
                    val (_, players) = match.groupValues
                    for (playerString in players.split(", ")) {
                        individualPlayerRegex.find(playerString)?.let { matchResult ->
                            val (player, duration) = matchResult.destructured
                            MinecraftClient.getInstance().submit {
                                MinecraftClient.getInstance().player?.sendMessage(
                                    Text.of(
                                        "§b§lNearby player: ${
                                            if (Settings.teamMembers.contains(player)) {
                                                "§a§l"
                                            } else {
                                                "§c§l"
                                            }
                                        }${player} §r-> ${duration}m"
                                    )
                                )
                            }
                            it.isCancelled = true
                        }
                    }
                }
            }
        }
    }
}