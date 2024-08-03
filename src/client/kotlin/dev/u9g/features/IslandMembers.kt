package dev.u9g.features

import dev.u9g.IslandInfoScreen
import dev.u9g.mc
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.client.item.TooltipContext
import net.minecraft.inventory.SimpleInventory
import java.util.concurrent.TimeUnit

object IslandMembers {
    private var screen: Screen? = null
    private var ticks = 0

    init {
        ScreenEvents.AFTER_INIT.register { client, screen, scaledWidth, scaledHeight ->
            this.screen = screen
            this.ticks = 0
        }

        ClientTickEvents.END_CLIENT_TICK.register {
            val currentScreen = MinecraftClient.getInstance().currentScreen
            if (currentScreen == screen) {
                ticks++
            }

            if (ticks == 4) {
                if (currentScreen is GenericContainerScreen && currentScreen.title.string == "Island Members") {
                    val inv = currentScreen.screenHandler.inventory
                    if (inv is SimpleInventory) {
                        val usernameANDrole = inv.getHeldStacks().mapNotNull {
                            val lines = it.getTooltip(mc.player, TooltipContext.BASIC)

                            if (lines.getOrNull(2)?.string == "Island Rank") {
                                val username = lines[0].string

                                val role = lines[3].string

                                Pair(username, role)
                            } else {
                                null
                            }
                        }


                        isOnline(usernameANDrole) {
                            MinecraftClient.getInstance().submit {
                                MinecraftClient.getInstance().setScreen(IslandInfoScreen(it))
                            }
                        }
                    }
                }
            }
        }
    }

    fun isOnline(
        usernameANDRole: List<Pair<String, String>>,
        done: (List<Triple<String, String, Boolean>>) -> Unit
    ) {
        mc.player?.networkHandler?.commandDispatcher?.let { cmdDispatcher ->
            mc.player?.networkHandler?.commandSource?.let { cmdSrc ->

                val cmd = "a who "
                val parsed = cmdDispatcher.parse(cmd, cmdSrc)

                cmdDispatcher.getCompletionSuggestions(parsed, "a who ".length).orTimeout(1, TimeUnit.SECONDS)
                    .handle { it, exc ->
                        done(usernameANDRole.map { tt ->
                            Triple(tt.first, tt.second, it.list.any { t -> t.text == tt.first })
                        })
                    }
            }
        }
    }
}