package dev.u9g.features

import dev.u9g.util.worldName
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.block.ChestBlock
import net.minecraft.client.MinecraftClient
import net.minecraft.util.ActionResult
import kotlin.io.path.appendText
import kotlin.io.path.createFile

object DownloadPosWhenRightClicking {
    init {
        UseBlockCallback.EVENT.register { player, world, hand, hitresult ->
            hitresult.blockPos?.let {
                MinecraftClient.getInstance().world?.getBlockState(it)?.let {
                    if (it.block is ChestBlock) {
                        FabricLoader.getInstance().configDir.resolve("${worldName()}.txt")?.let {
                            try {
                                it.createFile()
                            } catch (e: Exception) {
                            }
                            try {
                                it.appendText("${"${hitresult.blockPos.x},${hitresult.blockPos.y},${hitresult.blockPos.z}"}\n")
                            } catch (e: Exception) {
                            }
                        }
                    }
                }
            }
            ActionResult.PASS
        }
    }
}