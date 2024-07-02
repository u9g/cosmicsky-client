package dev.u9g.features.`fun`

import dev.u9g.commands.thenExecute
import dev.u9g.events.ChatMessageReceivedCallback
import dev.u9g.events.CommandCallback
import dev.u9g.features.Settings
import dev.u9g.mc
import java.text.DecimalFormat

class ImHighUp {
    private val regex = "ImHighUp's Balance: \\$([\\d,]+)".toRegex()
    private val decimalFormat = DecimalFormat("#,###")

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
                    val (balanceWithUnderscores) = matched.destructured

                    val balanceAsNumber = balanceWithUnderscores.replace(",", "").toIntOrNull(10)

                    if (balanceAsNumber != null) {
                        val jpTickets = balanceAsNumber / 10_000

                        val jpTicketsString = if (jpTickets > 0) {
                            "enough for a cf or $jpTickets jackpot tickets"
                        } else {
                            "$${decimalFormat.format(10_000 - balanceAsNumber)} away from a cf"
                        }

                        mc.player?.networkHandler?.sendChatMessage("ImHighUp's Balance: $$balanceWithUnderscores (${jpTicketsString})")
                    }
                }
            }
        }
    }
}