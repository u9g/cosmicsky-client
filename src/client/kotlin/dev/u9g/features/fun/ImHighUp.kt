package dev.u9g.features.`fun`

import dev.u9g.commands.thenExecute
import dev.u9g.events.ChatMessageReceivedCallback
import dev.u9g.events.CommandCallback
import dev.u9g.features.Settings
import dev.u9g.mc
import java.text.DecimalFormat
import kotlin.math.ceil

class ImHighUp {
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

                    val balanceAsNumber = balanceWithCommas.replace(",", "")
                        .toDoubleOrNull()?.let { d ->
                            ceil(d).toInt()
                        }

                    if (balanceAsNumber != null) {
                        val jpTickets = balanceAsNumber / 10_000

                        val jpTicketsString = if (jpTickets > 0) {
                            "enough for a cf or $jpTickets jackpot tickets"
                        } else {
                            "$${addCommas.format(10_000 - balanceAsNumber)} away from a cf"
                        }

                        mc.player?.networkHandler?.sendChatMessage("ImHighUp's Balance: $$balanceAsNumber (${jpTicketsString})")
                    }
                }
            }
        }
    }
}