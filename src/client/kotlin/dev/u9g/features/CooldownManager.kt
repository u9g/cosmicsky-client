package dev.u9g.features

import dev.u9g.commands.thenExecute
import dev.u9g.events.ChatMessageReceivedCallback
import dev.u9g.events.CommandCallback
import dev.u9g.mc
import dev.u9g.util.MoveHudOnScreen
import dev.u9g.util.ScreenUtil
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.StringNbtReader
import java.io.File
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.readText
import kotlin.io.path.writeText

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

// PET: Dire Wolf [Dire Wolf Buff (2x Mobs Killed per Stack [35s])]

class Cooldown(
    var cooldownInSeconds: Int,
    val icon: ItemStack,
    val command: String,
    private val colorString: String
) {
    var timeSinceLastUsed = 0L

    fun color() = "§" + this.colorString

    open fun timeStr(): String {
        val timeSinceLastUsed = if (MinecraftClient.getInstance().currentScreen is MoveHudOnScreen) {
            System.currentTimeMillis() - (this.cooldownInSeconds * 1000) / 2
        } else {
            this.timeSinceLastUsed
        }

        return formatTime(this.cooldownInSeconds - (System.currentTimeMillis() - timeSinceLastUsed) / 1000)
    }
}

@Serializable
data class CooldownSettings(
    var middleXPercent: Double = 50.0,
    var middleYPercent: Double = 50.0,
    var isBackgroundOn: Boolean = true,
    var isEnabled: Boolean = true,
)

val SETTINGS_FILE_PATH: Path = File("cooldown_hud_settings.json").toPath()

object CooldownManager {
    private val DIRE_HEAD = ItemStack(Items.PLAYER_HEAD).also {
        it.nbt =
            StringNbtReader.parse("{SkullOwner:{Id:[I;1510996716,1410484424,-1530203921,-1510562530],Name:\"dire_wolf\",Properties:{textures:[{Value:\"eyJ0aW1lc3RhbXAiOjE1NjMyNzg4MTM0OTksInByb2ZpbGVJZCI6IjdjODk1YWQwMTFkMDQzNTA5YWU1ZjJiYjFjZjZjOGVhIiwicHJvZmlsZU5hbWUiOiJCYXNpY0JBRSIsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9iNTUyMGNmNTExOTg3YTkzYjAxOGY4MzA4N2U4ZGI0Y2JlODNjZjlkZTdkZTg4MzQ5NDViODJjMDczN2JlM2YyIn19fQ==\"}]}}}")
    }

    private val cooldowns = mapOf(
        "heal" to Cooldown(300, ItemStack(Items.GLISTERING_MELON_SLICE), "/heal", "e"),
        "eat" to Cooldown(300, ItemStack(Items.COOKED_BEEF), "/eat", "a"),
        "fix" to Cooldown(2 * 60, ItemStack(Items.ANVIL), "/fix", "b"),
        "near" to Cooldown(30, ItemStack(Items.COMPASS), "/near", "c"),
        "bleed" to Cooldown(3 * 60, ItemStack(Items.GOLDEN_SWORD), "Bleed", "f"),
        "dire" to Cooldown(1111111, DIRE_HEAD, "Dire", "b")
    )

    private val DIRE_REGEX = "PET: Dire Wolf \\[Dire Wolf Buff \\(\\dx Mobs Killed per Stack \\[(\\d+)s]\\)]".toRegex()

    var settings: CooldownSettings

    private fun serializeSettings() {
        try {
            SETTINGS_FILE_PATH.writeText(Json.encodeToString(settings))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setMiddleXPercent(x: Double) {
        settings.middleXPercent = x

        serializeSettings()
    }

    private fun setMiddleYPercent(y: Double) {
        settings.middleYPercent = y

        serializeSettings()
    }

    fun setIsBackgroundOn(isBackgroundOn: Boolean) {
        settings.isBackgroundOn = isBackgroundOn

        serializeSettings()
    }

    private fun setIsEnabled(isEnabled: Boolean) {
        settings.isEnabled = isEnabled

        serializeSettings()
    }

    init {
        try {
            settings = Json.decodeFromString(SETTINGS_FILE_PATH.readText())
        } catch (e: Exception) {
            e.printStackTrace()
            settings = CooldownSettings()
        }

        CommandCallback.event.register {
            it.register("cooldownhud") {
                thenExecute {
                    ScreenUtil.setScreenLater(
                        MoveHudOnScreen(
                            ::setMiddleXPercent,
                            ::setMiddleYPercent,
                            ::setIsEnabled
                        )
                    )
                }
            }
        }

        HudRenderCallback.EVENT.register { drawCtx, _ ->
            if (settings.isEnabled && Settings.enableMod) {
                fun drawItemCentered(stack: ItemStack, centerX: Int, centerY: Int) {
                    drawCtx.drawItem(
                        stack,
                        centerX - 8,
                        centerY - 8
                    )
                }

                fun drawTextCentered(msg: String, centerX: Int, centerY: Int) {
                    drawCtx.drawText(
                        mc.textRenderer,
                        msg,
                        (centerX - (mc.textRenderer.getWidth(msg).toFloat() * 0.5)).toInt(),
                        (centerY - 3.5).toInt(),
                        -1,
                        true
                    )
                }

                drawCtx.matrices.push()

                var centerX = (mc.window.scaledWidth.toFloat() * (settings.middleXPercent / 100)).toInt()
                val centerY = (mc.window.scaledHeight.toFloat() * (settings.middleYPercent / 100)).toInt()

                var first: Int? = null
                var largest = 0
                var i = 0

                for (cooldown in cooldowns) {
                    if (cooldown.value.timeStr() != "") {
                        val str = cooldown.value.timeStr()

                        val r0 = mc.textRenderer.getWidth(cooldown.value.command)

                        val r = mc.textRenderer.getWidth(str)


                        val curr = if (r0 > r) {
                            r0 + 4
                        } else {
                            r + 3 // 3px padding after text length
                        }

                        largest = largest.coerceAtLeast(curr)
                        i++

                        if (first == null) {
                            first = curr
                        }
                    }
                }

                if (first != null) {
                    centerX += (first / 2)
                }

                centerX -= (largest * i) / 2

                val padding = 3

                if (settings.isBackgroundOn && i > 0) {
                    drawCtx.fill(
                        (centerX - largest / 2) - padding,
                        (centerY - 8) - padding,
                        ((centerX - largest / 2) + (largest * i) - 6) + padding * 2,
                        ((centerY - 8) + 16 + 11 + 11 + 2) + padding * 2,
                        -1072689136
                    )
                }
                for (cooldown in cooldowns) {
                    if (cooldown.value.timeStr() != "") {

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
                }

                drawCtx.matrices.pop()
            }
        }

        listenToChatEvent()
    }

    private val BLEED_REGEX = "^\\*\\* ENEMY BLEEDING \\(.+\\) \\*\\*$".toRegex()

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

            if (DIRE_REGEX.matches(event.msg)) {
                DIRE_REGEX.find(event.msg)?.groups?.get(1)?.let {
                    cooldowns["dire"]?.cooldownInSeconds = it.value.toInt()
                    cooldowns["dire"]?.timeSinceLastUsed = System.currentTimeMillis()
                }
            }

            if (BLEED_REGEX.matches(event.msg)) {
                cooldowns["bleed"]?.timeSinceLastUsed = System.currentTimeMillis()
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

