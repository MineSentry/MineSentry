package com.minesentry.minesentry.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.tree.ArgumentCommandNode
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class MsCommandAdapterTest {

    private data class NativeSource(val allowed: Boolean)

    @Test
    fun `native source is adapted for command requirements`() {
        val dispatcher = CommandDispatcher<NativeSource>()
        MsCommand.register(dispatcher) { native ->
            object : MsCommandSource {
                override val sender: MsCommandSender
                    get() = error("This test does not execute the command")

                override fun hasPermission(permission: String) = native.allowed
            }
        }

        val requirement = dispatcher.root
            .getChild("minesentry")
            .getChild("reload")
            .requirement

        assertTrue(requirement.test(NativeSource(allowed = true)))
        assertFalse(requirement.test(NativeSource(allowed = false)))
    }

    @Test
    fun `player uses a standard argument type`() {
        val dispatcher = CommandDispatcher<NativeSource>()
        MsCommand.register(dispatcher) { error("This test only inspects the command tree") }

        val playerArgument = dispatcher.root
            .getChild("minesentry")
            .getChild("check")
            .getChild("player")

        assertIs<StringArgumentType>(
            assertIs<ArgumentCommandNode<NativeSource, *>>(playerArgument).type
        )
    }

}
