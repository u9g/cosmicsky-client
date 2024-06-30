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

data class Ping(val pos: BlockPos, val time: Long, val username: String)

var pingsToRender = listOf<Ping>()

class Waypoints {
    private val pingKey = KeyBinding("Ping!", InputUtil.GLFW_KEY_F, "Pings")
    private var pinging = false

    init {
        KeyBindingHelper.registerKeyBinding(pingKey)

        WorldRenderLastCallback.event.register { event ->
            if (pingsToRender.isEmpty()) return@register
            RenderInWorldContext.renderInWorld(event) {
                pingsToRender.forEach { ping ->
                    color(1f, 1f, 1f, 1f)
                    val skin =
                        mc.networkHandler?.listedPlayerListEntries?.find { it.profile.name == ping.username }
                            ?.skinTextures
                            ?.texture
                    withFacingThePlayer(ping.pos.toCenterPos()) {
                        val secPassed = (System.currentTimeMillis() - ping.time) / 1000
                        if (secPassed > 0) {
                            text(
                                Text.literal(ping.username),
                                Text.literal("§e${FirmFormatters.formatDistance(mc.player?.pos?.distanceTo(ping.pos.toCenterPos()) ?: 42069.0)}" + " ꞏ " + secPassed.toString() + "s ago"),
                            )

                        } else {
                            text(
                                Text.literal(ping.username),
                                Text.literal("§e${FirmFormatters.formatDistance(mc.player?.pos?.distanceTo(ping.pos.toCenterPos()) ?: 42069.0)}")
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
                        "x" to block.x,
                        "y" to block.y,
                        "z" to block.z
                    )
                )
                pinging = true
            } else if (pinging && !pingKey.isPressed) {
                pinging = false
            }

            pingsToRender = pingsToRender.filter { System.currentTimeMillis() - it.time < 60_000 }
        }
    }
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