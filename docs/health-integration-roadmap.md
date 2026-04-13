# Health Integration Roadmap

YIMA integrates with platform health services (Apple HealthKit on iOS, Google Health Connect on Android) via the [HealthKMP](https://github.com/vitoksmile/HealthKMP) library.

## Phase 1: Menstrual Data Sync (Implemented)

Bidirectional sync of menstrual period and flow data.

- **Import**: Read `MenstruationPeriodRecord` and `MenstruationFlowRecord` from the health platform, convert to app's `MenstrualRecord` model, and merge with existing records (skip overlaps).
- **Export**: Write app records to the health platform so they appear in Apple Health / Health Connect.
- **Settings UI**: Toggle in Settings to enable/disable sync, manual "Sync Now" button, last sync timestamp display.
- **Authorization**: Requests read + write permissions for menstruation data types on first enable.
- **Conflict resolution**: Records that overlap with existing app records are skipped during import. Export relies on platform-level deduplication.

### Data mapping

| App Model | Health Platform |
|-----------|----------------|
| `MenstrualRecord` (startDate, endDate) | `MenstruationPeriodRecord` (startTime, endTime) |
| `DailyRecord.intensity` (LIGHT/MEDIUM/HEAVY) | `MenstruationFlowRecord.Flow` (Light/Medium/Heavy) |
| `RecordSource.HEALTH_IMPORT` | Records imported from health platform |

---

## Phase 2: Body Temperature + Weight (Planned)

Read body temperature (BBT) and weight data from health platforms to enrich cycle analysis.

### Features

- **Basal Body Temperature (BBT) chart**: Read `BodyTemperature` records from HealthKit / Health Connect. Display a temperature curve overlay on the detail calendar view, aligned with cycle phases.
- **Weight trend**: Read `Weight` records. Show a weight trend card on the statistics page, grouped by cycle phase to surface phase-correlated patterns.
- **Ovulation detection assist**: Use BBT data to refine the ovulation window estimate. The typical BBT pattern shows a 0.2-0.5 C rise after ovulation — this can complement the count-back method currently used.

### HealthKMP API

```kotlin
// Read body temperature
healthManager.readBodyTemperature(startTime, endTime)
// Returns List<BodyTemperatureRecord> with:
//   - time: Instant
//   - temperature: Temperature (value + unit: Celsius/Fahrenheit)

// Read weight
healthManager.readWeight(startTime, endTime)
// Returns List<WeightRecord> with:
//   - time: Instant
//   - weight: Mass (value + unit: kg/lbs/etc.)
```

### Implementation notes

- Read-only — the app does not write temperature or weight data
- New domain model: `HealthMetric(date: LocalDate, type: MetricType, value: Double, unit: String)`
- Temperature unit preference: use `healthManager.getRegionalPreferences()` to respect user's preferred unit
- Chart library: reuse the existing bar chart pattern from statistics, adapt for line/area charts

---

## Phase 3: Sleep + Heart Rate + Steps Insights (Planned)

Create a "Health Insights" dashboard that correlates cycle phases with broader health metrics.

### Features

- **Sleep quality by phase**: Read `Sleep` records (with stages: Awake, Light, Deep, REM). Display average sleep duration and quality score per cycle phase.
- **Resting heart rate trend**: Read `HeartRate` records. Show how resting heart rate varies across cycle phases (typically elevated in the luteal phase).
- **Activity level**: Read `Steps` records. Display daily step count averages per phase to surface patterns (e.g., reduced activity during menstruation).
- **Insights dashboard**: New screen accessible from the home page. Phase-grouped cards showing averages and trends with simple visualizations.

### HealthKMP API

```kotlin
// Sleep
healthManager.readSleep(startTime, endTime)
// Returns List<SleepSessionRecord> with stages

// Heart rate
healthManager.readHeartRate(startTime, endTime)
// Returns List<HeartRateRecord> with bpm

// Steps
healthManager.readSteps(startTime, endTime)
// Returns List<StepsRecord> with count
```

### Implementation notes

- Read-only — all these metrics are produced by wearables, fitness apps, or the phone itself
- Requires sufficient historical data (2+ cycles) to show meaningful phase-grouped insights
- Consider a "not enough data" placeholder when metrics are sparse
- New navigation route: `InsightsRoute` with dedicated screen
- Aggregate calculations should be done in the domain layer, not the UI

### Required permissions (to be added when implementing)

**Android** (`AndroidManifest.xml`):
```xml
<uses-permission android:name="android.permission.health.READ_BODY_TEMPERATURE"/>
<uses-permission android:name="android.permission.health.READ_WEIGHT"/>
<uses-permission android:name="android.permission.health.READ_SLEEP"/>
<uses-permission android:name="android.permission.health.READ_HEART_RATE"/>
<uses-permission android:name="android.permission.health.READ_STEPS"/>
```

**iOS** (`Info.plist`): HealthKit usage descriptions already cover read access. Additional `HKObjectType` identifiers will be requested at authorization time.
