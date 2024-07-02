package dev.u9g.features

import dev.u9g.events.ServerConnectCallback
import dev.u9g.events.SlotClickCallback
import dev.u9g.features.Settings.whatAdventureToDisplay
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.screen.GenericContainerScreenHandler

class WhatAdventure {
    private var advPart1: String? = null
    private var setTime = 0L
    private var ignoreFirst = false // connect from hub
    private var ignoreSecond = false // connect from hub to server

    init {
        HudRenderCallback.EVENT.register { draw, _ ->
            if (whatAdventureToDisplay != null) {
                draw.drawText(
                    MinecraftClient.getInstance().textRenderer,
                    whatAdventureToDisplay,
                    2,
                    MinecraftClient.getInstance().window.scaledHeight - 11,
                    0xFFFFFF,
                    true
                )
            }
        }

        ServerConnectCallback.event.register {
            ignoreFirst = true
        }

        ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
            if (!ignoreFirst && !ignoreSecond && System.currentTimeMillis() - setTime > 2000) {
                whatAdventureToDisplay = null
                advPart1 = null
            } else if (ignoreFirst) {
                ignoreFirst = false
                ignoreSecond = true
            } else if (ignoreSecond) {
                ignoreSecond = false
            }
        }

        SlotClickCallback.event.register {
            val screen = MinecraftClient.getInstance().currentScreen
            if (screen is GenericContainerScreen) {
                if (screen.screenHandler is GenericContainerScreenHandler) {
                    if (screen.title.string.trim() == "Adventures") {
                        when (it.slot.index) {
                            10 -> {
                                advPart1 = "1chao-"
                            }

                            11 -> {
                                advPart1 = "2chao-"
                            }

                            12 -> {
                                advPart1 = "3chao-"
                            }

                            14 -> {
                                whatAdventureToDisplay = "1scav"
                            }

                            15 -> {
                                whatAdventureToDisplay = "2scav-"
                            }

                            16 -> {
                                whatAdventureToDisplay = "3scav-"
                            }
                        }
                    } else if (advPart1 != null && screen.title.string.trim().endsWith(" Adventures") &&
                        advPart1 != null &&
                        it.slot.index < 9
                    ) {
                        whatAdventureToDisplay = "$advPart1${it.slot.index + 1}"
                        advPart1 = null
                        setTime = System.currentTimeMillis()
                    } else {
                        advPart1 = null
                    }
                }
            }
        }
    }
}