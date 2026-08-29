package com.minesentry.minesentry

import com.minesentry.minesentry.patch.MsServer
import java.io.File
import java.util.logging.Logger

/** MineSentryのプラットフォーム別実装のIF */
interface MsPlatform {

    /** プラグインのロガー */
    val logger: Logger

    /** プラグインのデータを配置するディレクトリ */
    val dataDir: File

    /** サーバーのパッチ */
    val server: MsServer

}