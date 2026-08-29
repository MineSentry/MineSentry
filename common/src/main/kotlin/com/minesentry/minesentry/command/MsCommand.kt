package com.minesentry.minesentry.command

import com.minesentry.lib.MineSentryException
import com.minesentry.lib.model.PunishmentSummary
import com.minesentry.lib.model.PunishmentType
import com.minesentry.minesentry.MineSentry
import com.minesentry.minesentry.command.aguments.Arguments
import com.minesentry.minesentry.command.aguments.resolver.PlayerResolver
import com.minesentry.minesentry.util.ApiUtil
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import java.io.IOException
import java.util.logging.Level

object MsCommand {

    private const val PAGE_SIZE = 5

    private data class PunishmentData(
        val punishment: PunishmentSummary,
        val type: PunishmentType,
    )

    fun register(dispatcher: CommandDispatcher<MsCommandSource>) =
        register(dispatcher) { it }

    fun <S> register(
        dispatcher: CommandDispatcher<S>,
        sourceAdapter: MsCommandSourceAdapter<S>,
    ) {
        val reloadNode = Commands.literal<S>("reload")
            .requires { sourceAdapter.adapt(it).hasPermission("minesentry.command.reload") }
            .executes { ctx ->
                val source = sourceAdapter.adapt(ctx.source)
                try {
                    MineSentry.configHelper.reload()
                } catch (e: IOException) {
                    MineSentry.platform.logger.log(Level.SEVERE, "設定ファイルの再読み込み中にエラーが発生しました。", e)
                    source.sender.sendErrorMessage("設定ファイルの再読み込み中にエラーが発生しました。詳しくはサーバーログを確認してください。")
                    return@executes Command.SINGLE_SUCCESS
                }

                MineSentry.newApiClient()

                source.sender.sendCompleteMessage("設定を再読み込みしました！")

                Command.SINGLE_SUCCESS
            }

        val statusNode = Commands.literal<S>("status")
            .requires { sourceAdapter.adapt(it).hasPermission("minesentry.command.status") }
            .executes { ctx ->
                val sender = sourceAdapter.adapt(ctx.source).sender
                sender.sendMessage(
                    Component.text("ステータスを確認しています...", NamedTextColor.GRAY)
                )
                try {
                    val status = MineSentry.api.probeStatus()

                    val gban = if (status.apiReachable) "<green>利用可能</green>" else "<red>利用不可</red>"
                    val gbanDetail = status.apiError?.let { "<br><gray>  └ $it</gray>" }.orEmpty()
                    val community = when {
                        !status.communityConfigured ->
                            "<yellow>未設定</yellow> <gray>(config.toml で APIキー の設定が必要)</gray>"

                        status.communityAvailable -> {
                            val c = status.community!!
                            "<green>利用可能</green><br><gray>  └ ${c.communityName} (ID: ${c.communityId})</gray>"
                        }

                        else -> {
                            val detail = status.communityError?.let { "<br><gray>  └ $it</gray>" }.orEmpty()
                            "<red>利用不可</red>$detail"
                        }
                    }
                    val statusMessage = "<gold><b>MineSentry - ステータス</b></gold>" +
                            "<br><gray>◆API: ${MineSentry.configHelper.config.api.url}</gray>" +
                            "<br><gray>◆グローバルBAN:</gray> $gban$gbanDetail" +
                            "<br><gray>◆コミュニティ機能:</gray> $community"

                    sender.sendRichMessage(statusMessage)
                } catch (e: Exception) {
                    sender.sendErrorMessage("ステータス確認中にエラーが発生しました: ${e.message}")
                }
                Command.SINGLE_SUCCESS
            }

        val checkNode = Commands.literal<S>("check")
            .requires { sourceAdapter.adapt(it).hasPermission("minesentry.command.check") }
            .then(Arguments.player("player", sourceAdapter)
                .executes { checkCmd(it, sourceAdapter) }
                .then(Arguments.integer<S>("page")
                        .executes { ctx ->
                            val page = ctx.getArgument("page", Int::class.java)
                            return@executes checkCmd(ctx, sourceAdapter, page)
                        }
                )
            )



        dispatcher.register(
            Commands.literal<S>("minesentry")
                .then(reloadNode)
                .then(statusNode)
                .then(checkNode)
        )

        dispatcher.register(
            Commands.literal<S>("msentry")
                .then(reloadNode)
                .then(statusNode)
                .then(checkNode)
        )
    }


    private fun <S> checkCmd(
        ctx: CommandContext<S>,
        sourceAdapter: MsCommandSourceAdapter<S>,
        page: Int = 1,
    ): Int {
        val source = sourceAdapter.adapt(ctx.source)
        val sender = source.sender
        val player = PlayerResolver(ctx.getArgument("player", String::class.java)).resolve(source)

        // APIからプレイヤーデータを取得
        val playerData = try {
            ApiUtil.getCacheOrRequestPlayerData(player.uuid)
        } catch (e: MineSentryException) {
            sender.sendErrorMessage("プレイヤーデータの取得中にエラーが発生しました: ${e.message}")
            return Command.SINGLE_SUCCESS
        } catch (e: Exception) {
            sender.sendErrorMessage("予期せぬエラーが発生しました: ${e.message}")
            MineSentry.platform.logger.log(Level.SEVERE, "Unexpected error while checking ban status", e)
            return Command.SINGLE_SUCCESS
        }

        // ペナルティがない場合
        if (playerData == null || (playerData.activeWarns.isEmpty() && playerData.activeGbans.isEmpty())) {
            sender.sendCompleteMessage("プレイヤーには有効なペナルティがありませんでした。</green>" +
                        "<br><gray><u><click:open_url:'https://minesentry.com/players/${player.uuid}'>(ウェブサイトで確認する)</click></u></gray>"
            )
            return Command.SINGLE_SUCCESS
        }

        val punishments = buildList {
            addAll(playerData.activeWarns.map { PunishmentData(it, PunishmentType.WARN) })
            addAll(playerData.activeGbans.map { PunishmentData(it, PunishmentType.GBAN) })
        }.sortedBy { it.punishment.id }

        val maxPage = (punishments.size + PAGE_SIZE - 1) / PAGE_SIZE
        val pageBody = punishments
            .chunked(PAGE_SIZE)
            .getOrElse(page - 1) { emptyList() }
            .joinToString("<br>") { (punishment, type) ->
                val id = punishment.id
                "<aqua><click:open_url:'https://minesentry.com/punishments/$id'>#$id</click></aqua>" +
                        "<gray>(<gold>${type.name}</gold>) -</gray> <yellow>${punishment.reason}</yellow>"
            }

        fun pageLink(target: Int, label: String) =
            if (target in 1..maxPage) {
                "<click:run_command:'/msentry check ${player.name} $target'><white>$label</white></click>"
            } else {
                " "
            }

        sender.sendRichMessage(
            "<gold>[MineSentry]</gold> <red>プレイヤーには <b><yellow>${punishments.size}件のペナルティ</yellow></b> が発行されています。</red>" +
                    "<br><gray><u><click:open_url:'https://minesentry.com/players/${player.uuid}'>(公式サイトで確認する)</click></u></gray>" +
                    "<br>$pageBody<br><br>" +
                    "<gray><st>　　　　　　　　　　　　　</st>　${pageLink(page - 1, "<")} $page / $maxPage ${
                        pageLink(
                            page + 1,
                            ">"
                        )
                    }　<st>　　　　　　　　　　　　　</st></gray>"
        )
        return Command.SINGLE_SUCCESS
    }

}
