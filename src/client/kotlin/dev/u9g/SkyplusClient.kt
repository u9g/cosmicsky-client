package dev.u9g

import com.eclipsesource.json.Json
import com.neovisionaries.ws.client.WebSocket
import com.neovisionaries.ws.client.WebSocketAdapter
import com.neovisionaries.ws.client.WebSocketFrame
import dev.u9g.events.CommandCallback
import dev.u9g.events.CommandEvent
import dev.u9g.features.*
import dev.u9g.util.Coroutines
import dev.u9g.util.MinecraftDispatcher
import dev.u9g.util.coroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minecraft.client.MinecraftClient
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.sound.SoundEvent
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

lateinit var mc: MinecraftClient
lateinit var webSocket: WebSocket
var isOnline = false

fun playSound(identifier: Identifier) {
    mc.soundManager.play(PositionedSoundInstance.master(SoundEvent.of(identifier), 1F))
}

suspend fun makeWebsocket() {
    withContext(MinecraftDispatcher) {
        async(coroutineContext) {
            webSocket = com.neovisionaries.ws.client.WebSocketFactory()
                .createSocket("wss://cosmicsky-server-1.onrender.com")
                .setPingInterval(10)
                .addListener(object : WebSocketAdapter() {
                    override fun onTextMessage(ws: WebSocket, message: String) {
                        println("received message: $message")
                        if (isOnline) {
                            val parsed = Json.parse(message).asObject()
                            val type = parsed["type"].asString()
                            when (type) {
                                "ping" -> {
                                    val username = parsed["username"].asString()
                                    val x = parsed["x"].asInt()
                                    val y = parsed["y"].asInt()
                                    val z = parsed["z"].asInt()
                                    pingsToRender = pingsToRender.filterNot { it.username == username } + Ping(
                                        BlockPos(x, y, z),
                                        System.currentTimeMillis(),
                                        username
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
                                    if (parsed["minimessage"] != null) {
                                        MinecraftClient.getInstance().player?.sendMessage(
                                            MiniMessage.miniMessage().deserialize(parsed["minimessage"].asString())
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
                                            val shouldShowPings = parsed["value"].asBoolean()

                                            Settings.showPings = shouldShowPings
                                        }
                                    }
                                }

                                else -> {
                                    println("Unexpected message type from websocket: $type")
                                }
                            }
                        }
                    }

                    override fun onDisconnected(
                        websocket: WebSocket?,
                        serverCloseFrame: WebSocketFrame?,
                        clientCloseFrame: WebSocketFrame?,
                        closedByServer: Boolean
                    ) {
                        super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer)
                        webSocket = webSocket.recreate().connect()
                    }
                })
                .connect()
        }.await()
    }
}

object SkyplusClient : ClientModInitializer {
    override fun onInitializeClient() {
        println("SkyPlus-Client starting.")
        JavaMain.LOGGER.info("SkyPlus-Client starting. VIA LOGGER")
        mc = MinecraftClient.getInstance()
        coroutineScope.launch {
            makeWebsocket()
        }
        Waypoints()
        Calculator()
        Coroutines()
        Teams()
        Settings.start()

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, ctx ->
            CommandCallback.event.invoker().invoke(CommandEvent(dispatcher, ctx, mc.networkHandler?.commandDispatcher))
        }
    }
}