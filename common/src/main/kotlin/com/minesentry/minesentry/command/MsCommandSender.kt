package com.minesentry.minesentry.command

import com.minesentry.minesentry.MineSentry
import com.minesentry.minesentry.patch.MsServer
import net.kyori.adventure.text.Component

abstract class MsCommandSender {

    /** 送信者の名前 */
    abstract val name: String

    /** 実行者が存在するサーバー */
    abstract val server: MsServer


    /** 権限を持っているかを確認する */
    abstract fun hasPermission(permission: String): Boolean

    /**
     * 成功用のプレフィックスを付けたMiniMessage形式のメッセージを送信します。
     */
    fun sendCompleteMessage(miniMessage: String) {
        sendRichMessage("<dark_gray>[<green><bold>✔</bold></green><dark_gray>]<green> $miniMessage")
    }

    /**
     * エラー用のプレフィックスを付けたMiniMessage形式のメッセージを送信します。
     */
    fun sendErrorMessage(miniMessage: String) {
        sendRichMessage("<dark_gray>[<red><bold>✖</bold></red><dark_gray>]<red> $miniMessage")
    }

    /**
     * MiniMessage形式のメッセージを送信します。
     */
    fun sendRichMessage(miniMessage: String) {
        val component = MineSentry.miniMessage.deserialize(miniMessage)
        sendMessage(component)
    }

    /** コンポーネントを送信します */
    abstract fun sendMessage(component: Component)

}