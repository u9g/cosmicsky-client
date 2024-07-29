package dev.u9g.features

import dev.u9g.events.ChatMessageReceivedCallback
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.core.*
import io.wispforest.owo.ui.hud.Hud
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import java.util.concurrent.TimeUnit

class Cooldown(
    val cooldownID: String,
    cooldownInSeconds: Int,
    val text: String,
    val icon: ItemStack,
    val color: String
) {
    val cooldownTime: Long = System.currentTimeMillis() + (cooldownInSeconds * 1000)
}

class CooldownManager {
    init {
        ClientTickEvents.END_CLIENT_TICK.register {
            Hud.remove(Identifier("skyplus", "cooldown-hud"))

            val gl = Containers.grid(Sizing.content(), Sizing.content(), 3, cooldownMap.size)

            for (cooldown in cooldownMap.iterator().withIndex()) {
                gl.child(
                    Components.item(cooldown.value.value.icon).margins(Insets.of(2)),
                    0,
                    cooldown.index
                )

                gl.child(
                    Components.label(Text.of(cooldown.value.value.color + cooldown.value.value.text))
                        .margins(Insets.of(2)),
                    1,
                    cooldown.index
                )

                gl.child(
                    Components.label(Text.of(cooldown.value.value.color + formatTime(getTimeRemaining(cooldown.value.value))))
                        .margins(Insets.of(2)),
                    2,
                    cooldown.index
                )
            }

            Hud.add(Identifier("skyplus", "cooldown-hud")) {
                gl
                    .horizontalAlignment(HorizontalAlignment.CENTER)
                    .surface(Surface.VANILLA_TRANSLUCENT)
                    .positioning(Positioning.relative(50, 10))
            }

            val toRemove = mutableMapOf<String, Cooldown>()

            cooldownMap.forEach { (id, cooldown) ->
                if (cooldown.cooldownTime <= System.currentTimeMillis()) toRemove[id] = cooldown
            }

            toRemove.forEach { (id, _) ->
                cooldownMap.remove(id)
            }
        }

        listenToChatEvent()
    }

    private fun getCooldownChildren(): List<Component> {
        val cooldowns = mutableListOf<Component>()

        cooldownMap.forEach { (_, cooldown) ->
            Containers.verticalFlow(Sizing.content(), Sizing.content())
                .child(Components.item(cooldown.icon))
                .child(Components.label(Text.of(cooldown.color + cooldown.text)))
                .child(Components.label(Text.of(cooldown.color + formatTime(getTimeRemaining(cooldown)))))
                .verticalAlignment(VerticalAlignment.CENTER)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .margins(Insets.of(3))
                .also { cooldowns.add(it) }
        }

        return cooldowns
    }

    private fun getTimeRemaining(cooldown: Cooldown): Long {
        return (cooldown.cooldownTime - System.currentTimeMillis()) / 1000
    }

    fun formatTime(seconds: Long): String {
        val minutes = TimeUnit.SECONDS.toMinutes(seconds)
        val remainingSeconds = seconds - TimeUnit.MINUTES.toSeconds(minutes)
        val stringBuilder: StringBuilder = StringBuilder()

        if (minutes > 0) stringBuilder.append(minutes.toString() + "m")
        if (seconds > 0) {
            if (stringBuilder.toString().isNotBlank()) stringBuilder.append(" ");
            stringBuilder.append(remainingSeconds.toString() + "s")
        }

        return stringBuilder.toString()
    }

    private fun listenToChatEvent() {
        ChatMessageReceivedCallback.event.register { event ->
            when (event.msg) {
                "(!) Healed" -> cooldownMap["heal"] =
                    Cooldown("heal", 300, "/heal", ItemStack(Items.GLISTERING_MELON_SLICE), "§c")

                "Appetite has been satiated." -> cooldownMap["eat"] =
                    Cooldown("eat", 300, "/eat", ItemStack(Items.COOKED_BEEF), "§e")
            }

            if (event.msg.startsWith("(!) Repaired:")) {
                cooldownMap["fix"] = Cooldown("fix", 120, "/fix", ItemStack(Items.ANVIL), "§f")
            }

            if (event.msg.startsWith("Nearby Players") || event.msg.startsWith("(!) There is no one nearby")) {
                cooldownMap["near"] = Cooldown("near", 120, "/near", ItemStack(Items.COMPASS), "§b")
            }
        }
    }

    companion object {
        private val cooldownMap: MutableMap<String, Cooldown> = mutableMapOf()
    }
}
