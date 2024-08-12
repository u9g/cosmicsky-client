package dev.u9g.features

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtOps

object LogPv {
    init {
        val vaultRegex = "^Vault #(\\d)$".toRegex()
        val previousItems: MutableList<NbtCompound?> = MutableList(54) { null }
        val defaultItem = ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, ItemStack.EMPTY).getOrThrow(false) { x ->
            println(x)
        }
        val previousPv: MutableList<String> = MutableList(54) { defaultItem.asString() }
        ClientTickEvents.END_CLIENT_TICK.register {
            val screen = it.currentScreen
            if (Settings.enableMod && screen is GenericContainerScreen && vaultRegex.matches(screen.title.string)) {
                val inv = screen.screenHandler.inventory
                if (inv is SimpleInventory) {
                    var somethingChanged = false
                    inv.heldStacks.forEachIndexed { i, stack ->
                        val nbt = stack.nbt

                        if (previousItems[i] != nbt) {
                            previousItems[i] = nbt
                            previousPv[i] = try {
                                ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, stack)
                                    .getOrThrow(false) { x ->
                                        println(x)
                                    }
                            } catch (e: Exception) {
                                defaultItem
                            }.asString()

                            somethingChanged = true
                        }
                    }

                    if (somethingChanged) {
                        val pv = Json.encodeToString(previousPv)
                        Websocket.sendText(
                            jsonObjectOf(
                                "type" to "pv",
                                "pvNumber" to vaultRegex.find(screen.title.string)!!.groups[1]!!.value.toInt(),
                                "pv" to pv
                            )
                        )
                    }
                }
            }
        }
    }
}