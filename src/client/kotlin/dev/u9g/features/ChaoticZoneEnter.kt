package dev.u9g.features

import dev.u9g.events.ChatMessageReceivedCallback
import dev.u9g.playSound
import net.minecraft.util.Identifier

object ChaoticZoneEnter {
    init {
        ChatMessageReceivedCallback.event.register {
            if (Settings.enableMod && it.msg.startsWith("(!)") && it.msg.endsWith("has entered the Chaotic Zone!")) {
                playSound(Identifier("minecraft", "entity.ravager.celebrate"))
            }
        }
    }
}