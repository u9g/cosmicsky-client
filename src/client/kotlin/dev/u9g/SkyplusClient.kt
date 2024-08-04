package dev.u9g

import dev.u9g.commands.thenExecute
import dev.u9g.events.CommandCallback
import dev.u9g.events.CommandEvent
import dev.u9g.features.*
import dev.u9g.features.`fun`.ImHighUp
import dev.u9g.util.Coroutines
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.sound.SoundEvent
import net.minecraft.util.Identifier


val mc: MinecraftClient get() = MinecraftClient.getInstance()

fun playSound(identifier: Identifier) {
    MinecraftClient.getInstance().submit {
        mc.soundManager.play(PositionedSoundInstance.master(SoundEvent.of(identifier), 1F))
    }
}


object SkyplusClient : ClientModInitializer {
    override fun onInitializeClient() {
        println("SkyPlus-Client starting.")
        JavaMain.LOGGER.info("SkyPlus-Client starting. VIA LOGGER")

        CommandCallback.event.register {
            it.register("skyplusre") {
                thenExecute {
                    Websocket.reset()
                }
            }
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
//        ChaoticZoneEnter

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, ctx ->
            CommandCallback.event.invoker().invoke(CommandEvent(dispatcher, ctx, mc.networkHandler?.commandDispatcher))
        }
    }
}