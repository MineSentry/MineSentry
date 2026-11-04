package buildsrc.convention

import groovy.json.JsonOutput
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * 配布用 jar を `build/release/` にリリース名でコピーし、ダウンロードページ登録用の情報を
 * `build/release/release-info.json` に書き出す。scripts/release/publish.mjs が全プロジェクト分を集める。
 */
abstract class ReleaseInfoTask : DefaultTask() {
    /** ビルドターゲット名（spigot / fabric-1.21 など）。ダウンロードページのバージョン番号に使う。 */
    @get:Input
    abstract val target: Property<String>

    /** ダウンロードページのプラットフォーム ID（fabric / neoforge / spigot / velocity）。 */
    @get:Input
    abstract val platform: Property<String>

    /** 対応 Minecraft バージョンの範囲。fabric 形式 (`>=1.21 <=1.21.10`)・maven 形式 (`[1.21,1.21.10]`)・空 (= 非依存)。 */
    @get:Input
    abstract val gameVersionRange: Property<String>

    @get:Input
    abstract val javaVersion: Property<Int>

    @get:Input
    abstract val version: Property<String>

    @get:Input
    abstract val fileName: Property<String>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val artifact: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun write() {
        val dir = outputDir.get().asFile
        dir.listFiles()?.forEach { it.delete() }
        artifact.get().asFile.copyTo(dir.resolve(fileName.get()), overwrite = true)
        val info = mapOf(
            "version" to version.get(),
            "target" to target.get(),
            "platform" to platform.get(),
            "file" to fileName.get(),
            "gameVersionRange" to gameVersionRange.get(),
            "javaVersion" to javaVersion.get(),
        )
        dir.resolve("release-info.json").writeText(JsonOutput.prettyPrint(JsonOutput.toJson(info)) + "\n")
    }
}
