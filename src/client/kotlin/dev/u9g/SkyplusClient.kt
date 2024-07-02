package dev.u9g

import com.eclipsesource.json.Json
import com.neovisionaries.ws.client.WebSocket
import com.neovisionaries.ws.client.WebSocketAdapter
import com.neovisionaries.ws.client.WebSocketFrame
import dev.u9g.commands.thenExecute
import dev.u9g.events.CommandCallback
import dev.u9g.events.CommandEvent
import dev.u9g.events.ServerConnectCallback
import dev.u9g.features.*
import dev.u9g.util.Coroutines
import dev.u9g.util.coroutineScope
import kotlinx.coroutines.launch
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.sound.SoundEvent
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

lateinit var mc: MinecraftClient
val webSocket = SkyPlusWebSocket(null)

fun playSound(identifier: Identifier) {
    mc.soundManager.play(PositionedSoundInstance.master(SoundEvent.of(identifier), 1F))
}

fun makeWebsocket() {
    coroutineScope.launch {
        val ws = com.neovisionaries.ws.client.WebSocketFactory()
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

                            if (Settings.shouldPingMakeSounds &&
                                username != MinecraftClient.getInstance().session.username
                            ) {
                                when (pingType) {
                                    "manual" -> {
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
                                    pingType
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

                                "allow_swinging_at_low_durability" -> {
                                    Settings.disableSwingingAtLowDurability = parsed["value"].asBoolean()
                                }

                                "should_ping_make_sounds" -> {
                                    Settings.shouldPingMakeSounds = parsed["value"].asBoolean()
                                }

                                "should_show_death_pings" -> {
                                    Settings.shouldShowDeathPings = parsed["value"].asBoolean()
                                }

                                "replace_fix_to_fix_all" -> {
                                    Settings.replaceFixToFixAll = parsed["value"].asBoolean()
                                }

                                "enable_mod" -> {
                                    Settings.enableMod = parsed["value"].asBoolean()
                                }

                                "what_adventure_to_display" -> {
                                    Settings.whatAdventureToDisplay = if (parsed["value"].isNull) {
                                        null
                                    } else {
                                        parsed["value"].asString()
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
                    MinecraftClient.getInstance().player?.sendMessage(Text.of("skyplus > websocket connected"))
                    super.onConnected(websocket, headers)

                    MinecraftClient.getInstance().session.uuidOrNull?.let {
                        webSocket.sendText(
                            jsonObjectOf(
                                "type" to "connected",
                                "username" to MinecraftClient.getInstance().session.username,
                                "uuid" to it.toString(),
                                "version" to "1.2.0"
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
                    MinecraftClient.getInstance().player?.sendMessage(Text.of("websocket disconnected, reconnect with /skyplusre"))
                    super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer)
                }
            })
            .connect()
        webSocket.ws = ws
    }.start()
}

class SkyPlusWebSocket(var ws: WebSocket?) {
    fun sendText(text: String) {
        val currWs = ws
        if (currWs?.isOpen == true) {
            currWs.sendText(text)
        } else {
            MinecraftClient.getInstance().player?.sendMessage(Text.of("websocket disconnected, reconnect with /skyplusre"))
        }
    }
}

object SkyplusClient : ClientModInitializer {
    override fun onInitializeClient() {
        println("SkyPlus-Client starting.")
        JavaMain.LOGGER.info("SkyPlus-Client starting. VIA LOGGER")
        mc = MinecraftClient.getInstance()
        ServerConnectCallback.event.register {
            makeWebsocket()
        }
        CommandCallback.event.register {
            it.register("skyplusre") {
                thenExecute {
                    webSocket.ws = null

                    makeWebsocket()
                }
            }
        }
        Waypoints()
        Calculator()
        Coroutines()
        Teams()
        Settings.start()
        AreaFishedOut()
        TPOutAnnouncer()
//        WhatAdventure()

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, ctx ->
            CommandCallback.event.invoker().invoke(CommandEvent(dispatcher, ctx, mc.networkHandler?.commandDispatcher))
        }
    }
}