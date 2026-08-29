package com.minesentry.minesentry.spigot

import com.minesentry.minesentry.command.CommandUtil.getAccessibleSuggestions
import com.minesentry.minesentry.command.MsCommandSource
import com.minesentry.minesentry.spigot.platform.PlatformUtil.patch
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.exceptions.CommandSyntaxException
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class MineSentryCommand(
    val dispatcher: CommandDispatcher<MsCommandSource>
): CommandExecutor, TabCompleter {

    private fun commandLine(command: Command, args: Array<out String>): String {
        return "${command.name} ${args.joinToString(" ")}"
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val cmd = commandLine(command, args)
        val source = sender.patch()
        val result = try {
            dispatcher.execute(cmd, source)
        }catch (_: CommandSyntaxException) {
            source.sender.sendRichMessage("<red>不明なコマンドです。")
        }

        return result == 1
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
        val cmd = commandLine(command, args)
        val source = sender.patch()
        val parse = dispatcher.parse(cmd, source)

        return dispatcher
            .getAccessibleSuggestions(parse)
            .join()
            .list
            .map { it.text }
    }

}