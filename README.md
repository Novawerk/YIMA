# YIMA — Period Calendar

[中文版本](README_ZH.md)

A privacy-first, open-source menstrual cycle tracker built with Kotlin Multiplatform and Compose Multiplatform. All data stays on your device. No account, no cloud, no network, no analytics, no ads.

**By [Novawerk](https://github.com/Novawerk)** — Open-source apps, made with care.

## Why YIMA exists

Most small utility apps used to be good. Somewhere along the way, the pressure to monetize turned them into something else — core features hidden behind paywalls, interfaces bloated with ads and upsells, settings pages that quietly exist to nudge you toward a subscription. The experience gets cut down until what's left barely resembles the tool you originally wanted.

YIMA is a deliberate experiment in the other direction: **design a small tool without profit as a constraint, and see whether the experience can become good again**. No business model means no reason to compromise the core loop. No ads means no reason to keep you opening the app. No account means no reason to harvest data. What's left is just the thing itself — a period tracker that tries to be genuinely useful, then gets out of your way.

If that experiment lands, maybe more small utilities can be rebuilt this way.

## Features

### Logging
- **One-tap period tracking** — "Period arrived" / "Period ended" buttons on the home screen, with an estimated end date based on your average period length until you confirm it
- **Backfill past periods** — Add any historical period by selecting a range on the calendar
- **In-calendar editing** — Tap any day in the detail view to add, delete, extend, or shorten a period directly

### Predictions & cycle phases
- **Next 3 cycles predicted** — Based on your historical average cycle length, with a fallback to the cycle length you set in onboarding
- **Anomaly filter** — Cycles shorter than 14 days are treated as anomalies and excluded from the prediction average so a one-off irregular cycle doesn't skew your forecast
- **Smart auto-confirm** — If a predicted period is 3+ days past its expected end and you haven't logged anything, YIMA quietly auto-confirms it so the calendar stays in sync even if you forget to tap
- **4 cycle phases** — Menstrual, Follicular, Ovulation, Luteal — calculated with the medical count-back method (ovulation at `cycleLength − 14`, 6-day fertile window)
- **Per-day phase info** — Tap any day in the detail calendar to see the cycle day, current phase, ovulation peak, and contextual tips

### Home screen — three views
- **Overview** — Large circular calendar showing the current cycle at a glance with phase indicator
- **Detail calendar** — Month-by-month swipeable pager with color-coded period days, predicted days, ovulation window, ovulation-peak flower icon, and period start/end markers
- **Statistics** — Bar chart of recent cycles (with anomaly highlighting), averages over the last 6 cycles (period length + cycle length), and a scrollable record history

### Daily logging (per-day bottom sheet)
- **Flow intensity** — Light / Medium / Heavy
- **Mood** — 😊 😐 😔 😢
- **Symptoms** — Cramps, back pain, headache, breast pain, fatigue
- **Free-text notes**

### Notifications (opt-in)
- **Period reminder** — 1–7 days before your next predicted period
- **Ovulation reminder** — 1–7 days before your ovulation peak day
- **Daily reminder** — At a time you choose

All three are off by default and require an explicit OS-level permission grant.

### Health data sync
- **Apple Health & Google Health Connect** — Bidirectional sync of menstrual period and flow data via [HealthKMP](https://github.com/vitoksmile/HealthKMP)
- **Import** — Reads periods from the health platform and merges them with your existing records (skips overlapping ranges)
- **Export** — Writes your manually-logged periods back to the health platform
- **Opt-in** — Off by default; enable in Settings under "Health Data"

### Cycle report export
- **Long-image PNG report** — Generates a single shareable image containing your summary (total records, average cycle, average period) and every record with its daily logs (flow, mood, symptoms, notes)
- **Pick the report language** — English or Chinese, independent of the app language
- **Native share sheet** — Uses the OS share sheet on both Android and iOS so you can save it, send it to a doctor, or share with whoever you want — YIMA never sees the file

### Onboarding
- Welcome screen with app logo
- Pick your last period range on a calendar
- Set period duration (2–10 days) and cycle length (20–45 days) with sliders
- YIMA auto-generates 5 past cycles using your settings, so predictions and statistics work from day one — you don't have to wait for 2+ real cycles

### Settings
- Display mode — System / Light / Dark
- Language — Auto / English / 中文
- Period duration & cycle length (sliders, editable any time)
- Health data sync (Apple Health / Google Health Connect)
- Notifications
- Cycle report export
- About (version, author)
- Clear all data (destructive, with confirmation)

### Privacy
- **Local-only storage** — Everything is stored in Jetpack DataStore on your device
- **No account, no sign-in, no phone number**
- **No network calls for user data** — the app makes no HTTP requests carrying cycle information
- **No analytics, no trackers, no ads**
- **Open source** — Audit the code yourself
- **Disclaimer on first launch** — YIMA is a tracking tool, not a medical device

### Platform & design
- **Android** (min SDK 24) and **iOS** — shared codebase
- **Material Design 3 Expressive** with custom Comfortaa typography
- Full English + Simplified Chinese localization with localized month and weekday names

## Screenshots

<!-- TODO: Add screenshots -->

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 2.2 |
| UI | Compose Multiplatform 1.8 (Material 3 Expressive) |
| Navigation | Compose Navigation with type-safe `@Serializable` routes |
| DI | kotlin-inject + KSP (`@KmpComponentCreate`) |
| Health | [HealthKMP](https://github.com/vitoksmile/HealthKMP) (Apple HealthKit + Google Health Connect) |
| Storage | Jetpack DataStore Preferences + kotlinx.serialization |
| Date/Time | kotlinx-datetime + kotlinx-datetime-names |
| Notifications | Platform-specific schedulers via `expect` / `actual` |
| Targets | Android (min SDK 24, compile/target 36), iOS |
| Build | Gradle 8.7 with version catalog |

## Architecture

Single module (`:composeApp`) with clean layering:

```
composeApp/src/commonMain/kotlin/com/haodong/yimalaile/
├── App.kt                            # Root composable + NavHost
├── di/                               # kotlin-inject root component
├── domain/
│   ├── menstrual/                    # MenstrualService, cycle logic, models
│   ├── health/                       # HealthService, HealthSyncManager
│   ├── notifications/                # Reminder scheduling contracts
│   └── settings/                     # SettingsRepository + AppDarkMode
├── infrastructure/
│   └── persistence/                  # DataStore-backed repository implementations
└── ui/
    ├── theme/                        # M3 Expressive theme + Comfortaa font
    ├── navigation/                   # Type-safe routes
    ├── components/                   # Shared calendar & layout composables
    └── pages/
        ├── disclaimer/               # First-launch medical disclaimer
        ├── onboarding/               # 4-step onboarding
        ├── home/                     # Overview / detail / statistics modes
        ├── settings/                 # Settings + notification settings
        └── sheet/                    # Shared bottom-sheet host for logging, details, pickers
```

## Quick Start

```bash
# Prerequisites: JDK 17+, Android SDK

# Build Android
./gradlew :composeApp:assembleDebug

# Install on device / emulator
./gradlew :composeApp:installDebug
```

For iOS, open `iosApp/` in Xcode and build normally. Copy `iosApp/Configuration/Config.xcconfig.template` to `Config.xcconfig` and fill in your Team ID first.

## Business Rules

1. Cannot start a new period while one is already active (no confirmed end date)
2. Period date ranges cannot overlap
3. Backfill is not blocked by an active period
4. Predictions require at least one existing record; onboarding seeds 5 past cycles so the app is immediately useful
5. Cycles shorter than 14 days are treated as anomalies and excluded from the prediction average and the statistics averages
6. Predictions that pass their expected end date by 3+ days are auto-confirmed as real records
7. Ending or shortening a period auto-trims any daily records outside the new range
8. All data is local-only; a disclaimer is shown on first launch
9. YIMA is a tracker, not a medical device — no clinical claims

## Download

- **App Store** — search "YIMA" / 姨妈来了
- **Google Play** — search "YIMA" / 姨妈来了

## Contributing

We welcome contributions! Open an issue or submit a pull request.

## License

This project is licensed under the [MIT License](LICENSE).

Copyright (c) 2025–2026 [Novawerk](https://github.com/Novawerk). You are free to use, modify, and distribute this software, as long as the original copyright notice is included.
