<a href="https://minesentry.com/">![](/images/minesentry-header.png)</a>
<p align="center">
  <strong>日本のコミュニティのための情報共有サービス</strong><br />
  Minecraft 内の違反情報を、コミュニティ間で共有します。
</p>
<p align="center">
  <a href="https://modrinth.com/plugin/minesentry">
    <img
      src="https://img.shields.io/badge/Download-Modrinth-00AF5C?logo=modrinth&logoColor=white"
      alt="Download on Modrinth"
    />
  </a>
  <a href="https://github.com/MineSentry/MineSentry/releases">
    <img
      src="https://img.shields.io/badge/GitHub-Releases-181717?logo=github"
      alt="Download on GitHub"
    />
  </a>
</p>

---

### 開発者向け

This project uses [Gradle](https://gradle.org/).
To build and run the application, use the *Gradle* tool window by clicking the Gradle icon in the right-hand toolbar,
or run it directly from the terminal:

* Run `./gradlew run` to build and run the application.
* Run `./gradlew build` to only build the application.
* Run `./gradlew check` to run all checks, including tests.
* Run `./gradlew clean` to clean all build outputs.

Note the usage of the Gradle Wrapper (`./gradlew`).
This is the suggested way to use Gradle in production projects.

[Learn more about the Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html).

[Learn more about Gradle tasks](https://docs.gradle.org/current/userguide/command_line_interface.html#common_tasks).

This project follows the suggested multi-module setup and consists of the `app` and `utils` subprojects.
The shared build logic was extracted to a convention plugin located in `buildSrc`.

This project uses a version catalog (see `gradle/libs.versions.toml`) to declare and version dependencies
and both a build cache and a configuration cache (see `gradle.properties`).