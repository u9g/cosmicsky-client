package dev.u9g.features

import dev.u9g.events.PlaySoundCallback
import dev.u9g.events.SubtitleTextCallback
import dev.u9g.events.TitleTextCallback
import net.minecraft.text.MutableText
import net.minecraft.text.PlainTextContent.Literal

object HidePOINotification {
    init {
        //§a§lGusty Grove
        TitleTextCallback.event.register {
            if (Settings.enableMod && Settings.shouldHidePOINotification) {
                val t = it.text
                if (t is MutableText) {
                    val c = t.content
                    if (c is Literal) {
                        if (t.string.startsWith("§a§l")) {
                            it.isCancelled = true
                        }
                    }
                }
            }
        }

        PlaySoundCallback.event.register {
            if (Settings.enableMod && Settings.shouldHidePOINotification && it.soundKey == "entity.bat.takeoff") {
                it.isCancelled = true
            }
        }

        SubtitleTextCallback.event.register {
            if (Settings.enableMod && Settings.shouldHidePOINotification && it.msg.startsWith("Kill ")) {
                it.isCancelled = true
            }
        }
    }
}