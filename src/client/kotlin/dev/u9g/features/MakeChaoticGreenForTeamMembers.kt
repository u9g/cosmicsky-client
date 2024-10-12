package dev.u9g.features

import dev.u9g.events.DecorateNameAboveHeadCallback
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import java.util.regex.Pattern

object MakeChaoticGreenForTeamMembers {
    init {
        DecorateNameAboveHeadCallback.event.register {
            if (!Settings.enableMod) return@register

            if (Settings.teamMembers.contains(it.username)) {
                val root = Text.of(
                    Pattern.compile("§f ${it.username}").matcher(
                        Pattern.compile("§e ${it.username}").matcher(
                            Pattern.compile("§c☠§c ${it.username} §c☠").matcher(it.textToSend.string)
                                .replaceAll("§a☠§a ${it.username} §a☠")
                        ).replaceAll("§a ${it.username}")
                    ).replaceAll("§a ${it.username}")
                )

                it.textToSend = root as MutableText
            }
        }
    }
}