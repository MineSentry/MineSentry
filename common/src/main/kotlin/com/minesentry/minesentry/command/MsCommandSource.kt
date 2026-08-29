package com.minesentry.minesentry.command

interface MsCommandSource {

    val sender: MsCommandSender

    fun hasPermission(permission: String): Boolean

}

fun interface MsCommandSourceAdapter<S> {

    fun adapt(source: S): MsCommandSource

}
