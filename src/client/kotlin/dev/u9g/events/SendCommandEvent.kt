package dev.u9g.events

import net.fabricmc.fabric.api.event.EventFactory

fun interface UserSendMsgOrCmdCallback {
    operator fun invoke(event: UserSendMsgOrCmdEvent)

    companion object {
        @JvmStatic
        val event = EventFactory.createArrayBacked(UserSendMsgOrCmdCallback::class.java) { callbacks ->
            UserSendMsgOrCmdCallback { event ->
                for (callback in callbacks)
                    callback(event)

            }
        }
    }
}

enum class MsgOrCmd {
    MSG, CMD
}

data class UserSendMsgOrCmdEvent(
    val msg: String,
    val type: MsgOrCmd,
    var isCancelled: Boolean
)