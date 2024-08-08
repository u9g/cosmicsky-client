package dev.u9g.features

import com.eclipsesource.json.Json
import com.neovisionaries.ws.client.WebSocket
import com.neovisionaries.ws.client.WebSocketAdapter
import com.neovisionaries.ws.client.WebSocketFrame
import dev.u9g.playSound
import dev.u9g.printSession
import dev.u9g.util.worldName
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import java.util.concurrent.Executors

object Websocket {
    private var lastTimeSendingMessage = 0L
    private var websocket = com.neovisionaries.ws.client.WebSocketFactory()
        .createSocket("wss://cosmicsky-server.onrender.com")
        .setPingInterval(10)
        .addListener(object : WebSocketAdapter() {
            override fun onTextMessage(ws: WebSocket, message: String) {
                println("received message: $message")

                val parsed = Json.parse(message).asObject()
                val type = parsed["type"].asString()
                when (type) {
                    "tpTimer" -> {
                        val uuid = parsed["uuid"].asString()
                        val secLeft = parsed["secLeft"].asString()

                        uuid2tpOut = uuid2tpOut + Pair(uuid, TPOut(secLeft))
                    }

                    "ping" -> {
                        val username = parsed["username"].asString()
                        val x = parsed["x"].asInt()
                        val y = parsed["y"].asInt()
                        val z = parsed["z"].asInt()
                        val pingType = parsed["pingType"].asString()
                        val worldName = parsed["worldName"].asString()

                        if (Settings.shouldPingMakeSounds &&
                            username != MinecraftClient.getInstance().session.username &&
                            (worldName == worldName() || pingType == "death")
                        ) {
                            when (pingType) {
                                "manual", "focus" -> {
                                    playSound(Identifier("minecraft", "block.note_block.pling"))
                                }

                                "death" -> {
                                    playSound(Identifier("minecraft", "entity.evoker.death"))
                                }
                            }
                        }

                        pingsToRender =
                            pingsToRender.filterNot { it.username == username && it.pingType != "death" } + Ping(
                                BlockPos(x, y, z),
                                System.currentTimeMillis(),
                                username,
                                pingType,
                                worldName
                            )

                        if (Settings.showPingsInChat) {
                            MinecraftClient.getInstance().inGameHud.chatHud.addMessage(
                                Text.of(
                                    "$username pinged at ($x,$y,$z)"
                                )
                            )
                        }
                    }

                    "notification" -> {
                        if (parsed["json"] != null) {
                            MinecraftClient.getInstance().inGameHud.chatHud.addMessage(
                                Text.Serialization.fromLenientJson(
                                    parsed["json"].asString()
                                )
                            )
                        } else if (parsed["message"] != null) {
                            MinecraftClient.getInstance().inGameHud.chatHud.addMessage(
                                Text.of(
                                    parsed["message"].asString()
                                )
                            )
                        }
                    }

                    "setting" -> {
                        val settingName = parsed["name"].asString()

                        when (settingName) {
                            "show_pings" -> {
                                Settings.showPingsInGame = parsed["value"].asBoolean()
                            }

                            "pings_sent_to_chat" -> {
                                Settings.showPingsInChat = parsed["value"].asBoolean()
                            }

                            "disable_swinging_at_low_durability" -> {
                                Settings.disableSwingingAtLowDurability = parsed["value"].asBoolean()
                            }

                            "should_ping_make_sounds" -> {
                                Settings.shouldPingMakeSounds = parsed["value"].asBoolean()
                            }

                            "replace_fix_to_fix_all" -> {
                                Settings.replaceFixToFixAll = parsed["value"].asBoolean()
                            }

                            "enable_mod" -> {
                                Settings.enableMod = parsed["value"].asBoolean()
                            }

                            "should_show_mobs_per_second" -> {
                                Settings.shouldShowMobsPerSecond = parsed["value"].asBoolean()
                            }

                            "should_allow_breaking_glass" -> {
                                Settings.shouldAllowBreakingGlass = parsed["value"].asBoolean()
                            }

                            "t_alias" -> {
                                Settings.tAlias = parsed["value"].asBoolean()
                            }

                            "f_alias" -> {
                                Settings.zAlias = parsed["value"].asBoolean()
                            }

                            "single_escape_closes_chat" -> {
                                Settings.singleEscapeClosesChat = parsed["value"].asBoolean()
                            }

                            "should_hide_poi_notification" -> {
                                Settings.shouldHidePOINotification = parsed["value"].asBoolean()
                            }

                            "redirect_chat_a_to_chat_ally" -> {
                                Settings.redirectChatAToChatAlly = parsed["value"].asBoolean()
                            }

                            "color_max_enchants" -> {
                                Settings.colorMaxEnchants = parsed["value"].asBoolean()
                            }
                        }
                    }

                    "team_members" -> {
                        if (parsed["playerUsernames"].isArray) {
                            Settings.teamMembers = parsed["playerUsernames"].asArray().mapNotNull {
                                if (it.isString) {
                                    it.asString()
                                } else {
                                    null
                                }
                            }
                        }
                    }

                    else -> {
                        println("Unexpected message type from websocket: $type")
                    }
                }
            }

            override fun onConnected(websocket: WebSocket?, headers: MutableMap<String, MutableList<String>>?) {
                super.onConnected(websocket, headers)

                MinecraftClient.getInstance().player?.sendMessage(Text.of("§b§l(!) SkyPlus -> §eWebsocket successfully connected!"))

                MinecraftClient.getInstance().session.uuidOrNull?.let {
                    printSession("MinecraftClient.getInstance().session.uuidOrNull")
                    isRestarting = false
                    sendText(
                        jsonObjectOf(
                            "type" to "connected",
                            "username" to MinecraftClient.getInstance().session.username,
                            "uuid" to it.toString(),
                            "version" to "1.2.9"
                        )
                    )
                }
            }

            override fun onDisconnected(
                websocket: WebSocket?,
                serverCloseFrame: WebSocketFrame?,
                clientCloseFrame: WebSocketFrame?,
                closedByServer: Boolean
            ) {
                if (!isRestarting) {
                    if (System.currentTimeMillis() - lastTimeSendingMessage > 500) {
                        MinecraftClient.getInstance().player?.sendMessage(Text.of("§a§l[!] SEVERE!!! §aWebsocket disconnected! Reconnect with /skyplusre"))
                        lastTimeSendingMessage = System.currentTimeMillis()
                    }
                    executor.submit {
                        println("[gg] ondisconnected reset")
                        reset()
                    }
                }
                super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer)
            }
        })

    var isRestarting = false
    private val executor = Executors.newFixedThreadPool(2)

    init {
        websocket.connect()
    }

    fun reset() {
        if (!isRestarting) {
            isRestarting = true
            executor.submit {
                websocket.sendClose()
                websocket = websocket.recreate().connect()
            }
        }
    }

    fun sendText(text: String) {
        if (!websocket.isOpen && !isRestarting) {
            if (System.currentTimeMillis() - lastTimeSendingMessage > 500) {
                MinecraftClient.getInstance().player?.sendMessage(Text.of("§a§l(!) SEVERE!!! §aWebsocket disconnected! Reconnect with /skyplusre"))
                lastTimeSendingMessage = System.currentTimeMillis()
            }
            println("[gg] sendtext reset")
            reset()
        }
        websocket.sendText(text)
    }
}