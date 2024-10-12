package dev.u9g.features

import dev.u9g.commands.RestArgumentType
import dev.u9g.commands.get
import dev.u9g.commands.thenArgument
import dev.u9g.commands.thenExecute
import dev.u9g.events.BeforeDrawItemCallback
import dev.u9g.events.CommandCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.ItemStack
import net.minecraft.item.Items

object SearchChests {
    private var searchParam = "clear"

    init {
        CommandCallback.event.register {
            it.register("searchchests") {
                thenArgument(
                    "what to search for (search clear to clear)",
                    RestArgumentType
                ) { searchArgument ->
                    thenExecute {
                        if (!Settings.enableMod) return@thenExecute

                        searchParam = this[searchArgument]
                    }
                }
            }
        }

        BeforeDrawItemCallback.event.register {
            if (!Settings.enableMod) return@register

            if (MinecraftClient.getInstance().currentScreen is GenericContainerScreen && it.stack.item != Items.AIR) {
                if (searchParam != "clear" && it.stack.getTooltip(null, TooltipContext.BASIC)
                        .any { line -> line.string.contains(searchParam, ignoreCase = true) }
                ) {
                    it.instance.drawItem(ItemStack(Items.LIGHT_BLUE_STAINED_GLASS_PANE), it.x, it.y)
                }
            }
        }
    }
}