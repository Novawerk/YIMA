This is a Kotlin Multiplatform project targeting Android and iOS.

Structure
- [/composeApp](./composeApp/src) contains shared code and platform source sets:
  - [commonMain](./composeApp/src/commonMain/kotlin): shared Kotlin + Compose UI.
  - [androidMain](./composeApp/src/androidMain/kotlin): Android entry point and platform APIs.
  - [iosMain](./composeApp/src/iosMain/kotlin): iOS view controller and platform APIs.
- [/iosApp](./iosApp/iosApp) is the native iOS wrapper that consumes the ComposeApp framework.

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html).

Quick start (Android)
- Prerequisites:
  - JDK 17+ (AGP 8.7.x). Set JAVA_HOME accordingly.
  - Android SDK installed and sdk.dir set in local.properties.
  - Use the Gradle Wrapper: ./gradlew
- Build Debug APK: ./gradlew :composeApp:assembleDebug
- Install on device/emulator: ./gradlew :composeApp:installDebug
- Full clean build: ./gradlew clean build (note: this will configure iOS targets; see note below if avoiding iOS locally).

iOS note
- As per repository guidelines, for local verification do NOT run iOS builds. Focus on Android and shared logic. Xcode integration exists under iosApp, but is not required for local validation.

Project planning
- Business requirements: [.junie/plans/requirements.md](.junie/plans/requirements.md)
- Implementation plan: [.junie/plans/implementation_plan.md](.junie/plans/implementation_plan.md)

Phase 0 placeholders
- Legal copy strings (EN/ZH) live in composeApp/src/commonMain/composeResources/values*/strings.xml.
- A simple PrivacyDisclaimerPlaceholder composable exists at composeApp/src/commonMain/kotlin/com/haodong/yimalaile/ui/PrivacyDisclaimer.kt. It is not yet wired into the main UI; persistence of acceptance will be added alongside storage in a later phase.

Notes
- Follow the repository guidelines in .junie/guidelines.md. For local verification, do not run iOS builds; focus on Android and shared logic.
