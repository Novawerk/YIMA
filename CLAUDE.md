# CLAUDE.md

## Project Overview

**姨妈来了 (Yimalaile)** is a Kotlin Multiplatform (KMP) menstrual cycle tracking and prediction app targeting Android and iOS using Compose Multiplatform for shared UI.

## Tech Stack

- **Language**: Kotlin 2.2.0
- **UI**: Compose Multiplatform 1.8.2 (Material Design 3)
- **Navigation**: Compose Navigation 2.9.2 (type-safe @Serializable routes)
- **Storage**: Jetpack DataStore Preferences 1.1.7
- **DI**: kotlin-inject 0.8.0 with KSP (@KmpComponentCreate for multiplatform)
- **Date/Time**: kotlinx-datetime 0.6.1
- **Build**: Gradle 8.7.3 with version catalog (`gradle/libs.versions.toml`)
- **Android**: Min SDK 24, Compile/Target SDK 36, AGP 8.7.3
- **iOS**: Swift/SwiftUI wrapper in `iosApp/`, KMP framework named `ComposeApp`

## Project Structure

```
composeApp/src/
├── commonMain/kotlin/com/haodong/yimalaile/
│   ├── App.kt                      # Root Composable + NavHost
│   ├── Platform.kt                  # expect declarations
│   ├── di/AppComponent.kt           # DI root
│   ├── domain/
│   │   ├── menstrual/               # MenstrualService, models, repository interface
│   │   └── settings/                # SettingsRepository
│   ├── infrastructure/persistence/  # DataStoreRecordsRepository
│   └── ui/                          # All screens, components, theme, navigation
├── androidMain/                     # MainActivity, Platform.android.kt
└── iosMain/                         # MainViewController, Platform.ios.kt
iosApp/                              # Native Xcode project
```

## Build Commands

```bash
./gradlew :composeApp:assembleDebug        # Build debug APK
./gradlew :composeApp:installDebug         # Install on device/emulator
./gradlew clean build                      # Full clean build
```

**Do not build iOS locally** — only verify Android and shared logic.

## Development Guidelines

- **Code style**: Kotlin official style (`kotlin.code.style=official`)
- **Localization**: All UI strings in `composeResources/values/strings.xml` (EN) and `values-zh/strings.xml` (ZH). Access via `stringResource(Res.string.*)`. No hardcoded text.
- **Theming**: Use `MaterialTheme.colorScheme.*` everywhere — never hardcode `AppColors.*` in UI files. Three palettes (warm/vivid/mono) with light/dark variants.
- **DI**: kotlin-inject with `@KmpComponentCreate`. Interface binding via extension `@Provides`.
- **Platform differences**: Use `expect`/`actual` pattern via `Platform.kt`
- **Navigation**: Type-safe routes with `@Serializable data object` in `ui/navigation/Routes.kt`

## Architecture

- Single module (`:composeApp`)
- Domain layer: `MenstrualService` wraps `RecordsRepository` + cycle calculation
- Infrastructure: `DataStoreRecordsRepository` (JSON serialization in DataStore)
- UI: Compose screens with ViewModels, BottomSheet dialogs, custom `RangeCalendar`
- Privacy-first: local storage only, no cloud sync

## Key Business Rules

1. Cannot start a new period while one is active (no end date)
2. Period date ranges cannot overlap
3. Backfill not blocked by active period (records past data)
4. Predictions require ≥ 2 complete records
5. Ending a period auto-trims daily records outside the range
6. Data export/import/clear under user control
7. No medical claims; disclaimer on first launch
8. Dual language (EN/ZH)
