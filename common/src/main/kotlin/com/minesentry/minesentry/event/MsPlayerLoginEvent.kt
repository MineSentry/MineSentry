package com.minesentry.minesentry.event

import com.minesentry.minesentry.MineSentry
import com.minesentry.minesentry.patch.MsPlayerIdentity
import net.kyori.adventure.text.Component

class MsPlayerLoginEvent(
    val player: MsPlayerIdentity,
) {

    var rejectionMessage: Component? = null
        private set

    /** MiniMessage形式のメッセージで参加を拒否します。 */
    fun reject(message: String) {
        rejectionMessage = MineSentry.miniMessage.deserialize(message)
    }

}
