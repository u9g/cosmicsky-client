package dev.u9g.events

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.tree.LiteralCommandNode
import dev.u9g.commands.CaseInsensitiveLiteralCommandNode
import dev.u9g.commands.DefaultSource
import dev.u9g.commands.literal
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.command.CommandRegistryAccess

fun interface CommandCallback {
    operator fun invoke(event: CommandEvent)

    companion object {
        @JvmStatic
        val event = EventFactory.createArrayBacked(CommandCallback::class.java) { callbacks ->
            CommandCallback { event ->
                for (callback in callbacks)
                    callback(event)

            }
        }
    }
}

data class CommandEvent(
    val dispatcher: CommandDispatcher<DefaultSource>,
    val ctx: CommandRegistryAccess,
    val serverCommands: CommandDispatcher<*>?,
) {
    fun deleteCommand(name: String) {
        dispatcher.root.children.removeIf { it.name.equals(name, ignoreCase = false) }
        serverCommands?.root?.children?.removeIf { it.name.equals(name, ignoreCase = false) }
    }

    fun register(
        name: String,
        block: CaseInsensitiveLiteralCommandNode.Builder<DefaultSource>.() -> Unit
    ): LiteralCommandNode<DefaultSource> {
        return dispatcher.register(literal(name, block))
    }
}