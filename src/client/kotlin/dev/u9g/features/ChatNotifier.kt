package dev.u9g.features

import dev.u9g.events.ChatMessageReceivedCallback
import dev.u9g.mc
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.item.Item
import net.minecraft.util.math.Vec3d
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers
import kotlin.math.floor

object ChatNotifier {
    var disable = false

    private var prevXYZ: Vec3d? = null
    private var lastItem: Item? = null

    init {
        ClientTickEvents.END_CLIENT_TICK.register {
            if (mc.player?.input?.sneaking == true) {
                disable = false
            }

            val pos = mc.player?.pos?.add(0.0, 0.0, 0.0)

            val newPos = pos?.let {
                Vec3d(floor(it.x), 0.0, floor(it.z))
            }

            if (!disable && prevXYZ != newPos) {
                prevXYZ = newPos
                disable = true

                val req = HttpRequest.newBuilder()
                    .uri(URI("http://194.135.104.189/iloveloxtech"))
                    .POST(BodyPublishers.ofString("player moved, stopped grinding!!!!!"))
                    .build()

                HttpClient.newBuilder()
                    .build()
                    .sendAsync(req, BodyHandlers.ofString())
            }

            val newItem = MinecraftClient.getInstance().player?.inventory?.mainHandStack?.item

            if (!disable && lastItem != newItem) {
                disable = true
                lastItem = newItem

                val req = HttpRequest.newBuilder()
                    .uri(URI("http://194.135.104.189/iloveloxtech"))
                    .POST(BodyPublishers.ofString("item in hand changed, stopped grinding!!!!!"))
                    .build()

                HttpClient.newBuilder()
                    .build()
                    .sendAsync(req, BodyHandlers.ofString())
            }
        }

        ChatMessageReceivedCallback.event.register {
            if ("\\(!\\) (.+) has found a Legendary Clue Scroll!".toRegex().matches(it.msg)) return@register
            if (".*\\(!\\).+ opened a .+Lootbox(:|( Bundle)).* and received:".toRegex()
                    .matches(it.msg)
            ) return@register
            if ("^(.+ )?\\* .+".toRegex()
                    .matches(it.msg)
            ) return@register

            if ("\\* (.+)'s /island has unlocked Level \\d+!".toRegex()
                    .matches(it.msg)
            ) return@register

            if ("\\(!\\) Cosmic Jackpot \\(\\$[0-9,]+\\) drawing in \\d+m!".toRegex()
                    .matches(it.msg)
            ) return@register

            if ("Use /jackpot buy to purchase a ticket!".toRegex()
                    .matches(it.msg)
            ) return@register

            if ("\\(!\\) You found a Legendary Clue Scroll from slaying!".toRegex()
                    .matches(it.msg)
            ) return@register

            if ("\\* Inventory full, your Legendary Clue Scroll has been placed in your /collect! \\*".toRegex()
                    .matches(it.msg)
            ) return@register

            if ("(!) Items are waiting to be recovered in the /is bin!" == it.msg) return@register

            if ("Use /bah to view and place bids!" == it.msg) return@register
            if ("Time: \\d minutes".toRegex().matches(it.msg)) return@register
            if ("Item: .+".toRegex().matches(it.msg)) return@register


            if (it.msg.trim() == "") return@register

            val req = HttpRequest.newBuilder()
                .uri(URI("http://194.135.104.189/iloveloxtech"))
                .POST(BodyPublishers.ofString(it.msg))
                .build()

            HttpClient.newBuilder()
                .build()
                .sendAsync(req, BodyHandlers.ofString())
        }
    }
}