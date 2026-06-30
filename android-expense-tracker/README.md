# Ledgerly

**Business Expense Tracker for the Self-Employed**

[![Build](https://img.shields.io/github/actions/workflow/status/ledgerly/android-expense-tracker/ci.yml?branch=main&label=build)](https://github.com/ledgerly/android-expense-tracker/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/platform-Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Min SDK](https://img.shields.io/badge/minSdk-26-blue)](https://developer.android.com/tools/releases/platforms)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)

## Overview

Ledgerly is a native Android app that helps self-employed people — 1099 contractors, freelancers, consultants, and small business owners — track business expenses throughout the year and prepare a U.S. IRS **Schedule C** tax return without the year-end scramble.

Capture a receipt in seconds, categorize it against IRS Schedule C lines, and let Ledgerly keep an always-up-to-date, tax-ready Google Sheet in the background. Everything works offline first and syncs automatically when connectivity returns, so it's just as usable on a job site with no signal as it is at a desk.

## Key Features

### Expense Tracking
- Fast expense entry optimized for one-handed, on-the-go use
- Receipt photo capture via CameraX, plus import from the gallery
- Customizable expense categories with built-in **IRS Schedule C category mapping**
- Recurring expenses (subscriptions, rent, recurring services)
- Mileage tracking for vehicle-related deductions

### Sync & Reporting
- Automatic, two-way **Google Sheets** sync via WorkManager background jobs
- Offline-first architecture — full functionality with no network connection
- Tax-ready reports exportable as **PDF, CSV, Excel, or Google Sheets**
- Dashboard with spending charts and category breakdowns
- Powerful search and filtering across all expenses

### Security & Access
- Sign in with Google, email/password (Firebase Auth), or Guest mode
- PIN and biometric (fingerprint/face) app lock
- Local database encrypted at rest with SQLCipher
- Sensitive preferences stored in AndroidX EncryptedSharedPreferences

### Productivity
- Notifications and reminders to log expenses and review uncategorized items
- Crash reporting and usage analytics (Firebase Crashlytics, Firebase Analytics) to continuously improve reliability

## Screenshots

Screenshots and a short product walkthrough live in [`docs/screenshots`](docs/screenshots). Suggested set:

| Dashboard | Add Expense | Receipt Capture | Reports | Sheets Sync |
|---|---|---|---|---|
| ![Dashboard](docs/screenshots/dashboard.png) | ![Add Expense](docs/screenshots/add_expense.png) | ![Receipt Capture](docs/screenshots/receipt_capture.png) | ![Reports](docs/screenshots/reports.png) | ![Sync](docs/screenshots/sync.png) |

> Place PNG/WebP screenshots in `docs/screenshots/` using the filenames above (or update the table) as the UI stabilizes.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.0 |
| UI | Jetpack Compose, Material 3 |
| Architecture | Clean Architecture + MVVM |
| Dependency Injection | Hilt |
| Local Persistence | Room + SQLCipher (encrypted database) |
| Preferences | DataStore, AndroidX Security (EncryptedSharedPreferences) |
| Background Work | WorkManager |
| Networking | Retrofit, Google API Client (Sheets API, Drive API) |
| Camera / Media | CameraX |
| Auth | Firebase Auth (Google Sign-In, email/password), Guest mode |
| Observability | Firebase Crashlytics, Firebase Analytics |
| Security | AndroidX Biometric, EncryptedSharedPreferences, SQLCipher |
| Build | Gradle (Kotlin DSL), Version Catalog (`gradle/libs.versions.toml`) |

## Architecture Overview

Ledgerly follows **Clean Architecture** with a unidirectional dependency rule: outer layers depend inward, never the reverse. UI depends on Domain; Data depends on Domain; Domain depends on nothing.

```
┌──────────────────────────────────────────────────────────────┐
│                              UI                                │
│   Jetpack Compose Screens  ·  ViewModels  ·  Navigation         │
└───────────────────────────────┬────────────────────────────────┘
                                 │ depends on
                                 ▼
┌──────────────────────────────────────────────────────────────┐
│                            DOMAIN                               │
│   Models  ·  Repository Interfaces  ·  Use Cases                │
└───────────────────────────────▲────────────────────────────────┘
                                 │ implements
                                 │
┌────────────────────────────────┴───────────────────────────────┐
│                             DATA                                 │
│  Room (SQLCipher) · Repositories · Remote (Sheets/Drive) · Sync  │
└──────────────────────────────────────────────────────────────┘

                 Wired together by Hilt (DI layer)
```

For the full breakdown — data flow diagrams, sync/conflict resolution, and threading model — see [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md).

## Project Structure

```
android-expense-tracker/
├── app/
│   └── src/
│       ├── main/
│       │   ├── java/com/ledgerly/expense/
│       │   │   ├── data/
│       │   │   │   ├── local/          # Room database, DAOs, entities, SQLCipher setup
│       │   │   │   ├── remote/         # Retrofit/Google API clients (Sheets, Drive)
│       │   │   │   ├── repository/     # Repository implementations
│       │   │   │   ├── sync/           # WorkManager workers, sync engine
│       │   │   │   └── mapper/         # Entity <-> domain model mappers
│       │   │   ├── domain/
│       │   │   │   ├── model/          # Pure Kotlin domain models
│       │   │   │   ├── repository/     # Repository interfaces
│       │   │   │   └── usecase/        # Business logic use cases
│       │   │   ├── ui/
│       │   │   │   ├── dashboard/
│       │   │   │   ├── expense/        # Add/edit expense, receipt capture
│       │   │   │   ├── reports/
│       │   │   │   ├── mileage/
│       │   │   │   ├── settings/
│       │   │   │   ├── auth/
│       │   │   │   ├── common/         # Shared composables, theme
│       │   │   │   └── navigation/
│       │   │   ├── di/                 # Hilt modules
│       │   │   └── LedgerlyApplication.kt
│       │   ├── res/
│       │   └── AndroidManifest.xml
│       ├── test/                       # Unit tests (JUnit, MockK, Turbine)
│       └── androidTest/                # Instrumented tests (Espresso, Compose UI tests)
├── docs/
│   ├── BUILD.md
│   ├── ARCHITECTURE.md
│   └── screenshots/
├── gradle/
│   └── libs.versions.toml
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── README.md
```

## Prerequisites

- JDK 17
- Android Studio (Ladybug or newer)
- Android SDK 35 (compileSdk / targetSdk), minSdk 26
- A Firebase project with `google-services.json`
- A Google Cloud project with OAuth credentials and the Sheets API + Drive API enabled

See [`docs/BUILD.md`](docs/BUILD.md) for full, step-by-step setup.

## Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/ledgerly/android-expense-tracker.git
   cd android-expense-tracker
   ```

2. **Add Firebase configuration**
   Download `google-services.json` from the Firebase console for the `com.ledgerly.expense` app and place it at `app/google-services.json`.

3. **Configure Google Cloud OAuth + Sheets/Drive APIs**
   Create OAuth 2.0 client IDs (Android + Web) in Google Cloud Console, enable the Google Sheets API and Google Drive API, and add the Web client ID to your local configuration. Full instructions in [`docs/BUILD.md`](docs/BUILD.md#google-cloud-console--oauth-setup).

4. **Configure signing (release builds only)**
   Generate a keystore and set `KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` in `gradle.properties` (kept out of version control). See [`docs/BUILD.md`](docs/BUILD.md#signing-configuration).

## Build & Run

```bash
# Debug build, installed to a connected device/emulator
./gradlew installDebug

# Assemble a debug APK
./gradlew assembleDebug

# Assemble a release APK
./gradlew assembleRelease

# Bundle a release AAB (for Play Store distribution)
./gradlew bundleRelease
```

Full build documentation, including troubleshooting, is in [`docs/BUILD.md`](docs/BUILD.md).

## Testing

```bash
# Unit tests
./gradlew test

# Instrumented tests (requires a connected device or emulator)
./gradlew connectedAndroidTest
```

See the testing strategy section of [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md#testing-strategy) for how layers are tested in isolation.

## Roadmap

Planned features, in no particular priority order:

- [ ] OCR-based automatic receipt data extraction (vendor, amount, date)
- [ ] AI-assisted expense categorization
- [ ] Bank and credit card transaction import
- [ ] Multi-business / multi-entity support for users running several Schedule C businesses
- [ ] Quarterly estimated tax payment calculator and reminders
- [ ] Mileage auto-tracking via location services
- [ ] Web companion dashboard
- [ ] CPA/accountant shared-access mode
- [ ] Multi-currency support

## Contributing

Contributions are welcome. Please:

1. Fork the repository and create a feature branch.
2. Follow the existing Clean Architecture conventions (see [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)).
3. Add or update unit/instrumented tests for any behavior change.
4. Ensure `./gradlew test` and `./gradlew connectedAndroidTest` pass.
5. Open a pull request with a clear description of the change and motivation.

Please do not commit `google-services.json`, keystores, or any secrets — these are intentionally excluded via `.gitignore`.

## License

This project is licensed under the [MIT License](LICENSE).

---

**Disclaimer:** Ledgerly assists with organizing business expense records for Schedule C preparation. It is not a substitute for advice from a qualified tax professional.
