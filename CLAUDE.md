# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
./gradlew build                     # Full build
./gradlew :app:assembleDebug        # Debug APK
./gradlew :app:assembleRelease      # Release APK
./gradlew installDebug              # Build and install on connected device

# Test
./gradlew test                      # Unit tests
./gradlew testDebugUnitTest         # Debug unit tests only
./gradlew connectedAndroidTest      # Instrumentation tests (requires device/emulator)

# Quality
./gradlew lint                      # Lint checks
./gradlew clean                     # Clean build outputs
```

## Architecture

Single-module Android app (`com.example.primitivedevicestoic`) following MVVM + Clean Architecture layers:

```
di/                     # Koin modules (AppModule.kt)
domain/model/           # Data classes: UnlockEvent, AppInfo
domain/repository/      # UsageRepository interface
data/local/             # BroadcastReceivers (UnlockReceiver, ReminderReceiver)
data/repository/        # UsageRepositoryImpl (SharedPreferences + system APIs)
presentation/welcome/   # WelcomeScreen + WelcomeViewModel
presentation/home/      # HomeScreen + HomeViewModel
ui/theme/               # Compose theme (Color, Type, Theme)
util/                   # NotificationHelper
```

**DI**: Koin 4.0 (not Hilt). Modules defined in `di/AppModule.kt`, initialized in `StoicApp`.

**UI**: 100% Jetpack Compose + Material 3. No XML layouts. Navigation via `NavHost` with two routes: `"welcome"` and `"home"`.

**Persistence**: SharedPreferences only (key prefix: `stoic_prefs`). No Room database.

**State**: `StateFlow` / `MutableStateFlow` throughout. `HomeViewModel` manages 25+ state flows covering unlock count, screen time, battery, sleep time, intentions, selected apps, etc.

## Key Patterns

**Repository pattern**: `UsageRepository` interface in domain layer; `UsageRepositoryImpl` in data layer injected via Koin. UI never touches implementation directly.

**System integrations**:
- Unlock counting: `UnlockReceiver` listens to `ACTION_USER_PRESENT`
- Screen time: `UsageStatsManager.queryAndAggregateUsageStats()` — requires `PACKAGE_USAGE_STATS` permission (must be granted manually in settings)
- Evening reminders: `AlarmManager.setExactAndAllowWhileIdle()` scheduled by `ReminderReceiver` on boot and rescheduled nightly

**Launcher capability**: App declares `CATEGORY_HOME` intent filter. Welcome screen handles launcher role assignment via `RoleManager` (Android 10+) or `Settings.ACTION_HOME_SETTINGS`.

**Data limits**: Repository stores only the last 50 unlock timestamps; max 10 selected apps.

## Language & Content

All UI strings and in-code comments are in **Czech**. Stoic quotes displayed on the home screen are also in Czech. Notification text example: `"Dnes jsi na telefonu strávil Xh Ym. Čas na reflexi."` Keep this convention when adding new UI strings.

## Dependencies (key versions)

| Library | Version |
|---|---|
| AGP | 9.0.0-alpha06 |
| Kotlin | 2.0.21 |
| Compose BOM | 2024.09.00 |
| Koin | 4.0.0 |
| AndroidX Navigation Compose | 2.8.3 |
| AndroidX Lifecycle ViewModel | 2.8.7 |

Dependency versions are managed via the version catalog at [libs.versions.toml](air-file://50g7fpkfpfclupnkfvb3/Users/ales/AndroidStudioProjects/PrimitiveDeviceStoic/gradle/libs.versions.toml?type=file&root=%252F).
