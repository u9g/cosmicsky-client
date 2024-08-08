package dev.u9g.features

import dev.u9g.events.BeforeDrawItemCallback
import dev.u9g.events.GetTooltipCallback
import dev.u9g.mc
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object CheckShieldWhenAdventureEnter {
    init {
        GetTooltipCallback.event.register {
            fun fillLoreWithMessage() {
                val root = Text.of("")

                root.siblings.addAll(
                    Text.of("YOU NEED A SHIELD").getWithStyle(Style.EMPTY.withColor(Formatting.RED).withBold(true))
                )

                for (index in it.tooltipLines.indices) {
                    if (index == 0 // title line
                        || it.tooltipLines[index].string.matches("\\d+ / \\d+".toRegex()) // line with people in adventure
                    ) {
                        continue
                    }

                    it.tooltipLines[index] = root
                }
            }

            val stack = it.stack

            val itemTitle = it.tooltipLines[0].string

            val shieldTier = mc.player?.inventory?.offHand?.getOrNull(
                0
            )?.nbt?.getShort("shieldTier")?.toInt()

            when {
                stack.item == Items.CHAINMAIL_CHESTPLATE && itemTitle == "Abandoned Ruins" && shieldTier != 1 -> {
                    fillLoreWithMessage()
                }

                stack.item == Items.IRON_CHESTPLATE && itemTitle == "Lost Wasteland" && shieldTier != 3 -> {
                    fillLoreWithMessage()
                }

                stack.item == Items.DIAMOND_CHESTPLATE && itemTitle == "Demonic Realm" && shieldTier != 5 -> {
                    fillLoreWithMessage()
                }
            }
        }

        BeforeDrawItemCallback.event.register {
            val stack = it.stack
            val instance = it.instance
            val x = it.x
            val y = it.y
            val itemTitle = stack.getTooltip(null, TooltipContext.BASIC)
                .getOrNull(0)?.string

            val shieldTier = mc.player?.inventory?.offHand?.getOrNull(
                0
            )?.nbt?.getShort("shieldTier")?.toInt()

            when {
                stack.item == Items.CHAINMAIL_CHESTPLATE && itemTitle == "Abandoned Ruins" && shieldTier != 1 -> {
                    instance.drawItem(ItemStack(Items.YELLOW_STAINED_GLASS_PANE), x, y)
                }

                stack.item == Items.IRON_CHESTPLATE && itemTitle == "Lost Wasteland" && shieldTier != 3 -> {
                    instance.drawItem(ItemStack(Items.YELLOW_STAINED_GLASS_PANE), x, y)
                }

                stack.item == Items.DIAMOND_CHESTPLATE && itemTitle == "Demonic Realm" && shieldTier != 5 -> {
                    instance.drawItem(ItemStack(Items.YELLOW_STAINED_GLASS_PANE), x, y)
                }
            }
        }
    }
}