package com.minesentry.minesentry.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder

internal object Commands {

    fun <S> literal(literal: String): LiteralArgumentBuilder<S> = LiteralArgumentBuilder.literal(literal)

}