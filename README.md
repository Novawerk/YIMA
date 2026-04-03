# 姨妈来了 (Yimalaile)

A privacy-first menstrual cycle tracker built with Kotlin Multiplatform and Compose Multiplatform. All data stays on your device.

**By [Novawerk](https://github.com/Novawerk)** — In an era where action is cheapest, remaking every app the non-profit way.

## Features

- **Period tracking** — Record start/end dates, daily intensity, mood, symptoms, and notes
- **Cycle prediction** — Predicts next 3 periods based on your history
- **Backfill** — Batch-record past periods with a visual calendar
- **Statistics** — Average cycle/period length, duration chart, prediction confidence
- **Privacy-first** — All data stored locally via DataStore. No accounts, no cloud, no tracking.
- **Dual language** — Full English and Chinese (简体中文) localization
- **Theming** — 3 color palettes (Warm / Vivid / Minimal) + light/dark/system mode

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 2.2.0 |
| UI | Compose Multiplatform 1.8.2 (Material Design 3) |
| Navigation | Compose Navigation 2.9.2 (type-safe routes) |
| Storage | Jetpack DataStore Preferences 1.1.7 |
| DI | kotlin-inject 0.8.0 (KSP) |
| Date/Time | kotlinx-datetime 0.6.1 |
| Targets | Android (min SDK 24) / iOS |
| Build | Gradle 8.7.3 with version catalog |

## Architecture

```
composeApp/src/commonMain/kotlin/com/haodong/yimalaile/
├── App.kt                          # NavHost + startup logic
├── Platform.kt                     # expect declarations
├── di/
│   └── AppComponent.kt             # kotlin-inject DI root
├── domain/
│   ├── menstrual/                   # Core business logic
│   │   ├── MenstrualRecord.kt      # Data model
│   │   ├── DailyRecord.kt          # Daily entry model
│   │   ├── MenstrualService.kt     # Service (recording, prediction, validation)
│   │   ├── RecordsRepository.kt    # Persistence interface
│   │   ├── CycleState.kt           # Query state bundle
│   │   ├── PredictedCycle.kt       # Prediction result
│   │   └── AddRecordResult.kt      # Operation result type
│   └── settings/
│       └── SettingsRepository.kt   # Preferences (disclaimer, theme)
├── infrastructure/
│   └── persistence/
│       └── DataStoreRecordsRepository.kt  # DataStore implementation
└── ui/
    ├── theme/AppTheme.kt           # 3 palettes × light/dark
    ├── navigation/Routes.kt        # @Serializable route objects
    ├── components/                  # Shared: PrimaryCta, RangeCalendar, etc.
    ├── disclaimer/                  # First-launch disclaimer
    ├── onboarding/                  # Initial data collection
    ├── home/                        # Main screen + HomeViewModel
    ├── statistics/                  # History list
    ├── settings/                    # Theme, clear data, about
    └── record/                      # Start/End/Backfill/LogDay/Detail sheets
```

## Quick Start

```bash
# Prerequisites: JDK 17+, Android SDK

# Build
./gradlew :composeApp:assembleDebug

# Install on device/emulator
./gradlew :composeApp:installDebug
```

iOS builds via Xcode (`iosApp/`). Do not build iOS locally for development — focus on Android and shared logic.

## Business Rules

1. Cannot start a new period while one is active (no end date)
2. Period date ranges cannot overlap
3. Backfill is not blocked by an active period (it records past data)
4. Predictions require ≥ 2 complete records
5. Ending a period auto-trims daily records outside the range
6. All data local-only — no medical claims, disclaimer on first launch

## License

Non-profit open source by Novawerk.
