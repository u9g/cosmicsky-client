package dev.u9g.util

import net.minecraft.client.MinecraftClient

fun worldName() = MinecraftClient.getInstance().world?.registryKey?.value?.path.let {
    when (it) {
        "*" -> "*_" // if the world is actually called *, use the name *_
        else -> it
    }
} ?: "*" // if you can't get a world, send to * which will be shown to everyone