package com.minesentry.minesentry.config

import com.akuleshov7.ktoml.annotations.TomlComments
import kotlinx.serialization.Serializable

@Serializable
data class MsConfig(

    /** APIに関する設定 */
    @TomlComments(
        "==============================",
        "   [ APIの設定 ]",
        "=============================="
    )
    var api: Api = Api(),


    /** コンフィグのバージョン */
    @TomlComments(
        "コンフィグのバージョン",
        "絶対に変更しないでください！"
    )
    var configVersion: Int = 0,

    ) {

    companion object {
        /** 現在のコンフィグのバージョン */
        const val NOW_CONFIG_VERSION = 1
    }

    @Serializable
    data class Api(
        /** APIのURL */
        @TomlComments(
            "APIサーバーのURL",
            "Def: \"https://api.minesentry.com/\""
        )
        var url: String = "https://api.minesentry.com/",

        /** APIキー */
        @TomlComments(
            "APIキー - コミュニティ登録をすることで取得できます。( https://minesentry.com/docs/procedures/community )",
            "設定することで、警告機能の利用が可能になります。",
            "Def: \"\""
        )
        var key: String? = "",
    )

}