package dev.u9g.features

import dev.u9g.events.ChatMessageReceivedCallback
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers

object ChatNotifyer {
    init {
        ChatMessageReceivedCallback.event.register {
            val request: HttpRequest = HttpRequest.newBuilder()
                .uri(URI("https://ntfy.sh/iloveloxtech"))
                .POST(BodyPublishers.ofString(it.msg))
                .build()
        }
    }
}