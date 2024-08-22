package dev.u9g.features

import dev.u9g.events.WorldRenderLastCallback
import dev.u9g.util.render.RenderInWorldContext
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.EntityPose
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.StringNbtReader
import net.minecraft.util.Identifier

object TeamFocus {
    val item = ItemStack(Items.PLAYER_HEAD).also {
        it.nbt =
            StringNbtReader.parse("{display:{Name:'{\"text\":\"Red (#d60000)\",\"color\":\"gold\",\"underlined\":true,\"bold\":true,\"italic\":false}',Lore:['{\"text\":\"Custom Head ID: 72844\",\"color\":\"gray\",\"italic\":false}','{\"text\":\"www.minecraft-heads.com\",\"color\":\"blue\",\"italic\":false}']},SkullOwner:{Id:[I;117779425,-658027061,-1937179032,1707837796],Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDA1MWI1OTA4NWQyYzQyNDk1Nzc4MjNmNjNlMWUyZWI5ZjdjZjY0YjdjNzg3ODVhMjE4MDVmYWQzZWYxNCJ9fX0=\"}]}}}")
    }

    init {
//        DecorateNameAboveHeadCallback.event.register {
//            if (it.username.lowercase() == Settings.focusedPlayerUsername.lowercase()) {
//                MinecraftClient.getInstance().world?.players?.find { x -> x.gameProfile.name.lowercase() == Settings.focusedPlayerUsername.lowercase() }
//                    ?.let { player ->
//                        MinecraftClient.getInstance().player?.let { _ ->
//                            if (player.pose != EntityPose.CROUCHING) {
//                                val root = Text.of("")
//                                root.siblings.addAll(
//                                    Text.of("GGGGGGGG ")
//                                        .getWithStyle(
//                                            Style.EMPTY.withObfuscated(true).withColor(Formatting.RED).withBold(true)
//                                        )
//                                )
////                                root.siblings.addAll(it.textToSend.getWithStyle(Style.EMPTY.withBold(true)))
//                                root.siblings.addAll(
//                                    Text.of(it.username)
//                                        .getWithStyle(Style.EMPTY.withColor(Formatting.RED).withBold(true))
//                                )
//                                root.siblings.addAll(
//                                    Text.of(" GGGGGGGG")
//                                        .getWithStyle(
//                                            Style.EMPTY.withObfuscated(true).withColor(Formatting.RED).withBold(true)
//                                        )
//                                )
//                                it.textToSend = root as MutableText
//                            }
//                        }
//                    }
//            }
//        }

        WorldRenderLastCallback.event.register {
            RenderInWorldContext.renderInWorld(it) {
                MinecraftClient.getInstance().player?.let { me ->
                    MinecraftClient.getInstance().world?.players?.find { b ->
                        b.gameProfile.name.lowercase() == Settings.focusedPlayerUsername.lowercase()
                    }?.let { player ->
                        if (player.pose != EntityPose.CROUCHING && me.canSee(player)) {
                            player.getLerpedPos(it.tickDelta)?.let { pos ->
                                texture(
                                    pos.add(0.0, player.height.toDouble() + 1.1, 0.0),
                                    Identifier("skyplus", "warning_exclam.png"),
                                    8,
                                    16,
                                    0.5f,
                                    0f,
                                    1f,
                                    1f
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}