package dev.u9g.commands

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.u9g.util.MinecraftDispatcher
import dev.u9g.util.coroutineScope
import dev.u9g.util.iterate
import kotlinx.coroutines.launch
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable


typealias DefaultSource = FabricClientCommandSource


inline val <T : CommandContext<*>> T.context get() = this
operator fun <T : Any, C : CommandContext<*>> C.get(arg: TypeSafeArg<T>): T {
    return arg.get(this)
}

fun literal(
    name: String,
    block: CaseInsensitiveLiteralCommandNode.Builder<DefaultSource>.() -> Unit
): CaseInsensitiveLiteralCommandNode.Builder<DefaultSource> =
    CaseInsensitiveLiteralCommandNode.Builder<DefaultSource>(name).also(block)


private fun normalizeGeneric(argument: Type): Class<*> {
    return when (argument) {
        is Class<*> -> argument
        is TypeVariable<*> -> normalizeGeneric(argument.bounds[0])
        is ParameterizedType -> normalizeGeneric(argument.rawType)
        else -> Any::class.java
    }
}

data class TypeSafeArg<T : Any>(val name: String, val argument: ArgumentType<T>) {
    val argClass by lazy {
        argument.javaClass
            .iterate<Class<in ArgumentType<T>>> {
                it.superclass
            }
            .flatMap {
                it.genericInterfaces.toList()
            }
            .filterIsInstance<ParameterizedType>()
            .find { it.rawType == ArgumentType::class.java }!!
            .let { normalizeGeneric(it.actualTypeArguments[0]) }
    }

    @JvmName("getWithThis")
    fun <S> CommandContext<S>.get(): T =
        get(this)


    fun <S> get(ctx: CommandContext<S>): T {
        try {
            return ctx.getArgument(name, argClass) as T
        } catch (e: Exception) {
            if (ctx.child != null) {
                return get(ctx.child)
            }
            throw e
        }
    }
}


fun <T : Any> argument(
    name: String,
    argument: ArgumentType<T>,
    block: RequiredArgumentBuilder<DefaultSource, T>.(TypeSafeArg<T>) -> Unit
): RequiredArgumentBuilder<DefaultSource, T> =
    RequiredArgumentBuilder.argument<DefaultSource, T>(name, argument).also { block(it, TypeSafeArg(name, argument)) }

fun <T : ArgumentBuilder<DefaultSource, T>, AT : Any> T.thenArgument(
    name: String,
    argument: ArgumentType<AT>,
    block: RequiredArgumentBuilder<DefaultSource, AT>.(TypeSafeArg<AT>) -> Unit
): T = then(argument(name, argument, block))

fun <T : RequiredArgumentBuilder<DefaultSource, String>> T.suggestsList(provider: CommandContext<DefaultSource>.() -> Iterable<String>) {
    suggests { context, builder ->
        provider(context)
            .asSequence()
            .filter { it.startsWith(builder.remaining, ignoreCase = true) }
            .forEach {
                builder.suggest(it)
            }
        builder.buildFuture()
    }
}

fun <T : ArgumentBuilder<DefaultSource, T>> T.thenLiteral(
    name: String,
    block: CaseInsensitiveLiteralCommandNode.Builder<DefaultSource>.() -> Unit
): T =
    then(literal(name, block))

fun <T : ArgumentBuilder<DefaultSource, T>> T.then(node: ArgumentBuilder<DefaultSource, *>, block: T.() -> Unit): T =
    then(node).also(block)

fun <T : ArgumentBuilder<DefaultSource, T>> T.thenExecute(block: suspend CommandContext<DefaultSource>.() -> Unit): T =
    executes {
        coroutineScope.launch(MinecraftDispatcher) {
            block(it)
        }
        1
    }


