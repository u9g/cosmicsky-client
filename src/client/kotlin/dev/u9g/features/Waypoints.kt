package dev.u9g.features

import com.eclipsesource.json.Json
import dev.u9g.events.WorldRenderLastCallback
import dev.u9g.isOnline
import dev.u9g.mc
import dev.u9g.util.FirmFormatters
import dev.u9g.util.render.RenderInWorldContext
import dev.u9g.webSocket
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos

data class Ping(val pos: BlockPos, val time: Long, val username: String, val pingType: String)

var pingsToRender = listOf<Ping>()

class Waypoints {
    private val pingKey = KeyBinding("Ping!", InputUtil.GLFW_KEY_F, "Pings")
    private var pinging = false

    init {
        KeyBindingHelper.registerKeyBinding(pingKey)

        WorldRenderLastCallback.event.register { event ->
            if (pingsToRender.isEmpty()) return@register
            if (!Settings.showPingsInGame) return@register

            RenderInWorldContext.renderInWorld(event) {
                pingsToRender.forEach { ping ->
                    if (!Settings.shouldShowDeathPings && ping.pingType == "death") return@forEach

                    color(1f, 1f, 1f, 1f)
                    val skin =
                        mc.networkHandler?.listedPlayerListEntries?.find { it.profile.name == ping.username }
                            ?.skinTextures
                            ?.texture
                    withFacingThePlayer(ping.pos.toCenterPos()) {
                        val secPassed = (System.currentTimeMillis() - ping.time) / 1000
                        val minPassed = secPassed / 60
                        val deathPrefix = if (ping.pingType == "death") {
                            "§4§lDEATH PING §r"
                        } else {
                            ""
                        }
                        if (minPassed > 0) {
                            text(
                                Text.literal(ping.username),
                                Text.literal(
                                    deathPrefix + "§e${FirmFormatters.formatDistance(mc.player?.pos?.distanceTo(ping.pos.toCenterPos()) ?: 42069.0)}" + " ꞏ " + minPassed.toString() + "m ago"
                                ),
                            )
                        } else if (secPassed > 0) {
                            text(
                                Text.literal(ping.username),
                                Text.literal(
                                    deathPrefix + "§e${
                                        FirmFormatters.formatDistance(
                                            mc.player?.pos?.distanceTo(
                                                ping.pos.toCenterPos()
                                            ) ?: 42069.0
                                        )
                                    }" + " ꞏ " + secPassed.toString() + "s ago"
                                ),
                            )

                        } else {
                            text(
                                Text.literal(ping.username),
                                Text.literal(
                                    deathPrefix + "§e${
                                        FirmFormatters.formatDistance(
                                            mc.player?.pos?.distanceTo(
                                                ping.pos.toCenterPos()
                                            ) ?: 42069.0
                                        )
                                    }"
                                )
                            )
                        }
                        if (skin != null) {
                            matrixStack.translate(0F, -20F, 0F)
                            // Head front
                            texture(
                                skin, 16, 16,
                                1 / 8f, 1 / 8f,
                                2 / 8f, 2 / 8f,
                            )
                            // Head overlay
                            texture(
                                skin, 16, 16,
                                5 / 8f, 1 / 8f,
                                6 / 8f, 2 / 8f,
                            )
                        }
                    }
                }
            }
        }

        ClientPlayConnectionEvents.JOIN.register { handler, _, client ->
            val netHandler = client.networkHandler ?: return@register
            if (handler.connection.isLocal) return@register
            if (netHandler.world.isClient) {
                println("skyplus > sent connected")
                webSocket.sendText(
                    jsonObjectOf(
                        "type" to "connected",
                        "username" to MinecraftClient.getInstance().session.username,
                        "uuid" to MinecraftClient.getInstance().player!!.uuidAsString
                    )
                )
                isOnline = true
            }
        }
        ClientPlayConnectionEvents.DISCONNECT.register { handler, client ->
            val netHandler = client.networkHandler ?: return@register
            if (handler.connection.isLocal) return@register
            if (netHandler.world.isClient) {
                webSocket.sendText(jsonObjectOf("type" to "disconnected"))
                isOnline = false
            }
        }
        ClientTickEvents.END_CLIENT_TICK.register {
            if (!pinging && pingKey.isPressed && isOnline) {
                val block = MinecraftClient.getInstance().player!!.blockPos
                webSocket.sendText(
                    jsonObjectOf(
                        "type" to "ping",
                        "pingType" to "manual",
                        "x" to block.x,
                        "y" to block.y,
                        "z" to block.z
                    )
                )
                pinging = true
            } else if (pinging && !pingKey.isPressed) {
                pinging = false
            }

            pingsToRender = pingsToRender.filter {
                when (it.pingType) {
                    // five minutes for items to despawn + 1minute to mourn the loss I guess
                    "death" -> System.currentTimeMillis() - it.time < 6 * 60_000
                    "manual" -> System.currentTimeMillis() - it.time < 60_000

                    // should be unreachable vvv
                    else -> System.currentTimeMillis() - it.time < 60_000
                }
            }
        }
    }
}

fun onDeath() {
    val block = MinecraftClient.getInstance().player!!.blockPos
    webSocket.sendText(
        jsonObjectOf(
            "type" to "ping",
            "pingType" to "death",
            "x" to block.x,
            "y" to block.y,
            "z" to block.z
        )
    )
}

fun jsonObjectOf(vararg pairs: Pair<String, *>) =
    (mapOf(*pairs) to Json.`object`())
        .also { (pairs, json) ->
            pairs.forEach { k ->
                when (val value = k.value) {
                    is String -> json.add(k.key, value)
                    is Int -> json.add(k.key, value)
                    else -> throw RuntimeException("Unable to serialize $value")
                }
            }
        }.second.toString()