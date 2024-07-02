package dev.u9g.features

import dev.u9g.events.ChatMessageReceivedCallback
import dev.u9g.playSound
import net.minecraft.util.Identifier

class AreaFishedOut {
    init {
        ChatMessageReceivedCallback.event.register {
            if (it.msg == "(!) It seems this area has been fished out!") {
                playSound(Identifier("minecraft", "block.amethyst_block.break"))
            }
        }
    }
}