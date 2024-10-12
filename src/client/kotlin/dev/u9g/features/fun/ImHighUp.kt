package dev.u9g.features.`fun`

import dev.u9g.commands.thenExecute
import dev.u9g.events.ChatMessageReceivedCallback
import dev.u9g.events.CommandCallback
import dev.u9g.features.Settings
import dev.u9g.mc
import net.minecraft.client.MinecraftClient
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat


object ImHighUp {
    private val regex = "ImHighUp's Balance: \\$([\\d,.]+)".toRegex()
    private val addCommas = DecimalFormat("#,###")

    init {
        CommandCallback.event.register {
            it.register("imhighup") {
                thenExecute {
                    if (Settings.enableMod) {
                        mc.player?.networkHandler?.sendChatCommand("bal ImHighUp")
                    }
                }
            }
        }

        ChatMessageReceivedCallback.event.register {
            if (Settings.enableMod) {
                val matched = regex.matchEntire(it.msg)

                if (matched != null) {
                    val (balanceWithCommas) = matched.destructured

                    val balanceAsNumber = BigDecimal(balanceWithCommas.replace(",", ""))
                        .setScale(0, RoundingMode.UP)

                    val jpTickets = balanceAsNumber / 10_000.bi()

                    val jpTicketsString = if (jpTickets > 0.bi()) {
                        "enough for a cf or $jpTickets jackpot tickets"
                    } else {
                        "$${addCommas.format(10_000.bi() - balanceAsNumber)} away from a cf"
                    }

                    MinecraftClient.getInstance().submit {
                        mc.player?.networkHandler?.sendChatMessage("ImHighUp's Balance: $$balanceAsNumber (${jpTicketsString})")
                    }
                }
            }
        }
    }
}

fun Int.bi(): BigDecimal {
    return BigDecimal(this.toString())
}