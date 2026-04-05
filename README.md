# YIMA: Period Calendar | 姨妈来了

[中文版本](README_ZH.md)

A privacy-first, open-source menstrual cycle tracker built with Kotlin Multiplatform and Compose Multiplatform. All data stays on your device.

**By [Novawerk](https://github.com/Novawerk)** — Open-source apps, made with care.

## Features

- **Period Tracking** — Record start/end dates, daily flow intensity, mood, symptoms, and notes
- **Smart Predictions** — Automatically predicts your next 3 periods after just 2 cycles
- **Cycle Phases** — Real-time phase indicator (Menstrual, Follicular, Ovulation, Luteal) with explanations
- **Visual Calendar** — Color-coded period days, predictions, and today indicator
- **Statistics** — Pill-shaped bar chart with cycle trends, outlier detection, averages, and full history
- **Backfill** — Batch-record past periods with a visual calendar picker
- **Privacy-First** — All data stored locally via DataStore. No accounts, no cloud, no tracking, no ads.
- **Dual Language** — Full English and Chinese (简体中文) with localized date/weekday names
- **Theming** — Light/Dark/System mode with Material Design 3 Expressive

## Screenshots

<!-- TODO: Add screenshots -->

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 2.2 |
| UI | Compose Multiplatform (Material Design 3 Expressive) |
| Navigation | Compose Navigation (type-safe `@Serializable` routes) |
| Storage | Jetpack DataStore Preferences |
| DI | kotlin-inject (KSP, `@KmpComponentCreate`) |
| Date/Time | kotlinx-datetime + kotlinx-datetime-names |
| Targets | Android (min SDK 24) / iOS |
| Build | Gradle with version catalog |

## Architecture

Single module (`:composeApp`) with clean separation:

```
composeApp/src/commonMain/kotlin/com/haodong/yimalaile/
├── App.kt                          # NavHost + startup logic
├── domain/
│   ├── menstrual/                   # MenstrualService, models, repository
│   └── settings/                    # SettingsRepository
├── infrastructure/
│   └── persistence/                 # DataStore implementation
└── ui/
    ├── theme/                       # M3 Expressive theme
    ├── navigation/                  # Type-safe routes
    ├── components/                  # Shared composables
    ├── pages/
    │   ├── disclaimer/              # First-launch disclaimer
    │   ├── onboarding/              # Initial data collection (3 cycles)
    │   ├── home/                    # Calendar, inline statistics, phase info
    │   ├── settings/                # Theme, language, about, clear data
    │   └── record/                  # Period start/end/backfill/detail sheets
    └── locale/                      # App-level locale management
```

## Quick Start

```bash
# Prerequisites: JDK 17+, Android SDK

# Build Android
./gradlew :composeApp:assembleDebug

# Install on device/emulator
./gradlew :composeApp:installDebug
```

For iOS, open `iosApp/` in Xcode and build normally.

## Business Rules

1. Cannot start a new period while one is active
2. Period date ranges cannot overlap
3. Backfill is not blocked by an active period
4. Predictions require >= 2 complete records
5. Ending a period auto-trims daily records outside the range
6. All data local-only; disclaimer on first launch
7. No medical claims

## Contributing

We welcome contributions! Please open an issue or submit a pull request.

## License

Non-profit open source by [Novawerk](https://github.com/Novawerk).
