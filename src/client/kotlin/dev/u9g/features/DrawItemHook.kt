package dev.u9g.features

import com.llamalad7.mixinextras.injector.wrapoperation.Operation
import net.minecraft.client.gui.DrawContext
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.text.Text
import net.minecraft.world.World

object DrawItemHook {

    fun onDrawItem(
        instance: DrawContext,
        entity: LivingEntity?,
        world: World?,
        stack: ItemStack,
        x: Int,
        y: Int,
        seed: Int,
        z: Int,
        original: Operation<Void>
    ) {
        fun showTwo(first: ItemStack?, second: ItemStack?) {
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

        original.call(instance, entity, world, stack, x, y, seed, z)

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
                        ItemStack(Items.DIAMOND_PICKAXE),
                        mapOf(
                            "LOG" to ItemStack(Items.OAK_LOG),
                            "COBBLESTONE" to ItemStack(Items.COBBLESTONE),
                            "DIAMOND" to ItemStack(Items.DIAMOND_ORE),
                            "IRON" to ItemStack(Items.IRON_ORE),
                            "COAL" to ItemStack(Items.COAL_ORE),
                            "GOLD" to ItemStack(Items.GOLD_ORE),
                        )[step],
                    )
                }

                "harvest" -> {
                    showTwo(
                        ItemStack(Items.DIAMOND_HOE),
                        mapOf(
                            "MELON" to ItemStack(Items.MELON),
                            "SUGAR_CANE" to ItemStack(Items.SUGAR_CANE),
                            "BEETROOT" to ItemStack(Items.BEETROOT),
                            "CARROT" to ItemStack(Items.CARROT),
                            "WHEAT" to ItemStack(Items.WHEAT),
                            "POTATO" to ItemStack(Items.POTATO),
                        )[step],
                    )
                }

                "sell" -> {
                    showTwo(
                        ItemStack(Items.PAPER).also { it.setCustomName(Text.of("Money Note")) },
                        mapOf(
                            "ROTTEN_FLESH" to ItemStack(Items.ROTTEN_FLESH),
                            "GOLDEN_NUGGET" to ItemStack(Items.GOLD_NUGGET),
                            "MELON" to ItemStack(Items.MELON),
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
                    )
                }

                "craft" -> {
                    showTwo(
                        ItemStack(Items.CRAFTING_TABLE),
                        mapOf(
                            "BREAD" to ItemStack(Items.BREAD),
                            "LOG_NODE" to ItemStack(Items.OAK_LOG),
                            "COBBLESTONE_NODE" to ItemStack(Items.COBBLESTONE)
                        )[step]
                    )
                }

                "cook" -> {
                    showTwo(
                        ItemStack(Items.FURNACE),
                        mapOf(
                            "CHICKEN" to ItemStack(Items.CHICKEN),
                            "POTATOES" to ItemStack(Items.POTATO),
                            "RABBIT" to ItemStack(Items.RABBIT),
                            "BEEF" to ItemStack(Items.BEEF),
                            "SALMON" to ItemStack(Items.SALMON),
                            "COD" to ItemStack(Items.COD),
                        )[step]
                    )
                }

                "slayer" -> {
                    showTwo(
                        ItemStack(Items.DIAMOND_SWORD),
                        mapOf(
                            "ZOMBIE_PIGMAN" to ItemStack(Items.GOLD_NUGGET),
                            "COW" to ItemStack(Items.BEEF),
                            "ZOMBIE" to ItemStack(Items.ROTTEN_FLESH),
                            "SPIDER" to ItemStack(Items.SPIDER_EYE),
                            "SKELETON" to ItemStack(Items.BONE),
                            "RABBIT" to ItemStack(Items.RABBIT),
                            "CHICKEN" to ItemStack(Items.CHICKEN),
                        )[step]
                    )
                }

                "fish" -> {
                    showTwo(
                        mapOf(
                            "COD" to ItemStack(Items.COOKED_COD),
                            "SALMON" to ItemStack(Items.COOKED_SALMON),
                        )[step],
                        ItemStack(Items.FISHING_ROD),
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
