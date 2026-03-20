# CLAUDE.md

## Project Overview

**姨妈来了 (Yimalaile)** is a Kotlin Multiplatform (KMP) menstrual cycle tracking and prediction app targeting Android and iOS using Compose Multiplatform for shared UI.

## Tech Stack

- **Language**: Kotlin 2.2.0
- **UI**: Compose Multiplatform 1.8.2 (Material Design 3)
- **Build**: Gradle 8.7.3 with version catalog (`gradle/libs.versions.toml`)
- **Android**: Min SDK 24, Compile/Target SDK 36, AGP 8.7.3
- **iOS**: Swift/SwiftUI wrapper in `iosApp/`, KMP framework named `ComposeApp`

## Project Structure

```
composeApp/src/
├── commonMain/kotlin/com/haodong/yimalaile/   # Shared KMP code
│   ├── App.kt                                  # Root Composable
│   ├── Platform.kt                             # expect declarations
│   └── ui/                                     # Shared UI components
├── androidMain/kotlin/com/haodong/yimalaile/   # Android-specific
│   ├── MainActivity.kt
│   └── Platform.android.kt
└── iosMain/kotlin/com/haodong/yimalaile/       # iOS-specific
    ├── MainViewController.kt
    └── Platform.ios.kt
iosApp/                                         # Native Xcode project
.junie/                                         # Project planning docs
```

## Build Commands

```bash
# Android
./gradlew :composeApp:assembleDebug        # Build debug APK
./gradlew :composeApp:installDebug         # Install on device/emulator

# General
./gradlew build                            # Full build
./gradlew clean                            # Clean artifacts
```

**Do not build iOS locally** — only verify Android and shared logic.

## Development Guidelines

- **Code style**: Kotlin official style (`kotlin.code.style=official` in `gradle.properties`)
- **Configuration cache**: All code must be configuration cache compatible
- **JVM target**: Keep at JVM 11+; Gradle/JDK at 17+
- **Non-transitive R**: `android.nonTransitiveRClass=true` is enabled
- **Localization**: String resources in `composeResources/values/strings.xml` (EN) and `values-zh/strings.xml` (ZH); access via `stringResource(Res.string.*)`
- **Platform differences**: Use `expect`/`actual` pattern via `Platform.kt`

## Architecture

- Single module (`:composeApp`) — no multi-module separation yet
- Common UI and business logic in `commonMain`
- Platform-specific implementations in `androidMain` / `iosMain`
- Privacy-first: local storage only, no cloud sync

## Current Phase

**Phase 0 (MVP scaffolding)** — UI skeleton, localization, and placeholders in place. No persistent storage or core cycle logic yet.

Planned next: Phase 1 (local data persistence, cycle calculation, prediction algorithm).

## Key Business Rules

1. Period records require a start date; end date, intensity, symptoms, notes are optional
2. A complete cycle = two consecutive start dates
3. Prediction window adapts based on cycle stability
4. Data export/import/clear under user control
5. No medical claims; include disclaimer on first launch
6. Dual language (EN/ZH) + timezone handling
