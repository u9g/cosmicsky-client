package dev.u9g.features

import dev.u9g.events.BeforeDrawItemCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.item.ItemStack
import net.minecraft.item.Items

object PotCounter {
    init {
        BeforeDrawItemCallback.event.register {
            if (!Settings.enableMod) return@register

            if (MinecraftClient.getInstance().currentScreen is GenericContainerScreen && it.stack.item != Items.AIR) {
                if (
                    (it.stack.nbt?.getString("cosmicItem") == "stackable_potion" && it.stack.count < 8) ||
                    it.stack.nbt?.getString("Potion") == "minecraft:healing"
                ) {
                    it.instance.drawItem(ItemStack(Items.RED_STAINED_GLASS_PANE), it.x, it.y)
                }
            }
        }
    }
}