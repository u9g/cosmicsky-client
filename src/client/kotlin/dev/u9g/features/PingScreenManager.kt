package dev.u9g.features

import dev.u9g.mc
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.*
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.text.Text

object PingScreenManager {
    private val pingScreenKey = KeyBinding("Open Pings Screen", InputUtil.GLFW_KEY_RIGHT_BRACKET, "Pings")

    init {
        ClientTickEvents.END_CLIENT_TICK.register {
            if (!Settings.enableMod) return@register

            while (pingScreenKey.wasPressed()) {
                mc.setScreen(PingScreen())
            }
        }
    }
}

class PingScreen : BaseOwoScreen<FlowLayout>() {
    override fun createAdapter(): OwoUIAdapter<FlowLayout> {
        return OwoUIAdapter.create(
            this
        ) { a, b ->
            val verticalFlow = Containers.verticalFlow(a, b).children(pingsToRender.map {
                Containers.horizontalFlow(Sizing.content(), Sizing.content()).children(
                    listOf(
                        Components.label(
                            Text.of(
                                "§a§l${it.username} §r(${
                                    it.worldName.substring(
                                        0,
                                        20
                                    )
                                }) ${it.pos.x}x, ${it.pos.y}y, ${it.pos.z}z "
                            )
                        ),
                        Components.button(Text.of("§lX")) { _ ->
                            pingsToRender = pingsToRender.filter { ping -> ping.username != it.username }
                            mc.submit {
                                mc.setScreen(null)
                                mc.submit {
                                    mc.setScreen(PingScreen())
                                }
                            }
                        }
                    )
                ).horizontalAlignment(HorizontalAlignment.CENTER).verticalAlignment(VerticalAlignment.CENTER)
            })

            verticalFlow.surface(Surface.VANILLA_TRANSLUCENT)
            verticalFlow.horizontalAlignment(HorizontalAlignment.CENTER)
            verticalFlow.verticalAlignment(VerticalAlignment.CENTER)

            verticalFlow
        }
    }

    override fun build(rootComponent: FlowLayout) {

    }
}