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

    var enabled = false

    init {
        ClientTickEvents.END_CLIENT_TICK.register {
            if (mc.options.fov.value != 100 || !enabled) {
                disable = false
                return@register
            }

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
            if (mc.options.fov.value != 100 || !enabled) {
                return@register
            }

            val t = it.msg.trim()

            when (it.msg) {
                "No players bet on the winner!",
                "No bets placed on any sheep!",
                "Use /jackpot buy to purchase a ticket!",
                "Use /bah to view and place bids!",
                "Shuttling you to the galaxy...",
                "" -> return@register
            }

            if (listOf(
                    "Hello .+, please setup /2fa \\(2-Factor\\) Authentication to secure and protect your CosmicSky account!".toRegex(),
                    "(.+) Sheep is glowing! \\(\\+[\\d.]+x Prize Bonus\\)".toRegex(),
                    "^\\[/bah].+".toRegex(),
                    "^[0-9,]+ ticket\\(s\\) sold!".toRegex(),
                    "Winning Sheep: (.+) Sheep \\(\\d+ x\\)".toRegex(),
                    "^ ?\\*\\*\\*.+\\*\\*\\* ?".toRegex(),
//                "*** Sheep Roulette has begun! ***",
//                "*** Sheep Roulette has finished! ***",
//                "*** Sheep Roulette was cancelled! ***",
                    "^ ?\\(!\\).+".toRegex(),
//                    "\\(!\\) (.+) has found a [a-zA-Z]+ Clue Scroll!".toRegex(),
//                    "\\(!\\) (.+) has found a [a-zA-Z]+ Clue Scroll while .+!".toRegex(),
//                    "\\(!\\) .+ has won the /jackpot and received".toRegex(),
//                    "\\(!\\) Cosmic Jackpot.+".toRegex(),
//                    "\\(!\\) You found a [a-zA-Z]+ Clue Scroll from slaying!".toRegex(),
//                    ".*\\(!\\).* opened a .*Lootbox(:|( Bundle)).* and received:".toRegex(),
//                    "\\(!\\) .+ won the /bah on:".toRegex(),
//                    "\\(!\\) .+ has taken control /baltop #\\d with".toRegex(),
//                "(!) Bleed - READY",
//                "(!) You have successfully joined the queue, you can use /queue to check your position in the queue!",
//                "(!) Items are waiting to be recovered in the /is bin!",
                    "^ ?\\*.+".toRegex(),
//                    "^ ?\\* .+".toRegex(),
//                    "\\* (.+)'s /island has unlocked Level \\d+!".toRegex(),
//                    "\\* Inventory full, your [a-zA-Z]+ Clue Scroll has been placed in your /collect! \\*".toRegex(),
//                    "\\* Inventory full, your [a-zA-Z]+ Clue Scroll has been placed in your /collect! \\*".toRegex(),
//                "* Black Market Auction *",
                    "\\$[0-9,]+!( They purchased .+)?".toRegex(),
                    "^((Item)|(Winning Bid)|(Time)): .+".toRegex(),
                    "^\\+ \\$[0-9,]".toRegex()
                ).any { p -> p.matches(t) }
            ) return@register

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