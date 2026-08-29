package com.minesentry.minesentry.util

import com.minesentry.lib.MineSentryNotFoundException
import com.minesentry.lib.model.CommunityCheckResponse
import com.minesentry.minesentry.MineSentry
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set

class ApiUtil {

    companion object {
        private val playersCache = ConcurrentHashMap<UUID, CommunityCheckResponse>()

        /**
         * キャッシュまたはAPIサーバーから、プレイヤーのデータを取得します。
         * api-key 設定時はコミュニティ API（GBAN + WARN）、未設定時は公開 API（GBAN のみ）。
         */
        fun getCacheOrRequestPlayerData(playerUUID: UUID): CommunityCheckResponse? {
            return playersCache[playerUUID] ?: try {
                val result = fetchPlayerData(playerUUID)
                playersCache[playerUUID] = result
                result
            } catch (_: MineSentryNotFoundException) {
                null
            }
        }

        private fun fetchPlayerData(playerUUID: UUID): CommunityCheckResponse {
            val uuid = playerUUID.toString()
            val apiKey = MineSentry.configHelper.config.api.key
            return if (!apiKey.isNullOrBlank()) {
                MineSentry.api.communityCheck(uuid = uuid)
            } else {
                val public = MineSentry.api.check(uuid = uuid)
                CommunityCheckResponse(
                    banned = public.banned,
                    player = public.player,
                    activeGbans = public.activeGbans,
                    activeWarns = emptyList(),
                )
            }
        }

        fun removeCache(playerUUID: UUID) {
            playersCache.remove(playerUUID)
        }

        fun clearCache() {
            playersCache.clear()
        }

        /** config に api-key が設定されているか（ネットワーク確認なし） */
        fun isCommunityFeaturesConfigured(): Boolean =
            MineSentry.api.isCommunityConfigured()

        /** API に問い合わせてコミュニティ機能が利用可能か確認する */
        fun isCommunityFeaturesAvailable(): Boolean =
            MineSentry.api.probeStatus().communityFeaturesAvailable
    }

}