package com.minesentry.minesentry.command.aguments.resolver

import com.minesentry.minesentry.command.MsCommandSource
import com.minesentry.minesentry.patch.MsPlayerIdentity
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.UUID

class PlayerResolver(
    private val raw: String
) {

    /**
     * オンラインのプレイヤーから検索し、見つからなければMojang APIでUUIDを解決する。
     * 解決できなかった場合は null を返す。
     */
    fun resolve(source: MsCommandSource): MsPlayerIdentity? {
        source.sender.server.getOnlinePlayers()
            .firstOrNull { it.name.equals(raw, ignoreCase = true) }
            ?.let { return MsPlayerIdentity(it.name, it.uuid) }

        return lookupMojang()
    }

    private fun lookupMojang(): MsPlayerIdentity? {
        return try {
            val request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.mojang.com/users/profiles/minecraft/" + URLEncoder.encode(raw, Charsets.UTF_8)))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build()
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() != 200) return null

            val body = response.body()
            val id = ID_REGEX.find(body)?.groupValues?.get(1) ?: return null
            val name = NAME_REGEX.find(body)?.groupValues?.get(1) ?: raw
            val uuid = UUID.fromString(id.replaceFirst(UUID_REGEX, "$1-$2-$3-$4-$5"))
            MsPlayerIdentity(name, uuid)
        } catch (e: Exception) {
            null
        }
    }

    private companion object {
        val client: HttpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build()
        val ID_REGEX = Regex("\"id\"\\s*:\\s*\"([0-9a-fA-F]{32})\"")
        val NAME_REGEX = Regex("\"name\"\\s*:\\s*\"([^\"]+)\"")
        val UUID_REGEX = Regex("(.{8})(.{4})(.{4})(.{4})(.{12})")
    }

}
