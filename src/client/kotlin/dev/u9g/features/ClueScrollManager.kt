package dev.u9g.features

import dev.u9g.events.AfterDrawItemCallback
import dev.u9g.events.GetTooltipCallback
import dev.u9g.util.sectionCharColor
import kotlinx.serialization.Serializable
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import kotlin.math.floor
import kotlin.math.min

@Serializable
data class ClueScrollSettings(
    var middleXPercent: Double = 50.0,
    var middleYPercent: Double = 40.0,
    var isEnabled: Boolean = true
)

object ClueScrollManager {
    var settings: ClueScrollSettings = ClueScrollSettings()
    var showAllClueScrollsOnHud = false
    private val R = "(\\d+)/(\\d+)".toRegex()

//    private val toggleShowAllClueScrollsOnHudKey =
//        KeyBinding("Toggle showing all clue scrolls on hud", InputUtil.GLFW_KEY_Z, "Clue Scroll Hud")

    fun showTwo(
        instance: DrawContext,
        first: ItemStack?, second: ItemStack?,
        x: Int, y: Int,
    ) {
        first?.let { f ->
            second?.let { s ->
                val v = .8f
                instance.matrices.push()
                instance.scale(v, v, 1f)
                instance.drawItem(
                    f,
                    ((x - 1) * (1 / v)).toInt(),
                    ((y - 1) * (1 / v)).toInt()
                )
                instance.matrices.pop()
                instance.scale(v, v, 1f)
                instance.drawItem(
                    s,
                    ((x + 3) * (1 / v)).toInt(),
                    ((y + 3) * (1 / v)).toInt()
                )
            }
        }

    }

    init {
        GetTooltipCallback.event.register {
            val csi = getClueScrollInfo(it.stack, it.tooltipLines)
            if (csi != null) {
                val percentDone = min(csi.progress, 1.0)
                val cutoff = floor(percentDone * csi.line.length).toInt()
                val before = csi.line.substring(0, cutoff)
                val after = csi.line.substring(cutoff)

                val root = Text.of("")

                root.siblings.addAll(
                    Text.of(before).getWithStyle(Style.EMPTY.withColor(Formatting.GREEN))
                )

                root.siblings.addAll(
                    Text.of(after).getWithStyle(Style.EMPTY.withColor(Formatting.RED))
                )

                it.tooltipLines[csi.lineNumber] = root
            }
        }

        AfterDrawItemCallback.event.register {
            val instance = it.instance
            val stack = it.stack
            val x = it.x
            val y = it.y

            if (stack.item == Items.MAP) {
                instance.matrices.push()
                val type = stack.nbt?.getCompound("current")?.getString("type")
                val step = stack.nbt?.getCompound("current")?.getString("step")

                when (type) {
                    "gemforge" -> {
                        when (step) {
                            "FAIL" -> {
                                val v = .8f
                                instance.scale(v, v, 1f)
                                instance.drawItem(
                                    ItemStack(Items.GUNPOWDER),
                                    ((x - 1) * (1 / v)).toInt(),
                                    ((y - 1) * (1 / v)).toInt()
                                )
                            }

                            "APPLY" -> {
                                val v = .8f
                                instance.scale(v, v, 1f)
                                instance.drawItem(
                                    ItemStack(Items.ENCHANTING_TABLE),
                                    ((x - 1) * (1 / v)).toInt(),
                                    ((y - 1) * (1 / v)).toInt()
                                )
                            }

                            else -> {
                                val v = .8f
                                instance.scale(v, v, 1f)
                                instance.drawItem(
                                    ItemStack(Items.EMERALD).also {
                                        it.setCustomName(
                                            Text.of(
                                                "Minor ${
                                                    when (stack.nbt?.getCompound("current")?.getString("step")) {
                                                        "DIAMOND_GEM" -> "Diamond"
                                                        "IRON_GEM" -> "Iron"
                                                        else -> "Stone"
                                                    }
                                                } Gem"
                                            )
                                        )
                                    },
                                    ((x - 1) * (1 / v)).toInt(),
                                    ((y - 1) * (1 / v)).toInt()
                                )
                            }
                        }
                    }

                    "wear" -> {
                        val v = .7f
                        instance.scale(v, v, 1f)
                        instance.drawItem(
                            ItemStack(
                                when (stack.nbt?.getCompound("current")?.getString("step")) {
                                    "DIAMOND" -> Items.DIAMOND_CHESTPLATE
                                    "IRON" -> Items.IRON_CHESTPLATE
                                    else -> Items.CHAINMAIL_CHESTPLATE
                                }
                            ),
                            ((x - 1) * (1 / v)).toInt(),
                            ((y - 1) * (1 / v)).toInt()
                        )
                    }

                    "misc" -> {
                        when (stack.nbt?.getCompound("current")?.getString("step")) {
                            "PLACE_ISLAND_BLOCKS" -> {
                                val v = .7f
                                instance.scale(v, v, 1f)
                                instance.drawItem(
                                    ItemStack(
                                        Items.DIRT
                                    ),
                                    ((x - 1) * (1 / v)).toInt(),
                                    ((y - 1) * (1 / v)).toInt()
                                )
                            }

                            "COSMIC_COIN" -> {
                                val v = .7f
                                instance.scale(v, v, 1f)
                                instance.drawItem(
                                    ItemStack(
                                        Items.LIGHT
                                    ),
                                    ((x) * (1 / v)).toInt(),
                                    ((y) * (1 / v)).toInt()
                                )
                            }

                            "SHOP_SPEND" -> {
                                val v = .7f
                                instance.scale(v, v, 1f)
                                instance.drawItem(
                                    ItemStack(Items.PAPER).also { it.setCustomName(Text.of("Money Note")) },
                                    ((x) * (1 / v)).toInt(),
                                    ((y) * (1 / v)).toInt()
                                )
                            }
                        }
                    }

                    "mine" -> {
                        showTwo(
                            instance,
                            ItemStack(Items.DIAMOND_PICKAXE),
                            mapOf(
                                "LOG" to ItemStack(Items.OAK_LOG),
                                "COBBLESTONE" to ItemStack(Items.COBBLESTONE),
                                "DIAMOND" to ItemStack(Items.DIAMOND_ORE),
                                "IRON" to ItemStack(Items.IRON_ORE),
                                "COAL" to ItemStack(Items.COAL_ORE),
                                "GOLD" to ItemStack(Items.GOLD_ORE),
                            )[step],
                            x, y
                        )
                    }

                    "harvest" -> {
                        showTwo(
                            instance,
                            ItemStack(Items.DIAMOND_HOE),
                            mapOf(
                                "MELON" to ItemStack(Items.MELON),
                                "SUGAR_CANE" to ItemStack(Items.SUGAR_CANE),
                                "BEETROOT" to ItemStack(Items.BEETROOT),
                                "CARROT" to ItemStack(Items.CARROT),
                                "WHEAT" to ItemStack(Items.WHEAT),
                                "POTATO" to ItemStack(Items.POTATO),
                                "PUMPKIN" to ItemStack(Items.PUMPKIN),
                            )[step],
                            x, y
                        )
                    }

                    "sell" -> {
                        showTwo(
                            instance,
                            ItemStack(Items.PAPER).also { it.setCustomName(Text.of("Money Note")) },
                            mapOf(
                                "ROTTEN_FLESH" to ItemStack(Items.ROTTEN_FLESH),
                                "GOLDEN_NUGGET" to ItemStack(Items.GOLD_NUGGET),
                                "MELON" to ItemStack(Items.MELON),
                                "PUMPKIN" to ItemStack(Items.PUMPKIN),
                                "POTATO" to ItemStack(Items.POTATO),
                                "BEETROOT" to ItemStack(Items.BEETROOT),
                                "CARROT" to ItemStack(Items.CARROT),
                                "WHEAT" to ItemStack(Items.WHEAT),
                                "STRING" to ItemStack(Items.STRING),
                                "BONE" to ItemStack(Items.BONE),
                                "LEATHER" to ItemStack(Items.LEATHER),
                                "FEATHERS" to ItemStack(Items.FEATHER),
                                "BONES" to ItemStack(Items.BONE)
                            )[step],
                            x, y
                        )
                    }

                    "craft" -> {
                        showTwo(
                            instance,
                            ItemStack(Items.CRAFTING_TABLE),
                            mapOf(
                                "BREAD" to ItemStack(Items.BREAD),
                                "LOG_NODE" to ItemStack(Items.OAK_LOG),
                                "COBBLESTONE_NODE" to ItemStack(Items.COBBLESTONE)
                            )[step],
                            x, y
                        )
                    }

                    "cook" -> {
                        showTwo(
                            instance,
                            ItemStack(Items.FURNACE),
                            mapOf(
                                "CHICKEN" to ItemStack(Items.CHICKEN),
                                "POTATOES" to ItemStack(Items.POTATO),
                                "RABBIT" to ItemStack(Items.RABBIT),
                                "BEEF" to ItemStack(Items.BEEF),
                                "SALMON" to ItemStack(Items.SALMON),
                                "COD" to ItemStack(Items.COD),
                            )[step],
                            x, y
                        )
                    }

                    "slayer" -> {
                        showTwo(
                            instance,
                            ItemStack(Items.DIAMOND_SWORD),
                            mapOf(
                                "ZOMBIE_PIGMAN" to ItemStack(Items.GOLD_NUGGET),
                                "COW" to ItemStack(Items.BEEF),
                                "ZOMBIE" to ItemStack(Items.ROTTEN_FLESH),
                                "SPIDER" to ItemStack(Items.SPIDER_EYE),
                                "SKELETON" to ItemStack(Items.BONE),
                                "RABBIT" to ItemStack(Items.RABBIT),
                                "CHICKEN" to ItemStack(Items.CHICKEN),
                            )[step],
                            x, y
                        )
                    }

                    "fish" -> {
                        showTwo(
                            instance,
                            mapOf(
                                "COD" to ItemStack(Items.COOKED_COD),
                                "SALMON" to ItemStack(Items.COOKED_SALMON),
                            )[step],
                            ItemStack(Items.FISHING_ROD),
                            x, y
                        )
                    }

                    "purchase" -> {
                        if (step?.startsWith("DIAMOND") == true) {
                            val v = .8f
                            instance.scale(v, v, 1f)
                            instance.drawItem(
                                ItemStack(Items.BOOK).also {
                                    it.setCustomName(Text.of("Diamond Armor Enchant Book"))
                                },
                                ((x - 1) * (1 / v)).toInt(),
                                ((y - 1) * (1 / v)).toInt()
                            )
                        } else if (step?.startsWith("IRON") == true) {
                            val v = .8f
                            instance.scale(v, v, 1f)
                            instance.drawItem(
                                ItemStack(Items.BOOK).also {
                                    it.setCustomName(Text.of("Iron Armor Enchant Book"))
                                },
                                ((x - 1) * (1 / v)).toInt(),
                                ((y - 1) * (1 / v)).toInt()
                            )
                        } else if (step?.startsWith("CHAIN") == true) {
                            val v = .8f
                            instance.scale(v, v, 1f)
                            instance.drawItem(
                                ItemStack(Items.BOOK).also {
                                    it.setCustomName(Text.of("Chainmail Armor Enchant Book"))
                                },
                                ((x - 1) * (1 / v)).toInt(),
                                ((y - 1) * (1 / v)).toInt()
                            )
                        }
                    }
                }
                instance.matrices.pop()
            }
        }
    }

    init {
//        KeyBindingHelper.registerKeyBinding(toggleShowAllClueScrollsOnHudKey)
//        var lines = listOf<Text>()
//
//
//        ClientTickEvents.END_CLIENT_TICK.register {
//            while (toggleShowAllClueScrollsOnHudKey.wasPressed()) {
//                showAllClueScrollsOnHud = !showAllClueScrollsOnHud
//            }
//
//            lines = mc.player?.inventory?.main?.let { items ->
//                items.filterIndexed { a, b ->
//                    if (!showAllClueScrollsOnHud) {
//                        a < 9
//                    } else {
//                        true
//                    }
//                }.mapNotNull { item ->
//                    if (item.nbt?.getString("persistentItem") == "clue_scroll") {
//                        item.getTooltip(null, TooltipContext.BASIC)
//                            .findLast { it.string.startsWith(" * ") }
//                    } else {
//                        null
//                    }
//                }
//            } ?: emptyList()
//        }

//        HudRenderCallback.EVENT.register { a, b ->
//            val centerX = (mc.window.scaledWidth.toFloat() * (settings.middleXPercent / 100)).toInt()
//            var centerY = (mc.window.scaledHeight.toFloat() * (settings.middleYPercent / 100)).toInt()
//
//            if (CooldownManager.settings.isEnabled && Settings.enableMod) {
//                fun drawTextCentered(msg: Text, centerX: Int, centerY: Int) {
//                    a.drawText(
//                        mc.textRenderer,
//                        msg,
//                        (centerX - (mc.textRenderer.getWidth(msg).toFloat() * 0.5)).toInt(),
//                        (centerY - 3.5).toInt(),
//                        -1,
//                        true
//                    )
//                }
//
//                a.matrices.push()
//
//                for (line in lines) {
//                    drawTextCentered(line, centerX, centerY)
//
//                    centerY -= 11
//                }
//
//                a.matrices.pop()
//            }
//        }
    }

    data class ClueScrollInfo(val progress: Double, val line: String, val lineNumber: Int)

    @JvmOverloads
    fun getClueScrollInfo(
        item: ItemStack,
        original: List<Text> = item.getTooltip(null, TooltipContext.BASIC)
    ): ClueScrollInfo? {
        val nbt = item.nbt
        if (nbt != null && "clue_scroll" == nbt.getString("persistentItem")) {
            for (i in original.indices.reversed()) {
                val t = original[i]

                val line = sectionCharColor.replace(t.string, "")

                if (line.startsWith(" * ")) {
                    val found = R.find(line, 0)
                    if (found != null) {
                        val groups = found.groups
                        val finished = groups[1]
                        val total = groups[2]

                        try {
                            if (finished != null && total != null) {
                                val finishedD = finished.value.toDouble()
                                val totalD = total.value.toDouble()
                                val percentDone = min(finishedD / totalD, 1.0)

                                return ClueScrollInfo(percentDone, line, i)
                            }
                        } catch (e: Exception) {
                            return null
                        }
                    }
                }
            }
        }

        return null
    }
}