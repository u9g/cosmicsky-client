package dev.u9g.util

import dev.u9g.features.CooldownManager
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.CheckboxComponent
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.component.DiscreteSliderComponent
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.HorizontalAlignment
import io.wispforest.owo.ui.core.OwoUIAdapter
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.VerticalAlignment
import net.minecraft.text.Text

class MoveHudOnScreen(
    val setX: (Double) -> Unit,
    val setY: (Double) -> Unit,
    val setEnabled: (Boolean) -> Unit
) : BaseOwoScreen<FlowLayout>() {
    override fun build(rootComponent: FlowLayout) {
        fun fixAlignment() {
            rootComponent.verticalAlignment(
                if (CooldownManager.settings.middleYPercent > 50) {
                    VerticalAlignment.TOP
                } else {
                    VerticalAlignment.BOTTOM
                }
            )
        }

        rootComponent.horizontalAlignment(
            HorizontalAlignment.CENTER
        )

        rootComponent.childById(DiscreteSliderComponent::class.java, "x")
            .also {
                it.onChanged().subscribe { value ->
                    setX(value)
                }
            }
            .also {
                it.slideEnd().subscribe {
                    fixAlignment()
                }
            }


        rootComponent.childById(DiscreteSliderComponent::class.java, "y").also {
            it.onChanged().subscribe { value ->
                setY(value)
            }
        }.also {
            it.slideEnd().subscribe {
                fixAlignment()
            }
        }

        rootComponent.childById(CheckboxComponent::class.java, "background")
            .onChanged {
                CooldownManager.setIsBackgroundOn(!CooldownManager.settings.isBackgroundOn)
            }

        rootComponent.childById(CheckboxComponent::class.java, "enabled")
            .onChanged {
                setEnabled(it)
            }
    }

    override fun createAdapter(): OwoUIAdapter<FlowLayout> {
        return OwoUIAdapter.create(
            this
        ) { a, b ->
            Containers.verticalFlow(a, b).child(
                Containers.grid(Sizing.content(), Sizing.content(), 2, 2).child(
                    Components.discreteSlider(Sizing.fill(50), 0.0, 100.0)
                        .value(CooldownManager.settings.middleXPercent / 100)
                        .id("x"), 0, 0
                ).child(
                    Components.discreteSlider(Sizing.fill(50), 0.0, 100.0)
                        .value(CooldownManager.settings.middleYPercent / 100)
                        .id("y"), 1, 0
                ).child(
                    Components.checkbox(Text.of("Background")).checked(CooldownManager.settings.isBackgroundOn)
                        .id("background"),
                    0, 1
                ).child(
                    Components.checkbox(Text.of("Enabled")).checked(CooldownManager.settings.isBackgroundOn)
                        .id("enabled"),
                    1, 1
                )
            )
        }
    }
}