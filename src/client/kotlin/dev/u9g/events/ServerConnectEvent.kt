package dev.u9g.events

import net.fabricmc.fabric.api.event.EventFactory

fun interface ServerConnectCallback {
    operator fun invoke()

    companion object {
        @JvmStatic
        val event =
            EventFactory.createArrayBacked(ServerConnectCallback::class.java) { callbacks ->
                ServerConnectCallback {
                    for (callback in callbacks)
                        callback()
                }
            }
    }
}