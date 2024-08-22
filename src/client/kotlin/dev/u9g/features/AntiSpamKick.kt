package dev.u9g.features

import dev.u9g.events.MsgOrCmd
import dev.u9g.events.UserSendMsgOrCmdCallback
import dev.u9g.mc
import net.minecraft.text.Text

object AntiSpamKick {
    private val COMMANDS_THAT_TRIGGER_ANTISPAM = "^((m)|(msg)|(w)|(whisper)|(t)|(tell)|(r)) .+".toRegex()
    private var lastMessageAt = 0L

    init {
        UserSendMsgOrCmdCallback.event.register {
            if (!Settings.enableMod) return@register

            if (it.type == MsgOrCmd.MSG || (it.type == MsgOrCmd.CMD && COMMANDS_THAT_TRIGGER_ANTISPAM.matches(it.msg))) {
                if (System.currentTimeMillis() - lastMessageAt < 3000L) {
                    it.isCancelled = true
                    mc.player?.sendMessage(Text.of("§b§l(!) SkyPlus -> §r§fPrevented you from getting kicked from spam."))
                } else {
                    lastMessageAt = System.currentTimeMillis()
                }
            }
        }
    }
}