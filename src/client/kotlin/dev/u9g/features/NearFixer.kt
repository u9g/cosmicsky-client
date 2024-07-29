package dev.u9g.features

import dev.u9g.events.ChatMessageReceivedCallback
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text

class NearFixer {
    private var lastNear = 0L

    val nearRegex = "(?:Nearby Players \\(\\d+\\): )?(.+) \\((\\d+m)\\)".toRegex()

    init {
        ClientSendMessageEvents.COMMAND.register {
            if (it.startsWith("near")) {
                lastNear = System.currentTimeMillis()
            }
        }
        ChatMessageReceivedCallback.event.register {
            if (System.currentTimeMillis() - lastNear < 1000) {
                for (match in nearRegex.findAll(it.msg.replace("§.".toRegex(), ""))) {
                    val (_, a, b) = match.groupValues

                    MinecraftClient.getInstance().submit {
                        MinecraftClient.getInstance().player?.sendMessage(
                            Text.of(
                                "§b§lNearby player: ${
                                    if (Settings.teamMembers.contains(a)) {
                                        "§a§l"
                                    } else {
                                        "§c§l"
                                    }
                                }${a} §r-> $b"
                            )
                        )
                    }

                    it.isCancelled = true
                }
            }
        }
    }
}