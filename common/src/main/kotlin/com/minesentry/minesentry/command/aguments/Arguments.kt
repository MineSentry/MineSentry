package com.minesentry.minesentry.command.aguments

import com.minesentry.minesentry.command.MsCommandSourceAdapter
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.RequiredArgumentBuilder

object Arguments {

    fun <S> player(
        name: String,
        sourceAdapter: MsCommandSourceAdapter<S>,
    ): RequiredArgumentBuilder<S, String> =
        RequiredArgumentBuilder.argument<S, String>(name, StringArgumentType.word())
            .suggests { context, builder ->
                sourceAdapter.adapt(context.source).sender.server.getOnlinePlayers()
                    .asSequence()
                    .map { it.name }
                    .filter { it.startsWith(builder.remaining, ignoreCase = true) }
                    .forEach(builder::suggest)
                builder.buildFuture()
            }

    fun <S> integer(name: String): RequiredArgumentBuilder<S, Int> =
        RequiredArgumentBuilder.argument(name, IntegerArgumentType.integer())

    fun <S> integer(
        name: String,
        minimum: Int = Int.MIN_VALUE,
        maximum: Int = Int.MAX_VALUE
    ): RequiredArgumentBuilder<S, Int> =
        RequiredArgumentBuilder.argument(name, IntegerArgumentType.integer(minimum, maximum))


}
