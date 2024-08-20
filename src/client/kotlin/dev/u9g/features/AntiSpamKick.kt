package dev.u9g.features

import dev.u9g.events.UserSendMsgOrCmdCallback
import dev.u9g.mc
import net.minecraft.text.Text

object AntiSpamKick {
    private var lastMessageAt = 0L

    init {
        UserSendMsgOrCmdCallback.event.register {
            if (System.currentTimeMillis() - lastMessageAt < 3000L) {
                it.isCancelled = true
                mc.player?.sendMessage(Text.of("§b§l(!) SkyPlus -> §r§fPrevented you from getting kicked from spam."))
            } else {
                lastMessageAt = System.currentTimeMillis()
            }
        }
    }
}