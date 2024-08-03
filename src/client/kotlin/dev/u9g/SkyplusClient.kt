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
import dev.u9g.features.`fun`.ImHighUp
import dev.u9g.util.Coroutines
import dev.u9g.util.coroutineScope
import dev.u9g.util.worldName
import kotlinx.coroutines.launch
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.sound.SoundEvent
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


lateinit var mc: MinecraftClient
var ex: ExecutorService = Executors.newSingleThreadExecutor()
val webSocket = SkyPlusWebSocket(null)

fun playSound(identifier: Identifier) {
    MinecraftClient.getInstance().submit {
        mc.soundManager.play(PositionedSoundInstance.master(SoundEvent.of(identifier), 1F))
    }
}

fun makeWebsocket() {
    coroutineScope.launch {
        ex.shutdownNow()
        ex.awaitTermination(1, TimeUnit.DAYS)
        ex = Executors.newSingleThreadExecutor()

        ex.submit {
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
                                        Settings.fAlias = parsed["value"].asBoolean()
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
                        MinecraftClient.getInstance().player?.sendMessage(Text.of("§b§l(!) SkyPlus -> §eWebsocket successfully connected!"))
                        super.onConnected(websocket, headers)

                        MinecraftClient.getInstance().session.uuidOrNull?.let {
                            webSocket.sendText(
                                jsonObjectOf(
                                    "type" to "connected",
                                    "username" to MinecraftClient.getInstance().session.username,
                                    "uuid" to it.toString(),
                                    "version" to "1.2.4"
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
                        MinecraftClient.getInstance().player?.sendMessage(Text.of("§a§l(!) SEVERE!!! §aWebsocket disconnected! Reconnect with /skyplusre"))
                        super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer)
                    }
                })
                .connect()
            webSocket.ws = ws
        }
    }
}

class SkyPlusWebSocket(var ws: WebSocket?) {
    fun sendText(text: String) {
        val currWs = ws
        if (currWs != null) {
            if (!currWs.isOpen) {
                MinecraftClient.getInstance().player?.sendMessage(Text.of("§a§l(!) SEVERE!!! §aWebsocket disconnected! Reconnect with /skyplusre"))
            }
            currWs.sendText(text)
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
                    makeWebsocket()
                }
            }

        }
        Waypoints
        Calculator
        Coroutines
        Teams
        Settings
        AreaFishedOut
        TPOutAnnouncer
        ImHighUp
        MobCps
        IslandMembers
        NearFixer
        CooldownManager
        HidePOINotification
        PetCooldowns
        ClueScrollManager
        ChaoticZoneEnter

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, ctx ->
            CommandCallback.event.invoker().invoke(CommandEvent(dispatcher, ctx, mc.networkHandler?.commandDispatcher))
        }
    }
}