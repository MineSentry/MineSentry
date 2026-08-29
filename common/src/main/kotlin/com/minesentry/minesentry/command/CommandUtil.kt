package com.minesentry.minesentry.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.ParseResults
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import java.util.concurrent.CompletableFuture

object CommandUtil {

    /**
     * 利用可能なコマンドの補完提案を取得します。
     * [CommandUtil]で実装されています。
     */
    fun <S> CommandDispatcher<S>.getAccessibleSuggestions(
        parse: ParseResults<S>
    ): CompletableFuture<Suggestions> {
        val context = parse.context
        val cursor = parse.reader.totalLength

        val suggestionContext = context.findSuggestionContext(cursor)
        val parent = suggestionContext.parent
        val start = minOf(suggestionContext.startPos, cursor)

        val fullInput = parse.reader.string
        val truncatedInput = fullInput.substring(0, cursor)

        val futures = parent.children
            .filter { node ->
                node.canUse(context.source)
            }
            .map { node ->
                try {
                    node.listSuggestions(
                        context.build(truncatedInput),
                        SuggestionsBuilder(truncatedInput, start)
                    )
                } catch (_: CommandSyntaxException) {
                    Suggestions.empty()
                }
            }

        return CompletableFuture
            .allOf(*futures.toTypedArray())
            .thenApply {
                Suggestions.merge(
                    fullInput,
                    futures.map { it.join() }
                )
            }
    }

}