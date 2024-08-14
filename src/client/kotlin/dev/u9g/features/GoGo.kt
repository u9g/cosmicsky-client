package dev.u9g.features

import dev.u9g.events.ChatMessageReceivedCallback
import net.minecraft.client.MinecraftClient

object GoGo {
    var goNext = false
    var hasUsed = false

    var nextI: Int? = null

    init {
        ChatMessageReceivedCallback.event.register {
            if (it.msg == " * Dire Wolf Buff Expired *") {
                goNext = true
            }
        }
    }

    fun findNext(): Int {
        for ((i, item) in MinecraftClient.getInstance().player?.inventory?.main?.withIndex() ?: emptyList()) {
            if (i > 8) break

            val lastUsed = item.nbt?.getLong("lastUsed")

            if (lastUsed != 0L && lastUsed != null && (System.currentTimeMillis() - lastUsed) > 15 * 60 * 1000) {
                return i
            }
        }

        return -1
    }
}