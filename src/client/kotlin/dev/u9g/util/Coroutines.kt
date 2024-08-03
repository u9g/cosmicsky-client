package dev.u9g.util

import kotlinx.coroutines.*
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.minecraft.client.MinecraftClient
import kotlin.coroutines.EmptyCoroutineContext

val MinecraftDispatcher by lazy { MinecraftClient.getInstance().asCoroutineDispatcher() }

val globalJob = Job()

val coroutineScope =
    CoroutineScope(EmptyCoroutineContext + CoroutineName("Firmament")) + SupervisorJob(globalJob)

object Coroutines {
    init {
        ClientLifecycleEvents.CLIENT_STOPPING.register(ClientLifecycleEvents.ClientStopping {
            println("Shutting down coroutines")
            globalJob.cancel()
        })
    }
}