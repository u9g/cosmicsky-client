package dev.u9g.features

import dev.u9g.commands.thenExecute
import dev.u9g.events.ChatMessageReceivedCallback
import dev.u9g.events.CommandCallback
import dev.u9g.mc
import dev.u9g.util.ScreenUtil
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.component.DiscreteSliderComponent
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.OwoUIAdapter
import io.wispforest.owo.ui.core.Sizing
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import java.util.concurrent.TimeUnit

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

class Cooldown(val cooldownInSeconds: Int, val icon: ItemStack, val command: String, val colorString: String) {
    var timeSinceLastUsed = System.currentTimeMillis() - (120 * 1000) //0L

    fun color() = "§" + this.colorString

    fun timeStr() =
        formatTime(this.cooldownInSeconds - (System.currentTimeMillis() - this.timeSinceLastUsed) / 1000)
}


object CooldownManager {
    private val cooldowns = mapOf(
        "heal" to Cooldown(300, ItemStack(Items.GLISTERING_MELON_SLICE), "/heal", "e"),
        "eat" to Cooldown(300, ItemStack(Items.COOKED_BEEF), "/eat", "a"),
        "fix" to Cooldown(300, ItemStack(Items.ANVIL), "/fix", "b"),
        "near" to Cooldown(300, ItemStack(Items.COMPASS), "/near", "c")
    )

    var middleXPercent: Double = 50.0
    var middleYPercent: Double = 50.0

    init {
        CommandCallback.event.register {
            it.register("movehud") {
                thenExecute {
                    ScreenUtil.setScreenLater(MoveHudOnScreen())
                }
            }
        }


        HudRenderCallback.EVENT.register { a, b ->
            fun drawItemCentered(stack: ItemStack, centerX: Int, centerY: Int) {
                a.drawItem(
                    stack,
                    centerX - 8,
                    centerY - 8
                )
            }

            fun drawTextCentered(msg: String, centerX: Int, centerY: Int) {
                a.drawText(
                    mc.textRenderer,
                    msg,
                    (centerX - (mc.textRenderer.getWidth(msg).toFloat() * 0.5)).toInt(),
                    (centerY - 3.5).toInt(),
                    -1,
                    true
                )
            }

            a.matrices.push()

            var centerX = (mc.window.scaledWidth.toFloat() * (middleXPercent / 100)).toInt()
            val centerY = (mc.window.scaledHeight.toFloat() * (middleYPercent / 100)).toInt()

            var first: Int? = null
            var largest = 0
            var i = 0

            for (cooldown in cooldowns) {
                val str = cooldown.value.timeStr()

                val r0 = mc.textRenderer.getWidth(cooldown.value.command)

                val r = mc.textRenderer.getWidth(str)


                val curr = if (r0 > r) {
                    r0
                } else {
                    r + 3 // 3px padding after text length
                }

                largest = largest.coerceAtLeast(curr)
                i++

                if (first == null) {
                    first = curr
                }
            }

            if (first != null) {
                centerX += (first / 2)
            }

            centerX -= (largest * i) / 2

            for (cooldown in cooldowns) {
                drawItemCentered(cooldown.value.icon, centerX, centerY)

                drawTextCentered(
                    cooldown.value.color() + cooldown.value.command,
                    centerX,
                    centerY + 16
                )

                drawTextCentered(
                    cooldown.value.color() + cooldown.value.timeStr(),
                    centerX,
                    centerY + 16 + 11
                )

                centerX += largest
            }

            a.matrices.pop()
        }

        listenToChatEvent()
    }

    private fun listenToChatEvent() {
        ChatMessageReceivedCallback.event.register { event ->
            when (event.msg) {
                "(!) Healed" -> {
                    cooldowns["heal"]?.timeSinceLastUsed = System.currentTimeMillis()
                }

                "Appetite has been satiated." -> {
                    cooldowns["eat"]?.timeSinceLastUsed = System.currentTimeMillis()
                }
            }

            if (event.msg.startsWith("(!) Repaired:")) {
                cooldowns["fix"]?.timeSinceLastUsed = System.currentTimeMillis()
            }

            if (event.msg.startsWith("Nearby Players") || event.msg.startsWith("(!) There is no one nearby")) {
                cooldowns["near"]?.timeSinceLastUsed = System.currentTimeMillis()
            }
        }
    }
}

class MoveHudOnScreen : BaseOwoScreen<FlowLayout>() {
    override fun build(rootComponent: FlowLayout) {
        rootComponent.childById(DiscreteSliderComponent::class.java, "x").onChanged().subscribe {
            CooldownManager.middleXPercent = it
        }
        rootComponent.childById(DiscreteSliderComponent::class.java, "y").onChanged().subscribe {
            CooldownManager.middleYPercent = it
        }
    }

    override fun createAdapter(): OwoUIAdapter<FlowLayout> {
        return OwoUIAdapter.create(
            this
        ) { a, b ->
            Containers.verticalFlow(a, b).children(
                listOf(
                    Components.discreteSlider(Sizing.fill(50), 0.0, 100.0).value(CooldownManager.middleXPercent / 100)
                        .id("x"),
                    Components.discreteSlider(Sizing.fill(50), 0.0, 100.0).value(CooldownManager.middleYPercent / 100)
                        .id("y")
                )
            )
        }
    }
}