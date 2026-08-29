package com.minesentry.minesentry.command.aguments.resolver

import com.minesentry.minesentry.command.MsCommandSource
import com.minesentry.minesentry.patch.MsPlayer

class PlayerResolver(
    private val raw: String
) {

    fun resolve(source: MsCommandSource): MsPlayer {
        return source.sender.server.getOnlinePlayers().first { it.name == raw }
    }

}