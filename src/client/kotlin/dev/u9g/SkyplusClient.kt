package dev.u9g

import dev.u9g.commands.thenExecute
import dev.u9g.events.CommandCallback
import dev.u9g.events.CommandEvent
import dev.u9g.events.ServerConnectCallback
import dev.u9g.features.*
import dev.u9g.features.`fun`.ImHighUp
import dev.u9g.util.Coroutines
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.sound.SoundEvent
import net.minecraft.text.Text
import net.minecraft.util.Identifier


val mc: MinecraftClient get() = MinecraftClient.getInstance()

fun playSound(identifier: Identifier) {
    MinecraftClient.getInstance().submit {
        mc.soundManager.play(PositionedSoundInstance.master(SoundEvent.of(identifier), 1F))
    }
}

fun printSession(from: String) {
    val sess = MinecraftClient.getInstance().session
    println("from = $from | username = ${sess.username} | uuid or null = ${sess.uuidOrNull} | ${sess.accountType}")
}

object SkyplusClient : ClientModInitializer {
    override fun onInitializeClient() {
        println("SkyPlus-Client starting.")
        JavaMain.LOGGER.info("SkyPlus-Client starting. VIA LOGGER")

        CommandCallback.event.register {
            it.register("skyplusre") {
                thenExecute {
                    println("[gg] /skyplusre (forcing isRestarting to false)")
                    if (Websocket.isRestarting) {
                        MinecraftClient.getInstance().player?.sendMessage(Text.of("Tried restarting when isRestarting=true, forcing isRestarting to false"))
                    }
                    Websocket.isRestarting = false
                    Websocket.reset("/skyplusre")
                }
            }
        }

        ServerConnectCallback.event.register {
            println("[gg] websocket started/init'd on server connect")
            Websocket.reset("server connect callback")
            println("[gg] Session")
            printSession("ServerConnectCallback")
        }

        Websocket
        Waypoints
        Calculator
        Coroutines
        Teams
        Settings
        AreaFishedOut
        TPOutAnnouncer
        ImHighUp
        MobCps
        IslandMembers
        NearFixer
        CooldownManager
        HidePOINotification
        PetCooldowns
        ClueScrollManager
        MakeChaoticGreenForTeamMembers
        CheckShieldWhenAdventureEnter
        TeamFocus
        LogPv
        AntiSpamKick
//        DownloadPosWhenRightClicking
//        ChaoticZoneEnter

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, ctx ->
            CommandCallback.event.invoker().invoke(CommandEvent(dispatcher, ctx, mc.networkHandler?.commandDispatcher))
        }
    }
}