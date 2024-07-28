package dev.u9g.features

import dev.u9g.events.ChatMessageReceivedCallback
import dev.u9g.playSound
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.core.HorizontalAlignment
import io.wispforest.owo.ui.core.Positioning
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.VerticalAlignment
import io.wispforest.owo.ui.hud.Hud
import net.minecraft.util.Identifier
import java.util.TimerTask
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ChaoticZoneEnter {
    val identifier = Identifier.of("skyplus", "chaotic-zone-enter")

    init {
        ChatMessageReceivedCallback.event.register {
            if (Settings.enableMod && it.msg.startsWith("(!)") && it.msg.endsWith("has entered the Chaotic Zone!")) {
                Hud.add(identifier) {
                    Components.label(it.text)
                        .verticalTextAlignment(VerticalAlignment.CENTER)
                        .horizontalTextAlignment(HorizontalAlignment.CENTER)
                        .positioning(Positioning.relative(50, 45))
                };

                Executors.newScheduledThreadPool(1).schedule({
                    Hud.remove(identifier);
                }, 15, TimeUnit.SECONDS)

                playSound(Identifier("minecraft", "entity.ravager.celebrate"))
            }
        }
    }
}