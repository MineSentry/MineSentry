package com.minesentry.minesentry

import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.TomlIndentation
import com.akuleshov7.ktoml.TomlInputConfig
import com.akuleshov7.ktoml.TomlOutputConfig
import com.minesentry.lib.MineSentryClient
import com.minesentry.lib.model.CommunityCheckResponse
import com.minesentry.lib.model.PunishmentSummary
import com.minesentry.minesentry.config.ConfigHelper
import com.minesentry.minesentry.event.MsPlayerLoginEvent
import com.minesentry.minesentry.event.MsPlayerPostLoginEvent
import com.minesentry.minesentry.util.ApiUtil
import net.kyori.adventure.text.minimessage.MiniMessage
import kotlin.system.measureTimeMillis

object MineSentry {

    lateinit var api: MineSentryClient private set
    lateinit var platform: MsPlatform private set
    lateinit var configHelper: ConfigHelper private set

    val TOML = Toml(
        inputConfig = TomlInputConfig(
            ignoreUnknownNames = true,
            allowEmptyValues = true,
            allowNullValues = true,
            allowEscapedQuotesInLiteralStrings = true,
            allowEmptyToml = true,
            ignoreDefaultValues = false,
        ),
        outputConfig = TomlOutputConfig(
            indentation = TomlIndentation.TWO_SPACES,
        )
    )

    val miniMessage = MiniMessage.miniMessage()

    fun inject(platform: MsPlatform) {
        this.platform = platform
    }

    fun onEnable(platformRunnable: Runnable) {
        // コンフィグ
        try {
            platform.dataDir.mkdirs()
            configHelper = ConfigHelper(platform.dataDir.resolve("config.toml"))
        }catch (e: Exception) {
            platform.logger.severe("設定ファイルの読み込み中にエラーが発生しました。")
            throw e
        }

        // API クライアント
        newApiClient()

        // プラットフォーム別の起動処理
        val times = measureTimeMillis {
            platformRunnable.run()
        }

        platform.logger.info("MineSentryが起動しました！ (${times}ms)")
    }

    fun onDisable() {}

    fun onPlayerLogin(event: MsPlayerLoginEvent) {
        val player = event.player
        val uuid = player.uuid

        // APIからプレイヤーデータを取得する
        val result = try {
            ApiUtil.getCacheOrRequestPlayerData(uuid)
        } catch (e: Exception) {
            platform.logger.warning("プレイヤーデータの取得中にエラーが発生しました: ${e.message}")
            return
        }

        // プレイヤーが登録されていなければ無視
        val playerData = result?.activeGbans?.firstOrNull() ?: return

        event.reject(
            "<gold><b>[MineSentry]</b></gold>" +
            "<br><b><red>あなたはグローバルBANされています！</red></b><br>" +
            "<br>" +
            "<gray>理由: </gray><yellow>${playerData.reason}</yellow><br><br>" +
            "<gray>※詳細の確認・異議申し立てを希望する場合は、               <br>" +
            "          <u>https://minesentry.com/</u> にアクセスしてください。</gray>"
        )
    }

    fun onPostLogin(event: MsPlayerPostLoginEvent) {
        val player = event.player
        val playerData = ApiUtil.getCacheOrRequestPlayerData(player.uuid) ?: return

        if (playerData.activeWarns.isEmpty() && playerData.activeGbans.isEmpty()) return

        platform.server.getOnlinePlayers()
            .filter { it.hasPermission("minesentry.notification") }
            .forEach { moderator ->
                moderator.sendRichMessage(
                    "<gold>[MineSentry] ${player.name}には${playerData.activeWarns.size + playerData.activeGbans.size}件のペナルティが登録されています。</gold>" +
                            "<br><gray>詳細: <u><click:run_command:'/msentry check ${player.name}'>/msentry check ${player.name}</click></u></gray>"
                )
            }
    }


    fun newApiClient(): MineSentryClient {
        val apiConfig = configHelper.config.api
        api = MineSentryClient(
            baseUrl = apiConfig.url,
            communityApiKey = apiConfig.key
        )
        return api
    }

}
