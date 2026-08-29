package com.minesentry.minesentry.patch

import com.minesentry.minesentry.command.MsCommandSender
import java.util.UUID

abstract class MsPlayer: MsCommandSender() {

    abstract val uuid: UUID

}