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
            if ("(!) Bleed - READY" == it.msg.trim()) return@register
            if ("No players bet on the winner!" == it.msg.trim()) return@register
            if ("*** Sheep Roulette has finished! ***" == it.msg.trim()) return@register
            if ("*** Sheep Roulette has begun! ***" == it.msg.trim()) return@register
            if ("(.+) Sheep is glowing! \\(\\+[\\d.]+x Prize Bonus\\)".toRegex().matches(it.msg)) return@register

            if ("\\(!\\) (.+) has found a Legendary Clue Scroll!".toRegex().matches(it.msg)) return@register
            if ("\\(!\\) (.+) has found a Legendary Clue Scroll while .+!".toRegex().matches(it.msg)) return@register
            if ("^\\[/bah].+".toRegex().matches(it.msg)) return@register
            if ("^[0-9,]+ ticket\\(s\\) sold!".toRegex().matches(it.msg)) return@register

            if ("Winning Sheep: (.+) Sheep \\(\\d+ x\\)".toRegex().matches(it.msg)) return@register

            if (".*\\(!\\).* opened a .*Lootbox(:|( Bundle)).* and received:".toRegex()
                    .matches(it.msg)
            ) return@register

            if ("^ ?\\* .+".toRegex()
                    .matches(it.msg)
            ) return@register

            if ("\\* (.+)'s /island has unlocked Level \\d+!".toRegex()
                    .matches(it.msg)
            ) return@register

            if ("\\(!\\) .+ has won the /jackpot and received".toRegex()
                    .matches(it.msg)
            ) return@register

            if ("\\(!\\) Cosmic Jackpot.+".toRegex()
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

            if ("\\(!\\) .+ won the /bah on:".toRegex()
                    .matches(it.msg)
            ) return@register

            if ("\\$[0-9,]+!".toRegex()
                    .matches(it.msg)
            ) return@register

            if ("\\(!\\) .+ has taken control /baltop #\\d with".toRegex()
                    .matches(it.msg)
            ) return@register

            if ("(!) Items are waiting to be recovered in the /is bin!" == it.msg) return@register

            if ("Use /bah to view and place bids!" == it.msg) return@register
            if ("((Item)|(Winning Bid)|(Time)): .+".toRegex().matches(it.msg)) return@register


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