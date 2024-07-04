package dev.u9g.features

import dev.u9g.events.ChatMessageReceivedCallback
import dev.u9g.mc
import dev.u9g.util.render.RenderCircleProgress
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import java.sql.Time
import kotlin.math.roundToLong

const val FIVE_MINS = 5.0f * 60.0f * 1_000.0f
const val TWO_MINS = 2.0f * 60.0f * 1_000f
const val THIRTY_SECS = 30 * 1_000f

class CooldownHud {
    private var lastHeal = 0L
    private var lastFeed = 0L
    private var lastFix = 0L
    private var lastCombat = 0L
    private var lastNear = 0L;

    init {
        HudRenderCallback.EVENT.register { draw, tickDelta ->
            if (!Settings.cooldownHud) return@register

            var amountOnCooldown: Int = 0

            var healOnCooldown: Boolean = lastHeal != 0L && (System.currentTimeMillis() - lastHeal) <= FIVE_MINS;
            var feedOnCooldown: Boolean = lastFeed != 0L && (System.currentTimeMillis() - lastFeed) <= FIVE_MINS;
            var fixOnCooldown: Boolean = lastFix != 0L && (System.currentTimeMillis() - lastFix) <= TWO_MINS;
            var nearOnCooldown: Boolean = lastNear != 0L && (System.currentTimeMillis() - lastNear) <= THIRTY_SECS;

            if (healOnCooldown) amountOnCooldown++;
            if (feedOnCooldown) amountOnCooldown++;
            if (fixOnCooldown) amountOnCooldown++;
            if (nearOnCooldown) amountOnCooldown++;

            //- TODO Chance Combat Time
            if (lastCombat != 0L && (System.currentTimeMillis() - lastHeal) <= TWO_MINS) amountOnCooldown++;

            //- Heal First Left
            val sinceLastHeal = System.currentTimeMillis() - lastHeal
            if (lastHeal != 0L && sinceLastHeal <= FIVE_MINS) {
                val defaultX = if (amountOnCooldown % 2 != 0) {
                    mc.window.scaledWidth / 2F
                } else {
                    mc.window.scaledWidth / 2F - (5 * mc.window.scaleFactor).toFloat()
                }
                val defaultY = mc.window.scaledHeight / 2F - (50 * mc.window.scaleFactor).toFloat()
                renderCooldownCircle(draw, defaultX, defaultY, FIVE_MINS, sinceLastHeal, Items.GLISTERING_MELON_SLICE, "/HEAL");
            }

            //- Feed First Right
            val sinceLastEat = System.currentTimeMillis() - lastFeed
            if (lastFeed != 0L && sinceLastEat <= FIVE_MINS) {
                val defaultX = if (amountOnCooldown % 2 != 0) {
                    if (amountOnCooldown == 1) {
                        mc.window.scaledWidth / 2F
                    } else {
                        (mc.window.scaledWidth / 2F - (10 * mc.window.scaleFactor)).toFloat()
                    }
                } else {
                    mc.window.scaledWidth / 2F + (5 * mc.window.scaleFactor).toFloat()
                }
                val defaultY = mc.window.scaledHeight / 2F - (50 * mc.window.scaleFactor).toFloat()
                renderCooldownCircle(draw, defaultX, defaultY, FIVE_MINS, sinceLastEat, Items.COOKED_BEEF, "/EAT");
            }

            val sinceLastFix = System.currentTimeMillis() - lastFix
            if (lastFix != 0L && sinceLastFix <= TWO_MINS) {
                val defaultX = if (amountOnCooldown % 2 != 0) {
                    if (amountOnCooldown == 1) mc.window.scaledWidth / 2F
                    else (mc.window.scaledWidth / 2F + (10 * mc.window.scaleFactor)).toFloat()
                } else {
                    if (amountOnCooldown == 2) {
                        if (healOnCooldown) mc.window.scaledWidth / 2F + (5 * mc.window.scaleFactor).toFloat()
                        else mc.window.scaledWidth / 2F - (5 * mc.window.scaleFactor).toFloat()
                    }
                    else mc.window.scaledWidth / 2F + (15 * mc.window.scaleFactor).toFloat()
                }
                val defaultY = mc.window.scaledHeight / 2F - (50 * mc.window.scaleFactor).toFloat()
                renderCooldownCircle(draw, defaultX, defaultY, TWO_MINS, sinceLastFix, Items.ANVIL, "/FIX");
            }

            val sinceLastNear = System.currentTimeMillis() - lastNear
            if (lastNear != 0L && sinceLastNear <= THIRTY_SECS) {
                val defaultX = if (amountOnCooldown % 2 != 0) {
                    if (amountOnCooldown == 1) mc.window.scaledWidth / 2F
                    else {
                        if (healOnCooldown && feedOnCooldown) (mc.window.scaledWidth / 2F + (10 * mc.window.scaleFactor)).toFloat()
                        else if (feedOnCooldown && fixOnCooldown) mc.window.scaledWidth / 2F
                        else (mc.window.scaledWidth / 2F - (10 * mc.window.scaleFactor)).toFloat()
                    }
                } else {
                    if (amountOnCooldown == 2) {
                        if (healOnCooldown || fixOnCooldown) mc.window.scaledWidth / 2F + (5 * mc.window.scaleFactor).toFloat()
                        else mc.window.scaledWidth / 2F - (5 * mc.window.scaleFactor).toFloat()
                    }
                    else mc.window.scaledWidth / 2F - (15 * mc.window.scaleFactor).toFloat()
                }
                val defaultY = mc.window.scaledHeight / 2F - (50 * mc.window.scaleFactor).toFloat()
                renderCooldownCircle(draw, defaultX, defaultY, THIRTY_SECS, sinceLastNear, Items.COMPASS, "/NEAR");
            }
        }

        ChatMessageReceivedCallback.event.register {
            when (it.msg) {
                "(!) Healed" -> {
                    lastHeal = System.currentTimeMillis()
                }

                "Appetite has been satiated." -> {
                    lastFeed = System.currentTimeMillis()
                }
            }

            if (it.msg.startsWith("(!) Repaired:")) {
                lastFix = System.currentTimeMillis();
            }

            if (it.msg.startsWith("Nearby Players") || it.msg.startsWith("(!) There is no one nearby")) {
                lastNear = System.currentTimeMillis()
            }
        }
    }

    fun convertSecondsToString(seconds: Long): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60

        val minutesPart = if (minutes > 0) "${minutes}m" else ""
        val secondsPart = if (remainingSeconds > 0) "${remainingSeconds}s" else ""

        return "$minutesPart $secondsPart".trim()
    }

    fun renderCooldownCircle(draw: DrawContext, defaultX: Float, defaultY: Float, cooldownTime: Float, sinceLast: Long, centerIcon: Item, text: String) {
        draw.matrices.push()
        draw.matrices.translate(defaultX, defaultY, 0F)
        draw.matrices.scale(10f, 10f, 1F)
        RenderCircleProgress.renderCircle(
                draw, Identifier("skyplus", "circle.png"),
                1.0f,
                0f, 1f, 0f, 1f,
                0, 0, 0,
        )

        RenderCircleProgress.renderCircle(
                draw, Identifier("skyplus", "circle.png"),
                1.0f - (sinceLast / cooldownTime),
                0f, 1f, 0f, 1f,
                255, 255, 255, 255
        )

        draw.matrices.pop()

        draw.matrices.push()
        draw.matrices.translate(defaultX - 6, defaultY - 6, 0F)
        draw.matrices.scale(0.7f, 0.7f, 1F)
        draw.drawItem(ItemStack(centerIcon),
                0,
                0
        )

        //draw.matrices.translate(mc.window.scaledWidth / 2F - 6, mc.window.scaledHeight / 2F - 156, 0F)
        draw.matrices.translate(-2F, 25F, 0F)
        draw.matrices.scale(0.8f, 0.8f, 1F)
        draw.drawText(MinecraftClient.getInstance().textRenderer,
                text,
                0,
                0,
                0xFFFFFF,
                false)

        draw.matrices.translate(0F, 10F, 0F)
        draw.drawText(MinecraftClient.getInstance().textRenderer,
                convertSecondsToString((cooldownTime / 1000 - (sinceLast / 1000)).roundToLong()),
                0,
                0,
                0xFFFFFF,
                false)
        draw.matrices.pop()
    }
}