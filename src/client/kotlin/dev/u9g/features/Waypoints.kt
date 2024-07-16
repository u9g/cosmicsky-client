package dev.u9g.features

import com.eclipsesource.json.Json
import dev.u9g.events.WorldRenderLastCallback
import dev.u9g.mc
import dev.u9g.util.FirmFormatters
import dev.u9g.util.render.RenderInWorldContext
import dev.u9g.util.worldName
import dev.u9g.webSocket
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import kotlin.math.floor

data class Ping(val pos: BlockPos, val time: Long, val username: String, val pingType: String, val worldName: String)

var pingsToRender = listOf<Ping>()

const val TIME_TO_WAIT_BEFORE_TARGETED_PING = 200

var lastTickDelta: Float = 0.0F
var lastDeathPing = System.currentTimeMillis()

class Waypoints {
    private val pingKey = KeyBinding("Ping!", InputUtil.GLFW_KEY_F, "Pings")
    private val focusPingKey = KeyBinding("Focus Ping!", InputUtil.GLFW_KEY_X, "Pings")
    private var startedPingingTime: Long = 0L
    private var startedFocusPingingTime: Long = 0L
    private var lastLowHPPing = System.currentTimeMillis()
    private val starIndicator = Identifier("skyplus", "newitemmarks.png")

    init {
        KeyBindingHelper.registerKeyBinding(pingKey)
        KeyBindingHelper.registerKeyBinding(focusPingKey)

        WorldRenderLastCallback.event.register { event ->
            if (pingsToRender.isEmpty()) return@register
            if (!Settings.showPingsInGame) return@register

            RenderInWorldContext.renderInWorld(event) {
                if ((System.currentTimeMillis() - startedPingingTime > TIME_TO_WAIT_BEFORE_TARGETED_PING && startedPingingTime != 0L) ||
                    (System.currentTimeMillis() - startedFocusPingingTime > TIME_TO_WAIT_BEFORE_TARGETED_PING && startedFocusPingingTime != 0L)
                ) {
                    color(1f, 1f, 1f, 1f)
                    val raycasted = MinecraftClient.getInstance().player!!.raycast(5000.0, event.tickDelta, false)
                    lastTickDelta = event.tickDelta
                    if (raycasted.type == HitResult.Type.BLOCK) {
                        withFacingThePlayer(raycasted.pos) {
                            val skin =
                                mc.networkHandler?.listedPlayerListEntries?.find { it.profile.name == MinecraftClient.getInstance().session.username }
                                    ?.skinTextures
                                    ?.texture
                            text(
                                Text.literal(MinecraftClient.getInstance().session.username),
                                Text.literal(
                                    "§2§lMY PING §r§e${
                                        FirmFormatters.formatDistance(
                                            mc.player?.pos?.distanceTo(
                                                raycasted.pos
                                            ) ?: 42069.0
                                        )
                                    }"
                                ),
                            )
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

                pingsToRender.forEach { ping ->
                    val worldName = worldName()

                    if (ping.worldName != "*" && ping.worldName != worldName) return@forEach

                    color(1f, 1f, 1f, 1f)

                    val skin =
                        mc.networkHandler?.listedPlayerListEntries?.find { it.profile.name == ping.username }
                            ?.skinTextures
                            ?.texture

                    withFacingThePlayer(ping.pos.toCenterPos()) {
                        val secPassed = (System.currentTimeMillis() - ping.time) / 1000
                        val minPassed = secPassed / 60
                        val prefix = when (ping.pingType) {
                            "death" -> {
                                "§4§lDEATH PING §r"
                            }

                            "lowhp" -> {
                                "§6§lLOW HP PING §r"
                            }

                            "focus" -> {
                                "§1§lFOCUS PING §r"
                            }

                            else -> {
                                ""
                            }
                        }
                        if (minPassed > 0) {
                            text(
                                Text.literal(ping.username),
                                Text.literal(
                                    prefix + "§e${FirmFormatters.formatDistance(mc.player?.pos?.distanceTo(ping.pos.toCenterPos()) ?: 42069.0)}" + " ꞏ " + minPassed.toString() + "m ago"
                                ),
                            )
                        } else if (secPassed > 0) {
                            text(
                                Text.literal(ping.username),
                                Text.literal(
                                    prefix + "§e${
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
                                    prefix + "§e${
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

                            if (secPassed <= 10) {
                                matrixStack.scale(2F, 2F, 1F)
                                matrixStack.translate(3F, -5F, 0F)
                                texture(
                                    starIndicator, 8, 8,
                                    0 / 8f, 0 / 8f,
                                    3 / 8f, 3 / 8f,
                                )
                            }
                        }
                    }
                }
            }
        }

        ClientTickEvents.END_CLIENT_TICK.register {
//            MinecraftClient.getInstance().player?.let {
//                if (System.currentTimeMillis() - lastLowHPPing > 1000 && it.health <= 6) {
//                    sendPing("lowhp", false)
//                    lastLowHPPing = System.currentTimeMillis()
//                }
//            }

            if (MinecraftClient.getInstance().currentScreen == null) {
                if (startedFocusPingingTime == 0L && focusPingKey.isPressed) {
                    startedFocusPingingTime = System.currentTimeMillis()
                } else if (startedFocusPingingTime != 0L && !focusPingKey.isPressed) {
                    sendPing(
                        "focus",
                        System.currentTimeMillis() - startedFocusPingingTime > TIME_TO_WAIT_BEFORE_TARGETED_PING
                    )
                    startedFocusPingingTime = 0L
                }

                if (startedPingingTime == 0L && pingKey.isPressed) {
                    startedPingingTime = System.currentTimeMillis()
                } else if (startedPingingTime != 0L && !pingKey.isPressed) {
                    sendPing(
                        "manual",
                        System.currentTimeMillis() - startedPingingTime > TIME_TO_WAIT_BEFORE_TARGETED_PING
                    )
                    startedPingingTime = 0L
                }
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

//        ChatMessageReceivedCallback.event.register {
//            if (System.currentTimeMillis() - lastDeathPing > 5000 && it.msg == "(!) Oh no, you have died!") {
//                sendPing("death", false)
//                lastDeathPing = System.currentTimeMillis()
//            }
//        }
    }
}

fun sendPing(pingType: String, isTargetedPing: Boolean) {
    val worldName = worldName()

    when (isTargetedPing) {
        true -> {
            val raycasted = MinecraftClient.getInstance().player!!.raycast(5000.0, lastTickDelta, false)
            if (raycasted.type == HitResult.Type.BLOCK) {
                webSocket.sendText(
                    jsonObjectOf(
                        "type" to "ping",
                        "pingType" to pingType,
                        "worldName" to worldName,
                        "x" to floor(raycasted.pos.x).toInt(),
                        "y" to floor(raycasted.pos.y).toInt(),
                        "z" to floor(raycasted.pos.z).toInt()
                    )
                )
            }
        }

        false -> {
            val block = MinecraftClient.getInstance().player!!.blockPos
            webSocket.sendText(
                jsonObjectOf(
                    "type" to "ping",
                    "pingType" to pingType,
                    "worldName" to worldName,
                    "x" to block.x,
                    "y" to block.y,
                    "z" to block.z
                )
            )
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