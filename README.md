This is a Kotlin Multiplatform project targeting Web, Desktop (JVM).

* [/composeApp](./composeApp/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./composeApp/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./composeApp/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./composeApp/src/jvmMain/kotlin)
    folder is the appropriate location.

### Build and Run Desktop (JVM) Application

To build and run the development version of the desktop app, use the run configuration from the run widget
in your IDE’s toolbar or run it directly from the terminal:
- on macOS/Linux
  ```shell
  ./gradlew :composeApp:run
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:run
  ```

#### Run with Hot Reload (Desktop)
This project applies the Compose Hot Reload Gradle plugin (org.jetbrains.compose.hot-reload). Use the following task to run the desktop app with hot reload so UI changes are applied instantly without restarting the app.

- on macOS/Linux
  ```shell
  ./gradlew :composeApp:hotRunJvm --mainClass=org.energy.pricing.MainKt
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:hotRunJvm --mainClass=org.energy.pricing.MainKt
  ```

Notes:
- The `runWithHotReload` task does not exist. Use `hotRunJvm` (or the deprecated alias `jvmRunHot`).
- If Gradle tasks aren’t visible, re-import/sync the Gradle project in your IDE to pick up the plugin.
- Hot reload applies to changes in Compose code. Changes to Gradle build scripts or resources like icons may still trigger a short restart.
- You can also use IntelliJ IDEA’s Live Edit for Compose Multiplatform (2024.3+). Start the app normally, then enable Live Edit from the Run toolbar lightning icon. This provides near-instant updates as you edit composables.
- Advanced: There’s also `:composeApp:hotDevJvm` which runs a dev entrypoint; it requires `--className` and `--funName` arguments.

### Build and Run Web Application

To build and run the development version of the web app, use the run configuration from the run widget
in your IDE’s toolbar or run it directly from the terminal:
- on macOS/Linux
  ```shell
  ./gradlew :composeApp:wasmJsBrowserDevelopmentRun
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:wasmJsBrowserDevelopmentRun
  ```

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html),
[Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/#compose-multiplatform),
[Kotlin/Wasm](https://kotl.in/wasm/)…

We would appreciate your feedback on Compose/Web and Kotlin/Wasm in the public Slack channel [#compose-web](https://slack-chats.kotlinlang.org/c/compose-web).
If you face any issues, please report them on [YouTrack](https://youtrack.jetbrains.com/newIssue?project=CMP).