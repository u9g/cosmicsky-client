package dev.u9g

import com.eclipsesource.json.Json
import com.neovisionaries.ws.client.WebSocket
import com.neovisionaries.ws.client.WebSocketAdapter
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.text.Text
import java.net.InetSocketAddress

lateinit var mc: MinecraftClient
lateinit var webSocket: WebSocket
var isOnline = false

val pingKey = KeyBinding("Ping!", InputUtil.GLFW_KEY_F, "Pings")
var pinging = false

data class Ping(val x: Int, val y: Int, val z: Int, val time: Long)

var pingsToRender = listOf<Ping>()

fun createWebsocket() {
	webSocket = com.neovisionaries.ws.client.WebSocketFactory()
		.createSocket("wss://cosmicsky-server.onrender.com")
		.addListener(object : WebSocketAdapter() {
			override fun onTextMessage(ws: WebSocket, message: String) {
				try {
					println("received message: $message")
					if (isOnline) {
						val parsed = Json.parse(message).asObject()
						val username = parsed["username"].asString()
						val x = parsed["x"].asInt()
						val y = parsed["y"].asInt()
						val z = parsed["z"].asInt()
						pingsToRender = pingsToRender + Ping(x, y, z, System.currentTimeMillis())
						MinecraftClient.getInstance().inGameHud.chatHud.addMessage(Text.of(
							"$username pinged at ($x,$y,$z)"))
					}
				} catch (e: Exception) {}
			}
		})
		.connect()
}

object SkyplusClient : ClientModInitializer {
	override fun onInitializeClient() {
		mc = MinecraftClient.getInstance()
		createWebsocket()
		KeyBindingHelper.registerKeyBinding(pingKey)
		ClientPlayConnectionEvents.JOIN.register { handler, _, client ->
			val netHandler = client.networkHandler ?: return@register
			if (handler.connection.isLocal) return@register
			if (netHandler.world.isClient) {
				webSocket.sendText(jsonObjectOf(
					"type" to "connected",
					"username" to MinecraftClient.getInstance().session.username,
					"host" to (handler.connection.address as InetSocketAddress).hostName
				))
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
				webSocket.sendText(jsonObjectOf(
					"type" to "ping",
					"x" to block.x,
					"y" to block.y,
					"z" to block.z))
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
		.also { (pairs, json) -> pairs.forEach { k ->
			when (val value = k.value) {
				is String -> json.add(k.key, value)
				is Int -> json.add(k.key, value)
				else -> throw RuntimeException("Unable to serialize $value")
			}
		}}.second.toString()