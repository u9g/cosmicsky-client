package dev.u9g.features

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtOps

object ExcellentSocks {
    init {
        val vaultRegex = "^Vault #(\\d)$".toRegex()
        val defaultItem = ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, ItemStack.EMPTY).getOrThrow(false) { x ->
            println(x)
        }

        val previousItemsInPv: MutableList<NbtCompound?> = MutableList(54) { null }
        val previousStringsOfItemsInPv = MutableList(54) { defaultItem.asString() }

        val previousItemsInInventory: MutableList<NbtCompound?> = MutableList(36 + 4 + 1) { null }
        val previousStringsOfItemsInInventory = MutableList(36 + 4 + 1) { defaultItem.asString() }

        ClientTickEvents.END_CLIENT_TICK.register {
            val screen = it.currentScreen
            if (Settings.enableMod && screen is GenericContainerScreen && vaultRegex.matches(screen.title.string)) {
                val inv = screen.screenHandler.inventory
                if (inv is SimpleInventory) {
                    var somethingChanged = false
                    inv.heldStacks.forEachIndexed { i, stack ->
                        val nbt = stack.nbt

                        if (previousItemsInPv[i] != nbt) {
                            previousItemsInPv[i] = nbt
                            previousStringsOfItemsInPv[i] = try {
                                ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, stack)
                                    .getOrThrow(false) { x ->
                                        println(x)
                                    }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                defaultItem
                            }.asString()

                            somethingChanged = true
                        }
                    }

                    if (somethingChanged) {
                        val pv = Json.encodeToString(previousStringsOfItemsInPv)
                        Websocket.sendText(
                            jsonObjectOf(
                                "type" to "pv",
                                "pvNumber" to vaultRegex.find(screen.title.string)!!.groups[1]!!.value.toInt(),
                                "pv" to pv
                            )
                        )
                    }
                }
            } else {
                MinecraftClient.getInstance().player?.inventory?.let { inventory ->
                    var somethingChanged = false
                    inventory.main.forEachIndexed { i, stack ->
                        val nbt = stack.nbt

                        if (previousItemsInInventory[i] != nbt) {
                            previousItemsInInventory[i] = nbt
                            previousStringsOfItemsInInventory[i] = try {
                                ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, stack)
                                    .getOrThrow(false) { x ->
                                        println(x)
                                    }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                defaultItem
                            }.asString()

                            somethingChanged = true
                        }
                    }

                    inventory.armor.forEachIndexed { i, stack ->
                        val nbt = stack.nbt

                        if (previousItemsInInventory[i + 36] != nbt) {
                            previousItemsInInventory[i + 36] = nbt
                            previousStringsOfItemsInInventory[i + 36] = try {
                                ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, stack)
                                    .getOrThrow(false) { x ->
                                        println(x)
                                    }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                defaultItem
                            }.asString()

                            somethingChanged = true
                        }
                    }

                    inventory.offHand.forEachIndexed { i, stack ->
                        val nbt = stack.nbt

                        if (previousItemsInInventory[i + 36 + 4] != nbt) {
                            previousItemsInInventory[i + 36 + 4] = nbt
                            previousStringsOfItemsInInventory[i + 36 + 4] = try {
                                ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, stack)
                                    .getOrThrow(false) { x ->
                                        println(x)
                                    }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                defaultItem
                            }.asString()

                            somethingChanged = true
                        }
                    }
                    if (somethingChanged) {
                        val pv = Json.encodeToString(previousStringsOfItemsInInventory)
                        Websocket.sendText(
                            jsonObjectOf(
                                "type" to "pv",
                                "pvNumber" to 0,
                                "pv" to pv
                            )
                        )
                    }
                }
            }
        }
    }
}